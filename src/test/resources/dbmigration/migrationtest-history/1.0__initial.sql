-- Migrationscripts for ebean unittest DbMigrationDropHistoryTest
-- apply changes
create table migtest_e_history7 (
  id                            integer auto_increment not null,
  constraint pk_migtest_e_history7 primary key (id)
);

alter table migtest_e_history7 add column sys_period_start timestamp default now();
alter table migtest_e_history7 add column sys_period_end timestamp;
create table migtest_e_history7_history(
  id                            integer,
  sys_period_start              timestamp,
  sys_period_end                timestamp
);
create view migtest_e_history7_with_history as select * from migtest_e_history7 union all select * from migtest_e_history7_history;

create trigger migtest_e_history7_history_upd before update,delete on migtest_e_history7 for each row call "io.ebean.config.dbplatform.h2.H2HistoryTrigger";
