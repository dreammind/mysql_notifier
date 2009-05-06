#!/usr/bin/ruby

require 'rubygems'

require 'active_record'
require File.dirname(__FILE__) + '/db_common'

User.find(:all).each do |user|
  puts "User##{user.id}: #{user.name}"
end

UserAttribute.find(:all).each do |user_attr|
  puts "UserAttribute##{user_attr.id}: user_id:#{user_attr.user_id}, #{user_attr.k}=#{user_attr.v}"
end

EventQueue.find(:all).each do |queue|
  puts "Queue##{queue.id}: attribute_id:#{queue.user_attribute_id}, #{queue.user_attribute.user.name}"
end
