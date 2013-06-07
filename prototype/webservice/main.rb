require 'mongo_mapper'
require 'geokit'
require 'sinatra'
require 'json'

#add the directory of this file to Ruby's load paths array
$LOAD_PATH.unshift('.')

require 'models/profile'
require 'models/match'
require 'models/playerstate'
require 'models/gamesnapshot'
require 'models/location'
require 'assassinswebservice'


configure do
  AssassinsWebService.hook_up_database
end


#def authenticate!
#  TODO Authenticate
#end

#before '/protected/*' do
#  authenticate!
#end


get '/protected/authenticate/:install_id' do
  content_type :json
  profile = AssassinsWebService.authenticate params[:install_id]
  profile.to_json
end

post '/protected/profile' do
  content_type :json
  #make sure there are no other duplicate install_id's
  profile = AssassinsWebService.create_profile JSON.parse(request.body.read.to_s)
  profile.to_json
end

post '/protected/match/:profile_id' do
  content_type :json
  match_json = JSON.parse(request.body.read.to_s)
  
  profile_id = params[:profile_id]
  created_match = AssassinsWebService.create_match match_json, profile_id
  created_match.to_json
end


get '/protected/public/matches/:oid/:lat/:lon' do
  content_type :json
  location = [ params[:lat], params[:lon] ]
  matches = AssassinsWebService.get_public_matches_near_user params[:oid], location
  matches.to_json
end

get '/protected/join/match/:match_id/:install_id' do
  content_type :json
  
  profile = AssassinsWebService.authenticate params[:install_id]
  match = nil
  
  if profile != nil
    match = AssassinsWebService.join_match params[:install_id], params[:match_id]
  end
  match.to_json
end

post '/update/location/:oid/:lat/:lon' do
  content_type :json
  location = [ params[:lat], params[:lon] ]
  game_snapshot = AssassinsWebService.update_game_snapshot params[:oid], location
  game_snapshot.to_json
end

post '/attack/:oid/:lat/:lon' do
  content_type :json
  location = [ params[:lat], params[:lon] ]
  game_snapshot = AssassinsWebService.update_game_snapshot params[:oid], location
  match = AssassinsWebService.get_current_match params[:oid]  
  game_snapshot = AssassinsWebService.attempt_attack game_snapshot, match.attack_range
  game_snapshot.to_json   
end

get '/current/match/:oid' do
  content_type :json
  match = AssassinsWebService.get_current_match params[:oid]  
  match.to_json
end

get '/secret/match/:name/:pw/:profile_id' do
  content_type :json
  match = AssassinsWebService.get_secret_match params[:name], params[:pw], params[:profile_id]  
  match.to_json
end

