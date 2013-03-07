ENV['RACK_ENV'] = 'test'

require 'sinatra_app'
require 'rspec'
require 'rack/test'
require 'json'

$LOAD_PATH.unshift('.')

RSpec.configure do |conf|
  conf.include Rack::Test::Methods
  conf.before(:all) { DataMapper.finalize.auto_migrate! }
end

describe 'GCM_Prototype' do
  include Rack::Test::Methods

  def app
    Sinatra::Application
  end
  
  it "accepts a new user" do
    user_json = IO.read("spec/user.json")

    post '/services/users', user_json

    last_response.should be_ok

    actual = JSON.parse(last_response.body)
    
    puts last_response.body
    
    actual.should have_key('created_at')
    actual.should have_key('updated_at')
  end
  
  it "updates a user location" do
    user_json = IO.read("spec/user_with_location.json")

    
    #gcm = double('GCM')
    
    #gcm.should_receive(:send_notification).with(["DEF456"], { :install_id => "ABC123", 
    #                                                          :latitude => 1.0, 
    #                                                          :longitude => 1.5 })
    
    post '/services/location', user_json
    
    last_response.should be_ok

    puts last_response.body
    actual = JSON.parse(last_response.body)

    actual.should have_key('created_at')
    actual.should have_key('updated_at')
    
    actual['latitude'].should_not equal(nil)
    actual['latitude'].should_not equal(nil)
    
    prev_user_json = JSON.parse(IO.read("spec/user.json"))
       
    prev_user_json['latitude'].should_not equal(actual['latitude'])
    
    
  end
  
end
