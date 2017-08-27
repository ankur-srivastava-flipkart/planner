--liquibase formatted sql
--changeset author:ankur.srivastava

create table team (
  id int primary key,
  name varchar(255)
);

--rollback drop table team