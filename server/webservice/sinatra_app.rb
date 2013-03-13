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

#cloudfoundry 
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



# update a user account
post '/api/users/:token' do
  content_type :json  
  data = JSON.parse(request.body.read)  
  
  u = User.where(token: params[:token]).first

  unless u.nil? or data['password'].nil? or data['username'].nil?
    salt = BCrypt::Engine.generate_salt
    
    u.update_attributes!({
      salt:        salt,
      password:    BCrypt::Engine.hash_secret(data['password'], salt),
      username:    data['username'],
      provisional: false
    })
    
    if u.persisted?
      return {
        status: 'ok',
        message: 'updated user',
        token: u.token
      }.to_json 
    end
  end
  
  {
    status: 'error',
    message: 'failed to update user'
  }.to_json
end


# TODO send a push notification to verify the push id
# Return a temporary limited token (5 mins), but for their token to last,
# Require an acknowledgment of a push id verification token
# Otherwise, the sender may not be an authentic client

post '/api/provisional-users' do
  content_type :json  
  data = JSON.parse(request.body.read)  
  
  u = User.create({ 
    install_id: data['install_id'], 
    push_id: data['push_id'],
    provisional: true
  })
  
  if u.persisted?
    return {
      status: 'ok',
      message: 'created provisional user',
      token: u.token
    }.to_json 
  end
  
  {
    status: 'error',
    message: 'failed to create provisional user'
  }.to_json
end

post '/api/users' do
  content_type :json  
  data = JSON.parse(request.body.read)  

  unless data['password'].nil? or data['username'].nil?
    data['salt']     = BCrypt::Engine.generate_salt
    data['password'] = BCrypt::Engine.hash_secret(data['password'], data['salt'])

    u = User.create data
    
    if u.persisted?
      return {
        status: 'ok',
        message: 'created user',
        token: u.token
      }.to_json 
    end

  end

  {
    status: 'error',
    message: 'failed to create user'
  }.to_json
end


post '/api/matches' do
  content_type :json
  data = JSON.parse(request.body.read) 
  
  user = User.where( :token => data['token'] ).first
  
  unless user.nil?
    match = Match.create(data['match'])
    if match.persisted?

      return {
        status: 'ok',
        message: 'match created',
        match: match
      }.to_json 
    end
  end
  
  {
    status: 'error',
    message: 'failed to create match'
  }.to_json
end

#Accepts: JoinMatchRequest
#Returns: MatchResponse
post '/api/matches/:id/users' do
  content_type :json
  data = JSON.parse(request.body.read)
  
  match = Match.find(params[:id].to_i)
  user = User.where(:token => data['token']).first

  unless match.nil? or user.nil? or user.provisional?
    match.add_user user
    return {
      status: 'ok',
      message: 'joined match',
      match: match
    }.to_json 
  end
  
  {
    status: 'error',
    message: 'failed to join match'
  }.to_json
end


#Accepts: LocationMessage
#Returns: LocationResponse
post '/api/users/:token/location' do
  content_type :json
  data = JSON.parse(request.body.read)  
  
  user = User.where({ 
    :token => params[:token], 
    :install_id => data['install_id']
  }).first
  
  unless user.nil? 
  
    # if they are sending their location and are not in a match,
    # they are in freeplay - look around them for (and possibly generate)
    # some loot that they can pick up
    if user.player.nil?
      # query for loot, traps, etc.
      return { 
        status: 'ok', 
        message: '', 
        latitude: data['latitude'], 
        longitude: data['longitude'] 
        # achievements: [ ] 
        # nearby_items: [ ]
        # other_items:  [ ]
        # strength:     [ ] # (when not in a match, a user has a persistent strength)
      }.to_json
    elsif
      user.player.location = { lat: data['latitude'], lng: data['longitude'] }
      user.player.save
      user.match.notify_target user.player
      user.match.notify_enemy  user.player
    
      return { 
        status: 'ok', 
        message: '', 
        latitude: user.player.location[:lat], 
        longitude: user.player.location[:lng]  
        # items: [ ]
      }.to_json
    end
    
  end
  
  { 
    status: 'error', 
    message: (user.nil? ? 'invalid user' : 'user is not in a match')
  }.to_json
end


#Accepts: UserLoginMessage
#Returns: UserLoginResponse	
post '/api/login' do
  content_type :json
  data = JSON.parse(request.body.read)  
  
  u = User.where(:username => data['username']).first
  
  unless u.nil? or u.password != BCrypt::Engine.hash_secret(data['password'], u.salt)
   
    u.push_id = data['push_id']
    
    msg = 'login successful'
    if u.match.nil?
      u.install_id = data['install_id']
    else
      msg = 'cannot login on a different device when in a match'
    end
    
    u.save
    
    return {
      status: 'ok',
      message: msg,
      token: u.token
    }.to_json 
  end
  
  {
    status: 'error',
    message: 'failed to login'
  }.to_json
end


# TODO do not send notifications to a logged out user
# just check if token is null in send_push_notification
#post 'logout' do
#end

#Accepts: GCMRegistrationMessage
#Returns: UserLoginResponse 
post '/api/users/:token/gcm' do
  content_type :json
  data = JSON.parse(request.body.read)  
  
  user = User.where(:token => params[:token]).first

  unless u.nil?
    user.push_id = data['push_id']
    user.save
    return {
      status: 'ok',
      message: 'updated gcm registration id',
      token: user.token
    }.to_json 
  end
  
  {
    status: 'error',
    message: 'failed to update gcm registration id'
  }.to_json
end

#Accepts: GCMRegistrationMessage
#Returns: 
post '/api/users/:token/gcm/unregister' do
  content_type :json
  data = JSON.parse(request.body.read)  
  
  user = User.where(:token => params[:token]).first

  unless user.nil? or user.push_id != data['push_id']
    user.push_id = nil
    user.save
    return {
      status: 'ok',
      message: 'cleared gcm registration id',
      token: user.token
    }.to_json 
  end
  
  {
    status: 'error',
    message: 'failed to clear gcm registration id'
  }.to_json 
end


#Accepts: LocationMessage
#Returns: AttackResponse
post '/api/users/:token/attack' do
  content_type :json
  data = JSON.parse(request.body.read)  

  user = User.where({ 
    :token => params[:token], 
    :install_id => data['install_id']
  }).first
  
  unless user.nil? or user.player.nil? or user.match.winner != nil or user.player.life < 1
    
    match = user.match
    
    target = match.target_of user.player
    
    #TODO enforce attack range
    target.life = target.life - 1
    target.save
    
    target.user.send_push_notification({
       type: 'player_event',
       my_life: target.life
    })
    
    if target.life < 1
    
      match.player_ids.delete target
      match.save
      
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

