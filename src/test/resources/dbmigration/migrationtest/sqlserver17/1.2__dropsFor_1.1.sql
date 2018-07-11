-- Migrationscripts for ebean unittest
-- apply changes
-- drop column migtest_e_basic.old_boolean;
IF (OBJECT_ID('uq_migtest_e_basic_old_boolean', 'UQ') IS NOT NULL) alter table migtest_e_basic drop constraint uq_migtest_e_basic_old_boolean;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'uq_migtest_e_basic_old_boolean') drop index uq_migtest_e_basic_old_boolean ON migtest_e_basic;
delimiter $$
DECLARE @Tmp nvarchar(200);select @Tmp = t1.name  from sys.default_constraints t1
  join sys.columns t2 on t1.object_id = t2.default_object_id
  where t1.parent_object_id = OBJECT_ID('migtest_e_basic') and t2.name = 'old_boolean';
if @Tmp is not null EXEC('alter table migtest_e_basic drop constraint ' + @Tmp)$$;
IF (OBJECT_ID('ck_migtest_e_basic_old_boolean', 'C') IS NOT NULL) alter table migtest_e_basic drop constraint ck_migtest_e_basic_old_boolean;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'ix_migtest_e_basic_old_boolean') drop index ix_migtest_e_basic_old_boolean ON migtest_e_basic;
IF OBJECT_ID('fk_migtest_e_basic_old_boolean', 'F') IS NOT NULL alter table migtest_e_basic drop constraint fk_migtest_e_basic_old_boolean;
alter table migtest_e_basic drop column old_boolean;

-- drop column migtest_e_basic.old_boolean2;
IF (OBJECT_ID('uq_migtest_e_basic_old_boolean2', 'UQ') IS NOT NULL) alter table migtest_e_basic drop constraint uq_migtest_e_basic_old_boolean2;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'uq_migtest_e_basic_old_boolean2') drop index uq_migtest_e_basic_old_boolean2 ON migtest_e_basic;
delimiter $$
DECLARE @Tmp nvarchar(200);select @Tmp = t1.name  from sys.default_constraints t1
  join sys.columns t2 on t1.object_id = t2.default_object_id
  where t1.parent_object_id = OBJECT_ID('migtest_e_basic') and t2.name = 'old_boolean2';
if @Tmp is not null EXEC('alter table migtest_e_basic drop constraint ' + @Tmp)$$;
IF (OBJECT_ID('ck_migtest_e_basic_old_boolean2', 'C') IS NOT NULL) alter table migtest_e_basic drop constraint ck_migtest_e_basic_old_boolean2;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'ix_migtest_e_basic_old_boolean2') drop index ix_migtest_e_basic_old_boolean2 ON migtest_e_basic;
IF OBJECT_ID('fk_migtest_e_basic_old_boolean2', 'F') IS NOT NULL alter table migtest_e_basic drop constraint fk_migtest_e_basic_old_boolean2;
alter table migtest_e_basic drop column old_boolean2;

-- drop column migtest_e_basic.eref_id;
IF (OBJECT_ID('uq_migtest_e_basic_eref_id', 'UQ') IS NOT NULL) alter table migtest_e_basic drop constraint uq_migtest_e_basic_eref_id;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'uq_migtest_e_basic_eref_id') drop index uq_migtest_e_basic_eref_id ON migtest_e_basic;
delimiter $$
DECLARE @Tmp nvarchar(200);select @Tmp = t1.name  from sys.default_constraints t1
  join sys.columns t2 on t1.object_id = t2.default_object_id
  where t1.parent_object_id = OBJECT_ID('migtest_e_basic') and t2.name = 'eref_id';
if @Tmp is not null EXEC('alter table migtest_e_basic drop constraint ' + @Tmp)$$;
IF (OBJECT_ID('ck_migtest_e_basic_eref_id', 'C') IS NOT NULL) alter table migtest_e_basic drop constraint ck_migtest_e_basic_eref_id;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'ix_migtest_e_basic_eref_id') drop index ix_migtest_e_basic_eref_id ON migtest_e_basic;
IF OBJECT_ID('fk_migtest_e_basic_eref_id', 'F') IS NOT NULL alter table migtest_e_basic drop constraint fk_migtest_e_basic_eref_id;
alter table migtest_e_basic drop column eref_id;

-- drop column migtest_e_history2.obsolete_string1;
IF (OBJECT_ID('uq_migtest_e_history2_obsolete_string1', 'UQ') IS NOT NULL) alter table migtest_e_history2 drop constraint uq_migtest_e_history2_obsolete_string1;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_history2','U') AND name = 'uq_migtest_e_history2_obsolete_string1') drop index uq_migtest_e_history2_obsolete_string1 ON migtest_e_history2;
delimiter $$
DECLARE @Tmp nvarchar(200);select @Tmp = t1.name  from sys.default_constraints t1
  join sys.columns t2 on t1.object_id = t2.default_object_id
  where t1.parent_object_id = OBJECT_ID('migtest_e_history2') and t2.name = 'obsolete_string1';
if @Tmp is not null EXEC('alter table migtest_e_history2 drop constraint ' + @Tmp)$$;
IF (OBJECT_ID('ck_migtest_e_history2_obsolete_string1', 'C') IS NOT NULL) alter table migtest_e_history2 drop constraint ck_migtest_e_history2_obsolete_string1;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_history2','U') AND name = 'ix_migtest_e_history2_obsolete_string1') drop index ix_migtest_e_history2_obsolete_string1 ON migtest_e_history2;
IF OBJECT_ID('fk_migtest_e_history2_obsolete_string1', 'F') IS NOT NULL alter table migtest_e_history2 drop constraint fk_migtest_e_history2_obsolete_string1;
alter table migtest_e_history2 drop column obsolete_string1;

-- drop column migtest_e_history2.obsolete_string2;
IF (OBJECT_ID('uq_migtest_e_history2_obsolete_string2', 'UQ') IS NOT NULL) alter table migtest_e_history2 drop constraint uq_migtest_e_history2_obsolete_string2;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_history2','U') AND name = 'uq_migtest_e_history2_obsolete_string2') drop index uq_migtest_e_history2_obsolete_string2 ON migtest_e_history2;
delimiter $$
DECLARE @Tmp nvarchar(200);select @Tmp = t1.name  from sys.default_constraints t1
  join sys.columns t2 on t1.object_id = t2.default_object_id
  where t1.parent_object_id = OBJECT_ID('migtest_e_history2') and t2.name = 'obsolete_string2';
if @Tmp is not null EXEC('alter table migtest_e_history2 drop constraint ' + @Tmp)$$;
IF (OBJECT_ID('ck_migtest_e_history2_obsolete_string2', 'C') IS NOT NULL) alter table migtest_e_history2 drop constraint ck_migtest_e_history2_obsolete_string2;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_history2','U') AND name = 'ix_migtest_e_history2_obsolete_string2') drop index ix_migtest_e_history2_obsolete_string2 ON migtest_e_history2;
IF OBJECT_ID('fk_migtest_e_history2_obsolete_string2', 'F') IS NOT NULL alter table migtest_e_history2 drop constraint fk_migtest_e_history2_obsolete_string2;
alter table migtest_e_history2 drop column obsolete_string2;

IF OBJECT_ID('migtest_e_ref', 'U') IS NOT NULL drop table migtest_e_ref;
IF OBJECT_ID('migtest_e_ref_seq', 'SO') IS NOT NULL drop sequence migtest_e_ref_seq;
