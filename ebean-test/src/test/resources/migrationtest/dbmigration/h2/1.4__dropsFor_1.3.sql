-- Migrationscripts for ebean unittest
-- drop dependencies
drop trigger migtest_e_history_history_upd;
drop view migtest_e_history_with_history;
drop table migtest_e_history_history;

-- apply changes
drop trigger migtest_e_history2_history_upd;
drop view migtest_e_history2_with_history;
drop trigger migtest_e_history5_history_upd;
drop view migtest_e_history5_with_history;
drop trigger table_history_upd;
drop view table_with_history;
-- apply alter tables
alter table "table" drop column textfield;
alter table "table" drop column textfield2;
alter table migtest_ckey_detail drop column one_key;
alter table migtest_ckey_detail drop column two_key;
alter table migtest_ckey_parent drop column assoc_id;
alter table migtest_e_basic drop column new_string_field;
alter table migtest_e_basic drop column new_boolean_field;
alter table migtest_e_basic drop column new_boolean_field2;
alter table migtest_e_basic drop column progress;
alter table migtest_e_basic drop column new_integer;
alter table migtest_e_history drop column sys_period_start;
alter table migtest_e_history drop column sys_period_end;
alter table migtest_e_history2 drop column test_string2;
alter table migtest_e_history2 drop column test_string3;
alter table migtest_e_history2 drop column new_column;
alter table migtest_e_history2_history drop column test_string2;
alter table migtest_e_history2_history drop column test_string3;
alter table migtest_e_history2_history drop column new_column;
alter table migtest_e_history5 drop column test_boolean;
alter table migtest_e_history5_history drop column test_boolean;
alter table migtest_e_softdelete drop column deleted;
alter table migtest_oto_child drop column master_id;
alter table table_history drop column textfield;
alter table table_history drop column textfield2;
-- apply post alter
create view migtest_e_history2_with_history as select * from migtest_e_history2 union all select * from migtest_e_history2_history;
create trigger migtest_e_history2_history_upd before update,delete on migtest_e_history2 for each row call "io.ebean.platform.h2.H2HistoryTrigger";
create view migtest_e_history5_with_history as select * from migtest_e_history5 union all select * from migtest_e_history5_history;
create trigger migtest_e_history5_history_upd before update,delete on migtest_e_history5 for each row call "io.ebean.platform.h2.H2HistoryTrigger";
create view table_with_history as select * from "table" union all select * from table_history;
create trigger table_history_upd before update,delete on "table" for each row call "io.ebean.platform.h2.H2HistoryTrigger";
drop table if exists drop_main;
drop sequence if exists drop_main_seq;
drop table if exists drop_main_drop_ref_many;
drop table if exists drop_ref_many;
drop sequence if exists drop_ref_many_seq;
drop table if exists drop_ref_one;
drop sequence if exists drop_ref_one_seq;
drop table if exists drop_ref_one_to_one;
drop sequence if exists drop_ref_one_to_one_seq;
drop table if exists migtest_e_test_binary;
drop sequence if exists migtest_e_test_binary_seq;
drop table if exists migtest_e_test_json;
drop sequence if exists migtest_e_test_json_seq;
drop table if exists migtest_e_test_lob;
drop sequence if exists migtest_e_test_lob_seq;
drop table if exists migtest_e_test_varchar;
drop sequence if exists migtest_e_test_varchar_seq;
drop table if exists migtest_e_user;
drop sequence if exists migtest_e_user_seq;
drop table if exists migtest_mtm_c_migtest_mtm_m;
drop table if exists migtest_mtm_m_migtest_mtm_c;
drop table if exists migtest_mtm_m_phone_numbers;
