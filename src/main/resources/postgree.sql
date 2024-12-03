--Entities

CREATE TABLE IF NOT EXISTS person(
    
  id uuid,
  name varchar,
  status varchar,
  type varchar,
  gender varchar,
  nationality uuid,
  marital_status varchar,
  birthplace varchar,
  image varchar
);

CREATE TABLE IF NOT EXISTS person_address(
    
  id uuid,
  person uuid,
  address varchar,
  neighborhood varchar,
  number varchar,
  city uuid,
  complement varchar,
  postal_code varchar
);

CREATE TABLE IF NOT EXISTS city(
    
  id uuid,
  name varchar,
  ibge varchar,
  state uuid
);

CREATE TABLE IF NOT EXISTS state(
    
  id uuid,
  name varchar,
  abreviation varchar,
  country uuid
);

CREATE TABLE IF NOT EXISTS country(
    
  id uuid,
  name varchar,
  code_iso varchar,
  ddi varchar
);

CREATE TABLE IF NOT EXISTS person_docs(
    
  id uuid,
  person uuid,
  cpf varchar,
  rg varchar,
  cnpj varchar,
  birth_date date,
  type_person varchar,
  ie varchar
);

CREATE TABLE IF NOT EXISTS user_confirmation(
    
  id uuid,
  user_id uuid,
  hash varchar
);

CREATE TABLE IF NOT EXISTS person_telphone(
    
  id uuid,
  person uuid,
  phone varchar,
  cell_phone varchar
);

CREATE TABLE IF NOT EXISTS person_email(
    
  id uuid,
  person uuid,
  email varchar
);

CREATE TABLE IF NOT EXISTS position_members(
    
  id uuid,
  name varchar,
  description varchar
);

CREATE TABLE IF NOT EXISTS appointments(
    
  id uuid,
  scheduler_type uuid,
  obs varchar,
  person uuid
);

CREATE TABLE IF NOT EXISTS scheduler_type(
    
  id uuid,
  name varchar,
  color varchar,
  description varchar
);

CREATE TABLE IF NOT EXISTS plan_account(
    
  id uuid,
  description varchar,
  code_tree varchar,
  type varchar,
  parent_code uuid
);

CREATE TABLE IF NOT EXISTS cost_center(
    
  id uuid,
  description varchar,
  code_tree varchar,
  parent_code uuid
);

CREATE TABLE IF NOT EXISTS bank(
    
  id uuid,
  name varchar,
  code varchar
);

CREATE TABLE IF NOT EXISTS cash(
    
  id uuid,
  description varchar,
  type_cash varchar,
  bank uuid,
  number_account varchar,
  digit varchar,
  status varchar
);

CREATE TABLE IF NOT EXISTS financial(
    
  id uuid,
  description varchar,
  type_financial varchar,
  cash uuid,
  value numeric,
  person uuid,
  plan_account uuid,
  cost_center uuid,
  issue_date date,
  due_date date,
  payment_receipt_date date
);

CREATE TABLE IF NOT EXISTS transactions(
    
  id uuid,
  description varchar,
  value numeric,
  transaction_operation varchar,
  person uuid,
  financial uuid,
  date_transaction date,
  cash_transaction uuid
);

CREATE TABLE IF NOT EXISTS user_configuration(
    
  id uuid,
  name varchar,
  user_photo varchar,
  theme varchar,
  lang varchar,
  email varchar,
  hash uuid
);

CREATE TABLE IF NOT EXISTS cash_transactions(
    
  id uuid,
  start_date date,
  balance numeric,
  initial_balance numeric,
  final_balance numeric,
  cash uuid,
  end_date date
);

CREATE TABLE IF NOT EXISTS scheduler_events(
    
  id uuid,
  scheduler_type uuid,
  user uuid,
  initial_date timestamp,
  final_date timestamp,
  local varchar
);

-- PKs

