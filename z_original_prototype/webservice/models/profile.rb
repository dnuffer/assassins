require 'mongo_mapper'

class Profile
  include MongoMapper::Document
  key :name,    String
  key :username, String
  key :password, String
  key :email, String
  key :install_id,  String
  key :rank,  String
  key :score, Integer
  key :achievements_completed, Array
  key :current_match, ObjectId
  key :playerstate_id, ObjectId
  
  def in_match?
    @current_match != nil && @playerstate_id != nil
  end
end
