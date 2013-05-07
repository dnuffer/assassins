ENV['RACK_ENV'] = 'test'

require 'spec_helper'
require 'rspec'
require 'rack/test'
require 'json'

$LOAD_PATH.unshift('.')

RSpec.configure do |conf|
  conf.include Rack::Test::Methods
  conf.after(:all) do
    Mongoid.database.collections.each do |collection|
      unless collection.name.match(/^system\./)
        collection.remove
      end
    end
  end
end

describe 'Hunted Game' do
  include Rack::Test::Methods

  def app
    Sinatra::Application
  end


  it "accepts a provisional user" do
    SecureRandom.stub(:hex).and_return('provisionaltoken1')
    
    usr_json = IO.read("spec/provisional_user.json")
    post '/api/provisional-users', usr_json
    last_response.should be_ok
    actual = JSON.parse(last_response.body)
    puts last_response.body
    
  end

  
  it "upgrades a provisional user" do
    
    usr_json = IO.read("spec/provisional_user_upgrade.json")
    post '/api/users/provisionaltoken1', usr_json
    last_response.should be_ok
    actual = JSON.parse(last_response.body)
    puts last_response.body
  end
  
  
  it "accepts a new user" do
    SecureRandom.stub(:hex).and_return('token1', 'token2')
    
    usr_json = IO.read("spec/user.json")
    post '/api/users', usr_json
    last_response.should be_ok
    actual = JSON.parse(last_response.body)
    puts last_response.body
    
    usr_json = IO.read("spec/user2.json")
    post '/api/users', usr_json
    last_response.should be_ok
    actual = JSON.parse(last_response.body)    
    puts last_response.body
    
  end
  
  it "accepts a new match" do
    match_json = IO.read("spec/match.json")
    post '/api/matches', match_json
    last_response.should be_ok
    actual = JSON.parse(last_response.body)
    puts last_response.body
  end
  
  it "allows a user to join a match" do
    
    GCM.should_receive(:send_notification).once
    
    match_msg = JSON.parse(IO.read("spec/join_match.json"))
    match_name = match_msg['match_name']
    
    match_msg['token'] = 'token1'
    user_json = IO.read("spec/user.json")
    post "/api/matches/#{match_name}/players", match_msg.to_json
    last_response.should be_ok
    actual = JSON.parse(last_response.body)
    puts last_response.body
    
    match_msg['token'] = 'token2'
    user_json = IO.read("spec/user2.json")
    post "/api/matches/#{match_name}/players", match_msg.to_json
    last_response.should be_ok
    actual = JSON.parse(last_response.body)
    puts last_response.body
    
    sleep 0.4
  end
  
  it "accepts a user location update" do
    
    GCM.should_receive(:send_notification).exactly(4).times
    
    token="token1"      

    post("/api/users/#{token}/location", {
      install_id: "install1",
      latitude: 2,
      longitude: 3
    }.to_json)
    
    last_response.should be_ok
    actual = JSON.parse(last_response.body)
    puts last_response.body
    
    token="token2"
    
    post("/api/users/#{token}/location", {
      install_id: "install2",
      latitude: 3,
      longitude: 4
    }.to_json)
    
    last_response.should be_ok
    actual = JSON.parse(last_response.body)
    puts last_response.body
 
    sleep 0.4
  end
  
  it "accepts an attack" do
    GCM.should_receive(:send_notification).once
    token="token1"
    
    player_json = IO.read("spec/player.json")        

    post "/api/users/#{token}/attack", player_json 
    last_response.should be_ok
    actual = JSON.parse(last_response.body)
    puts last_response.body
    
    sleep 0.4
  end
  
  it "accepts an attack" do
    GCM.should_receive(:send_notification).exactly(4).times
    token="token1"
    
    player_json = IO.read("spec/player.json")        

    post "/api/users/#{token}/attack", player_json
    last_response.should be_ok
    actual = JSON.parse(last_response.body)
    puts last_response.body
    
    post "/api/users/#{token}/attack", player_json
    last_response.should be_ok
    actual = JSON.parse(last_response.body)
    puts last_response.body
    
    sleep 0.4
  end
  
end
