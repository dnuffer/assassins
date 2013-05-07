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
    
    def location?
      location[:lat] != nil and location[:lng] != nil
    end
    
    def alive?
      self.life > 0
    end
    
    def take_hit amount
      if amount <= self.life
        self.life = self.life - amount
        self.save
      end
    end
    
    def update_location lat, lng
      self.location = { lat: lat, lng: lng }
      self.save
    end
    
end
