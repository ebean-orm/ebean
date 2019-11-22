-- Inital script to create stored procedures etc for oracle platform
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
