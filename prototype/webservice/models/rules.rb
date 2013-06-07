require 'mongo_mapper'

class Rule
  include MongoMapper::Document
  key :grace_period, Integer
  key :regeneration_rule, String
  key :health_object, Array
end
