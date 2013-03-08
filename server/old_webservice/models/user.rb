require 'mongoid'

class User
  include Mongoid::Document
  #field :username, type: String
  #field :password, type: String
  field :push_id,  type: String
  field :install_id,  type: String

  belongs_to :match
  has_one    :player
  #has_many   :achievements
end


