-- Migrationscripts for ebean unittest
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
alter table migtest_e_history2 drop column test_string2;
alter table migtest_e_history2 drop column test_string3;
alter table migtest_e_history2 drop column new_column;
alter table migtest_e_history5 drop column test_boolean;
alter table migtest_e_softdelete drop column deleted;
alter table migtest_oto_child drop column master_id;
-- apply post alter
drop table drop_main cascade constraints purge;
delimiter $$
declare
  expected_error exception;
  pragma exception_init(expected_error, -2289);
begin
  execute immediate 'drop sequence drop_main_seq';
exception
  when expected_error then null;
end;
$$;
drop table drop_main_drop_ref_many cascade constraints purge;
drop table drop_ref_many cascade constraints purge;
delimiter $$
declare
  expected_error exception;
  pragma exception_init(expected_error, -2289);
begin
  execute immediate 'drop sequence drop_ref_many_seq';
exception
  when expected_error then null;
end;
$$;
drop table drop_ref_one cascade constraints purge;
delimiter $$
declare
  expected_error exception;
  pragma exception_init(expected_error, -2289);
begin
  execute immediate 'drop sequence drop_ref_one_seq';
exception
  when expected_error then null;
end;
$$;
drop table drop_ref_one_to_one cascade constraints purge;
delimiter $$
declare
  expected_error exception;
  pragma exception_init(expected_error, -2289);
begin
  execute immediate 'drop sequence drop_ref_one_to_one_seq';
exception
  when expected_error then null;
end;
$$;
drop table migtest_e_test_binary cascade constraints purge;
delimiter $$
declare
  expected_error exception;
  pragma exception_init(expected_error, -2289);
begin
  execute immediate 'drop sequence migtest_e_test_binary_seq';
exception
  when expected_error then null;
end;
$$;
drop table migtest_e_test_json cascade constraints purge;
delimiter $$
declare
  expected_error exception;
  pragma exception_init(expected_error, -2289);
begin
  execute immediate 'drop sequence migtest_e_test_json_seq';
exception
  when expected_error then null;
end;
$$;
drop table migtest_e_test_lob cascade constraints purge;
delimiter $$
declare
  expected_error exception;
  pragma exception_init(expected_error, -2289);
begin
  execute immediate 'drop sequence migtest_e_test_lob_seq';
exception
  when expected_error then null;
end;
$$;
drop table migtest_e_test_varchar cascade constraints purge;
delimiter $$
declare
  expected_error exception;
  pragma exception_init(expected_error, -2289);
begin
  execute immediate 'drop sequence migtest_e_test_varchar_seq';
exception
  when expected_error then null;
end;
$$;
drop table migtest_e_user cascade constraints purge;
delimiter $$
declare
  expected_error exception;
  pragma exception_init(expected_error, -2289);
begin
  execute immediate 'drop sequence migtest_e_user_seq';
exception
  when expected_error then null;
end;
$$;
drop table migtest_mtm_c_migtest_mtm_m cascade constraints purge;
drop table migtest_mtm_m_migtest_mtm_c cascade constraints purge;
drop table migtest_mtm_m_phone_numbers cascade constraints purge;
