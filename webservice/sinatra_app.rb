require 'rubygems'
require 'sinatra'
require 'logger'
require 'sinatra/logger'
require 'mongoid'
require 'json'
require 'pushmeup'
require 'bcrypt'
require 'rack'
require 'haml'
require 'regex'

$LOAD_PATH.unshift('.')

require 'models/user'
require 'models/match'
require 'models/player'

#Android device push notification API KEY for google cloud messaging (GCM)
GCM.key = File.open('gcm_key', &:readline)

configure :production do
  services = JSON.parse(ENV['VCAP_SERVICES'])
  mongoKey = services.keys.select{|s| s =~ /mongo/i}.first
  mongo = services[mongoKey].first['credentials']['uri']
  conn = Hash[[:user, :pass, :host, :port, :db].zip(mongo.split('//')[1].split(/[\/:@]/))]
  Mongoid.configure do |config|
    config.database = Mongo::Connection.new(conn[:host], conn[:port]).db(conn[:db])
    config.database.authenticate(conn[:user], conn[:pass])
  end
  enable :logging
end

configure :test do
  Mongoid.configure do |config|
    config.database = Mongo::Connection.new('localhost', '27017').db('testdb')
  end
  enable :logging
end

def make_response(status, message, content={})
  content.merge({
    status: status,
    message: message,
    time: (Time.now.utc.to_f*1000).round #milliseconds
  })
end


get '/' do
  'running hunted'
end

get '/api/matches/:match_name/targets' do
  content_type :json  
  match = Match.where(name: params[:match_name]).first
  match.players.map { |p| p.user.username }.to_json
end

get '/api/matches/:match_name/details' do
  content_type :json  
  match = Match.where(name: params[:match_name]).first
  
  {
    match: match.as_json,
    is_active: match.in_progress?,
    has_begun: match.has_begun?,
    winner_nil: match.winner.nil?,
    now: Time.now.utc.to_i*1000 
  }.to_json
  
end


# TODO send a push notification to client in order to verify the push id
# Return a temporary limited token (5 mins)
# For their token to last, require an acknowledgment of the push id verification token
# Otherwise, the sender may not be an authentic client

#Accepts: 
#Returns:
post '/api/provisional-users' do
  content_type :json  
  data = JSON.parse(request.body.read)  
  
  unless data['install_id'].nil? or data['push_id'].nil? 
    user = User.create({ 
      install_id: data['install_id'], 
      push_id: data['push_id'],
      provisional: true
    })
    
    if user.persisted?
      return make_response('ok', 'created provisional user', { token: user.token }).to_json 
    end
  end
  
  make_response('error', 'failed to create provisional user').to_json
end


#Accepts: 
#Returns: 
post '/api/users' do
  content_type :json  
  data = JSON.parse(request.body.read)  
  unless data['password'].nil? or data['username'].nil?
    data['salt']     = BCrypt::Engine.generate_salt
    data['password'] = BCrypt::Engine.hash_secret(data['password'], data['salt'])

    user = User.create data
    
    if user.persisted?
      return make_response('ok', 'created user', { token: user.token }).to_json 
    end

  end

  make_response('error', 'failed to create user').to_json
end



#Accepts:  
#Returns:
post '/api/users/:token' do
  content_type :json  
  data = JSON.parse(request.body.read)  
  
  user = User.authenticate params[:token]
  user.upgrade_from_provisional data['username'], data['password']
  
  make_response('ok', 'created account', { token: user.token }).to_json 
end


#Accepts: 
#Returns: 
post '/api/matches' do
  content_type :json
  data = JSON.parse(request.body.read) 
  
  user = User.authenticate data['token']
  
  password = data['match']['password']
  
  unless password.nil?
    salt = BCrypt::Engine.generate_salt
    data['match']['salt'] = salt
    data['match']['password'] = BCrypt::Engine.hash_secret(password, salt)
  end

  match = Match.create(data['match'])
  
  if match.persisted?
    if data['join_on_create'] == true    
      match.add_user user
    end
    message = "match created #{data['join_on_create'] == true ? 'and joined' : ''}"
    return make_response('ok', message, { match: match.sanitized_with_players }).to_json 
  end
  
  make_response('error', 'failed to create match').to_json
