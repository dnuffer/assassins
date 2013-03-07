require 'rubygems'
require 'data_mapper'
require 'sinatra'
require 'json'
require 'haml'
require 'regex'
require 'pushmeup'

$LOAD_PATH.unshift('.')



require 'models/user'

enable :show_exceptions
enable :logging

#Android device push notification API KEY for google cloud messaging (GCM)
GCM.key = "AIzaSyCo6BoZUN9WwccMUPkUC69IcVp23YldgBY"

configure :production do 
  api = JSON.parse( ENV['VCAP_SERVICES'] )
  pgrKey = api.keys.select{ |s| s =~ /postgres/i }.first
  pgr = api[pgrKey].first['credentials']
  uri = "postgres://#{pgr['username']}:#{pgr['password']}@#{pgr['host']}:#{pgr['port']}/#{pgr['name']}"
  DataMapper::Logger.new( $stdout, :debug )
  DataMapper.setup( :default, uri )   
end

configure :test do 
  uri = "postgres://#{Dir.pwd}/test.db}"
  DataMapper::Logger.new( $stdout, :debug )
  DataMapper.setup( :default, uri ) #'sqlite::memory:')   
end

post '/services/users' do
  content_type :json
  data = JSON.parse request.body.read
  
  #does first_or_create only check the primary key?
  u = User.first( :install_id => data['install_id'] )
  
  logger.info data['gcm_reg_id'].length
  
  logger.info data
  logger.info u
  
  if u.nil?
    u = User.create data
  else
    u.update data
    u.save
  end
  
  logger.info u
  
  u.to_json
end

post '/services/location' do
  content_type :json
  data = JSON.parse request.body.read
  
  logger.info data
  
  u = User.first( :install_id => data['install_id'] )
  
  logger.info User.all
  logger.info u
  
  if !u.nil?
    u.update data
    u.save
    
    gcm_ids = User.all( :gcm_reg_id.not => nil ).map { |usr| usr.gcm_reg_id }
    #send new location to other users

    gcm_msg = { :install_id => u.install_id,
                :latitude   => u.latitude,
                :longitude  => u.longitude }

    logger.info gcm_msg
    
    response = GCM.send_notification( gcm_ids, gcm_msg )

    logger.info response
  end
  
  u.to_json
end

DataMapper.finalize.auto_upgrade!


