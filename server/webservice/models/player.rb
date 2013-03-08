require 'mongoid'
require 'geokit'
require 'mongoid_spacial'

class Player
    include Mongoid::Document
    include Mongoid::Spacial::Document
    
    belongs_to :user
    
    field :location, type: Array,   spacial: true
    field :life,     type: Integer, default: 3
    
    spacial_index :location
    
    def latlng
      GeoKit::LatLng.new(location[:lat], location[:lng])
    end
    
end
