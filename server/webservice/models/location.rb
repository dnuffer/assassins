require 'geokit'

# TODO replace geokit (dying) with geocoder gem

class Location

  def self.bearing from, to
    if from != nil and to != nil and from.location? and to.location?
      if from.latlng == to.latlng # sidestep geokit equal points bug
        return 0.0
      else
        return from.latlng.heading_to(to.latlng)
      end
    end
  end
  
  def self.distance p1, p2
    if not p1.nil? and not p2.nil? and p1.location? and p2.location?
      return p1.latlng.distance_to(p2.latlng)
    end
    
    return 0.0 / 0.0 #NaN
  end
  
end

