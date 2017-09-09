-- apply changes
create table migtest_e_user (
  id                            integer identity(1,1) not null,
  constraint pk_migtest_e_user primary key (id)
);


update migtest_e_basic set status = 'A' where status is null;
IF (OBJECT_ID('ck_migtest_e_basic_status', 'C') IS NOT NULL) alter table migtest_e_basic drop constraint ck_migtest_e_basic_status;
alter table migtest_e_basic add default 'A' for status;
alter table migtest_e_basic alter column status varchar(1) not null;
alter table migtest_e_basic add constraint ck_migtest_e_basic_status check ( status in ('N','A','I','?'));

-- rename all collisions;
create unique nonclustered index uq_migtest_e_basic_description on migtest_e_basic(description) where description is not null;

update migtest_e_basic set some_date = '2000-01-01T00:00:00' where some_date is null;
alter table migtest_e_basic add default '2000-01-01T00:00:00' for some_date;
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

alter table migtest_e_history alter column test_string numeric(19);

update migtest_e_history2 set test_string = 'unknown' where test_string is null;
alter table migtest_e_history2 add default 'unknown' for test_string;
alter table migtest_e_history2 alter column test_string varchar(255) not null;
alter table migtest_e_history2 add test_string2 varchar(255);
alter table migtest_e_history2 add test_string3 varchar(255) not null default 'unknown';

alter table migtest_e_softdelete add deleted bit default 0 not null;

create index ix_migtest_e_basic_indextest3 on migtest_e_basic (indextest3);
create index ix_migtest_e_basic_indextest6 on migtest_e_basic (indextest6);
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'ix_migtest_e_basic_indextest1') drop index ix_migtest_e_basic_indextest1 ON migtest_e_basic;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'ix_migtest_e_basic_indextest5') drop index ix_migtest_e_basic_indextest5 ON migtest_e_basic;
alter table migtest_e_history
    add sys_periodFrom datetime2 GENERATED ALWAYS AS ROW START NOT NULL DEFAULT SYSUTCDATETIME(),
        sys_periodTo   datetime2 GENERATED ALWAYS AS ROW END   NOT NULL DEFAULT '9999-12-31T23:59:59.9999999',
period for system_time (sys_periodFrom, sys_periodTo);
alter table migtest_e_history set (system_versioning = on (history_table=dbo.migtest_e_history_history));
