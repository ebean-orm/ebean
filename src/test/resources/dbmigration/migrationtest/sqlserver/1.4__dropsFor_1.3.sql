-- Migrationscripts for ebean unittest
-- apply changes
-- drop column migtest_e_basic.new_string_field;
IF (OBJECT_ID('uq_migtest_e_basic_new_string_field', 'UQ') IS NOT NULL) alter table migtest_e_basic drop constraint uq_migtest_e_basic_new_string_field;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'uq_migtest_e_basic_new_string_field') drop index uq_migtest_e_basic_new_string_field ON migtest_e_basic;
delimiter $$
DECLARE @Tmp nvarchar(200);select @Tmp = t1.name  from sys.default_constraints t1
  join sys.columns t2 on t1.object_id = t2.default_object_id
  where t1.parent_object_id = OBJECT_ID('migtest_e_basic') and t2.name = 'new_string_field';
if @Tmp is not null EXEC('alter table migtest_e_basic drop constraint ' + @Tmp)$$;
IF (OBJECT_ID('ck_migtest_e_basic_new_string_field', 'C') IS NOT NULL) alter table migtest_e_basic drop constraint ck_migtest_e_basic_new_string_field;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'ix_migtest_e_basic_new_string_field') drop index ix_migtest_e_basic_new_string_field ON migtest_e_basic;
IF OBJECT_ID('fk_migtest_e_basic_new_string_field', 'F') IS NOT NULL alter table migtest_e_basic drop constraint fk_migtest_e_basic_new_string_field;
alter table migtest_e_basic drop column new_string_field;

-- drop column migtest_e_basic.new_boolean_field;
IF (OBJECT_ID('uq_migtest_e_basic_new_boolean_field', 'UQ') IS NOT NULL) alter table migtest_e_basic drop constraint uq_migtest_e_basic_new_boolean_field;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'uq_migtest_e_basic_new_boolean_field') drop index uq_migtest_e_basic_new_boolean_field ON migtest_e_basic;
delimiter $$
DECLARE @Tmp nvarchar(200);select @Tmp = t1.name  from sys.default_constraints t1
  join sys.columns t2 on t1.object_id = t2.default_object_id
  where t1.parent_object_id = OBJECT_ID('migtest_e_basic') and t2.name = 'new_boolean_field';
if @Tmp is not null EXEC('alter table migtest_e_basic drop constraint ' + @Tmp)$$;
IF (OBJECT_ID('ck_migtest_e_basic_new_boolean_field', 'C') IS NOT NULL) alter table migtest_e_basic drop constraint ck_migtest_e_basic_new_boolean_field;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'ix_migtest_e_basic_new_boolean_field') drop index ix_migtest_e_basic_new_boolean_field ON migtest_e_basic;
IF OBJECT_ID('fk_migtest_e_basic_new_boolean_field', 'F') IS NOT NULL alter table migtest_e_basic drop constraint fk_migtest_e_basic_new_boolean_field;
alter table migtest_e_basic drop column new_boolean_field;

-- drop column migtest_e_basic.new_boolean_field2;
IF (OBJECT_ID('uq_migtest_e_basic_new_boolean_field2', 'UQ') IS NOT NULL) alter table migtest_e_basic drop constraint uq_migtest_e_basic_new_boolean_field2;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'uq_migtest_e_basic_new_boolean_field2') drop index uq_migtest_e_basic_new_boolean_field2 ON migtest_e_basic;
delimiter $$
DECLARE @Tmp nvarchar(200);select @Tmp = t1.name  from sys.default_constraints t1
  join sys.columns t2 on t1.object_id = t2.default_object_id
  where t1.parent_object_id = OBJECT_ID('migtest_e_basic') and t2.name = 'new_boolean_field2';
if @Tmp is not null EXEC('alter table migtest_e_basic drop constraint ' + @Tmp)$$;
IF (OBJECT_ID('ck_migtest_e_basic_new_boolean_field2', 'C') IS NOT NULL) alter table migtest_e_basic drop constraint ck_migtest_e_basic_new_boolean_field2;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'ix_migtest_e_basic_new_boolean_field2') drop index ix_migtest_e_basic_new_boolean_field2 ON migtest_e_basic;
IF OBJECT_ID('fk_migtest_e_basic_new_boolean_field2', 'F') IS NOT NULL alter table migtest_e_basic drop constraint fk_migtest_e_basic_new_boolean_field2;
alter table migtest_e_basic drop column new_boolean_field2;

