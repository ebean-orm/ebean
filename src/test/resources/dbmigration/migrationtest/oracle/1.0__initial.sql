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
create table migtest_ckey_assoc (
  id                            number(10) not null,
  assoc_one                     varchar2(255),
  constraint pk_migtest_ckey_assoc primary key (id)
);
create sequence migtest_ckey_assoc_seq;

create table migtest_ckey_detail (
  id                            number(10) not null,
  something                     varchar2(255),
  constraint pk_migtest_ckey_detail primary key (id)
);
create sequence migtest_ckey_detail_seq;

create table migtest_ckey_parent (
  one_key                       number(127) not null,
  two_key                       varchar2(127) not null,
  name                          varchar2(255),
  version                       number(10) not null,
  constraint pk_migtest_ckey_parent primary key (one_key,two_key)
);

create table migtest_e_basic (
  id                            number(10) not null,
  status                        varchar2(1),
  name                          varchar2(127),
  description                   varchar2(127),
  some_date                     timestamp,
  old_boolean                   number(1) default 0 not null,
  old_boolean2                  number(1),
  eref_id                       number(10),
  indextest1                    varchar2(127),
  indextest2                    varchar2(127),
  indextest3                    varchar2(127),
  indextest4                    varchar2(127),
  indextest5                    varchar2(127),
  indextest6                    varchar2(127),
  user_id                       number(10) not null,
  constraint ck_migtest_e_basic_status check ( status in ('N','A','I')),
  constraint uq_migtest_e_basic_indextest2 unique (indextest2),
  constraint uq_migtest_e_basic_indextest6 unique (indextest6),
  constraint pk_migtest_e_basic primary key (id)
);
create sequence migtest_e_basic_seq;

create table migtest_e_history (
  id                            number(10) not null,
  test_string                   varchar2(255),
  constraint pk_migtest_e_history primary key (id)
);
create sequence migtest_e_history_seq;

create table migtest_e_history2 (
  id                            number(10) not null,
  test_string                   varchar2(255),
  constraint pk_migtest_e_history2 primary key (id)
);
create sequence migtest_e_history2_seq;

create table migtest_e_ref (
  id                            number(10) not null,
  name                          varchar2(127) not null,
  constraint uq_migtest_e_ref_name unique (name),
  constraint pk_migtest_e_ref primary key (id)
);
create sequence migtest_e_ref_seq;

create table migtest_e_softdelete (
  id                            number(10) not null,
  test_string                   varchar2(255),
  constraint pk_migtest_e_softdelete primary key (id)
);
create sequence migtest_e_softdelete_seq;

create table migtest_mtm_c (
  id                            number(10) not null,
  name                          varchar2(255),
  constraint pk_migtest_mtm_c primary key (id)
);
create sequence migtest_mtm_c_seq;

create table migtest_mtm_m (
  id                            number(19) not null,
  name                          varchar2(255),
  constraint pk_migtest_mtm_m primary key (id)
);
create sequence migtest_mtm_m_seq;

create table migtest_oto_child (
  id                            number(10) not null,
  name                          varchar2(255),
  constraint pk_migtest_oto_child primary key (id)
);
create sequence migtest_oto_child_seq;

create table migtest_oto_master (
  id                            number(19) not null,
  name                          varchar2(255),
  constraint pk_migtest_oto_master primary key (id)
);
create sequence migtest_oto_master_seq;

create index ix_migtest_e_basic_indextest1 on migtest_e_basic (indextest1);
create index ix_migtest_e_basic_indextest5 on migtest_e_basic (indextest5);
alter table migtest_e_basic add constraint fk_migtest_e_basic_eref_id foreign key (eref_id) references migtest_e_ref (id);
create index ix_migtest_e_basic_eref_id on migtest_e_basic (eref_id);

