--Entities

CREATE TABLE IF NOT EXISTS pessoa(
    
  id serial,
  name varchar,
  --created_by varchar(80),
  --created_date timestamp,
  --last_modified_by varchar(80),
  --last_modified_date timestamp
);

CREATE TABLE IF NOT EXISTS pessoa_juridica(
    
  id serial,
  cnpj varchar,
  pessoa integer,
  --created_by varchar(80),
  --created_date timestamp,
  --last_modified_by varchar(80),
  --last_modified_date timestamp
);

CREATE TABLE IF NOT EXISTS pessoa_telefone(
    
  id serial,
  fone varchar,
  pessoa integer,
  --created_by varchar(80),
  --created_date timestamp,
  --last_modified_by varchar(80),
  --last_modified_date timestamp
);

-- PKs

ALTER TABLE pessoa  ADD CONSTRAINT ok_sLwhb94L1ZGHTdYmoD5w  PRIMARY KEY (id);
ALTER TABLE pessoa_juridica  ADD CONSTRAINT ok_tGlLmbgnAnnHlU9Plis5  PRIMARY KEY (id);
ALTER TABLE pessoa_telefone  ADD CONSTRAINT ok_3va473o7L3Uu2drDI8Rg  PRIMARY KEY (id);
-- Fks

ALTER TABLE pessoa_juridica ADD CONSTRAINT fk_K6ZPicVlWwYdnC0AyVAn FOREIGN KEY (pessoa) REFERENCES pessoa(id);
ALTER TABLE pessoa_telefone ADD CONSTRAINT fk_hK2QEurO3fpgWVRm1d4T FOREIGN KEY (pessoa) REFERENCES pessoa(id);
--RelationShips