end


#Accepts: JoinMatchRequest
#Returns: MatchResponse
post '/api/matches/:name/players' do
  content_type :json
  data = JSON.parse(request.body.read)
  user = User.authenticate data['token']

  unless user.provisional?
    match = Match.authenticate params[:name], data['password']
    match.add_user user
    return make_response('ok', 'joined match',
      { match: match.sanitized_with_players }).to_json
  end
  
  make_response('error', 'create an account to join a match').to_json
end



post '/api/matches/:match_id/user/:user_token/ready' do
  content_type :json
  user = User.authenticate(params[:user_token])
  match = Match.find(params[:match_id])
  
  if not match.nil? and not user.nil?
    player = user.players.where(match_id: match.id).first
    player.status = :ready
    player.save
    player.reload
    p_state = player.public_state.merge({ type: :player_event })
    
    #alert other players that this player is ready
    Thread.new {
      match.players.each { |p|
        p.user.send_push_notification(p_state)
      }
    }
    
    return make_response('ok', 'status changed to ready',
     { player: player.state }).to_json
  end
  make_response('error', 'failed to change status to ready').to_json
end

#Accepts: LocationMessage
#Returns: LocationResponse
post '/api/users/:token/location' do
  content_type :json
  data = JSON.parse(request.body.read)  
  user = User.authenticate params[:token]
  
  if user.in_match?
    user.update_location(data['lat'], data['lng'])
    response = make_response(:ok, 'location updated', 
      user.location.merge({ players: user.players.map { |p| p.state } }))
    return response.to_json
  end
    
  # if they are sending their location and are not in a match,
  # they are in freeplay - look around their location for (and possibly generate)
  # some loot that they can pick up
    
  #TODO query for loot, traps, etc.
  
  make_response('ok', 'location updated (not in match).', 
    { players: nil, lat: data['lat'], lng: data['lng'] }).to_json
end


#Accepts: UserLoginMessage
#Returns: UserLoginResponse	
post '/api/login' do
  content_type :json
  data = JSON.parse(request.body.read)  

  # once a user logs in, the provisional user is deleted
  user = User.where(install_id: data['install_id'], provisional: true).first
  user.delete unless user.nil?
  
  user = User.login(data)
    
  make_response('ok', 'log in successful.', { 
    token:  user.token, 
    matches: user.players.map { |p| p.match.sanitized_with_players }  
  }).to_json 
end


post '/api/users/:token/logout' do
  content_type :json
  
  user = User.authenticate params[:token]
  user.logout
  
  make_response('ok', 'logged out').to_json
end

#Accepts: GCMRegistrationMessage
#Returns: UserLoginResponse 
post '/api/users/:token/gcm/register' do
  content_type :json
  data = JSON.parse(request.body.read)  
  
  user = User.authenticate params[:token]
  user.push_id = data['push_id']
  user.save
  
  make_response('ok', 'updated gcm registration id', { token: user.token }).to_json 
end

#Accepts: GCMRegistrationMessage
#Returns: UserLoginResponse 
post '/api/users/:token/gcm/unregister' do
  content_type :json
  data = JSON.parse(request.body.read)  
  
  user = User.authenticate params[:token]
  user.push_id = nil
  user.save
  
  make_response('ok', 'unregistered gcm registration id' ).to_json 
end


#Accepts: LocationMessage
#Returns: AttackResponse
post '/api/match/:match_id/users/:token/attack' do
  content_type :json
  data = JSON.parse(request.body.read)  
  user = User.authenticate params[:token]
  
  state = nil
  result = false
  msg = 'attack failed'
  status = :error
  
  user.update_location(data['latitude'], data['longitude'])
  player = user.players.where(match_id: params[:match_id]).first
  
  unless player.nil? 
    result = player.attack_target
    target = player.get_target
    player.reload
    state = player.state
    msg = 'attack successful'
    status = :ok
  end

  make_response(status, msg, { player: state, hit: result }).to_json 
end



#return wrapped response to allow for error handling like twitter api
# {"response" : null, "type" : "error", "errors":[{"message":"Bad Authentication data","code":215}]}