-- drop column migtest_e_basic.progress;
IF (OBJECT_ID('uq_migtest_e_basic_progress', 'UQ') IS NOT NULL) alter table migtest_e_basic drop constraint uq_migtest_e_basic_progress;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'uq_migtest_e_basic_progress') drop index uq_migtest_e_basic_progress ON migtest_e_basic;
delimiter $$
DECLARE @Tmp nvarchar(200);select @Tmp = t1.name  from sys.default_constraints t1
  join sys.columns t2 on t1.object_id = t2.default_object_id
  where t1.parent_object_id = OBJECT_ID('migtest_e_basic') and t2.name = 'progress';
if @Tmp is not null EXEC('alter table migtest_e_basic drop constraint ' + @Tmp)$$;
IF (OBJECT_ID('ck_migtest_e_basic_progress', 'C') IS NOT NULL) alter table migtest_e_basic drop constraint ck_migtest_e_basic_progress;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'ix_migtest_e_basic_progress') drop index ix_migtest_e_basic_progress ON migtest_e_basic;
IF OBJECT_ID('fk_migtest_e_basic_progress', 'F') IS NOT NULL alter table migtest_e_basic drop constraint fk_migtest_e_basic_progress;
alter table migtest_e_basic drop column progress;

-- drop column migtest_e_basic.new_integer;
IF (OBJECT_ID('uq_migtest_e_basic_new_integer', 'UQ') IS NOT NULL) alter table migtest_e_basic drop constraint uq_migtest_e_basic_new_integer;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'uq_migtest_e_basic_new_integer') drop index uq_migtest_e_basic_new_integer ON migtest_e_basic;
delimiter $$
DECLARE @Tmp nvarchar(200);select @Tmp = t1.name  from sys.default_constraints t1
  join sys.columns t2 on t1.object_id = t2.default_object_id
  where t1.parent_object_id = OBJECT_ID('migtest_e_basic') and t2.name = 'new_integer';
if @Tmp is not null EXEC('alter table migtest_e_basic drop constraint ' + @Tmp)$$;
IF (OBJECT_ID('ck_migtest_e_basic_new_integer', 'C') IS NOT NULL) alter table migtest_e_basic drop constraint ck_migtest_e_basic_new_integer;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'ix_migtest_e_basic_new_integer') drop index ix_migtest_e_basic_new_integer ON migtest_e_basic;
IF OBJECT_ID('fk_migtest_e_basic_new_integer', 'F') IS NOT NULL alter table migtest_e_basic drop constraint fk_migtest_e_basic_new_integer;
alter table migtest_e_basic drop column new_integer;

-- drop column migtest_e_history2.test_string2;
IF (OBJECT_ID('uq_migtest_e_history2_test_string2', 'UQ') IS NOT NULL) alter table migtest_e_history2 drop constraint uq_migtest_e_history2_test_string2;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_history2','U') AND name = 'uq_migtest_e_history2_test_string2') drop index uq_migtest_e_history2_test_string2 ON migtest_e_history2;
delimiter $$
DECLARE @Tmp nvarchar(200);select @Tmp = t1.name  from sys.default_constraints t1
  join sys.columns t2 on t1.object_id = t2.default_object_id
  where t1.parent_object_id = OBJECT_ID('migtest_e_history2') and t2.name = 'test_string2';
if @Tmp is not null EXEC('alter table migtest_e_history2 drop constraint ' + @Tmp)$$;
IF (OBJECT_ID('ck_migtest_e_history2_test_string2', 'C') IS NOT NULL) alter table migtest_e_history2 drop constraint ck_migtest_e_history2_test_string2;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_history2','U') AND name = 'ix_migtest_e_history2_test_string2') drop index ix_migtest_e_history2_test_string2 ON migtest_e_history2;
IF OBJECT_ID('fk_migtest_e_history2_test_string2', 'F') IS NOT NULL alter table migtest_e_history2 drop constraint fk_migtest_e_history2_test_string2;
alter table migtest_e_history2 drop column test_string2;

