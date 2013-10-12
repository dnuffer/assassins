require 'mongoid'
require 'securerandom'
require 'bcrypt'
require 'mongoid_spacial'

class Match
  include Mongoid::Document
  include Mongoid::Spacial::Document
  
  field :creator, type: String
  field :name, type: String
  
  field :salt, type: String
  field :password, type: String
  field :token, type: String
  
  #has_many :users
  has_many :players
  
  # previous element is enemy, next element is target
  # as players are eliminated, they are removed
  field :player_ids, type: Array, default: []
  
  field :start_time, type: Integer, default: nil
  field :countdown_sec, type: Integer, default: 30
  field :end_time,   type: Integer, default: nil
  field :winner,     type: String, default: nil
  
  field :nw_corner, type: Array, spacial: true
  field :se_corner, type: Array, spacial: true
  
  field :hunt_range,   type: Float
  field :attack_range, type: Float
  field :escape_time,  type: Integer
  
  spacial_index :nw_corner
  
  validates_uniqueness_of :name
  before_create :assign_token

  def sanitized_with_players
    self.as_json({ include: [ :players ], except: [:salt, :password, :player_ids] })
  end
  
  def sanitized
    self.as_json(except: [:salt, :password, :player_ids])
  end
 
  #this will break if match bounds span hemispheres on the high-degrees
  def in_bounds? lat, lng
    #if no bounds specified, in bounds always returns true

    (nw_corner[:lat].nil? and 
     se_corner[:lat].nil? and 
     nw_corner[:lng].nil? and 
     se_corner[:lng].nil?) or
    (lat <= nw_corner[:lat] and 
     lat >= se_corner[:lat] and 
     lng >= nw_corner[:lng] and 
     lng <= se_corner[:lng])
  end

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
  
  def end_if_insufficient_players
    try_end_match
  end
  
  def add_user new_user
    unless in_progress? or new_user.nil? or new_user.in_match?
      new_player = new_user.players.create!(match_id: self.id)
      players << new_player
      player_ids << new_player.id.to_s
      player_ids.shuffle
      save
      
      #TODO this may still block in which case try: $gem install delayed_job
      Thread.new {
        
        new_player_state = {
            type: :player_joined_match
        }.merge!(new_player.public_state)
        
        #send to all but newest player (note player_ids are shuffled, NOT players)
        players[0...-1].each { |player|
          player.user.send_push_notification(new_player_state)
        }
        
        #this match was created to start when all players are ready 
        #instead of at a specific state time
        if start_time.nil? and all_players_ready?
          self.start_time = Time.now.utc.to_i*1000 + self.countdown_sec*1000
          self.save
          players.each {|p|
           p.user.send_push_notification({
              type: :match_start,
              match_id: id,
            }.merge!(p.state))
          }
        end
      } 
    end
  end
  
  def in_progress?
    has_begun? and players.length > 1 and winner.nil?
  end
  
  def has_begun?
    start_time != nil and Time.now.utc.to_i*1000 > start_time
  end
  
  def is_public?
    password.nil?
  end
  
  def get_winner
    if player_ids.length == 1
      return Player.find(player_ids[0])
    end
    nil
  end
  
  def eliminate target
    if in_progress?
      self.player_ids.delete target.id.to_s
      self.save
      
      #TODO this may still block in which case try: $gem install delayed_job
      target_state = target.state
      Thread.new {  
        players.each { |player|    
          player.user.send_push_notification(target_state)
        }
      }
      
      try_end_match
    end 
  end
  
  def try_end_match
      the_winner = get_winner
      if not the_winner.nil?
        self.winner = the_winner.user.username
        self.end_time = Time.now.utc.to_i*1000
        self.save
      end
      
      unless winner.nil?
        match_end = { 
          type: :match_end 
        }.merge!(self.sanitized)
        Thread.new {
          players.each { |player|    
            player.user.send_push_notification(match_end)
          }
        }
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
    
    if in_progress? and 
       not target.nil? and 
       attacker.alive? and 
       attacker.in_bounds? and 
       (escape_time.nil? or attacker.last_attack.nil? or 
        Time.now.utc.to_i  >= (attacker.last_attack + escape_time)) and
       attacker.distance_to(target) < attack_range

      target.adjust_health(-1)
      target.push_state
      
      if target.life < 1
        eliminate target    
        attacker.last_attack = nil
      else
        attacker.last_attack = Time.now.utc.to_i*1000
      end
      attacker.save
      return true
    end
    false
  end
  
end
