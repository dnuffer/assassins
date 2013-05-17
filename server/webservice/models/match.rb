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
  
  #has_many :users
  has_many :players
  
  # previous element is enemy, next element is target
  # as players are eliminated, they are removed
  field :player_ids, type: Array, default: []
  
  #field :type, String
  
  #field :start_time, Integer
  #field :max_players, Integer
  
  #field :nw_corner, type: Array, spacial: true
  #field :se_corner, type: Array, spacial: true
  
  #field :hunt_range, Float
  def hunt_range
    0.25 # miles
  end

  #field :attack_range, Float
  def attack_range
    0.05 # miles
  end
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
    Match.hash_password(pass, self.salt) == self.password
  end
  
  def self.gen_salt
    BCrypt::Engine.generate_salt
  end
  
  def self.hash_password pass, salt
    BCrypt::Engine.hash_secret(pass, salt)
  end
  
  def assign_token
    self.token = SecureRandom.hex
  end
  
  
  def add_user new_user
    unless new_user.nil? or new_user.in_match? #TODO or past match start time
      new_user.create_player
      players << new_user.player
      player_ids << new_user.player.id.to_s
      player_ids.shuffle
      save
      
      #TODO this may still block in which case try: $gem install delayed_job
      Thread.new do
        players[0...-1].each do |player|
          player.user.send_push_notification({
            type: :player_joined_match,
            match: name,
            player: new_user.username
          })
        end
      end 
    end
  end
  
  def in_progress?
    winner.nil?
  end
  
  def is_public?
    password.nil?
  end
  
  def winner
    if player_ids.length == 1
      return Player.find(player_ids[0])
    end
  end
  
  def eliminate target
    self.player_ids.delete target
    self.save
  end
  
  def target_of player
    if in_progress? and self.player_ids.length > 1
      index = player_ids.index player.id.to_s
      unless index.nil? 
        target_index = (index == player_ids.length - 1) ? 0 : index + 1
        target = players.find(player_ids[target_index])
        return target
      end
    end
  end
  
  def enemy_of player
    if in_progress? and self.player_ids.length > 1
      index = player_ids.index player.id.to_s
      unless index.nil?
        target_index = (index == 0) ? player_ids.length-1 : index-1
        return players.find(player_ids[target_index])
      end
    end
  end
  
  def attempt_attack attacker
    target = target_of attacker
    if in_progress? and target != nil and attacker.alive? and attacker.distance_to(target) < attack_range
      target.take_hit 1
      attacker.notify_target
      # sends a message to all users if a player is eliminated
      if target.life < 1
        eliminate target
        the_winner = (winner == attacker) ? attacker.user.username : nil
        
        #TODO this may still block in which case try: $gem install delayed_job
        Thread.new do 
          players.each do |player|    
            player.user.send_push_notification({
              type: :player_eliminated,
              match: self.name,
              player_eliminated: target.user.username
            })
            
            unless the_winner.nil?
              player.user.send_push_notification({
                type:  :match_winner,
                match: self.name,
                match_winner: the_winner
              })
            end
          end
        end      
      end
      return true
    end
    false
  end
  
end
