ENV['RACK_ENV'] = 'test'

require 'spec_helper'
require 'rspec'
require 'rack/test'
require 'json'
require 'factory_girl'


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

FactoryGirl.define do
  factory :match do
    s = Match.gen_salt
    name 'my_match'
    salt s
    password Match.hash_password('password', s)
    token 'token'
  end
end

describe 'Match' do
  include Rack::Test::Methods
  
  it 'can be created' do
    stub = FactoryGirl.build_stubbed(:match)
    # TODO how to really test create without mongo?
  end
  
  it 'cannot be joined without correct password' do
    stub = FactoryGirl.build_stubbed(:match)
    Match.stub(:where).and_return [ stub ]
    expect { Match.authenticate 'my_match', 'drowssap' }.to throw_symbol(:halt) 
  end
  
  it 'can be joined with valid credentials' do
    stub = FactoryGirl.build_stubbed(:match)
    Match.stub(:where).and_return [ stub ]
    match = Match.authenticate 'my_match', 'password'
    match.should_not eq nil
  end
  
  it 'assigns targets' do
  
  end
  
  it 'discloses target location to attacker only when in hunt range' do
  
  end
  
  it 'disallows an attack when out of attack range' do
  
  end
  
  it 'notifies an attacker when they change range' do
  
  end

  it 'notifies a user when an enemy changes range' do
  
  end

  it 'notifies a user when a target moves' do
  
  end

  it 'allows an attack when in attack range' do
  
  end

  it 'notifies a user when they have been attacked' do
  
  end

  it 'notifies a user when the match is over' do
  
  end
  
  it 'notifies all users when a player is eliminated' do
  
  end
  
end

describe 'Sinatra App' do
  include Rack::Test::Methods

  def app
    Sinatra::Application
  end


  it "accepts a provisional user" do
    SecureRandom.stub(:hex).and_return('tempToken1', 'tempToken2')
    
    usr_json = IO.read("spec/user.json")
    post '/api/provisional-users', usr_json
    last_response.should be_ok
    actual = JSON.parse(last_response.body)
    puts last_response.body
    
    usr_json = IO.read("spec/user2.json")
    post '/api/provisional-users', usr_json
    last_response.should be_ok
    actual = JSON.parse(last_response.body)
    puts last_response.body
    
  end

  
  it "upgrades a provisional user" do
    SecureRandom.stub(:hex).and_return('token1', 'token2')
    
    usr_json = IO.read("spec/user.json")
    post "/api/users/tempToken1", usr_json
    last_response.should be_ok
    actual = JSON.parse(last_response.body)
    puts last_response.body
    
    usr_json = IO.read("spec/user2.json")
    post '/api/users/tempToken2', usr_json
    last_response.should be_ok
    actual = JSON.parse(last_response.body)
    puts last_response.body
  end
  
  
#  it "accepts a new user" do
#    SecureRandom.stub(:hex).and_return('token1', 'token2')
#    
#    usr_json = IO.read("spec/user.json")
#   post '/api/users', usr_json
#    last_response.should be_ok
#    actual = JSON.parse(last_response.body)
#    puts last_response.body
#    
#    usr_json = IO.read("spec/user2.json")
#    post '/api/users', usr_json
#    last_response.should be_ok
#    actual = JSON.parse(last_response.body)    
#    puts last_response.body    
#  end
  
  it "accepts a new match" do
    match_json = IO.read("spec/match.json")
    post '/api/matches', match_json
    last_response.should be_ok
    actual = JSON.parse(last_response.body)
    puts last_response.body
  end
  
  it "allows a user to logout/login" do
    SecureRandom.stub(:hex).and_return('newToken1')

    post "/api/users/token1/logout"
    last_response.should be_ok
    puts last_response.body
    
    usr_json = IO.read("spec/user.json")
    post "/api/login", usr_json
    last_response.should be_ok
    puts last_response.body
  
  end
  
  it "allows a user to join a match" do
    
    GCM.should_receive(:send_notification).once
    
    match_msg = JSON.parse(IO.read("spec/join_match.json"))
    match_name = match_msg['match_name']
    
    match_msg['token'] = 'newToken1'
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
    
    sleep 0.7
  end
  
  it "can get targets (debug feature)" do
    match_msg = JSON.parse(IO.read("spec/join_match.json"))
    match_name = match_msg['match_name'] 
    get "/api/matches/#{match_name}/targets"
    last_response.should be_ok
    puts last_response.body  
  end
  
  it "accepts a user location update" do
    
    GCM.should_receive(:send_notification).exactly(4).times
    
    token="newToken1"      

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
      latitude: 2,
      longitude: 3
    }.to_json)
    
    last_response.should be_ok
    actual = JSON.parse(last_response.body)
    puts last_response.body
 
    sleep 0.4
  end
  
  it "accepts an attack" do
    GCM.should_receive(:send_notification).once
    token="newToken1"
    
    player_json = IO.read("spec/player.json")        

    post "/api/users/#{token}/attack", player_json 
    last_response.should be_ok
    actual = JSON.parse(last_response.body)
    puts last_response.body
    
    sleep 0.4
  end
  
  it "accepts an attack" do
    GCM.should_receive(:send_notification).exactly(4).times
    token="newToken1"
    
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
