CREATE TABLE utente (
	
    id int auto_increment NOT null,
    username VARCHAR(255) not null,
    password VARCHAR(255) not null,
    oauth varchar(255) not null,
    admin boolean not null default false,
    PRIMARY KEY (id)

);
