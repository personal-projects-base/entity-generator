--Entities

CREATE TABLE IF NOT EXISTS pessoa(
    
  id uuid,
  name varchar,
  is_active boolean,
  --created_by varchar(80),
  --created_date timestamp,
  --last_modified_by varchar(80),
  --last_modified_date timestamp
);

CREATE TABLE IF NOT EXISTS pessoa_endereco(
    
  id uuid,
  person uuid,
  address varchar,
  neighborhood varchar,
  number varchar,
  city uuid,
  complement varchar,
  postal_code varchar,
  --created_by varchar(80),
  --created_date timestamp,
  --last_modified_by varchar(80),
  --last_modified_date timestamp
);

CREATE TABLE IF NOT EXISTS city(
    
  id uuid,
  name varchar,
  ibge varchar,
  uf uuid,
  --created_by varchar(80),
  --created_date timestamp,
  --last_modified_by varchar(80),
  --last_modified_date timestamp
);

CREATE TABLE IF NOT EXISTS state(
    
  id uuid,
  name varchar,
  abreviation varchar,
  country uuid,
  --created_by varchar(80),
  --created_date timestamp,
  --last_modified_by varchar(80),
  --last_modified_date timestamp
);

CREATE TABLE IF NOT EXISTS country(
    
  id uuid,
  name varchar,
  --created_by varchar(80),
  --created_date timestamp,
  --last_modified_by varchar(80),
  --last_modified_date timestamp
);

CREATE TABLE IF NOT EXISTS pessoa_fisica(
    
  id uuid,
  person uuid,
  --created_by varchar(80),
  --created_date timestamp,
  --last_modified_by varchar(80),
  --last_modified_date timestamp
);

-- PKs

ALTER TABLE pessoa  ADD CONSTRAINT ok_wwOmpxCtFZZrO8kmVeeG  PRIMARY KEY (id);
ALTER TABLE pessoa_endereco  ADD CONSTRAINT ok_bDvmZy5GTN00guRe4BQS  PRIMARY KEY (id);
ALTER TABLE city  ADD CONSTRAINT ok_XkIkP7p6MMRc3agE0wOT  PRIMARY KEY (id);
ALTER TABLE state  ADD CONSTRAINT ok_dITOTkz68DgFTcfRBFZO  PRIMARY KEY (id);
ALTER TABLE country  ADD CONSTRAINT ok_7rRkBObV9m3Oj9l6O8pa  PRIMARY KEY (id);
ALTER TABLE pessoa_fisica  ADD CONSTRAINT ok_44pVqCtGJlFE7XwAM5eo  PRIMARY KEY (id);
-- Fks

ALTER TABLE pessoa_endereco ADD CONSTRAINT fk_FiuXLe330YOQlfSZ3A20 FOREIGN KEY (person) REFERENCES pessoa(id);
ALTER TABLE pessoa_endereco ADD CONSTRAINT fk_SbYsBGxddkjthiluQC16 FOREIGN KEY (city) REFERENCES city(id);
ALTER TABLE city ADD CONSTRAINT fk_7vuNVFX0xPbDycahFrF5 FOREIGN KEY (uf) REFERENCES state(id);
ALTER TABLE state ADD CONSTRAINT fk_Oy0QPyZMUatK0I4KOBaN FOREIGN KEY (country) REFERENCES country(id);
ALTER TABLE pessoa_fisica ADD CONSTRAINT fk_xSh9g5ZauzMmzWfeyYHU FOREIGN KEY (person) REFERENCES pessoa(id);
--RelationShips

