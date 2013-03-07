require 'geokit'
require 'models/mappablemixin'

class Location
    
    #Mappable has distance_between class method
    
    def self.geocoords_to_float lat_lon_arr
       
       float_arr = []
       
       if lat_lon_arr.length == 2
         float_arr[0] = lat_lon_arr[0].to_f/1E6
         float_arr[1] = lat_lon_arr[1].to_f/1E6
       end
       
        return float_arr
    end
    
    #miles
    def self.dist_btwn a, b
      
      a_as_float = geocoords_to_float a
      b_as_float = geocoords_to_float b
      
      #convert array to LatLng objects
      a_norm = GeoKit::LatLng::normalize(a_as_float)
      b_norm = GeoKit::LatLng::normalize(b_as_float)
      
      MappableMixin.distance_between(a_norm, b_norm)
    end
    
    
    def self.bearing_btwn a, b
      
      a_as_float = geocoords_to_float a
      b_as_float = geocoords_to_float b
      
      #convert array to LatLng objects
      a_norm = GeoKit::LatLng::normalize(a_as_float)
      b_norm = GeoKit::LatLng::normalize(b_as_float)
      
      MappableMixin.heading_between(a_norm, b_norm)
    end
    
    
end
