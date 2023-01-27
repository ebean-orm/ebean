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
call sysproc.admin_cmd('reorg table "table"');
alter table migtest_ckey_detail drop column one_key;
alter table migtest_ckey_detail drop column two_key;
call sysproc.admin_cmd('reorg table migtest_ckey_detail');
alter table migtest_ckey_parent drop column assoc_id;
call sysproc.admin_cmd('reorg table migtest_ckey_parent');
alter table migtest_e_basic drop column new_string_field;
alter table migtest_e_basic drop column new_boolean_field;
alter table migtest_e_basic drop column new_boolean_field2;
alter table migtest_e_basic drop column progress;
alter table migtest_e_basic drop column new_integer;
call sysproc.admin_cmd('reorg table migtest_e_basic');
alter table migtest_e_history drop column sys_period_start;
alter table migtest_e_history drop column sys_period_end;
alter table migtest_e_history drop column sys_period_txn;
call sysproc.admin_cmd('reorg table migtest_e_history');
alter table migtest_e_history2 drop column test_string2;
alter table migtest_e_history2 drop column test_string3;
alter table migtest_e_history2 drop column new_column;
call sysproc.admin_cmd('reorg table migtest_e_history2');
alter table migtest_e_history2_history drop column test_string2;
alter table migtest_e_history2_history drop column test_string3;
alter table migtest_e_history2_history drop column new_column;
call sysproc.admin_cmd('reorg table migtest_e_history2_history');
alter table migtest_e_history5 drop column test_boolean;
call sysproc.admin_cmd('reorg table migtest_e_history5');
alter table migtest_e_history5_history drop column test_boolean;
call sysproc.admin_cmd('reorg table migtest_e_history5_history');
alter table migtest_e_softdelete drop column deleted;
call sysproc.admin_cmd('reorg table migtest_e_softdelete');
alter table migtest_oto_child drop column master_id;
call sysproc.admin_cmd('reorg table migtest_oto_child');
alter table table_history drop column textfield;
alter table table_history drop column textfield2;
call sysproc.admin_cmd('reorg table table_history');
-- apply post alter
drop table migtest_e_history_history;
alter table migtest_e_history2 add versioning use history table migtest_e_history2_history;
alter table migtest_e_history5 add versioning use history table migtest_e_history5_history;
alter table "table" add versioning use history table table_history;
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
