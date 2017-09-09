-- apply changes
alter table migtest_e_basic drop column new_string_field;

alter table migtest_e_basic drop column new_boolean_field;

alter table migtest_e_basic drop column new_boolean_field2;

alter table migtest_e_basic drop column progress;

alter table migtest_e_basic drop column new_integer;

alter table migtest_e_history2 drop column test_string2;
alter table migtest_e_history2_history drop column test_string2;

alter table migtest_e_history2 drop column test_string3;
alter table migtest_e_history2_history drop column test_string3;

alter table migtest_e_softdelete drop column deleted;

IF OBJECT_ID('migtest_e_user', 'U') IS NOT NULL drop table migtest_e_user;
alter table migtest_e_history set (system_versioning = off);
alter table migtest_e_history drop column sys_periodFrom;
alter table migtest_e_history drop column sys_periodTo;
