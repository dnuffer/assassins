require 'mongoid'
require 'securerandom'

class Match
  include Mongoid::Document
  #include Mongoid::Spacial::Document
  
  #has_one :creator, class_name: "User"
  #has_one :winner,  class_name: "User"
  
  field :name, type: String
  field :password, type: String
  field :salt, type: String
  field :token, type: String
  
  has_many :users
  
  # previous element is enemy, next element is target
  # as players are eliminated, they are removed
  field :player_ids, type: Array, default: []
  
  #field :type, String
  
  #field :start_time, Integer
  #field :max_players, Integer
  
  #field :nw_corner, type: Array, spacial: true
  #field :se_corner, type: Array, spacial: true
  
  #field :hunt_range, Float
  #field :attack_range, Float
  #field :attack_delay, Integer
  
  #spacial_index :nw_corner
  
  validates_uniqueness_of :name
  
  before_create :assign_token
  
  def assign_token
    self.token = SecureRandom.hex
  end
  
  def add_user user
    unless user.nil? #TODO or past start time
      user.create_player
      users << user
      player_ids << user.player.id.to_s
      player_ids.shuffle
      save
    end
  end
  
  def winner
    if player_ids.length == 1
      return Player.find(@player_ids[0])
    end
  end
  
  def target_of player
    index = player_ids.index player.id.to_s
    unless index.nil? 
      target_index = (index == player_ids.length - 1) ? 0 : index + 1
      return Player.find(player_ids[target_index])
    end
  end
  
  def enemy_of player
    index = player_ids.index player.id.to_s
    unless index.nil?
      target_index = (index == 0) ? player_ids.length-1 : index-1
      return Player.find(player_ids[target_index])
    end
  end
  
  def bearing_to_target player
    target = target_of(player)
    unless player.nil? or target.nil?
      return player.latlng.heading_to(target.latlng)
    end
  end
  
  def proximity_to_target player
    target = target_of(player)
    unless player.nil? or target.nil?
      return player.latlng.distance_to(target.latlng)
    end
  end
  
  def notify_target enemy
    target = target_of enemy
    
    unless target.nil?
      #TODO change message based on distance, etc
      target.user.send_push_notification({ 
        :install_id => enemy.user.install_id,
        :latitude   => enemy.location[:lat],
        :longitude  => enemy.location[:lng] 
      })
    end
  
  end
  
  def notify_enemy target
    enemy = enemy_of target
    
    unless enemy.nil?
      #TODO change message based on distance, etc
      enemy.user.send_push_notification ({ 
        :install_id => target.user.install_id,
        :latitude   => target.location[:lat],
        :longitude  => target.location[:lng] 
      })
    end
    
  end
  
end
