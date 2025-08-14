--Entities

CREATE TABLE IF NOT EXISTS comment(
    
  id uuid,
  id_post uuid,
  content varchar,
  replies_code uuid,
  created_at timestamp,
  likes integer
);

CREATE TABLE IF NOT EXISTS cost_center(
    
  id uuid,
  description varchar,
  code_tree varchar,
  parent_code uuid
);

-- PKs

ALTER TABLE comment  ADD CONSTRAINT pk_comment  PRIMARY KEY (id);
ALTER TABLE cost_center  ADD CONSTRAINT pk_cost_center  PRIMARY KEY (id);
-- Fks

ALTER TABLE comment ADD CONSTRAINT fk_comment_comment_replies_code FOREIGN KEY (replies_code) REFERENCES comment(id);
ALTER TABLE cost_center ADD CONSTRAINT fk_cost_center_cost_center_parent_code FOREIGN KEY (parent_code) REFERENCES cost_center(id);
--RelationShips

