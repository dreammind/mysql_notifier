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

def create_users
  for i in 0..10
    user = User.find_by_name("taro" + i.to_s)
    if !user
      User.create(:name => "taro#{i}")
    end
  end
end

def sample_add_attributes
  5.times do
    name = "taro" + rand(10).to_s
    user = User.find_by_name(name)
    if !user
      puts "#{name} not found."
      next
    end
    k = get_rand_str
    UserAttribute.create(:k => k, :v => "0", :user => user)
  end
end

create_users

start = Time.now
sample_add_attributes
puts "elaps time: #{Time.now - start}"
