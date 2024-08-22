--Entities

CREATE TABLE IF NOT EXISTS cpf(
    
  id uuid,
  number varchar,
  city uuid,
  created_by varchar(80),
  created_date timestamp,
  last_modified_by varchar(80),
  last_modified_date timestamp
);

CREATE TABLE IF NOT EXISTS city(
    
  id uuid,
  name varchar,
  code varchar,
  created_by varchar(80),
  created_date timestamp,
  last_modified_by varchar(80),
  last_modified_date timestamp
);

CREATE TABLE IF NOT EXISTS state(
    
  id uuid,
  name varchar,
  code varchar,
  city uuid,
  created_by varchar(80),
  created_date timestamp,
  last_modified_by varchar(80),
  last_modified_date timestamp
);

CREATE TABLE IF NOT EXISTS country(
    
  id uuid,
  name varchar,
  code varchar,
  state uuid,
  created_by varchar(80),
  created_date timestamp,
  last_modified_by varchar(80),
  last_modified_date timestamp
);

-- PKs

ALTER TABLE cpf  ADD CONSTRAINT ok_4isV9VRcyfcr14awNedT  PRIMARY KEY (id);
ALTER TABLE city  ADD CONSTRAINT ok_8Mo0eG58rGTIX3iFAXyw  PRIMARY KEY (id);
ALTER TABLE state  ADD CONSTRAINT ok_y9MGPFQSTDyrC7FOvEuK  PRIMARY KEY (id);
ALTER TABLE country  ADD CONSTRAINT ok_9AyoSkvmgAezY3KSDZZQ  PRIMARY KEY (id);
-- Fks

ALTER TABLE cpf ADD CONSTRAINT fk_Yk175znzaDJuqXwTrZKl FOREIGN KEY (city) REFERENCES city(id);
ALTER TABLE state ADD CONSTRAINT fk_znElyKbcOs5LAZV44bhu FOREIGN KEY (city) REFERENCES city(id);
ALTER TABLE country ADD CONSTRAINT fk_GpKbmSETocrSXiy3SQ5N FOREIGN KEY (state) REFERENCES state(id);
--RelationShips

