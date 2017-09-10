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

IF OBJECT_ID('migtest_e_ref', 'U') IS NOT NULL drop table migtest_e_ref;
