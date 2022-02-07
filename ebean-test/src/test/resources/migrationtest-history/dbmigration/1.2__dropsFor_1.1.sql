-- Migrationscripts for ebean unittest DbMigrationDropHistoryTest
-- drop dependencies
drop trigger migtest_e_history7_history_upd;
drop view migtest_e_history7_with_history;
alter table migtest_e_history7 drop column sys_period_start;
alter table migtest_e_history7 drop column sys_period_end;
drop table migtest_e_history7_history;


-- apply changes
