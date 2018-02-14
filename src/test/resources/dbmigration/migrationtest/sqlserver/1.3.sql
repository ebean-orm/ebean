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
create table migtest_e_ref (
  id                            integer identity(1,1) not null,
  name                          varchar(255) not null,
  constraint pk_migtest_e_ref primary key (id)
);
alter table migtest_e_ref add constraint uq_migtest_e_ref_name unique  (name);

IF OBJECT_ID('fk_migtest_ckey_detail_parent', 'F') IS NOT NULL alter table migtest_ckey_detail drop constraint fk_migtest_ckey_detail_parent;
IF (OBJECT_ID('ck_migtest_e_basic_status', 'C') IS NOT NULL) alter table migtest_e_basic drop constraint ck_migtest_e_basic_status;
delimiter $$
DECLARE @Tmp nvarchar(200);select @Tmp = t1.name  from sys.default_constraints t1
  join sys.columns t2 on t1.object_id = t2.default_object_id
  where t1.parent_object_id = OBJECT_ID('migtest_e_basic') and t2.name = 'status';
if @Tmp is not null EXEC('alter table migtest_e_basic drop constraint ' + @Tmp)$$;
alter table migtest_e_basic add constraint ck_migtest_e_basic_status check ( status in ('N','A','I'));
IF (OBJECT_ID('uq_migtest_e_basic_description', 'UQ') IS NOT NULL) alter table migtest_e_basic drop constraint uq_migtest_e_basic_description;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'uq_migtest_e_basic_description') drop index uq_migtest_e_basic_description ON migtest_e_basic;
delimiter $$
DECLARE @Tmp nvarchar(200);select @Tmp = t1.name  from sys.default_constraints t1
  join sys.columns t2 on t1.object_id = t2.default_object_id
  where t1.parent_object_id = OBJECT_ID('migtest_e_basic') and t2.name = 'some_date';
if @Tmp is not null EXEC('alter table migtest_e_basic drop constraint ' + @Tmp)$$;

update migtest_e_basic set user_id = 23 where user_id is null;
IF OBJECT_ID('fk_migtest_e_basic_user_id', 'F') IS NOT NULL alter table migtest_e_basic drop constraint fk_migtest_e_basic_user_id;
alter table migtest_e_basic add default 23 for user_id;
alter table migtest_e_basic alter column user_id integer not null;
alter table migtest_e_basic add old_boolean bit default 0 not null;
alter table migtest_e_basic add old_boolean2 bit;
alter table migtest_e_basic add eref_id integer;

IF (OBJECT_ID('uq_migtest_e_basic_status_indextest1', 'UQ') IS NOT NULL) alter table migtest_e_basic drop constraint uq_migtest_e_basic_status_indextest1;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'uq_migtest_e_basic_status_indextest1') drop index uq_migtest_e_basic_status_indextest1 ON migtest_e_basic;
IF (OBJECT_ID('uq_migtest_e_basic_name', 'UQ') IS NOT NULL) alter table migtest_e_basic drop constraint uq_migtest_e_basic_name;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'uq_migtest_e_basic_name') drop index uq_migtest_e_basic_name ON migtest_e_basic;
IF (OBJECT_ID('uq_migtest_e_basic_indextest4', 'UQ') IS NOT NULL) alter table migtest_e_basic drop constraint uq_migtest_e_basic_indextest4;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'uq_migtest_e_basic_indextest4') drop index uq_migtest_e_basic_indextest4 ON migtest_e_basic;
IF (OBJECT_ID('uq_migtest_e_basic_indextest5', 'UQ') IS NOT NULL) alter table migtest_e_basic drop constraint uq_migtest_e_basic_indextest5;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'uq_migtest_e_basic_indextest5') drop index uq_migtest_e_basic_indextest5 ON migtest_e_basic;
create unique nonclustered index uq_migtest_e_basic_indextest2 on migtest_e_basic(indextest2) where indextest2 is not null;
create unique nonclustered index uq_migtest_e_basic_indextest6 on migtest_e_basic(indextest6) where indextest6 is not null;
delimiter $$
DECLARE @Tmp nvarchar(200);select @Tmp = t1.name  from sys.default_constraints t1
  join sys.columns t2 on t1.object_id = t2.default_object_id
  where t1.parent_object_id = OBJECT_ID('migtest_e_history2') and t2.name = 'test_string';
if @Tmp is not null EXEC('alter table migtest_e_history2 drop constraint ' + @Tmp)$$;
create index ix_migtest_e_basic_indextest1 on migtest_e_basic (indextest1);
create index ix_migtest_e_basic_indextest5 on migtest_e_basic (indextest5);
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'ix_migtest_e_basic_indextest3') drop index ix_migtest_e_basic_indextest3 ON migtest_e_basic;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'ix_migtest_e_basic_indextest6') drop index ix_migtest_e_basic_indextest6 ON migtest_e_basic;
alter table migtest_e_basic add constraint fk_migtest_e_basic_eref_id foreign key (eref_id) references migtest_e_ref (id);
create index ix_migtest_e_basic_eref_id on migtest_e_basic (eref_id);

