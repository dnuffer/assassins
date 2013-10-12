require 'mongoid'
require 'geokit'
require 'models/location'

class Player
  include Mongoid::Document
  
  belongs_to :user
  belongs_to :match
  
  field :life,     type: Integer, default: 3
  field :status,   type: String,  default: :pending
  
  field :last_attack, type: Integer
  
  #field :range_to_target, type: Enum[ :search, :hunt, :attack ]
  
  def location
    user.location
  end
  
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
  
  def adjust_health amt
      new_health = life + amt
      self.life = new_health > 0 ? new_health : 0
      save
  end
  
  def in_bounds?
    location[:lat].nil? or location[:lng].nil? ? false : match.in_bounds?(location[:lat], location[:lng])
  end

  def public_state
    {
      username:       user.username,
      match_id:       match_id,
      time:           Time.now.utc.to_i*1000,
      status:         status,
      health:         life
    }
  end
  
  def state
    my_enemy  = self.get_enemy
    my_target = self.get_target
    playerstate = {
      username:       user.username,
      match_id:       match_id,
      time:           Time.now.utc.to_i*1000,
      target_life:    (my_target.nil? ? nil : my_target.life),
      status:         status,
      target_bearing: self.bearing_to(my_target),
      target_range:   range_to(my_target),
      health:         life,
      enemy_range:    range_to(my_enemy),
      lat:            location[:lat],
      lng:            location[:lng],
      last_attack:    last_attack
    }
    
    #disclose target's location to this player if in range
    t_dist = self.distance_to(my_target)
    unless my_target.nil? or t_dist.nil? or t_dist > match.hunt_range
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
      my_enemy.user.send_push_notification(my_enemy.state.merge!({ type: :player_event }))
    end 
  end
  
  def notify_target
    my_target = self.get_target
    unless my_target.nil?
      my_target.user.send_push_notification(my_target.state.merge!({ type: :player_event }))
    end
  end
  
  def push_state
    user.send_push_notification(state.merge!({ type: :player_event }))
  end
     
end
