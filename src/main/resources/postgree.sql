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

ALTER TABLE cpf  ADD CONSTRAINT ok_R7gxsDg5K4mQ8pIPJ6hB  PRIMARY KEY (id);
ALTER TABLE city  ADD CONSTRAINT ok_NZoOroZctXrP0rgFCJxI  PRIMARY KEY (id);
ALTER TABLE state  ADD CONSTRAINT ok_CebNDd2VScABuX2iqTA9  PRIMARY KEY (id);
ALTER TABLE country  ADD CONSTRAINT ok_Fwy9zrSWGwPSkl79o4lg  PRIMARY KEY (id);
-- Fks

ALTER TABLE cpf ADD CONSTRAINT fk_HtZdZah4yIeUVfKauElw FOREIGN KEY (city) REFERENCES city(id);
ALTER TABLE state ADD CONSTRAINT fk_VyVXtneQeXeKWUXwJxqK FOREIGN KEY (city) REFERENCES city(id);
ALTER TABLE country ADD CONSTRAINT fk_RugAiBq9q3fhzLoubA7Z FOREIGN KEY (state) REFERENCES state(id);
