-- apply changes
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
  one_key                       number(10) not null,
  two_key                       varchar2(255) not null,
  name                          varchar2(255),
  version                       number(10) not null,
  constraint pk_migtest_ckey_parent primary key (one_key,two_key)
);

create table migtest_e_basic (
  id                            number(10) not null,
  status                        varchar2(1),
  name                          varchar2(255),
  description                   varchar2(255),
  some_date                     timestamp,
  old_boolean                   number(1) default 0 not null,
  old_boolean2                  number(1) default 0,
  eref_id                       number(10),
  indextest1                    varchar2(255),
  indextest2                    varchar2(255),
  indextest3                    varchar2(255),
  indextest4                    varchar2(255),
  indextest5                    varchar2(255),
  indextest6                    varchar2(255),
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
  name                          varchar2(255) not null,
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

create table migtest_mtm_child (
  id                            number(10) not null,
  name                          varchar2(255),
  constraint pk_migtest_mtm_child primary key (id)
);
create sequence migtest_mtm_child_seq;

create table migtest_mtm_master (
  id                            number(19) not null,
  name                          varchar2(255),
  constraint pk_migtest_mtm_master primary key (id)
);
create sequence migtest_mtm_master_seq;

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

