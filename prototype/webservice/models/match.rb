require 'mongo_mapper'

class Match
  include MongoMapper::Document
  key :creator, String
  key :name, String
  key :type, String
  key :is_public, Boolean
  key :start_time, Integer
  key :nw_corner, Array, :default => [-1, -1]
  key :se_corner, Array, :default => [-1, -1]
  key :password, String
  key :rules, Array,   :default => []
  key :invites, Array, :default => []
  key :players, Array, :default => []
  key :max_players, Integer
  key :hunt_range, Float
  key :attack_range, Float
  key :attack_delay, Integer
  key :end_time, Integer
  #key :match_status, Integer
  #key :winner, ObjectId
end
