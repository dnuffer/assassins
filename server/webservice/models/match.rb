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
  def hunt_range
    500.0
  end

  #field :attack_range, Float
  def attack_range
    50.0
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
    BCrypt::Engine.hash_secret(pass, self.salt) == self.password
  end
  
  def assign_token
    self.token = SecureRandom.hex
  end
  
  
  def add_user new_user
    unless new_user.nil? or new_user.in_match? #TODO or past match start time
      new_user.create_player
      users << new_user
      player_ids << new_user.player.id.to_s
      player_ids.shuffle
      save
      
      #TODO this may still block in which case try: $gem install delayed_job
      Thread.new do
        users[0...-1].each do |user|    
          user.send_push_notification({
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
    if in_progress?
      index = player_ids.index player.id.to_s
      unless index.nil? 
        target_index = (index == player_ids.length - 1) ? 0 : index + 1
        return Player.find(player_ids[target_index])
      end
    end
  end
  
  def enemy_of player
    if in_progress?
      index = player_ids.index player.id.to_s
      unless index.nil?
        target_index = (index == 0) ? player_ids.length-1 : index-1
        return Player.find(player_ids[target_index])
      end
    end
  end
  
  def bearing_to_target player
    if in_progress?
      target = target_of(player)
      if target.location? and player.location?
        return player.latlng.heading_to(target.latlng)
      end
    end
  end
  
  def proximity_to_target player
    if in_progress?
      target = target_of(player)
      if target.location? and player.location?
        return player.latlng.distance_to(target.latlng)
      end
    end
  end
  
  def notify_target_of enemy
    target = target_of enemy
    puts "notify_target_of(#{enemy.user.username}) [target: #{target.user.username}]"
    unless target.nil? or enemy.nil?
      target.user.send_push_notification({
        type:            :enemy_event,
        time:            Time.now.utc,
        my_life:         target.life,
        enemy_proximity: proximity_to_target(enemy)
      })
    end
    
  end

  def notify_enemy_of target
    enemy = enemy_of target
    puts "notify_enemy_of(#{target.user.username}) [enemy: #{enemy.user.username}]"
    unless enemy.nil? or target.nil?
      notification = {
        type:             :target_event,
        time:             Time.now.utc,
        target_life:      target.life,
        target_bearing:   bearing_to_target(enemy)
      }

      if proximity_to_target enemy < hunt_range
        notification.merge! {
          target_lat:       target.location[:lat],
          target_lng:       target.location[:lng]
        }
      end

      enemy.user.send_push_notification(notification)
    end 
  end
  
  
  def attempt_attack attacker
    target = target_of attacker

    if in_progress? and attacker.alive? and proximity_to_target attacker < attack_range
      target.take_hit 1
      notify_target_of attacker
      
      # sends a message to all users if a player is eliminated
      if target.life < 1
        eliminate target
        the_winner = (winner == attacker) ? attacker.user.username : nil
        
        #TODO this may still block in which case try: $gem install delayed_job
        Thread.new do 
          users.each do |user|    
            user.send_push_notification({
              type: :player_eliminated,
              player_eliminated: target.user.username
            })
            
            unless the_winner.nil?
              user.send_push_notification({
                type:  :match_winner,
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