-- drop column migtest_e_history2.test_string3;
IF (OBJECT_ID('uq_migtest_e_history2_test_string3', 'UQ') IS NOT NULL) alter table migtest_e_history2 drop constraint uq_migtest_e_history2_test_string3;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_history2','U') AND name = 'uq_migtest_e_history2_test_string3') drop index uq_migtest_e_history2_test_string3 ON migtest_e_history2;
delimiter $$
DECLARE @Tmp nvarchar(200);select @Tmp = t1.name  from sys.default_constraints t1
  join sys.columns t2 on t1.object_id = t2.default_object_id
  where t1.parent_object_id = OBJECT_ID('migtest_e_history2') and t2.name = 'test_string3';
if @Tmp is not null EXEC('alter table migtest_e_history2 drop constraint ' + @Tmp)$$;
IF (OBJECT_ID('ck_migtest_e_history2_test_string3', 'C') IS NOT NULL) alter table migtest_e_history2 drop constraint ck_migtest_e_history2_test_string3;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_history2','U') AND name = 'ix_migtest_e_history2_test_string3') drop index ix_migtest_e_history2_test_string3 ON migtest_e_history2;
IF OBJECT_ID('fk_migtest_e_history2_test_string3', 'F') IS NOT NULL alter table migtest_e_history2 drop constraint fk_migtest_e_history2_test_string3;
alter table migtest_e_history2 drop column test_string3;

-- drop column migtest_e_softdelete.deleted;
IF (OBJECT_ID('uq_migtest_e_softdelete_deleted', 'UQ') IS NOT NULL) alter table migtest_e_softdelete drop constraint uq_migtest_e_softdelete_deleted;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_softdelete','U') AND name = 'uq_migtest_e_softdelete_deleted') drop index uq_migtest_e_softdelete_deleted ON migtest_e_softdelete;
delimiter $$
DECLARE @Tmp nvarchar(200);select @Tmp = t1.name  from sys.default_constraints t1
  join sys.columns t2 on t1.object_id = t2.default_object_id
  where t1.parent_object_id = OBJECT_ID('migtest_e_softdelete') and t2.name = 'deleted';
if @Tmp is not null EXEC('alter table migtest_e_softdelete drop constraint ' + @Tmp)$$;
IF (OBJECT_ID('ck_migtest_e_softdelete_deleted', 'C') IS NOT NULL) alter table migtest_e_softdelete drop constraint ck_migtest_e_softdelete_deleted;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_softdelete','U') AND name = 'ix_migtest_e_softdelete_deleted') drop index ix_migtest_e_softdelete_deleted ON migtest_e_softdelete;
IF OBJECT_ID('fk_migtest_e_softdelete_deleted', 'F') IS NOT NULL alter table migtest_e_softdelete drop constraint fk_migtest_e_softdelete_deleted;
alter table migtest_e_softdelete drop column deleted;

IF OBJECT_ID('migtest_e_user', 'U') IS NOT NULL drop table migtest_e_user;
IF OBJECT_ID('migtest_e_user_seq', 'SO') IS NOT NULL drop sequence migtest_e_user_seq;
-- dropping history support for migtest_e_history;
delimiter $$
DECLARE @Tmp nvarchar(200);select @Tmp = t1.name  from sys.default_constraints t1
  join sys.columns t2 on t1.object_id = t2.default_object_id
  where t1.parent_object_id = OBJECT_ID('migtest_e_history') and t2.name = 'sys_periodFrom';
if @Tmp is not null EXEC('alter table migtest_e_history drop constraint ' + @Tmp)$$;
delimiter $$
DECLARE @Tmp nvarchar(200);select @Tmp = t1.name  from sys.default_constraints t1
  join sys.columns t2 on t1.object_id = t2.default_object_id
  where t1.parent_object_id = OBJECT_ID('migtest_e_history') and t2.name = 'sys_periodTo';
if @Tmp is not null EXEC('alter table migtest_e_history drop constraint ' + @Tmp)$$;
alter table migtest_e_history set (system_versioning = off);
alter table migtest_e_history drop period for system_time;
alter table migtest_e_history drop column sys_periodFrom;
alter table migtest_e_history drop column sys_periodTo;
IF OBJECT_ID('migtest_e_history_history', 'U') IS NOT NULL drop table migtest_e_history_history;

