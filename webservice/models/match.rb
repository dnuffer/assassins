require 'mongoid'
require 'securerandom'
require 'bcrypt'
require 'mongoid_spacial'

class Match
  include Mongoid::Document
  include Mongoid::Spacial::Document
  
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
  
  field :start_time, type: Integer
  #field :max_players, Integer
  
  field :nw_corner, type: Array, spacial: true
  field :se_corner, type: Array, spacial: true
  
  field :hunt_range,   type: Float
  field :attack_range, type: Float
  field :escape_time,  type: Integer
  
  spacial_index :nw_corner
  
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
    unless in_progress? or new_user.nil? or new_user.in_match?
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
            player_joined_match: new_user.username
          })
        end
        
        if in_progress?
          players.each do |player|
          #TODO this is only valid if it starts when a minimum number of players
          # is reached.  For a timed start, it may have to be a timer on the client
          # that fires off and intiates all clients reporting their location to begin
          
            player.user.send_push_notification({
              type: :match_start,
              match: name
            }.merge!(player.state))
          end
        end
      end 
    end
  end
  
  def in_progress?
    has_begun? and players.length > 1 and winner.nil?
  end
  
  def has_begun?
    Time.now.utc.to_i > start_time
  end
  
  def is_public?
    password.nil?
  end
  
  def winner
    if player_ids.length == 1
      return Player.find(player_ids[0])
    end
    nil
  end
  
  def eliminate target
    self.player_ids.delete target.id.to_s
    self.save
    
    if in_progress? or has_begun?
      the_winner = winner
      
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
              type:  :match_end,
              match: self.name,
              winner: the_winner.user.username
            })
          end
        end
      end 
    end 
  end
  
  def target_of player
    if in_progress? and player_ids.length > 1
      index = player_ids.map { |id| id.to_s }.find_index player.id.to_s 
      unless index.nil? 
        target_index = (index == player_ids.length - 1) ? 0 : index + 1
        target = players.find(player_ids[target_index])
        if target.location.nil?
          # dropped from match because no location reported by start_time
          eliminate target
          return target_of player
        end
        return target
      end
    end
  end
  
  def enemy_of player
    if in_progress? and self.player_ids.length > 1
      index = player_ids.index player.id.to_s
      unless index.nil?
        target_index = (index == 0) ? player_ids.length-1 : index-1
        enemy = players.find(player_ids[target_index])
        if enemy.location.nil?
          eliminate enemy
          return enemy_of player
        end
        return enemy
      end
    end
  end
  
  def attempt_attack attacker
    target = target_of attacker
    if in_progress? and target != nil and attacker.alive? and attacker.distance_to(target) < attack_range
      target.take_hit 1
      attacker.notify_target
      
      if target.life < 1
        eliminate target    
        
        #TODO: for efficiency, just return this in http attack request
        Thread.new do
          if winner.nil?
            new_target_notif = {
              type: :new_target,
              match: self.name
            }.merge!(attacker.state)
            attacker.user.send_push_notification(new_target_notif)
          end
        end
      end
      return true
    end
    false
  end
  
end
