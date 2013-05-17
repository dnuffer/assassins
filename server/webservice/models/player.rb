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
    life > 0
  end
  
  def distance_to point
    Location::distance self, point
  end
  
  def bearing_to point
    Location::bearing self, point
  end
  
  def get_target
    match.target_of(self)
  end
  
  def get_enemy
    match.enemy_of(self)
  end
  
  def attack_target
    match.attempt_attack self
  end
  
  def take_hit amount
    if amount <= life
      self.life -= amount
      save
    end
  end
  
  def update_location lat, lng
    self.location = { lat: lat, lng: lng }
    save
  end

  def notify_enemy
    my_enemy = self.get_enemy
    unless my_enemy.nil?
      notification = {
        type:             :target_event,
        time:             Time.now.utc,
        target_life:      life,
        target_bearing:   my_enemy.bearing_to(self)
      }

      enemy_distance = my_enemy.distance_to(self)
      unless enemy_distance.nil? or enemy_distance > match.hunt_range
        notification.merge!({
          target_lat: location[:lat],
          target_lng: location[:lng]
        })
      end

      my_enemy.user.send_push_notification(notification)
    end 
  end
  
  def notify_target
    my_target = self.get_target
    unless my_target.nil?
      my_target.user.send_push_notification({
        type:            :enemy_event,
        time:            Time.now.utc,
        my_life:         my_target.life,
        enemy_proximity: distance_to(my_target)
      })
    end
    
  end

    
end
