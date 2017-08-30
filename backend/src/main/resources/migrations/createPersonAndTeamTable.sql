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

--rollback drop table team
--rollback drop table person