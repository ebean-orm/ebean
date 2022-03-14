-- Migrationscripts for ebean unittest DbMigrationDropHistoryTest
-- drop dependencies
drop trigger migtest_e_history7_history_upd;
drop view migtest_e_history7_with_history;
drop table migtest_e_history7_history;

-- apply alter tables
alter table migtest_e_history7 drop column sys_period_start;
alter table migtest_e_history7 drop column sys_period_end;
