--Entities

CREATE TABLE IF NOT EXISTS pessoa(
    
  id uuid,
  name varchar,
  active uuid,
  --created_by varchar(80),
  --created_date timestamp,
  --last_modified_by varchar(80),
  --last_modified_date timestamp
);

-- PKs

ALTER TABLE pessoa  ADD CONSTRAINT pk_pessoa  PRIMARY KEY (id);
-- Fks

--RelationShips

