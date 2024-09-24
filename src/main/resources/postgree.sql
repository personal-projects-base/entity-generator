--Entities

CREATE TABLE IF NOT EXISTS pessoa(
    
  id uuid,
  name varchar,
  created_by varchar(80),
  created_date timestamp,
  last_modified_by varchar(80),
  last_modified_date timestamp
);

CREATE TABLE IF NOT EXISTS pessoa_juridica(
    
  id uuid,
  cnpj varchar,
  pessoa uuid,
  created_by varchar(80),
  created_date timestamp,
  last_modified_by varchar(80),
  last_modified_date timestamp
);

CREATE TABLE IF NOT EXISTS pessoa_telefone(
    
  id uuid,
  phone varchar,
  pessoa uuid,
  type integer,
  created_by varchar(80),
  created_date timestamp,
  last_modified_by varchar(80),
  last_modified_date timestamp
);

-- PKs

ALTER TABLE pessoa  ADD CONSTRAINT ok_YoihVuJgZ0wg3eywHMcm  PRIMARY KEY (id);
ALTER TABLE pessoa_juridica  ADD CONSTRAINT ok_eHtFWlvdiHHtX1IB2rme  PRIMARY KEY (id);
ALTER TABLE pessoa_telefone  ADD CONSTRAINT ok_UmKwJ0fWkxXKZfjz8dcu  PRIMARY KEY (id);
-- Fks

--RelationShips

