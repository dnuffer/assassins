require 'data_mapper'

class User
  include DataMapper::Resource
  property :id, DataMapper::Property::Serial
  property :install_id, String
  property :gcm_reg_id, String, :length => 1..255
  property :latitude, Float, :default => 0
  property :longitude, Float, :default => 0

  property :created_at, DateTime
  property :updated_at, DateTime 
end