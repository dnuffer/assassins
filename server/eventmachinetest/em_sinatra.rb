#require 'em-websocket'
#require 'sinatra/base'
require 'thin'
require 'eventmachine'
require 'sinatra/async'



require 'sinatra/async'

class AsyncTest < Sinatra::Base
  register Sinatra::Async

  aget '/' do
    body "hello async"
  end

  aget '/delay/:n' do |n|
    EM.add_timer(n.to_i) { body { "delayed for #{n} seconds" } }
  end

end

Thin::Server.start AsyncTest, '0.0.0.0', 4567


#EventMachine.run do
#  class App < Sinatra::Base
#    get '/' do
        
        
        #EventMachine.add_periodic_timer( 1 ) { puts "$" }
        
#        return "foo"
#    end
#  end


#  EM.add_timer(5) do
#        puts "I waited 5 seconds"
#  end
  #EM::WebSocket.start(:host => '0.0.0.0', :port => 3001) do
    # Websocket code here
  #end

  # You could also use Rainbows! instead of Thin.
  # Any EM based Rack handler should do.
#  Thin::Server.start App, '0.0.0.0', 4567
#end

