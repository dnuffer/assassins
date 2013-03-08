require 'mongoid'

class Match
  include Mongoid::Document
  #include Mongoid::Spacial::Document
  
  #has_one :creator, class_name: "User"
  #has_one :winner,  class_name: "User"
  
  #field :name, String
  
  has_many :users
  
  #field :type, String
  
  #field :start_time, Integer
  #field :max_players, Integer
  
  #field :nw_corner, type: Array, spacial: {lat: :latitude, lng: :longitude}
  #field :se_corner, type: Array, spacial: {lat: :latitude, lng: :longitude}
  
  #field :hunt_range, Float
  #field :attack_range, Float
  #field :attack_delay, Integer
  
  #spacial_index :nw_corner
end
