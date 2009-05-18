#!/usr/bin/env ruby
# usersテーブルに関連するuser_attribuesテーブルの属性情報が変化したとき、
# サブスクライバーに変化した属性を通知する。

require 'rubygems'

require 'thrift'
require 'gen-rb/publisher'
require 'gen-rb/subscriber'

require 'active_record'
require File.dirname(__FILE__) + '/db_common'

publisher_host = 'localhost'
publisher_port = 9090

class PublisherHandler
  def publish user_attributes
    print "[debug] publish: "; p user_attributes
    return if !user_attributes && user_attributes.size == 0

    user_attributes.each do |u_attr|
      user = User.find(u_attr.user_id)
      if user
        attr = UserAttribute.find_by_k_and_user_id(u_attr.k, user.id)
        if !attr
          UserAttribute.create(:k => u_attr.k, :v => u_attr.v, :user_id=>user.id)
        else
          attr.v = u_attr.v
          attr.save
        end
      end
    end
  end

  # if user_id == 0 then subscribe all users.
  def subscribe user_id, peer_host, peer_port
    return 0 if user_id < 0

    begin
      puts "[debug] subscribe: user_id:#{user_id}, host:#{peer_host}, port=#{peer_port}"

      sub_transport = Thrift::BufferedTransport.new(
	Thrift::Socket.new(peer_host, peer_port))
      subscriber = Subscriber::Client.new(Thrift::BinaryProtocol.new(sub_transport))
      sub_transport.open

      @subscription_id += 1

      @subscribers[user_id] = {} if !@subscribers[user_id]
      @subscribers[user_id][@subscription_id] = [subscriber, sub_transport]
      return @subscription_id
    rescue=>e
      p e
      return 0
    end
  end

  def notifies events
    return if @subscribers.size == 0

    u = {}; all_attrs = []
    events.each do |event|
      uattr = event.user_attribute

      t_user_attr = TUserAttribute.new
      t_user_attr.k = uattr.k
      t_user_attr.v = uattr.v
      t_user_attr.user_id = uattr.user_id

      u[uattr.user_id] = [] if !u[uattr.user_id]
      u[uattr.user_id].push(t_user_attr)

      all_attrs.push(t_user_attr) if @subscribers[0]
    end

    if @subscribers[0]
      map = @subscribers[0]
      next if !map
      notifies_sub map, all_attrs
    end

    u.each do |uid, u_attr|
      map = @subscribers[uid]
      next if !map
      notifies_sub map, u_attr
    end

    return
  end

  def notifies_sub map, u_attr
    closed = []
    map.each do |sub_id, arr|
      puts "[debug] Found subscription_id:#{sub_id}, attribute size:#{u_attr.size}"
      begin
        subscriber = arr[0]
        subscriber.notify(u_attr)
        puts "[debug] end of notify"
      rescue=>e
        closed.push(sub_id)      # すでにサブスクライバーは停止している
        p e
      end
    end

    closed.each do |sub_id|
      arr = map[sub_id]
      arr[1].close
      map.delete(sub_id)
    end
  end

  def get_user_attributes user_id
    puts "[debug] get_user_attributes: user_id:#{user_id}"

    ret = []
    user = User.find(user_id)
    if !user
       ret.push(TUserAttribute.new)
       return ret
    end

    user.user_attributes.each do |attr|
      t = TUserAttribute.new
      t.k = attr.k
      t.v = attr.v
      t.user_id = attr.user_id
      ret.push(t)
    end
    ret
  end

  def initialize
    @subscribers = {}  # TODO 排他制御
    @subscription_id = 0;
    @users = {}
  end
end

handler = PublisherHandler.new

t = Thread.new do 
  processor = Publisher::Processor.new(handler)
  transport = Thrift::ServerSocket.new(publisher_host, publisher_port)
  server = Thrift::ThreadPoolServer.new(processor, transport)
  puts "Starting the Publisher server..."
  server.serve
end

# 定期的にuser_attribuesテーブルが更新されたかどうかをチェックし、
# 更新されていれば、notifyで通知する。
while true do
  events = EventQueue.find(:all)   # DBからユーザの属性変更情報を取得
  if events != nil && events.size > 0
    handler.notifies events

    events.each do |event|
      event.delete
    end
  end
  sleep 1
end
