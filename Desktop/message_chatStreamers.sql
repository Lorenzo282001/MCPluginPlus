CREATE TABLE message_chatStreamers (
	
    timestamp TIMESTAMP DEFAULT current_timestamp,
    nameStreamer VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL,
    chat_message TEXT not null,
    length_message int not null,
    is_mod BOOLEAN not null,
	is_sub BOOLEAN not null,
    first_message BOOLEAN not null,
    is_turbo BOOLEAN not null,
    user_id varchar(255) not null,
    display_name varchar(255) not null,
    colorName varchar(10) not null
);