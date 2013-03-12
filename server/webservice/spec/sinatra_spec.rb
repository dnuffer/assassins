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
    match_id=3
    
    user_json = IO.read("spec/user.json")
    post "/api/matches/#{match_id}/users", JSON.dump({ :token => "token1" })
    last_response.should be_ok
    actual = JSON.parse(last_response.body)
    puts last_response.body
    
    user_json = IO.read("spec/user2.json")
    post "/api/matches/#{match_id}/users", JSON.dump({ :token => "token2" })
    last_response.should be_ok
    actual = JSON.parse(last_response.body)
    puts last_response.body
  end
  
  it "accepts a user location update" do
    
    GCM.should_receive(:send_notification).twice
    token="token1"
    
    player_json = IO.read("spec/player.json")        

    post "/api/users/#{token}/location", player_json
    last_response.should be_ok
    actual = JSON.parse(last_response.body)
    puts last_response.body
    
  end
  
  it "accepts an attack" do
    GCM.should_receive(:send_notification).once
    token="token1"
    
    player_json = IO.read("spec/player.json")        

    post "/api/users/#{token}/attack", player_json 
    last_response.should be_ok
    actual = JSON.parse(last_response.body)
    puts last_response.body
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
