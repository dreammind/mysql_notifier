
class User < ActiveRecord::Base
  has_many :user_attributes
end

class UserAttribute < ActiveRecord::Base
  belongs_to :user
end

class EventQueue < ActiveRecord::Base
  belongs_to :user_attribute
end

ActiveRecord::Base.default_timezone = :utc
#logger = ActiveRecord::Base.logger = Logger.new(STDOUT)

ActiveRecord::Base.establish_connection(
  :adapter => "mysql", :host => "localhost",
  :username => "foo", :password => "foofoo", 
  :encoding => "utf8", :database => "test")

