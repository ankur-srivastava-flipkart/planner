--liquibase formatted sql
--changeset author:ankur.srivastava

create table team (
  id int primary key AUTO_INCREMENT,
  name varchar(255),
  em_id int
);

create table person (
  id int primary key AUTO_INCREMENT,
  name varchar(255),
  email varchar(255),
  level varchar(255),
  productivity float
);

create table team_person (
  team_id varchar(255),
  teamMember_id varchar(255)
);

create table okr (
  id int primary key AUTO_INCREMENT,
  quarter varchar(255),
  description TEXT,
  jiraEpic varchar(255),
  effortinPersonDays INT,
  complexity varchar(255),
  priority INT,
  parallelism INT,
  status INT,
  willSpill BOOLEAN,
  team_id INT
);

--rollback drop table team
--rollback drop table person
--rollback drop table team_person
--rollback drop table okr