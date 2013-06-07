require 'mongo_mapper'

class Health
  include MongoMapper::Document
  key :location, Array
  key :collector_id, Array
  key :time_appears, DateTime
  key :when_collected, DateTime
end
