require 'rubygems'
require 'sinatra'
require 'mongoid'
require 'json'
require 'pushmeup'

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


post '/api/users' do
  content_type :json
  data = JSON.parse(request.body.read)  
  
  u = User.create(data)
  u.to_json
end

post '/api/matches' do
  content_type :json
  data = JSON.parse(request.body.read) 
  m = Match.create(data)
  m.to_json 
end

post '/api/matches/:id/players' do
  content_type :json
  data = JSON.parse(request.body.read)
  
  match = Match.find(params[:id].to_i)

  unless match.nil?
    match.add_user User.find(data['id'].to_i)
  end
  
  match.to_json(:include => [ :users ])  
end

post '/api/users/:id/location' do
  content_type :json
  data = JSON.parse(request.body.read)  
  
  user = User.find(params[:id].to_i)
  
  unless user.nil? or user.player.nil?
    user.player.location = data['location']
    user.match.notify_target user.player
    user.match.notify_enemy  user.player
  end
  
  user.player.to_json
end

#return wrapped response to allow for error handling like twitter api
# {"response" : null, "type" : "error", "errors":[{"message":"Bad Authentication data","code":215}]}

