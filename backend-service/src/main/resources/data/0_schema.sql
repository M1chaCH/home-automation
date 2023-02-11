DROP USER IF EXISTS java;
CREATE USER java WITH PASSWORD 'java';
GRANT ALL PRIVILEGES ON DATABASE room_automation TO  java;

drop table if exists scene cascade;
create table scene
(
    id serial primary key,
    name varchar(50) not null unique,
    default_scene boolean
);

drop table if exists yeelight_devices cascade;
create table yeelight_devices
(
--  id: id from YeeLight, name: given by user or generated one
    id int primary key,
    name varchar(50) not null unique
);

drop table if exists light_configuration cascade;
create table light_configuration
(
    id serial primary key,
    name varchar(50) not null unique,
    power boolean not null,
    red int not null,
    green int not null,
    blue int not null,
    brightness int not null,
    change_duration int
);

drop table if exists device_light_scene cascade;
create table device_light_scene
(
    device_id int references yeelight_devices(id),
    configuration_id int references light_configuration(id),
    scene_id int references scene(id)
);

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO java;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public to java;