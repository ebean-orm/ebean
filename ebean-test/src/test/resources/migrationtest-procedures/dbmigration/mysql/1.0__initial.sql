-- Migrationscripts for ebean unittest
-- apply changes
create table migtest_e_basic (
  id                            integer auto_increment not null,
  status                        varchar(1),
  status2                       varchar(1) default 'N' not null,
  name                          varchar(127),
  description                   varchar(127),
  constraint pk_migtest_e_basic primary key (id)
);

