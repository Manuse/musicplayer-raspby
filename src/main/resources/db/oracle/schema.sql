create table if not exists authority (
id number(1) primary key,
name varchar2(50) unique);

CREATE SEQUENCE authority_id_seq

create or replace trigger trg_auth_id before insert on authority for each row
begin
select authority_id_seq.nextval
into :new.id
from dual;
end;
/

create table users (
id number(2) primary key,
login varchar2(50) NOT NULL UNIQUE,
password varchar2(70),
enabled number(1));

CREATE SEQUENCE users_id_seq_seq;

create or replace trigger trg_users_id before insert on users for each row
begin
select users_id_seq.nextval
into :new.id
from dual;
end;
/

create table users_authority (
id_user number(2),
id_authority number(1));

create table token (
series varchar2(50) primary key,
value varchar2(70),
dates timestamp,
ip_address varchar2(50),
user_agent varchar2(200),
user_login varchar2(50));

CREATE TABLE song (
  id number primary key,
  name VARCHAR2(100) NOT NULL,
  user_id number(2),
  CONSTRAINT fk_user_song
    FOREIGN KEY (user_id)
    REFERENCES users (id)
    ON DELETE cascade;
  
CREATE SEQUENCE song_id_seq;

create or replace trigger trg_song_id before insert on song for each row
begin
select song_id_seq.nextval
into :new.id
from dual;
end;
/

CREATE TABLE playlist (
  id number primary key,
  name varchar2(45) not null,
  user_id number(2),
  CONSTRAINT fk_user_playlist
    FOREIGN KEY (user_id)
    REFERENCES users (id)
    ON DELETE cascade);
  
CREATE SEQUENCE playlist_id_seq;

create or replace trigger trg_playlist_id before insert on playlist for each row
begin
select playlist_id_seq.nextval
into :new.id
from dual;
end;
/

CREATE TABLE song_playlist (
  sort number NOT NULL,
  song_id number NOT NULL,
  playlist_id number NOT NULL,
  PRIMARY KEY (song_id, playlist_id),
  CONSTRAINT fk_song_playlist_song
    FOREIGN KEY (song_id)
    REFERENCES song (id)
    ON DELETE cascade,
  CONSTRAINT fk_song_playlist_playlist
    FOREIGN KEY (playlist_id)
    REFERENCES playlist (id)
    ON DELETE cascade);
