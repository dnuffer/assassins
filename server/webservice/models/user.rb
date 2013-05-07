require 'mongoid'
require 'pushmeup'
require 'securerandom'

class User
  include Mongoid::Document
  field :push_id,  type: String
  field :install_id,  type: String
  field :username, type: String
  field :password, type: String
  field :salt, type: String
  field :token, type: String
  field :provisional, type: Boolean, default: false
  
  belongs_to :match
  has_one    :player
  #has_many   :achievements
    
  validates :username, :password, presence: true, :if => :full_user?  
  validates_uniqueness_of :username, allow_nil: true
  validates_uniqueness_of :token,    allow_nil: true
    
  before_create :assign_token

  def in_match?
    match != nil and player != nil
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
  
  def self.authenticate token
    user = User.where(:token => token).first
    if user.nil?
      throw :halt, {  status: 'error',
                      message: 'authentication failure' }.to_json
    end
    user
  end
  
  def correct_password? pass
    BCrypt::Engine.hash_secret(pass, self.salt) == self.password
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
    
    unless user.nil? or not user.correct_password? data['password']
      if user.in_match?
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
      salt = BCrypt::Engine.generate_salt
      
      update_attributes!({
        salt:        salt,
        password:    BCrypt::Engine.hash_secret(pass, salt),
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


