CREATE TABLE users(
	username VARCHAR(150),
	pass VARCHAR(150) NOT NULL,
	
	PRIMARY KEY (username)
);


CREATE TABLE posts(
	username VARCHAR(150) NOT NULL,
	scheduled_date TIMESTAMP,
	captions VARCHAR(255) NOT NULL,
	
	PRIMARY KEY(scheduled_date),
	
	FOREIGN KEY (username)
	REFERENCES users(username)
	ON UPDATE CASCADE
	ON DELETE CASCADE
);


CREATE TABLE files(
	scheduled_date TIMESTAMP,
	file_id BIGINT AUTO_INCREMENT,
	
	PRIMARY KEY (scheduled_date, file_id),
	
	FOREIGN KEY (scheduled_date)
	REFERENCES posts(scheduled_date)
	ON UPDATE CASCADE
	ON DELETE CASCADE
);



INSERT INTO posts (scheduled_date, captions) VALUES
('2025-04-01 10:00:00', 'Publicación sobre primavera'),
('2025-04-02 15:30:00', 'Consejos de productividad'),
('2025-04-03 20:00:00', 'Evento nocturno en vivo');

INSERT INTO files (scheduled_date, file_id) VALUES
('2025-04-01 10:00:00', 101),
('2025-04-01 10:00:00', 102),
('2025-04-02 15:30:00', 201),
('2025-04-02 15:30:00', 202),
('2025-04-03 20:00:00', 301);
