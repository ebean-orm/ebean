-- Migrationscripts for ebean unittest
-- apply changes
drop trigger migtest_e_history2_history_upd;
drop view migtest_e_history2_with_history;
-- apply alter tables
alter table migtest_e_basic drop column description_file;
alter table migtest_e_basic drop column old_boolean;
alter table migtest_e_basic drop column old_boolean2;
alter table migtest_e_basic drop column eref_id;
alter table migtest_e_history2 drop column obsolete_string1;
alter table migtest_e_history2 drop column obsolete_string2;
alter table migtest_e_history2_history drop column obsolete_string1;
alter table migtest_e_history2_history drop column obsolete_string2;
-- apply post alter
create view migtest_e_history2_with_history as select * from migtest_e_history2 union all select * from migtest_e_history2_history;
create trigger migtest_e_history2_history_upd before update,delete on migtest_e_history2 for each row call "io.ebean.config.dbplatform.h2.H2HistoryTrigger";
drop table if exists "migtest_QuOtEd";
drop table if exists migtest_e_ref;
drop sequence if exists migtest_e_ref_seq;
