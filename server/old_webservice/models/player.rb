require 'mongoid'
require 'geokit'
require 'mongoid_spacial'

class Player
    include Mongoid::Document
    include Mongoid::Spacial::Document
    
    belongs_to :user
    
    key :location, type: Array,   spacial: {lng: :longitude, lat: :latitude}
    key :life, type: Integer, default: 3
    
    has_one    :target, class_name: "Player"
    belongs_to :enemy,  class_name: "Player"

    spacial_index :location
    
    def bearing_to_target
      latlng.heading_to(@target.latlng)
    end
    
    def enemy_proximity
      latlng.distance_to(@enemy.latlng)
    end
    
    def latlng
      GeoKit::LatLng.new(@location[:lat], @location[:lng])
    end
    
end
