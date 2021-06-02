CREATE TABLE chatroom (
  id SERIAL,
  name VARCHAR(80),
  PRIMARY KEY(id)
);

CREATE TABLE chatmessage (
  id SERIAL,
  name VARCHAR(1024),
  chatroom_id INT,
  PRIMARY KEY(id),
  CONSTRAINT fk_chatroom
    FOREIGN KEY(chatroom_id)
  	  REFERENCES chatroom(id)
);
