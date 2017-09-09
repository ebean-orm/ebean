-- apply changes
create table migtest_e_ref (
  id                            integer identity(1,1) not null,
  constraint pk_migtest_e_ref primary key (id)
);

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
alter table migtest_e_basic add old_boolean2 bit default 0;
alter table migtest_e_basic add eref_id integer;

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

