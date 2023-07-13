drop table if exists scene cascade;
create table scene
(
    id serial primary key,
    name varchar(50) not null unique,
    default_scene boolean,
    spotify_resource varchar(250),
    spotify_volume int
);

drop table if exists yeelight_devices cascade;
create table yeelight_devices
(
    id serial primary key,
    name varchar(50) not null unique,
    device_ip varchar(20) not null unique
);

drop table if exists light_configuration cascade;
create table light_configuration
(
    id serial primary key,
    name varchar(50) not null unique,
    red int not null,
    green int not null,
    blue int not null,
    brightness int not null
);

drop table if exists device_light_scene cascade;
create table device_light_scene
(
    device_id int references yeelight_devices(id) on delete cascade,
    configuration_id int references light_configuration(id),
    scene_id int references scene(id) on delete cascade
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

drop table if exists alarm cascade;
create table alarm
(
    id serial primary key,
    cron_schedule varchar(50) not null,
    active bool not null default true,
    spotify_resource varchar(250) not null,
    max_volume int not null default 30
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