ALTER TABLE person  ADD CONSTRAINT pk_person  PRIMARY KEY (id);
ALTER TABLE person_address  ADD CONSTRAINT pk_person_address  PRIMARY KEY (id);
ALTER TABLE city  ADD CONSTRAINT pk_city  PRIMARY KEY (id);
ALTER TABLE state  ADD CONSTRAINT pk_state  PRIMARY KEY (id);
ALTER TABLE country  ADD CONSTRAINT pk_country  PRIMARY KEY (id);
ALTER TABLE person_docs  ADD CONSTRAINT pk_person_docs  PRIMARY KEY (id);
ALTER TABLE user_confirmation  ADD CONSTRAINT pk_user_confirmation  PRIMARY KEY (id);
ALTER TABLE person_telphone  ADD CONSTRAINT pk_person_telphone  PRIMARY KEY (id);
ALTER TABLE person_email  ADD CONSTRAINT pk_person_email  PRIMARY KEY (id);
ALTER TABLE position_members  ADD CONSTRAINT pk_position_members  PRIMARY KEY (id);
ALTER TABLE appointments  ADD CONSTRAINT pk_appointments  PRIMARY KEY (id);
ALTER TABLE scheduler_type  ADD CONSTRAINT pk_scheduler_type  PRIMARY KEY (id);
ALTER TABLE plan_account  ADD CONSTRAINT pk_plan_account  PRIMARY KEY (id);
ALTER TABLE cost_center  ADD CONSTRAINT pk_cost_center  PRIMARY KEY (id);
ALTER TABLE bank  ADD CONSTRAINT pk_bank  PRIMARY KEY (id);
ALTER TABLE cash  ADD CONSTRAINT pk_cash  PRIMARY KEY (id);
ALTER TABLE financial  ADD CONSTRAINT pk_financial  PRIMARY KEY (id);
ALTER TABLE transactions  ADD CONSTRAINT pk_transactions  PRIMARY KEY (id);
ALTER TABLE user_configuration  ADD CONSTRAINT pk_user_configuration  PRIMARY KEY (id);
ALTER TABLE cash_transactions  ADD CONSTRAINT pk_cash_transactions  PRIMARY KEY (id);
ALTER TABLE scheduler_events  ADD CONSTRAINT pk_scheduler_events  PRIMARY KEY (id);
-- Fks

ALTER TABLE person ADD CONSTRAINT fk_person_country_nationality FOREIGN KEY (nationality) REFERENCES country(id);
ALTER TABLE person_address ADD CONSTRAINT fk_person_address_person_person FOREIGN KEY (person) REFERENCES person(id);
ALTER TABLE person_address ADD CONSTRAINT fk_person_address_city_city FOREIGN KEY (city) REFERENCES city(id);
ALTER TABLE city ADD CONSTRAINT fk_city_state_state FOREIGN KEY (state) REFERENCES state(id);
ALTER TABLE state ADD CONSTRAINT fk_state_country_country FOREIGN KEY (country) REFERENCES country(id);
ALTER TABLE person_docs ADD CONSTRAINT fk_person_docs_person_person FOREIGN KEY (person) REFERENCES person(id);
ALTER TABLE person_telphone ADD CONSTRAINT fk_person_telphone_person_person FOREIGN KEY (person) REFERENCES person(id);
ALTER TABLE person_email ADD CONSTRAINT fk_person_email_person_person FOREIGN KEY (person) REFERENCES person(id);
ALTER TABLE appointments ADD CONSTRAINT fk_appointments_scheduler_type_scheduler_type FOREIGN KEY (scheduler_type) REFERENCES scheduler_type(id);
ALTER TABLE appointments ADD CONSTRAINT fk_appointments_person_person FOREIGN KEY (person) REFERENCES person(id);
ALTER TABLE plan_account ADD CONSTRAINT fk_plan_account_plan_account_parent_code FOREIGN KEY (parent_code) REFERENCES plan_account(id);
ALTER TABLE cost_center ADD CONSTRAINT fk_cost_center_cost_center_parent_code FOREIGN KEY (parent_code) REFERENCES cost_center(id);
ALTER TABLE cash ADD CONSTRAINT fk_cash_bank_bank FOREIGN KEY (bank) REFERENCES bank(id);
ALTER TABLE financial ADD CONSTRAINT fk_financial_cash_cash FOREIGN KEY (cash) REFERENCES cash(id);
ALTER TABLE financial ADD CONSTRAINT fk_financial_person_person FOREIGN KEY (person) REFERENCES person(id);
ALTER TABLE financial ADD CONSTRAINT fk_financial_plan_account_plan_account FOREIGN KEY (plan_account) REFERENCES plan_account(id);
ALTER TABLE financial ADD CONSTRAINT fk_financial_cost_center_cost_center FOREIGN KEY (cost_center) REFERENCES cost_center(id);
ALTER TABLE transactions ADD CONSTRAINT fk_transactions_person_person FOREIGN KEY (person) REFERENCES person(id);
ALTER TABLE transactions ADD CONSTRAINT fk_transactions_financial_financial FOREIGN KEY (financial) REFERENCES financial(id);
ALTER TABLE cash_transactions ADD CONSTRAINT fk_cash_transactions_cash_cash FOREIGN KEY (cash) REFERENCES cash(id);
ALTER TABLE scheduler_events ADD CONSTRAINT fk_scheduler_events_scheduler_type_scheduler_type FOREIGN KEY (scheduler_type) REFERENCES scheduler_type(id);
ALTER TABLE scheduler_events ADD CONSTRAINT fk_scheduler_events_user_configuration_user FOREIGN KEY (user) REFERENCES user_configuration(id);
--RelationShips

