require 'mongoid'
require 'geokit'
require 'models/location'
require 'mongoid_spacial'

class Player
  include Mongoid::Document
  include Mongoid::Spacial::Document
  
  belongs_to :user
  belongs_to :match
  
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
  
  def distance_to point
    Location::distance self, point
  end
  
  def bearing_to point
    Location::bearing self, point
  end
  
  def target
    self.match.target_of self unless match.nil?
  end
  
  def enemy
    self.match.enemy_of self unless match.nil?
  end
  
  def attack
    self.match.attempt_attack self
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

  def notify_enemy
    my_enemy = self.enemy
    unless my_enemy.nil?
      notification = {
        type:             :target_event,
        time:             Time.now.utc,
        target_life:      self.life,
        target_bearing:   my_enemy.bearing_to(self)
      }

      enemy_distance = my_enemy.distance_to(self)
      unless enemy_distance.nil? or enemy_distance > self.match.hunt_range
        notification.merge!({
          target_lat: self.location[:lat],
          target_lng: self.location[:lng]
        })
      end

      my_enemy.user.send_push_notification(notification)
    end 
  end
  
  def notify_target
    my_target = self.target
    unless my_target.nil?
      my_target.user.send_push_notification({
        type:            :enemy_event,
        time:            Time.now.utc,
        my_life:         my_target.life,
        enemy_proximity: self.distance_to(my_target)
      })
    end
    
  end

    
end
