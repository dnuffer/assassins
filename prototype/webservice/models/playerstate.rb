require 'mongo_mapper'

class PlayerState
    include MongoMapper::Document
    key :username, String
    key :location, Array,         :default => [-1,-1]
    key :life, Integer,           :default => 3
    key :enemy_proximity, String, :default => :search_range# - :search_mode, :hunt_mode
    key :proximity_to_target, String, :default => :unknown_range
    #key :kills, Integer

    def is_alive?
      @life > 0
    end
    
    def has_location?
      @location.length == 2
    end
    
end
