DROP USER IF EXISTS java;
CREATE USER java WITH PASSWORD 'java';
GRANT ALL PRIVILEGES ON  DATABASE room_automation TO  java;

drop table if exists spotify_reference cascade;
create table spotify_reference
(
    id serial primary key,
    name varchar(50) not null unique,
    type bit not null,
    spotify_uri varchar(250) not null unique
);

drop table if exists scene cascade;
create table scene
(
    id serial primary key,
    name varchar(50) not null unique,
    time_trigger boolean default false,
    trigger_time time,
    schedule bytea,
    music boolean default false,
--     volume from 0 - 100 because this is how spotify handles volume
    min_volume int,
    max_volume int,
    spotify_reference_id int references spotify_reference(id)
);

drop table if exists yeelight_devices cascade;
create table yeelight_devices
(
    id varchar(250) primary key,
    name varchar(50) not null unique
);

drop table if exists light_configuration cascade;
create table light_configuration
(
    id serial primary key,
    name varchar(50) not null unique,
    color varchar(10) not null,
    scene_id int references scene(id)
);

drop table if exists device_light_configuration cascade;
create table device_light_configuration
(
    id serial primary key,
    device_id varchar(250) references yeelight_devices(id),
    configuration_id int references light_configuration(id)
);