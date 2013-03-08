require 'mongo_mapper'
require 'geokit'
require 'json'


require 'models/profile'
require 'models/match'
require 'models/playerstate'
require 'models/gamesnapshot'
require 'models/location'

class AssassinsWebService
  
  def self.hook_up_database
     # parse cloudfoundry services string for mongoDB info to generate a mongoDB connection string
    services = JSON.parse(ENV['VCAP_SERVICES'])
    mongoKey = services.keys.select{|s| s =~ /mongodb/i}.first
    mongo = services[mongoKey].first['credentials']
    
    # build mongoDB connection string (uri) 
    # Ruby/Mongo uri format: 
    # "mongodb://[username:password@]host1[:port1][,host2[:port2],
    #            ...[,hostN[:portN]]][/[database][?options]]"
    uri = "mongodb://#{mongo['username']}:#{mongo['password']}@#{mongo['host']}:#{mongo['port']}/#{mongo['db']}"
    
    MongoMapper.connection = Mongo::Connection.from_uri(uri)
    MongoMapper.database = mongo['db']
   end
  
  
  def self.authenticate install_id
    p = Profile.find_by_install_id install_id
    
    unless p.nil? or p.current_match.nil?
      match = Match.find_by_id p.current_match
      match.players = get_playerstates_array match
      p.current_match = match  
    end
    
    p
  end
  
  def self.create_profile profile
    Profile.create profile
  end

  def self.get_profile profile_id
    p = Profile.find_by_id(profile_id)
  end
  
  def self.get_current_match profile_id
    p = Profile.find_by_id(profile_id)
    match = nil
    
    unless p.nil? or not p.in_match?
      match = Match.find_by_id(p.current_match)
      match.players = get_playerstates_array match
    end
    
    match
  end
  
  def self.get_secret_match name, pw, profile_id
    p = Profile.find_by_id(profile_id)
    match = nil

    unless p.nil?
      match = Match.find_by_name_and_password(name, pw)
      unless match.nil?
        match.players = get_playerstates_array match
      end
    end
    
    match
  end
  
  
  def self.create_match match, profile_id
  
    profile = Profile.find_by_id profile_id
    
    if profile != nil
      created_match = Match.create match
      created_match.creator = profile.username
      created_match.save
    end
    
    match
  end
 
  
  def self.join_match install_id, match_id
  
    m = Match.find_by_id(match_id)
    p = Profile.find_by_install_id(install_id)    
    s = nil

    unless m.nil? or p.nil? 
    
      if p.current_match != m._id
        s = PlayerState.create(:username => p.username)
        p.playerstate_id = s._id
        m.players.push(p.playerstate_id)
        p.current_match = m._id
        s.save
        m.save
        p.save
      end
      m.players = get_playerstates_array m
    end

    m
  end

  def self.update_game_snapshot profile_id, location
    
    p = Profile.find_by_id(profile_id)

    game_snapshot = GameSnapshot.new

    if p != nil && p.in_match?
        
        # get match and playerstate
        m = Match.find_by_id(p.current_match)
        my_state = PlayerState.find_by_id(p.playerstate_id)
        
        
        if my_state.is_alive?

            my_state.location = location
            my_state.save
        
            target_state = get_target p, m 
            
            if target_state != nil
            
                # miles
                distance = Location.dist_btwn     my_state.location, target_state.location
                bearing  = Location.bearing_btwn  my_state.location, target_state.location
                
                # updates target's playerstate!
                target_state = inform_of_enemy_proximity target_state, distance, m
                # does not update document in db - only returned to user
                my_state = inform_of_proximity_to_target my_state, distance, m
                
                game_snapshot.bearing_to_target = bearing
                game_snapshot.target_state = target_state
           else
                # we have a winner, folks!
                my_state.enemy_proximity = :no_assassin
                my_state.proximity_to_target = :no_target
                
                #clear match id and playerstate id from all players profiles
                #cleanup_match_references m
                #m.delete
           end
        end # alive?
    
        game_snapshot.my_state = my_state
    
    end # in match?
    
    game_snapshot
  end
  
  def self.cleanup_match_references match
 
    match.players.each do |state_id|
        
      state = PlayerState.find_by_id(state_id)

      curr_profile = Profile.find_by_username(state.username)

      curr_profile.current_match = nil
      curr_profile.playerstate_id = nil
        
    end
    
  end
  
  def self.inform_of_enemy_proximity playerstate, distance, match
    
    if distance < match.hunt_range
        # next time the target updates, 
        # they will receive a message that their assassin is near
        playerstate.enemy_proximity = :hunt_range
        playerstate.save
    elsif distance < match.hunt_range * 2
        playerstate.enemy_proximity = :alert_range
        playerstate.save
    else
        # don't disclose target location and health if not in range
        # clear location in copy, but do not save to document
        playerstate.enemy_proximity = :search_range
        playerstate.save
        # playerstate.location.clear
        # playerstate.life = -1
    end
    
    playerstate
  end
  
  def self.inform_of_proximity_to_target playerstate, distance, match
  
    if distance < match.attack_range
      playerstate.proximity_to_target = :attack_range
    elsif distance < match.hunt_range
      playerstate.proximity_to_target = :hunt_range
    elsif distance < (match.hunt_range * 2)
      playerstate.proximity_to_target = :alert_range
    else
      playerstate.proximity_to_target = :search_range
    end
      
    playerstate
  end

  
  def self.get_target profile, match
    # find the player's state index in the array of playerstates
    i = match.players.index(profile.playerstate_id)
    tmp = Array.new(match.players)
    
    # prepare the array of player_states to find target
    # make attacker the first in list
    player_states = tmp[i..-1]+tmp[0...i]
    # and then drop the attacker (can't assassinate self!)
    player_states.shift

    my_target = nil
    
    # search for the next player who is alive and kicking in the array  
    player_states.each do |state_id|
        
        potential_target = PlayerState.find_by_id(state_id)
        
        # if found a living target to assign
        if potential_target.is_alive? && potential_target.has_location?
            my_target =  potential_target
            break
        end 
        
    end

    my_target  
  end
  
  def self.attempt_attack game_snapshot, attack_range
    
    if attack_legal? game_snapshot, attack_range
      
      target_state = PlayerState.find_by_id game_snapshot.target_state._id
      target_state.life -= 1
      
      if target_state.life == 0
        target_state.enemy_proximity = :no_assassin
      end
      
      target_state.save
      game_snapshot.target_state = target_state
    end
    
    return game_snapshot
  end
  
  def self.attack_legal? game_snapshot, attack_range
    game_snapshot.attacker_alive? && 
    game_snapshot.target_alive?   && 
    game_snapshot.in_attack_range?(attack_range)
  end


  def self.get_public_matches_near_user profile_id, location
  
    #TODO: limit proximity - for now just show ALL public matches
    # MappableMixin.midpoint_between nw_corner, se_corner
      
    matches = Match.find_all_by_is_public true
    
    matches_with_states = matches.map do |match|
      match.players = get_playerstates_array match
      match
    end
    
    matches_with_states
  end
  
  def self.get_playerstates_array match
    # get each playerstate id and look up the actual document 
    states = []

    if not match.nil? && not match.players.nil? && match.players.length > 0
      states = match.players.map do |state| 
        state = PlayerState.find_by_id state
        unless state.nil?
          state.location = []
        end
        state
      end
    end
    states
  end
  
end
