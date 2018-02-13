-- apply changes
-- Migrationscripts for ebean unittest

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
create table migtest_e_user (
  id                            number(10) not null,
  constraint pk_migtest_e_user primary key (id)
);
create sequence migtest_e_user_seq;


update migtest_e_basic set status = 'A' where status is null;
alter table migtest_e_basic drop constraint ck_migtest_e_basic_status;
alter table migtest_e_basic modify status default 'A';
alter table migtest_e_basic modify status not null;
alter table migtest_e_basic add constraint ck_migtest_e_basic_status check ( status in ('N','A','I','?'));

-- rename all collisions;
-- NOT YET IMPLEMENTED: alter table migtest_e_basic add constraint uq_migtest_e_basic_description unique  (description);

update migtest_e_basic set some_date = { ts '2000-01-01 00:00:00' } where some_date is null;
alter table migtest_e_basic modify some_date default { ts '2000-01-01 00:00:00' };
alter table migtest_e_basic modify some_date not null;

insert into migtest_e_user (id) select distinct user_id from migtest_e_basic;
alter table migtest_e_basic add constraint fk_migtest_e_basic_user_id foreign key (user_id) references migtest_e_user (id);
alter table migtest_e_basic modify user_id null;
alter table migtest_e_basic add new_string_field varchar2(255) default 'foo''bar' not null;
alter table migtest_e_basic add new_boolean_field number(1) default 1 not null;
update migtest_e_basic set new_boolean_field = old_boolean;

alter table migtest_e_basic add new_boolean_field2 number(1) default 1 not null;
alter table migtest_e_basic add progress number(10) default 0 not null;
alter table migtest_e_basic add constraint ck_migtest_e_basic_progress check ( progress in (0,1,2));
alter table migtest_e_basic add new_integer number(10) default 42 not null;

alter table migtest_e_basic drop constraint uq_migtest_e_basic_indextest2;
alter table migtest_e_basic drop constraint uq_migtest_e_basic_indextest6;
-- NOT YET IMPLEMENTED: alter table migtest_e_basic add constraint uq_migtest_e_basic_name unique  (name);
-- NOT YET IMPLEMENTED: alter table migtest_e_basic add constraint uq_migtest_e_basic_indextest4 unique  (indextest4);
-- NOT YET IMPLEMENTED: alter table migtest_e_basic add constraint uq_migtest_e_basic_indextest5 unique  (indextest5);
comment on column migtest_e_history.test_string is 'Column altered to long now';
alter table migtest_e_history modify test_string number(19);
comment on table migtest_e_history is 'We have history now';

update migtest_e_history2 set test_string = 'unknown' where test_string is null;
alter table migtest_e_history2 modify test_string default 'unknown';
alter table migtest_e_history2 modify test_string not null;
alter table migtest_e_history2 add test_string2 varchar2(255);
alter table migtest_e_history2 add test_string3 varchar2(255) default 'unknown' not null;

alter table migtest_e_softdelete add deleted number(1) default 0 not null;

create index ix_migtest_e_basic_indextest3 on migtest_e_basic (indextest3);
create index ix_migtest_e_basic_indextest6 on migtest_e_basic (indextest6);
drop index ix_migtest_e_basic_indextest1;
drop index ix_migtest_e_basic_indextest5;
