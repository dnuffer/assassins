require 'models/location'

class GameSnapshot
    attr_accessor :my_state, :target_state, :bearing_to_target

    def initialize my_state = nil, target_state = nil, bearing_to_target = -1
      @my_state = my_state
      @target_state = target_state
      @bearing_to_target = bearing_to_target
    end
    
    def attacker_alive?
      @my_state != nil && @my_state.life > 0
    end
    
    def target_alive?
      @target_state != nil && @target_state.life > 0
    end
    
    def in_attack_range? attack_range
      Location.dist_btwn(@my_state.location, @target_state.location) < attack_range
    end
end
