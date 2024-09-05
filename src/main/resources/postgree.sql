--Entities

CREATE TABLE IF NOT EXISTS pessoa(
    
  id serial,
  name varchar,
  created_by varchar(80),
  created_date timestamp,
  last_modified_by varchar(80),
  last_modified_date timestamp
);

CREATE TABLE IF NOT EXISTS pessoa_juridica(
    
  id serial,
  cnpj varchar,
  pessoa integer,
  created_by varchar(80),
  created_date timestamp,
  last_modified_by varchar(80),
  last_modified_date timestamp
);

CREATE TABLE IF NOT EXISTS city(
    
  id uuid,
  name varchar,
  code varchar,
  state uuid,
  created_by varchar(80),
  created_date timestamp,
  last_modified_by varchar(80),
  last_modified_date timestamp
);

CREATE TABLE IF NOT EXISTS state(
    
  id uuid,
  name varchar,
  code varchar,
  country uuid,
  created_by varchar(80),
  created_date timestamp,
  last_modified_by varchar(80),
  last_modified_date timestamp
);

CREATE TABLE IF NOT EXISTS country(
    
  id uuid,
  name varchar,
  code varchar,
  created_by varchar(80),
  created_date timestamp,
  last_modified_by varchar(80),
  last_modified_date timestamp
);

CREATE TABLE IF NOT EXISTS fiscal(
    
  id serial,
  optante_simples boolean,
  mod_tributacao varchar,
  cod_regime_tributario integer,
  created_by varchar(80),
  created_date timestamp,
  last_modified_by varchar(80),
  last_modified_date timestamp
);

CREATE TABLE IF NOT EXISTS pessoa_telefone(
    
  id serial,
  fone varchar,
  pessoa integer,
  created_by varchar(80),
  created_date timestamp,
  last_modified_by varchar(80),
  last_modified_date timestamp
);

-- PKs

ALTER TABLE pessoa  ADD CONSTRAINT ok_JFDEkiDE5OD1a3lIXbAv  PRIMARY KEY (id);
ALTER TABLE pessoa_juridica  ADD CONSTRAINT ok_LS6ZGiZe5FrSj9eteddT  PRIMARY KEY (id);
ALTER TABLE city  ADD CONSTRAINT ok_DLsjB4XVAh1ewLLKJWvz  PRIMARY KEY (id);
ALTER TABLE state  ADD CONSTRAINT ok_aLwXE2O9Q19DBq3AUedY  PRIMARY KEY (id);
ALTER TABLE country  ADD CONSTRAINT ok_Nlsl8TW8HQf3lPjXX6iP  PRIMARY KEY (id);
ALTER TABLE fiscal  ADD CONSTRAINT ok_5pZXqBLkymJoWQdDxlgk  PRIMARY KEY (id);
ALTER TABLE pessoa_telefone  ADD CONSTRAINT ok_c8etUotV7Axot6xyVR9J  PRIMARY KEY (id);
-- Fks

ALTER TABLE pessoa_juridica ADD CONSTRAINT fk_Nwc1oNO7LrAQvGdfNYAv FOREIGN KEY (pessoa) REFERENCES pessoa(id);
ALTER TABLE city ADD CONSTRAINT fk_h68ZedvUCzKW10aofFFx FOREIGN KEY (state) REFERENCES state(id);
ALTER TABLE state ADD CONSTRAINT fk_aUDmsoxHUvuv29tlJHZn FOREIGN KEY (country) REFERENCES country(id);
ALTER TABLE pessoa_telefone ADD CONSTRAINT fk_dqhYGw6NiMS04qM2FAk9 FOREIGN KEY (pessoa) REFERENCES pessoa(id);
--RelationShips

