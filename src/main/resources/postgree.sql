--Entities

CREATE TABLE IF NOT EXISTS cpf(
    
  id integer,
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

ALTER TABLE cpf  ADD CONSTRAINT ok_Hmgjr0RTSRVMcE4pSrVv  PRIMARY KEY (id);
ALTER TABLE city  ADD CONSTRAINT ok_MWNuDv3bI0c39gmg29K3  PRIMARY KEY (id);
ALTER TABLE state  ADD CONSTRAINT ok_a8WdQ9y3dsKJrWIOnEv7  PRIMARY KEY (id);
ALTER TABLE country  ADD CONSTRAINT ok_pn0VFuupc0LBhZzSiZqQ  PRIMARY KEY (id);
-- Fks

ALTER TABLE cpf ADD CONSTRAINT fk_JzNJYiCjjahYf75rTOel FOREIGN KEY (city) REFERENCES city(id);
ALTER TABLE state ADD CONSTRAINT fk_E7NyQgJXtN4wwKefmt0T FOREIGN KEY (city) REFERENCES city(id);
ALTER TABLE country ADD CONSTRAINT fk_rEIEclWFElg56tEnUNc2 FOREIGN KEY (state) REFERENCES state(id);
--RelationShips

