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
  
  it "accepts a new user" do
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
    post '/api/matches/3/players', user_json
    last_response.should be_ok
    actual = JSON.parse(last_response.body)
    puts last_response.body
    
    user_json = IO.read("spec/user2.json")
    post '/api/matches/3/players', user_json
    last_response.should be_ok
    actual = JSON.parse(last_response.body)
    puts last_response.body
  end
  
  it "accepts a user location update" do
    
    GCM.should_receive(:send_notification).twice
    
    player_json = IO.read("spec/player.json")        
  
    user_id=1
    
    post '/api/users/1/location', player_json
    
    last_response.should be_ok

    actual = JSON.parse(last_response.body)
    
    puts last_response.body
    
  end
  
end
