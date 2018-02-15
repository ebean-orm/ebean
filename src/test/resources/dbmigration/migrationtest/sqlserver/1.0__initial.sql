-- apply changes
-- Migrationscripts for ebean unittest

if exists (select name  from sys.types where name = 'ebean_bigint_tvp') drop type ebean_bigint_tvp;
create type ebean_bigint_tvp as table (c1 bigint);
if exists (select name  from sys.types where name = 'ebean_float_tvp') drop type ebean_float_tvp;
create type ebean_float_tvp as table (c1 float);
if exists (select name  from sys.types where name = 'ebean_bit_tvp') drop type ebean_bit_tvp;
create type ebean_bit_tvp as table (c1 bit);
if exists (select name  from sys.types where name = 'ebean_date_tvp') drop type ebean_date_tvp;
create type ebean_date_tvp as table (c1 date);
if exists (select name  from sys.types where name = 'ebean_time_tvp') drop type ebean_time_tvp;
create type ebean_time_tvp as table (c1 time);
if exists (select name  from sys.types where name = 'ebean_datetime2_tvp') drop type ebean_datetime2_tvp;
create type ebean_datetime2_tvp as table (c1 datetime2);
if exists (select name  from sys.types where name = 'ebean_nvarchar_tvp') drop type ebean_nvarchar_tvp;
create type ebean_nvarchar_tvp as table (c1 nvarchar(max));
create table migtest_ckey_assoc (
  id                            integer identity(1,1) not null,
  assoc_one                     nvarchar(255),
  constraint pk_migtest_ckey_assoc primary key (id)
);

create table migtest_ckey_detail (
  id                            integer identity(1,1) not null,
  something                     nvarchar(255),
  constraint pk_migtest_ckey_detail primary key (id)
);

create table migtest_ckey_parent (
  one_key                       integer not null,
  two_key                       nvarchar(127) not null,
  name                          nvarchar(255),
  version                       integer not null,
  constraint pk_migtest_ckey_parent primary key (one_key,two_key)
);

create table migtest_e_basic (
  id                            integer identity(1,1) not null,
  status                        nvarchar(1),
  name                          nvarchar(127),
  description                   nvarchar(255),
  some_date                     datetime2,
  old_boolean                   bit default 0 not null,
  old_boolean2                  bit,
  eref_id                       integer,
  indextest1                    nvarchar(127),
  indextest2                    nvarchar(127),
  indextest3                    nvarchar(127),
  indextest4                    nvarchar(127),
  indextest5                    nvarchar(127),
  indextest6                    nvarchar(127),
  user_id                       integer not null,
  constraint ck_migtest_e_basic_status check ( status in ('N','A','I')),
  constraint pk_migtest_e_basic primary key (id)
);
create unique nonclustered index uq_migtest_e_basic_indextest2 on migtest_e_basic(indextest2) where indextest2 is not null;
create unique nonclustered index uq_migtest_e_basic_indextest6 on migtest_e_basic(indextest6) where indextest6 is not null;

create table migtest_e_history (
  id                            integer identity(1,1) not null,
  test_string                   nvarchar(255),
  constraint pk_migtest_e_history primary key (id)
);

create table migtest_e_history2 (
  id                            integer identity(1,1) not null,
  test_string                   nvarchar(255),
  constraint pk_migtest_e_history2 primary key (id)
);

create table migtest_e_ref (
  id                            integer identity(1,1) not null,
  name                          nvarchar(255) not null,
  constraint pk_migtest_e_ref primary key (id)
);
alter table migtest_e_ref add constraint uq_migtest_e_ref_name unique  (name);

create table migtest_e_softdelete (
  id                            integer identity(1,1) not null,
  test_string                   nvarchar(255),
  constraint pk_migtest_e_softdelete primary key (id)
);

create table migtest_mtm_c (
  id                            integer identity(1,1) not null,
  name                          nvarchar(255),
  constraint pk_migtest_mtm_c primary key (id)
);

create table migtest_mtm_m (
  id                            numeric(19) identity(1,1) not null,
  name                          nvarchar(255),
  constraint pk_migtest_mtm_m primary key (id)
);

create table migtest_oto_child (
  id                            integer identity(1,1) not null,
  name                          nvarchar(255),
  constraint pk_migtest_oto_child primary key (id)
);

create table migtest_oto_master (
  id                            numeric(19) identity(1,1) not null,
  name                          nvarchar(255),
  constraint pk_migtest_oto_master primary key (id)
);

create index ix_migtest_e_basic_indextest1 on migtest_e_basic (indextest1);
create index ix_migtest_e_basic_indextest5 on migtest_e_basic (indextest5);
alter table migtest_e_basic add constraint fk_migtest_e_basic_eref_id foreign key (eref_id) references migtest_e_ref (id);
create index ix_migtest_e_basic_eref_id on migtest_e_basic (eref_id);

alter table migtest_e_history2
    add sys_periodFrom datetime2 GENERATED ALWAYS AS ROW START NOT NULL DEFAULT SYSUTCDATETIME(),
        sys_periodTo   datetime2 GENERATED ALWAYS AS ROW END   NOT NULL DEFAULT '9999-12-31T23:59:59.9999999',
period for system_time (sys_periodFrom, sys_periodTo);
alter table migtest_e_history2 set (system_versioning = on (history_table=dbo.migtest_e_history2_history));
