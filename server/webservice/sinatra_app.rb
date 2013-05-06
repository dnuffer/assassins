require 'rubygems'
require 'sinatra'
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
 
enable :logging

#Android device push notification API KEY for google cloud messaging (GCM)
GCM.key = "AIzaSyCo6BoZUN9WwccMUPkUC69IcVp23YldgBY"


configure :production do
  services = JSON.parse(ENV['VCAP_SERVICES'])
  mongoKey = services.keys.select{|s| s =~ /mongodb/i}.first
  mongo = services[mongoKey].first['credentials']

  Mongoid.configure do |config|
    config.database = Mongo::Connection.new(mongo['host'], mongo['port']).db(mongo['db'])
    config.database.authenticate(mongo['username'], mongo['password'])
  end
end

configure :test do
  Mongoid.configure do |config|
    config.database = Mongo::Connection.new('localhost', '27017').db('testdb')
  end
end


get '/' do
  'running hunted'
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
  
  user = User.create({ 
    install_id: data['install_id'], 
    push_id: data['push_id'],
    provisional: true
  })
  
  if user.persisted?
    return {
      status: 'ok',
      message: 'created provisional user',
      token: user.token
    }.to_json 
  end
  
  {
    status: 'error',
    message: 'failed to create provisional user'
  }.to_json
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
      return {
        status: 'ok',
        message: 'created user',
        token: user.token
      }.to_json 
    end

  end

  {
    status: 'error',
    message: 'failed to create user'
  }.to_json
end



#Accepts: 
#Returns:
post '/api/users/:token' do
  content_type :json  
  data = JSON.parse(request.body.read)  
  
  user = User.authenticate params[:token]

  unless data['password'].nil? or data['username'].nil?
    salt = BCrypt::Engine.generate_salt
    
    user.update_attributes!({
      salt:        salt,
      password:    BCrypt::Engine.hash_secret(data['password'], salt),
      username:    data['username'],
      provisional: false
    })
    
    if user.persisted?
      return {
        status: 'ok',
        message: 'updated user',
        token: user.token
      }.to_json 
    end
  end
  
  {
    status: 'error',
    message: 'failed to update user'
  }.to_json
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
    return {
      status: 'ok',
      message: 'match created',
      match: match
    }.to_json 
  end
  
  {
    status: 'error',
    message: 'failed to create match'
  }.to_json
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
    return {
        status: 'ok',
        message: 'joined match',
        match: { 
          token: match.token,
          name:  match.name
        }
      }.to_json
  end
  
  {
    status: 'error',
    message: 'create an account to join a match'
  }.to_json
end


#Accepts: LocationMessage
#Returns: LocationResponse
post '/api/users/:token/location' do
  content_type :json
  data = JSON.parse(request.body.read)  
  
  user = User.authenticate params[:token]
  
  if user.in_match?
    user.player.update_location data['latitude'], data['longitude']
    user.match.notify_target user.player
    user.match.notify_enemy  user.player
  
    return { 
      status: 'ok', 
      message: '', 
      latitude: user.player.location[:lat], 
      longitude: user.player.location[:lng] #, 
      # items: [ ]
    }.to_json
  end
    
  # if they are sending their location and are not in a match,
  # they are in freeplay - look around their location for (and possibly generate)
  # some loot that they can pick up
    
  #TODO query for loot, traps, etc.
  
  { status: 'ok', 
    message: '', 
    latitude: data['latitude'], 
    longitude: data['longitude']#,
    # achievements: [ ] 
    # nearby_items: [ ]
    # other_items:  [ ]
    # strength:     [ ] # (when not in a match, a user has a persistent strength)
  }.to_json
end


#Accepts: UserLoginMessage
#Returns: UserLoginResponse	
post '/api/login' do
  content_type :json
  data = JSON.parse(request.body.read)  

  user = User.login(data)
    
  { status: 'ok',
    message: msg,
    token: user.token }.to_json 
end


post '/users/:token/logout' do
  content_type :json
  
  user = User.authenticate params[:token]
  user.logout
  #TODO save a timestamp and remove them from the match if they are logged out too long
  
  { status: 'ok',
    message: 'logged out' }.to_json
end

#Accepts: GCMRegistrationMessage
#Returns: UserLoginResponse 
post '/api/users/:token/gcm/register' do
  content_type :json
  data = JSON.parse(request.body.read)  
  
  user = User.authenticate params[:token]
  user.push_id = data['push_id']
  user.save
  
  { status: 'ok',
    message: 'updated gcm registration id',
    token: user.token }.to_json 
end

#Accepts: GCMRegistrationMessage
#Returns: UserLoginResponse 
post '/api/users/:token/gcm/unregister' do
  content_type :json
  data = JSON.parse(request.body.read)  
  
  user = User.authenticate params[:token]
  user.push_id = nil
  user.save
  
  { status: 'ok',
    message: 'cleared gcm registration id' }.to_json 
end


#Accepts: LocationMessage
#Returns: AttackResponse
post '/api/users/:token/attack' do
  content_type :json
  data = JSON.parse(request.body.read)  
  user = User.authenticate params[:token]
  
  #TODO check install id?
  
  if user.match.in_progress? and user.player.alive?
    
    match = user.match
    target = match.target_of user.player
    
    #TODO enforce attack range
    
    target.take_hit 1
    target.user.send_push_notification({
       type: 'player_event',
       my_life: target.life
    })
    
    # send a message to all users if a player is eliminated
    if target.life < 1
      match.eliminate target
      winner = match.winner == user.player ? user.username : nil
      
      Thread.new do 
        match.users.each do |other|    
          other.send_push_notification({
            type: 'match_event',
            event_type: 'player_elimination',
            detail: target.user.username
          })
          
          unless winner.nil?
            other.send_push_notification({
              type: 'match_event',
              event_type: 'winner',
              detail: winner
            })
          end
        end
      end      
    end
    
    return {
      status: 'ok',
      message: 'attack successful',
      hit: true,
      target_life: target.life
    }.to_json 
  end
  
  {
    status: 'error',
    message: 'attack failed',
    hit: false
  }.to_json 
end

#return wrapped response to allow for error handling like twitter api
# {"response" : null, "type" : "error", "errors":[{"message":"Bad Authentication data","code":215}]}

