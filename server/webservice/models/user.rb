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
    
  before_create :assign_token

  def full_user?
    provisional == false
  end
  
  def assign_token
    self.token = SecureRandom.hex
  end
  
  def send_push_notification msg_hash
    GCM.send_notification push_id, msg_hash
  end

end


