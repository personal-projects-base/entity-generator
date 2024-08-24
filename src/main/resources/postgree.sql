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

ALTER TABLE cpf  ADD CONSTRAINT ok_bataWVKSzP65F9hVMxVg  PRIMARY KEY (id);
ALTER TABLE city  ADD CONSTRAINT ok_rILB9AA1sSTBncCVWqrU  PRIMARY KEY (id);
ALTER TABLE state  ADD CONSTRAINT ok_1ak1KT1PHwCmPCdzbbxn  PRIMARY KEY (id);
ALTER TABLE country  ADD CONSTRAINT ok_ZaMQDBLkAjWihlEeMR4g  PRIMARY KEY (id);
-- Fks

ALTER TABLE cpf ADD CONSTRAINT fk_E1qCL3F2dyIbQ5owM6T0 FOREIGN KEY (city) REFERENCES city(id);
ALTER TABLE state ADD CONSTRAINT fk_a0XAVkuSDquq9IpwTjPD FOREIGN KEY (city) REFERENCES city(id);
ALTER TABLE country ADD CONSTRAINT fk_vlEZ7dK7tSHsbAAAC6nM FOREIGN KEY (state) REFERENCES state(id);
--RelationShips

