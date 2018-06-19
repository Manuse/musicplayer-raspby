create table if not exists authority (
id int(1) unsigned auto_increment primary key,
name varchar(50) unique);

create table if not exists users (
id int(2) unsigned auto_increment primary key,
login varchar(50) NOT NULL UNIQUE,
password varchar(70),
enabled boolean);

create table if not exists users_authority (
id_user int(2) unsigned,
id_authority int(1) unsigned);

create table if not exists token (
series varchar(50) primary key,
value varchar(70),
dates timestamp,
ip_address varchar(50),
user_agent varchar(200),
user_login varchar(50));


CREATE TABLE IF NOT EXISTS song (
  id INT UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  user_id int(2) unsigned,
  PRIMARY KEY (id),
  CONSTRAINT fk_user_song
    FOREIGN KEY (user_id)
    REFERENCES users (id)
    ON DELETE cascade)
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS playlist (
  id INT UNSIGNED NOT NULL AUTO_INCREMENT,
  name varchar(45) unique not null,
  user_id int(2) unsigned,
  PRIMARY KEY (id),
  CONSTRAINT fk_user_playlist
    FOREIGN KEY (user_id)
    REFERENCES users (id)
    ON DELETE cascade)
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS song_playlist (
  sort INT UNSIGNED NOT NULL,
  song_id INT UNSIGNED NOT NULL,
  playlist_id INT UNSIGNED NOT NULL,
  PRIMARY KEY (song_id, playlist_id),
  INDEX fk_song_playlist_song_idx (song_id ASC),
  INDEX fk_song_playlist_playlist_idx (playlist_id ASC),
  CONSTRAINT fk_song_playlist_song
    FOREIGN KEY (song_id)
    REFERENCES song (id)
    ON DELETE cascade,
  CONSTRAINT fk_song_playlist_playlist
    FOREIGN KEY (playlist_id)
    REFERENCES playlist (id)
    ON DELETE cascade)
ENGINE = InnoDB;
