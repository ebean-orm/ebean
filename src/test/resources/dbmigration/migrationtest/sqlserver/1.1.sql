-- apply changes
create table migtest_e_user (
  id                            integer identity(1,1) not null,
  constraint pk_migtest_e_user primary key (id)
);

create table migtest_mtm_child_migtest_mtm_master (
  migtest_mtm_child_id          integer not null,
  migtest_mtm_master_id         numeric(19) not null,
  constraint pk_migtest_mtm_child_migtest_mtm_master primary key (migtest_mtm_child_id,migtest_mtm_master_id)
);

create table migtest_mtm_master_migtest_mtm_child (
  migtest_mtm_master_id         numeric(19) not null,
  migtest_mtm_child_id          integer not null,
  constraint pk_migtest_mtm_master_migtest_mtm_child primary key (migtest_mtm_master_id,migtest_mtm_child_id)
);

alter table migtest_ckey_detail add one_key integer;
alter table migtest_ckey_detail add two_key varchar(255);

alter table migtest_ckey_detail add constraint fk_migtest_ckey_detail_parent foreign key (one_key,two_key) references migtest_ckey_parent (one_key,two_key);
alter table migtest_ckey_parent add assoc_id integer;


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

IF (OBJECT_ID('uq_migtest_e_basic_indextest2', 'UQ') IS NOT NULL) alter table migtest_e_basic drop constraint uq_migtest_e_basic_indextest2;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'uq_migtest_e_basic_indextest2') drop index uq_migtest_e_basic_indextest2 ON migtest_e_basic;
IF (OBJECT_ID('uq_migtest_e_basic_indextest6', 'UQ') IS NOT NULL) alter table migtest_e_basic drop constraint uq_migtest_e_basic_indextest6;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'uq_migtest_e_basic_indextest6') drop index uq_migtest_e_basic_indextest6 ON migtest_e_basic;
create unique nonclustered index uq_migtest_e_basic_status_indextest1 on migtest_e_basic(status,indextest1) where indextest1 is not null;
create unique nonclustered index uq_migtest_e_basic_name on migtest_e_basic(name) where name is not null;
create unique nonclustered index uq_migtest_e_basic_indextest4 on migtest_e_basic(indextest4) where indextest4 is not null;
create unique nonclustered index uq_migtest_e_basic_indextest5 on migtest_e_basic(indextest5) where indextest5 is not null;
alter table migtest_e_history alter column test_string numeric(19);

update migtest_e_history2 set test_string = 'unknown' where test_string is null;
alter table migtest_e_history2 add default 'unknown' for test_string;
alter table migtest_e_history2 alter column test_string varchar(255) not null;
alter table migtest_e_history2 add test_string2 varchar(255);
alter table migtest_e_history2 add test_string3 varchar(255) not null default 'unknown';

alter table migtest_e_softdelete add deleted bit default 0 not null;

alter table migtest_oto_child add master_id numeric(19);

create index ix_migtest_e_basic_indextest3 on migtest_e_basic (indextest3);
create index ix_migtest_e_basic_indextest6 on migtest_e_basic (indextest6);
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'ix_migtest_e_basic_indextest1') drop index ix_migtest_e_basic_indextest1 ON migtest_e_basic;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'ix_migtest_e_basic_indextest5') drop index ix_migtest_e_basic_indextest5 ON migtest_e_basic;
alter table migtest_mtm_child_migtest_mtm_master add constraint fk_migtest_mtm_child_migtest_mtm_master_migtest_mtm_child foreign key (migtest_mtm_child_id) references migtest_mtm_child (id);
create index ix_migtest_mtm_child_migtest_mtm_master_migtest_mtm_child on migtest_mtm_child_migtest_mtm_master (migtest_mtm_child_id);

alter table migtest_mtm_child_migtest_mtm_master add constraint fk_migtest_mtm_child_migtest_mtm_master_migtest_mtm_master foreign key (migtest_mtm_master_id) references migtest_mtm_master (id);
create index ix_migtest_mtm_child_migtest_mtm_master_migtest_mtm_master on migtest_mtm_child_migtest_mtm_master (migtest_mtm_master_id);

alter table migtest_mtm_master_migtest_mtm_child add constraint fk_migtest_mtm_master_migtest_mtm_child_migtest_mtm_master foreign key (migtest_mtm_master_id) references migtest_mtm_master (id);
create index ix_migtest_mtm_master_migtest_mtm_child_migtest_mtm_master on migtest_mtm_master_migtest_mtm_child (migtest_mtm_master_id);

alter table migtest_mtm_master_migtest_mtm_child add constraint fk_migtest_mtm_master_migtest_mtm_child_migtest_mtm_child foreign key (migtest_mtm_child_id) references migtest_mtm_child (id);
create index ix_migtest_mtm_master_migtest_mtm_child_migtest_mtm_child on migtest_mtm_master_migtest_mtm_child (migtest_mtm_child_id);

alter table migtest_ckey_parent add constraint fk_migtest_ckey_parent_assoc_id foreign key (assoc_id) references migtest_ckey_assoc (id);
create index ix_migtest_ckey_parent_assoc_id on migtest_ckey_parent (assoc_id);

alter table migtest_oto_child add constraint fk_migtest_oto_child_master_id foreign key (master_id) references migtest_oto_master (id);

alter table migtest_e_history
    add sys_periodFrom datetime2 GENERATED ALWAYS AS ROW START NOT NULL DEFAULT SYSUTCDATETIME(),
        sys_periodTo   datetime2 GENERATED ALWAYS AS ROW END   NOT NULL DEFAULT '9999-12-31T23:59:59.9999999',
period for system_time (sys_periodFrom, sys_periodTo);
alter table migtest_e_history set (system_versioning = on (history_table=dbo.migtest_e_history_history));
