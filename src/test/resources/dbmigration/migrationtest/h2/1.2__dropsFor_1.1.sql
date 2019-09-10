-- Migrationscripts for ebean unittest
-- drop dependencies
drop view if exists migtest_e_history2_with_history;

-- apply changes
alter table migtest_e_basic drop column old_boolean;

alter table migtest_e_basic drop column old_boolean2;

alter table migtest_e_basic drop column eref_id;

alter table migtest_e_history2 drop column obsolete_string1;
alter table migtest_e_history2_history drop column obsolete_string1;

alter table migtest_e_history2 drop column obsolete_string2;
alter table migtest_e_history2_history drop column obsolete_string2;

drop table if exists migtest_e_ref;
drop sequence if exists migtest_e_ref_seq;
create view migtest_e_history2_with_history as select * from migtest_e_history2 union all select * from migtest_e_history2_history;

-- changes: [drop obsolete_string1, drop obsolete_string2]
drop trigger migtest_e_history2_history_upd;
create trigger migtest_e_history2_history_upd before update,delete on migtest_e_history2 for each row call "io.ebean.config.dbplatform.h2.H2HistoryTrigger";
