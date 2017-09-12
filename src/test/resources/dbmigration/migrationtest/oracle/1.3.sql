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
create table migtest_e_ref (
  id                            number(10) not null,
  name                          varchar2(255) not null,
  constraint uq_migtest_e_ref_name unique (name),
  constraint pk_migtest_e_ref primary key (id)
);
create sequence migtest_e_ref_seq;

alter table migtest_ckey_detail drop constraint fk_migtest_ckey_detail_parent;
alter table migtest_e_basic drop constraint ck_migtest_e_basic_status;
alter table migtest_e_basic modify status default null;
alter table migtest_e_basic modify status null;
alter table migtest_e_basic add constraint ck_migtest_e_basic_status check ( status in ('N','A','I'));
alter table migtest_e_basic drop constraint uq_migtest_e_basic_description;
alter table migtest_e_basic modify some_date default null;
alter table migtest_e_basic modify some_date null;

update migtest_e_basic set user_id = 23 where user_id is null;
alter table migtest_e_basic drop constraint fk_migtest_e_basic_user_id;
alter table migtest_e_basic modify user_id default 23;
alter table migtest_e_basic modify user_id not null;
alter table migtest_e_basic add old_boolean number(1) default 0 not null;
alter table migtest_e_basic add old_boolean2 number(1);
alter table migtest_e_basic add eref_id number(10);

alter table migtest_e_basic drop constraint uq_mgtst__bsc_stts_ndxtst1;
alter table migtest_e_basic drop constraint uq_migtest_e_basic_name;
alter table migtest_e_basic drop constraint uq_migtest_e_basic_indextest4;
alter table migtest_e_basic drop constraint uq_migtest_e_basic_indextest5;
-- NOT YET IMPLEMENTED: alter table migtest_e_basic add constraint uq_migtest_e_basic_indextest2 unique  (indextest2);
-- NOT YET IMPLEMENTED: alter table migtest_e_basic add constraint uq_migtest_e_basic_indextest6 unique  (indextest6);
comment on column migtest_e_history.test_string is '';
comment on table migtest_e_history is '';
alter table migtest_e_history2 modify test_string default null;
alter table migtest_e_history2 modify test_string null;
create index ix_migtest_e_basic_indextest1 on migtest_e_basic (indextest1);
create index ix_migtest_e_basic_indextest5 on migtest_e_basic (indextest5);
drop index ix_migtest_e_basic_indextest3;
drop index ix_migtest_e_basic_indextest6;
alter table migtest_e_basic add constraint fk_migtest_e_basic_eref_id foreign key (eref_id) references migtest_e_ref (id);
create index ix_migtest_e_basic_eref_id on migtest_e_basic (eref_id);

