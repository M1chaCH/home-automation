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

drop table if exists spotify_authorisation cascade;
create table spotify_authorisation
(
    id serial primary key,
    access_token varchar(250) not null unique,
    token_type varchar(250) not null,
    scope varchar(250) not null,
    generated_at int not null,
    expires_in int not null,
    refresh_token varchar(250) not null unique
);

drop function if exists validate_single_spotify_authorisation;
create function validate_single_spotify_authorisation()
    returns trigger
    language plpgsql
    as $$
    declare
        row_count int;
    begin
        select count(*) into row_count from spotify_authorisation;

        if row_count > 1 then
            raise sqlstate '27000' using message = 'cant insert more than one spotify authorisation';
        end if;
        return new;
    end;
$$;

create or replace trigger spotify_authorisation_insert_trigger
    before insert on spotify_authorisation
    for each row execute function validate_single_spotify_authorisation();


GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO java;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public to java;
GRANT ALL PRIVILEGES ON ALL PROCEDURES IN SCHEMA public to java;