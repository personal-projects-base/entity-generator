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

ALTER TABLE pessoa  ADD CONSTRAINT ok_kX67M3BUWqrswbHtnPcC  PRIMARY KEY (id);
ALTER TABLE pessoa_juridica  ADD CONSTRAINT ok_slMRc8V5Pxm9yxhnkPq7  PRIMARY KEY (id);
ALTER TABLE city  ADD CONSTRAINT ok_X2UqDZgBs42jmsjiT76E  PRIMARY KEY (id);
ALTER TABLE state  ADD CONSTRAINT ok_B8gBlOJ4os6qUc4IrMCv  PRIMARY KEY (id);
ALTER TABLE country  ADD CONSTRAINT ok_ZIUliN5HS9v95g2r6BC3  PRIMARY KEY (id);
ALTER TABLE fiscal  ADD CONSTRAINT ok_IwjF9UaQJoB7dOrWNpgG  PRIMARY KEY (id);
ALTER TABLE pessoa_telefone  ADD CONSTRAINT ok_cdn5OPoXbHVTu1mWnLEQ  PRIMARY KEY (id);
-- Fks

ALTER TABLE pessoa_juridica ADD CONSTRAINT fk_XUz6xLXriOyVV3suP714 FOREIGN KEY (pessoa) REFERENCES pessoa(id);
ALTER TABLE city ADD CONSTRAINT fk_3I524048DvtcBPBbdfV9 FOREIGN KEY (state) REFERENCES state(id);
ALTER TABLE state ADD CONSTRAINT fk_eqko7zITYZHf3vnarUKw FOREIGN KEY (country) REFERENCES country(id);
ALTER TABLE pessoa_telefone ADD CONSTRAINT fk_tyKudalt2RWRLT9qVC1M FOREIGN KEY (pessoa) REFERENCES pessoa(id);
--RelationShips

