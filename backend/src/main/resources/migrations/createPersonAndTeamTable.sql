--liquibase formatted sql
--changeset author:ankur.srivastava

create table team (
  id int primary key AUTO_INCREMENT,
  name varchar(255)
);

create table person (
  id int primary key AUTO_INCREMENT,
  name varchar(255),
  email varchar(255)
);

--rollback drop table team
--rollback drop table person