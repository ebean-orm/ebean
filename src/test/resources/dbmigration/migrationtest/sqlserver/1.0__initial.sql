-- Migrationscripts for ebean unittest
-- apply changes
create table migtest_fk_cascade (
  id                            numeric(19) identity(1,1) not null,
  one_id                        numeric(19),
  constraint pk_migtest_fk_cascade primary key (id)
);

create table migtest_fk_cascade_one (
  id                            numeric(19) identity(1,1) not null,
  constraint pk_migtest_fk_cascade_one primary key (id)
);

create table migtest_fk_none (
  id                            numeric(19) identity(1,1) not null,
  one_id                        numeric(19),
  constraint pk_migtest_fk_none primary key (id)
);

create table migtest_fk_none_via_join (
  id                            numeric(19) identity(1,1) not null,
  one_id                        numeric(19),
  constraint pk_migtest_fk_none_via_join primary key (id)
);

create table migtest_fk_one (
  id                            numeric(19) identity(1,1) not null,
  constraint pk_migtest_fk_one primary key (id)
);

create table migtest_fk_set_null (
  id                            numeric(19) identity(1,1) not null,
  one_id                        numeric(19),
  constraint pk_migtest_fk_set_null primary key (id)
);

create table migtest_e_basic (
  id                            integer identity(1,1) not null,
  status                        varchar(1),
  name                          varchar(127),
  description                   varchar(127),
  some_date                     datetime2,
  old_boolean                   bit default 0 not null,
  old_boolean2                  bit default 0,
  eref_id                       integer,
  indextest1                    varchar(127),
  indextest2                    varchar(127),
  indextest3                    varchar(127),
  indextest4                    varchar(127),
  indextest5                    varchar(127),
  indextest6                    varchar(127),
  user_id                       integer not null,
  constraint ck_migtest_e_basic_status check ( status in ('N','A','I')),
  constraint pk_migtest_e_basic primary key (id)
);
create unique nonclustered index uq_migtest_e_basic_indextest2 on migtest_e_basic(indextest2) where indextest2 is not null;
create unique nonclustered index uq_migtest_e_basic_indextest6 on migtest_e_basic(indextest6) where indextest6 is not null;

create table migtest_e_history (
  id                            integer identity(1,1) not null,
  test_string                   varchar(255),
  constraint pk_migtest_e_history primary key (id)
);

create table migtest_e_history2 (
  id                            integer identity(1,1) not null,
  test_string                   varchar(255),
  constraint pk_migtest_e_history2 primary key (id)
);

create table migtest_e_ref (
  id                            integer identity(1,1) not null,
  constraint pk_migtest_e_ref primary key (id)
);

create table migtest_e_softdelete (
  id                            integer identity(1,1) not null,
  test_string                   varchar(255),
  constraint pk_migtest_e_softdelete primary key (id)
);

create index ix_migtest_e_basic_indextest1 on migtest_e_basic (indextest1);
create index ix_migtest_e_basic_indextest5 on migtest_e_basic (indextest5);
alter table migtest_fk_cascade add constraint fk_migtest_fk_cascade_one_id foreign key (one_id) references migtest_fk_cascade_one (id) on delete cascade on update cascade;
create index ix_migtest_fk_cascade_one_id on migtest_fk_cascade (one_id);

alter table migtest_fk_set_null add constraint fk_migtest_fk_set_null_one_id foreign key (one_id) references migtest_fk_one (id) on delete set null on update set null;
create index ix_migtest_fk_set_null_one_id on migtest_fk_set_null (one_id);

alter table migtest_e_basic add constraint fk_migtest_e_basic_eref_id foreign key (eref_id) references migtest_e_ref (id);
create index ix_migtest_e_basic_eref_id on migtest_e_basic (eref_id);

alter table migtest_e_history2
    add sys_periodFrom datetime2 GENERATED ALWAYS AS ROW START NOT NULL DEFAULT SYSUTCDATETIME(),
        sys_periodTo   datetime2 GENERATED ALWAYS AS ROW END   NOT NULL DEFAULT '9999-12-31T23:59:59.9999999',
period for system_time (sys_periodFrom, sys_periodTo);
alter table migtest_e_history2 set (system_versioning = on (history_table=dbo.migtest_e_history2_history));
