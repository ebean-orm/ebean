-- Migrationscripts for ebean unittest
-- apply changes
alter table migtest_e_history drop versioning;
alter table migtest_e_history drop period system_time;
alter table migtest_e_history2 drop versioning;
alter table migtest_e_history5 drop versioning;
alter table "table" drop versioning;
-- apply alter tables
alter table "table" drop column textfield;
alter table "table" drop column textfield2;
call sysproc.admin_cmd('reorg table "table" ${reorgArgs}');
alter table migtest_ckey_detail drop column one_key;
alter table migtest_ckey_detail drop column two_key;
call sysproc.admin_cmd('reorg table migtest_ckey_detail ${reorgArgs}');
alter table migtest_ckey_parent drop column assoc_id;
call sysproc.admin_cmd('reorg table migtest_ckey_parent ${reorgArgs}');
alter table migtest_e_basic drop column new_string_field;
alter table migtest_e_basic drop column new_boolean_field;
alter table migtest_e_basic drop column new_boolean_field2;
alter table migtest_e_basic drop column progress;
alter table migtest_e_basic drop column new_integer;
call sysproc.admin_cmd('reorg table migtest_e_basic ${reorgArgs}');
alter table migtest_e_history drop column sys_period_start;
alter table migtest_e_history drop column sys_period_end;
alter table migtest_e_history drop column sys_period_txn;
call sysproc.admin_cmd('reorg table migtest_e_history ${reorgArgs}');
alter table migtest_e_history2 drop column test_string2;
alter table migtest_e_history2 drop column test_string3;
alter table migtest_e_history2 drop column new_column;
call sysproc.admin_cmd('reorg table migtest_e_history2 ${reorgArgs}');
alter table migtest_e_history2_history drop column test_string2;
alter table migtest_e_history2_history drop column test_string3;
alter table migtest_e_history2_history drop column new_column;
call sysproc.admin_cmd('reorg table migtest_e_history2_history ${reorgArgs}');
alter table migtest_e_history5 drop column test_boolean;
call sysproc.admin_cmd('reorg table migtest_e_history5 ${reorgArgs}');
alter table migtest_e_history5_history drop column test_boolean;
call sysproc.admin_cmd('reorg table migtest_e_history5_history ${reorgArgs}');
alter table migtest_e_softdelete drop column deleted;
call sysproc.admin_cmd('reorg table migtest_e_softdelete ${reorgArgs}');
alter table migtest_oto_child drop column master_id;
call sysproc.admin_cmd('reorg table migtest_oto_child ${reorgArgs}');
alter table table_history drop column textfield;
alter table table_history drop column textfield2;
call sysproc.admin_cmd('reorg table table_history ${reorgArgs}');
-- apply post alter
drop table migtest_e_history_history;
alter table migtest_e_history2 add versioning use history table migtest_e_history2_history;
alter table migtest_e_history5 add versioning use history table migtest_e_history5_history;
alter table "table" add versioning use history table table_history;
drop table drop_main;
delimiter $$
begin
if exists (select seqschema from syscat.sequences where seqschema = current_schema and ucase(seqname) = 'DROP_MAIN_SEQ') then
  prepare stmt from 'drop sequence drop_main_seq';
  execute stmt;
end if;
end$$;
drop table drop_main_drop_ref_many;
drop table drop_ref_many;
delimiter $$
begin
if exists (select seqschema from syscat.sequences where seqschema = current_schema and ucase(seqname) = 'DROP_REF_MANY_SEQ') then
  prepare stmt from 'drop sequence drop_ref_many_seq';
  execute stmt;
end if;
end$$;
drop table drop_ref_one;
delimiter $$
begin
if exists (select seqschema from syscat.sequences where seqschema = current_schema and ucase(seqname) = 'DROP_REF_ONE_SEQ') then
  prepare stmt from 'drop sequence drop_ref_one_seq';
  execute stmt;
end if;
end$$;
drop table drop_ref_one_to_one;
delimiter $$
begin
if exists (select seqschema from syscat.sequences where seqschema = current_schema and ucase(seqname) = 'DROP_REF_ONE_TO_ONE_SEQ') then
  prepare stmt from 'drop sequence drop_ref_one_to_one_seq';
  execute stmt;
end if;
end$$;
drop table migtest_e_test_binary;
delimiter $$
begin
if exists (select seqschema from syscat.sequences where seqschema = current_schema and ucase(seqname) = 'MIGTEST_E_TEST_BINARY_SEQ') then
  prepare stmt from 'drop sequence migtest_e_test_binary_seq';
  execute stmt;
end if;
end$$;
drop table migtest_e_test_json;
delimiter $$
begin
if exists (select seqschema from syscat.sequences where seqschema = current_schema and ucase(seqname) = 'MIGTEST_E_TEST_JSON_SEQ') then
  prepare stmt from 'drop sequence migtest_e_test_json_seq';
  execute stmt;
end if;
end$$;
drop table migtest_e_test_lob;
delimiter $$
begin
if exists (select seqschema from syscat.sequences where seqschema = current_schema and ucase(seqname) = 'MIGTEST_E_TEST_LOB_SEQ') then
  prepare stmt from 'drop sequence migtest_e_test_lob_seq';
  execute stmt;
end if;
end$$;
drop table migtest_e_test_varchar;
delimiter $$
begin
if exists (select seqschema from syscat.sequences where seqschema = current_schema and ucase(seqname) = 'MIGTEST_E_TEST_VARCHAR_SEQ') then
  prepare stmt from 'drop sequence migtest_e_test_varchar_seq';
  execute stmt;
end if;
end$$;
drop table migtest_e_user;
delimiter $$
begin
if exists (select seqschema from syscat.sequences where seqschema = current_schema and ucase(seqname) = 'MIGTEST_E_USER_SEQ') then
  prepare stmt from 'drop sequence migtest_e_user_seq';
  execute stmt;
end if;
end$$;
drop table migtest_mtm_c_migtest_mtm_m;
drop table migtest_mtm_m_migtest_mtm_c;
drop table migtest_mtm_m_phone_numbers;
