--Entities

CREATE TABLE IF NOT EXISTS user_configuration(
    
  id uuid,
  name varchar,
  user_photo varchar,
  theme int,
  lang int,
  email varchar,
  phone varchar,
  hash uuid
);

CREATE TABLE IF NOT EXISTS user_confirmation(
    
  id uuid,
  user_id uuid,
  hash varchar
);

CREATE TABLE IF NOT EXISTS client(
    
  id uuid,
  name varchar,
  birth_date date,
  cpf varchar,
  phone varchar,
  email varchar,
  status int
);

CREATE TABLE IF NOT EXISTS service(
    
  id uuid,
  name varchar,
  description varchar,
  execution_time integer,
  price numeric,
  status int,
  color varchar
);

CREATE TABLE IF NOT EXISTS appointment(
    
  id uuid,
  client uuid,
  service uuid,
  start_date timestamp,
  end_date timestamp,
  description varchar,
  user_name uuid,
  cell_color varchar,
  status int,
  discount numeric,
  total numeric
);

CREATE TABLE IF NOT EXISTS extras(
    
  id uuid,
  service uuid,
  appointment uuid
);

-- PKs

ALTER TABLE user_configuration  ADD CONSTRAINT pk_user_configuration  PRIMARY KEY (id);
ALTER TABLE user_confirmation  ADD CONSTRAINT pk_user_confirmation  PRIMARY KEY (id);
ALTER TABLE client  ADD CONSTRAINT pk_client  PRIMARY KEY (id);
ALTER TABLE service  ADD CONSTRAINT pk_service  PRIMARY KEY (id);
ALTER TABLE appointment  ADD CONSTRAINT pk_appointment  PRIMARY KEY (id);
ALTER TABLE extras  ADD CONSTRAINT pk_extras  PRIMARY KEY (id);
-- Fks

ALTER TABLE appointment ADD CONSTRAINT fk_appointment_client_client FOREIGN KEY (client) REFERENCES client(id);
ALTER TABLE appointment ADD CONSTRAINT fk_appointment_service_service FOREIGN KEY (service) REFERENCES service(id);
ALTER TABLE appointment ADD CONSTRAINT fk_appointment_user_configuration_user_name FOREIGN KEY (user_name) REFERENCES user_configuration(id);
ALTER TABLE extras ADD CONSTRAINT fk_extras_service_service FOREIGN KEY (service) REFERENCES service(id);
ALTER TABLE extras ADD CONSTRAINT fk_extras_appointment_appointment FOREIGN KEY (appointment) REFERENCES appointment(id);
--RelationShips

