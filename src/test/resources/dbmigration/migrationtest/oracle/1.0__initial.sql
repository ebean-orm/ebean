-- Migrationscripts for ebean unittest
-- apply changes
create table migtest_fk_cascade (
  id                            number(19) not null,
  one_id                        number(19),
  constraint pk_migtest_fk_cascade primary key (id)
);
create sequence migtest_fk_cascade_seq;

create table migtest_fk_cascade_one (
  id                            number(19) not null,
  constraint pk_migtest_fk_cascade_one primary key (id)
);
create sequence migtest_fk_cascade_one_seq;

create table migtest_fk_none (
  id                            number(19) not null,
  one_id                        number(19),
  constraint pk_migtest_fk_none primary key (id)
);
create sequence migtest_fk_none_seq;

create table migtest_fk_none_via_join (
  id                            number(19) not null,
  one_id                        number(19),
  constraint pk_migtest_fk_none_via_join primary key (id)
);
create sequence migtest_fk_none_via_join_seq;

create table migtest_fk_one (
  id                            number(19) not null,
  constraint pk_migtest_fk_one primary key (id)
);
create sequence migtest_fk_one_seq;

create table migtest_fk_set_null (
  id                            number(19) not null,
  one_id                        number(19),
  constraint pk_migtest_fk_set_null primary key (id)
);
create sequence migtest_fk_set_null_seq;

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
  constraint pk_migtest_e_ref primary key (id)
);
create sequence migtest_e_ref_seq;

create table migtest_e_softdelete (
  id                            number(10) not null,
  test_string                   varchar2(255),
  constraint pk_migtest_e_softdelete primary key (id)
);
create sequence migtest_e_softdelete_seq;

create index ix_migtest_e_basic_indextest1 on migtest_e_basic (indextest1);
create index ix_migtest_e_basic_indextest5 on migtest_e_basic (indextest5);
alter table migtest_fk_cascade add constraint fk_migtest_fk_cascade_one_id foreign key (one_id) references migtest_fk_cascade_one (id) on delete cascade;
create index ix_migtest_fk_cascade_one_id on migtest_fk_cascade (one_id);

alter table migtest_fk_set_null add constraint fk_migtest_fk_set_null_one_id foreign key (one_id) references migtest_fk_one (id) on delete set null;
create index ix_migtest_fk_set_null_one_id on migtest_fk_set_null (one_id);

alter table migtest_e_basic add constraint fk_migtest_e_basic_eref_id foreign key (eref_id) references migtest_e_ref (id);
create index ix_migtest_e_basic_eref_id on migtest_e_basic (eref_id);

