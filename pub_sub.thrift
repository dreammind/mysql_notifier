#!/usr/bin/thrift

namespace java gen_java

struct TUserAttribute
{
  1: string k
  2: string v
  3: i32 user_id
}

service Publisher
{
  list<TUserAttribute> get_user_attributes(1: i32 user_id)

  i64 subscribe(1: i32 user_id, 2: string my_host, 3: i32 my_port)
  void publish(1: list<TUserAttribute> user_attributes)
}

service Subscriber
{
  void notify(1: list<TUserAttribute> user_attributes)
}
