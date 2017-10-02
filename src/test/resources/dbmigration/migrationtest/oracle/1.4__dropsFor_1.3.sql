-- apply changes
-- Migrationscript for oracle;
-- identity type: SEQUENCE;

delimiter $$
create or replace type EBEAN_TIMESTAMP_TVP is table of timestamp;
/
$$
delimiter $$
create or replace type EBEAN_DATE_TVP is table of date;
/
$$
delimiter $$
create or replace type EBEAN_NUMBER_TVP is table of number(38);
/
$$
delimiter $$
create or replace type EBEAN_FLOAT_TVP is table of number(19,4);
/
$$
delimiter $$
create or replace type EBEAN_STRING_TVP is table of varchar2(32767);
/
$$
alter table migtest_e_basic drop column new_string_field;

alter table migtest_e_basic drop column new_boolean_field;

alter table migtest_e_basic drop column new_boolean_field2;

alter table migtest_e_basic drop column progress;

alter table migtest_e_basic drop column new_integer;

alter table migtest_e_history2 drop column test_string2;

alter table migtest_e_history2 drop column test_string3;

alter table migtest_e_softdelete drop column deleted;

drop table migtest_e_user cascade constraints purge;
drop sequence migtest_e_user_seq;
