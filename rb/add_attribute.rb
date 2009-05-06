#!/usr/bin/ruby

require 'rubygems'
require 'active_record'
require File.dirname(__FILE__) + '/db_common'

def get_rand_str (length = 8)
  source=("a".."z").to_a + ("A".."Z").to_a + (0..9).to_a + ["_","-","."]
  key=""
  length.times{ key += source[rand(source.size)].to_s }
  key
end

name = "taro" + rand(10).to_s
user = User.find_by_name(name)
if user
  k = get_rand_str
  v = "1"
  attr = UserAttribute.find_by_k_and_user_id(k, user.id)
  if !attr
    UserAttribute.create(:k => k, :v => v, :user => user)
  else
    attr.v = v
    attr.save
  end
  puts "updated user_attributes table: #{k}:#{v}, user.id:#{user.id}"
else
  puts "#{name} not found."
end
