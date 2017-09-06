-- apply changes
create table migtest_e_user (
  id                            integer not null,
  constraint pk_migtest_e_user primary key (id)
);
create sequence migtest_e_user_seq as bigint  start with 1 ;


update migtest_e_basic set status = 'A' where status is null;
alter table migtest_e_basic drop constraint ck_migtest_e_basic_status;
alter table migtest_e_basic add constraint df_migtest_e_basic_status default 'A' for status;
alter table migtest_e_basic alter column status varchar(1) not null;
alter table migtest_e_basic add constraint ck_migtest_e_basic_status check ( status in ('N','A','I','?'));

-- rename all collisions;
create unique nonclustered index uq_migtest_e_basic_description on migtest_e_basic(description) where description is not null;

update migtest_e_basic set some_date = '2000-01-01T00:00:00' where some_date is null;
alter table migtest_e_basic add constraint df_migtest_e_basic_some_date default '2000-01-01T00:00:00' for some_date;
alter table migtest_e_basic alter column some_date datetime2 not null;

insert into migtest_e_user (id) select distinct user_id from migtest_e_basic;
alter table migtest_e_basic add constraint fk_migtest_e_basic_user_id foreign key (user_id) references migtest_e_user (id);
alter table migtest_e_basic alter column user_id integer;
alter table migtest_e_basic add new_string_field varchar(255) not null default 'foo''bar';
alter table migtest_e_basic add new_boolean_field bit default 0 not null;
update migtest_e_basic set new_boolean_field = old_boolean;

alter table migtest_e_basic add new_boolean_field2 bit default 0 not null;
alter table migtest_e_basic add progress integer not null default 0;
alter table migtest_e_basic add constraint ck_migtest_e_basic_progress check ( progress in (0,1,2));
alter table migtest_e_basic add new_integer integer not null default 42;

IF (OBJECT_ID('uq_migtest_e_basic_indextest2', 'UQ') IS NOT NULL) alter table migtest_e_basic drop constraint uq_migtest_e_basic_indextest2;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'uq_migtest_e_basic_indextest2') drop index uq_migtest_e_basic_indextest2 ON migtest_e_basic;
IF (OBJECT_ID('uq_migtest_e_basic_indextest6', 'UQ') IS NOT NULL) alter table migtest_e_basic drop constraint uq_migtest_e_basic_indextest6;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'uq_migtest_e_basic_indextest6') drop index uq_migtest_e_basic_indextest6 ON migtest_e_basic;
create unique nonclustered index uq_migtest_e_basic_name on migtest_e_basic(name) where name is not null;
create unique nonclustered index uq_migtest_e_basic_indextest4 on migtest_e_basic(indextest4) where indextest4 is not null;
create unique nonclustered index uq_migtest_e_basic_indextest5 on migtest_e_basic(indextest5) where indextest5 is not null;
alter table migtest_e_history alter column test_string numeric(19);
create index ix_migtest_e_basic_indextest3 on migtest_e_basic (indextest3);
create index ix_migtest_e_basic_indextest6 on migtest_e_basic (indextest6);
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'ix_migtest_e_basic_indextest1') drop index ix_migtest_e_basic_indextest1 ON migtest_e_basic;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'ix_migtest_e_basic_indextest5') drop index ix_migtest_e_basic_indextest5 ON migtest_e_basic;
alter table migtest_e_history
    add sys_periodFrom datetime2 GENERATED ALWAYS AS ROW START NOT NULL DEFAULT SYSUTCDATETIME(),
        sys_periodTo   datetime2 GENERATED ALWAYS AS ROW END   NOT NULL DEFAULT '9999-12-31T23:59:59.9999999',
period for system_time (sys_periodFrom, sys_periodTo);
alter table migtest_e_history set (system_versioning = on (history_table=dbo.migtest_e_history_history));
