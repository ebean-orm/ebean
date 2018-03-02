-- Migrationscripts for ebean unittest
-- apply changes
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
delimiter $$
create or replace type EBEAN_BINARY_TVP is table of raw(32767);
/
$$
alter table migtest_e_basic drop column old_boolean;

alter table migtest_e_basic drop column old_boolean2;

alter table migtest_e_basic drop column eref_id;

drop table migtest_e_ref cascade constraints purge;
drop sequence migtest_e_ref_seq;
