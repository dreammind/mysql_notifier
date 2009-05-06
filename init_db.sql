drop table if exists event_queues;
drop table if exists user_attributes;
drop table if exists users;

drop trigger if exists ins_queues;
drop trigger if exists update_queues;

create table users(
       id int auto_increment not null,
       name varchar(128),
       index (name),
       primary key (id));

create table user_attributes (
       id int auto_increment not null,
       k varchar(128) not null,
       v varchar(512),
       user_id int references users(id),
       index (user_id),
       index (k),
       primary key (id));

create table event_queues (
       id int auto_increment not null,
       user_attribute_id int references user_attributes(id),
       index (user_attribute_id),
       primary key (id));

delimiter |
create trigger ins_queues after insert on user_attributes
     for each row begin
     insert into event_queues (user_attribute_id) values(NEW.id);
     end;
   |
create trigger update_queues after update on user_attributes
     for each row begin
     delete from event_queues where user_attribute_id = NEW.id;  
     insert into event_queues (user_attribute_id) values(NEW.id);
     end;
   |
delimiter ;

insert into users (name) values
       ('taro1'),('taro2'),('taro3'),('taro4'),('taro5'),('taro6'),('taro7'),
       ('taro8'),('taro9'),('taro0'),('hanako1');
insert into user_attributes(user_id,k,v) values(1, 'pos', 'shibuya');
