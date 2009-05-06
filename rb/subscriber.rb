#!/usr/bin/env ruby

require 'thrift'
require 'gen-rb/subscriber'
require 'gen-rb/publisher'

publisher_host = 'localhost'
publisher_port = 9090

subscriber_host='localhost'
subscriber_port=9091

class SubscriberHandler
  def notify user_attributes
    print "[debug] subscriber received a notify:"; p user_attributes
    return
  end
end

t = Thread.new do
  begin
    handler = SubscriberHandler.new
    processor = Subscriber::Processor.new(handler)
    transport = Thrift::ServerSocket.new(subscriber_host, subscriber_port)
    server = Thrift::ThreadPoolServer.new(processor, transport)

    puts "Starting the Subscriber server..."
    server.serve
  rescue=>e
    p e
  end
  puts "Thread stoped."
end

begin
  pub_transport = Thrift::BufferedTransport.new(
      Thrift::Socket.new(publisher_host, publisher_port))
  publiser    = Publisher::Client.new(Thrift::BinaryProtocol.new(pub_transport))
  pub_transport.open

  ta = TUserAttribute.new
  ta.k = "Time.now"
  ta.v = Time.now.to_s
  ta.user_id = 1
  publiser.publish([ta])  # publishのテスト

  user_attributes = publiser.get_user_attributes(1)
  print "user_attributes: "; p user_attributes

#  subscribe_id = publiser.subscribe(1, subscriber_host, subscriber_port)
  subscribe_id = publiser.subscribe(0, subscriber_host, subscriber_port) # 0 is all users.
  puts "subscribe_id: #{subscribe_id}"

  puts "waiting any arrived notify..."
  sleep
ensure
  pub_transport.close
end
