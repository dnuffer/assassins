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
  
  field :last_attack, type: Integer
  
  #field :range_to_target, type: Enum[ :search, :hunt, :attack ]
  
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
  
  def range_to other_player
    if not other_player.nil? and other_player.location?
      dist = distance_to(other_player) 
      if dist < match.attack_range
        return :attack_range
      elsif dist < match.hunt_range
        return :hunt_range
      end
      return :search_range
    end
    return :unknown_range
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
    notify_target
    notify_enemy
  end
  
  def in_bounds?
    location[:lat].nil? or location[:lng].nil? ? false : match.in_bounds?(location[:lat], location[:lng])
  end

  def state
    my_enemy  = self.get_enemy
    my_target = self.get_target
    playerstate = {
      time:           Time.now.utc.to_i*1000,
      target_life:    life,
      target_bearing: self.bearing_to(my_target),
      target_range:   range_to(my_enemy),
      my_life:        life,
      enemy_range:    range_to(my_target)
    }
    enemy_distance = self.distance_to(my_enemy)
    unless my_enemy.nil? or enemy_distance.nil? or enemy_distance > match.hunt_range
      playerstate.merge!({
        target_lat: my_target.location[:lat],
        target_lng: my_target.location[:lng]
      })
    end       
    return playerstate
  end

  def notify_enemy
    my_enemy = self.get_enemy
    unless my_enemy.nil?
      notification = {
        type:           :target_event,
        time:           Time.now.utc.to_i*1000,
        target_life:    life,
        target_bearing: my_enemy.bearing_to(self),
        target_range:   range_to(my_enemy) 
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
        type:        :enemy_event,
        time:        Time.now.utc.to_i*1000,
        #an attack is considered an enemy event
        # attack is only indicated by a change in life
        # at the moment, life is always sent, even if life has not changed.
        # TODO send a separate 'attacked' message with current life
        my_life:     my_target.life,
        enemy_range: range_to(my_target)
      })
    end
  end   
end
