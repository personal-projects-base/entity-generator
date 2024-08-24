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

ALTER TABLE cpf  ADD CONSTRAINT ok_PMbKRfEd5jASiro9C3mD  PRIMARY KEY (id);
ALTER TABLE city  ADD CONSTRAINT ok_Dj9QBjKEWCXxHbWtv2Wf  PRIMARY KEY (id);
ALTER TABLE state  ADD CONSTRAINT ok_RgiWUdjTyrhwZ17u3oat  PRIMARY KEY (id);
ALTER TABLE country  ADD CONSTRAINT ok_JE68DKdOYvpIDBBMoK4k  PRIMARY KEY (id);
-- Fks

ALTER TABLE cpf ADD CONSTRAINT fk_YnT1f4FMj5KRbrTLzNTK FOREIGN KEY (city) REFERENCES city(id);
ALTER TABLE state ADD CONSTRAINT fk_HAwKZqVENSeeCVmiaFxv FOREIGN KEY (city) REFERENCES city(id);
ALTER TABLE country ADD CONSTRAINT fk_FYAzCHLRuME84YKwkyBs FOREIGN KEY (state) REFERENCES state(id);
--RelationShips

