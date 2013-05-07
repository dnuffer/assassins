require 'mongoid'
require 'securerandom'
require 'bcrypt'

class Match
  include Mongoid::Document
  #include Mongoid::Spacial::Document
  
  #has_one :creator, class_name: "User"
  #has_one :winner,  class_name: "User"
  
  field :name, type: String
  field :salt, type: String
  field :password, type: String
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

  def self.authenticate match_name, match_password
    
    match = Match.where({ :name => match_name }).first
        
    if not match.nil? and (match.is_public? or match.correct_password? match_password)
      return match
    end
    
    throw :halt, {
      status:  'error',
      message: 'authentication failure'
    }.to_json
  end
  
  def correct_password? pass
    BCrypt::Engine.hash_secret(pass, self.salt) == self.password
  end
  
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
  
  def in_progress?
    self.winner.nil?
  end
  
  def is_public?
    self.password.nil?
  end
  
  def winner
    if player_ids.length == 1
      return Player.find(@player_ids[0])
    end
  end
  
  def eliminate target
    self.player_ids.delete target
    self.save
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
      #TODO do not send enemy location to target, send range data
      target.user.send_push_notification({ 
        #TODO do not send private data like install_id
        :install_id => enemy.user.install_id,
        :latitude   => enemy.location[:lat],
        :longitude  => enemy.location[:lng] 
      })
    end
  
  end
  
  def notify_enemy target
    enemy = enemy_of target
    
    unless enemy.nil?
      #TODO tailor message based on match parameters and distance between players
      enemy.user.send_push_notification ({ 
        :install_id => target.user.install_id,
        :latitude   => target.location[:lat],
        :longitude  => target.location[:lng] 
      })
    end
    
  end
  
end
