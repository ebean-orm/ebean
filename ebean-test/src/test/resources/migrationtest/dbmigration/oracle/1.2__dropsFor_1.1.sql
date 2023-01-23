-- Migrationscripts for ebean unittest
-- apply alter tables
alter table migtest_e_basic drop column description_file;
alter table migtest_e_basic drop column old_boolean;
alter table migtest_e_basic drop column old_boolean2;
alter table migtest_e_basic drop column eref_id;
alter table migtest_e_history2 drop column obsolete_string1;
alter table migtest_e_history2 drop column obsolete_string2;
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
drop table "migtest_QuOtEd" cascade constraints purge;
drop table migtest_e_ref cascade constraints purge;
delimiter $$
declare
  expected_error exception;
  pragma exception_init(expected_error, -2289);
begin
  execute immediate 'drop sequence migtest_e_ref_seq';
exception
  when expected_error then null;
end;
$$;
