require 'mongoid'
require 'pushmeup'
require 'securerandom'
require 'bcrypt'
require 'mongoid_spacial'

class User
  include Mongoid::Document
  include Mongoid::Spacial::Document
  
  field :push_id,  type: String
  field :install_id,  type: String
  field :username, type: String
  field :password, type: String
  field :salt, type: String
  field :token, type: String
  field :provisional, type: Boolean, default: false

  has_many :players
  #has_many :achievements
  
  field :location, type: Array,   spacial: true
  spacial_index :location
    
  validates :username, :password, presence: true, :if => :full_user?  
  validates_uniqueness_of :username, allow_nil: true
  validates_uniqueness_of :token,    allow_nil: true
    
  before_create :assign_token

  def update_location lat, lng
    self.location = { lat: lat, lng: lng }
    save
    players.each { |p| 
      p.notify_target
      p.notify_enemy
    }
  end

  def in_match?
    players.select { |p| !p.match.has_begun? or p.match.in_progress? }.count > 0
  end

  def in_active_match?
    players.select { |p| p.match.has_begun? }.count > 0
  end

  def full_user?
    self.provisional == false
  end
  
  def assign_token
    self.token = SecureRandom.hex
  end
  
  def send_push_notification msg_hash
    if logged_in?
      GCM.send_notification self.push_id, msg_hash
    end
  end
  
  def self.gen_salt
    BCrypt::Engine.generate_salt
  end
  
  def self.hash_password pass, salt
    BCrypt::Engine.hash_secret(pass, salt)
  end
  
  def self.authenticate token
    user = User.where(:token => token).first
    if user.nil?
      throw :halt, {  status: 'error',
                      message: 'authentication failure' }.to_json
    end
    user
  end
  
  def correct_password? pass
    User.hash_password(pass, self.salt) == self.password
  end
  
  def logged_in?
    self.token != nil
  end
  
  def logout
    self.token = nil
    self.push_id = nil
    self.save
  end
  
  def self.login data

    user = User.where(:username => data['username']).first
    
    if not user.nil? and user.correct_password? data['password']
      if user.in_active_match?
        throw :halt, { status: 'error',
                       message: 'cannot login on a different device when in a match' }.to_json
      end
      
      # the push id can change per google, so need to update on login
      user.push_id = data['push_id']
      
      # the installation can change if they uninstall and reinstall
      # track this to make sure they do not switch devices in the middle of a match
      user.install_id = data['install_id']
      user.assign_token
      user.save 
      return user
    end
    
    throw :halt, { status: 'error',
                   message: 'failed to login' }.to_json
  end
  
  def upgrade_from_provisional name, pass
    unless name.nil? or pass.nil?
      salt = User.gen_salt
      
      update_attributes!({
        salt:        salt,
        password:    User.hash_password(pass, salt),
        username:    name,
        provisional: false
      })
      
      if not persisted?
        throw :halt, {
          status: 'error',
          message: 'failed to update user'
        }.to_json 
      end
      
      self.assign_token
      self.save
    end
  end
end


