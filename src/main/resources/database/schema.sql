CREATE TABLE IF NOT EXISTS appcloud_authentication (
	access_token VARCHAR(255) NOT NULL, 
	access_token_expiration_time TIMESTAMP, 
	company VARCHAR(255) NOT NULL, 
	refresh_token VARCHAR(255) NOT NULL,  
	refresh_token_expiration_time TIMESTAMP,
	install_id VARCHAR(255) NOT NULL,
	PRIMARY KEY (company)
);