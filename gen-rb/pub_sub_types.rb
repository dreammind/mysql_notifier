#
# Autogenerated by Thrift
#
# DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
#


class TUserAttribute
  include ::Thrift::Struct
  K = 1
  V = 2
  USER_ID = 3

  ::Thrift::Struct.field_accessor self, :k, :v, :user_id
  FIELDS = {
    K => {:type => ::Thrift::Types::STRING, :name => 'k'},
    V => {:type => ::Thrift::Types::STRING, :name => 'v'},
    USER_ID => {:type => ::Thrift::Types::I32, :name => 'user_id'}
  }

  def struct_fields; FIELDS; end

  def validate
  end

end

