-- Migrationscripts for ebean unittest
-- apply changes
create table migtest_ckey_assoc (
  id                            integer identity(1,1) not null,
  assoc_one                     varchar(255),
  constraint pk_migtest_ckey_assoc primary key (id)
);

create table migtest_ckey_detail (
  id                            integer identity(1,1) not null,
  something                     varchar(255),
  constraint pk_migtest_ckey_detail primary key (id)
);

create table migtest_ckey_parent (
  one_key                       integer not null,
  two_key                       varchar(127) not null,
  name                          varchar(255),
  version                       integer not null,
  constraint pk_migtest_ckey_parent primary key (one_key,two_key)
);

create table migtest_fk_cascade (
  id                            bigint identity(1,1) not null,
  one_id                        bigint,
  constraint pk_migtest_fk_cascade primary key (id)
);

create table migtest_fk_cascade_one (
  id                            bigint identity(1,1) not null,
  constraint pk_migtest_fk_cascade_one primary key (id)
);

create table migtest_fk_none (
  id                            bigint identity(1,1) not null,
  one_id                        bigint,
  constraint pk_migtest_fk_none primary key (id)
);

create table migtest_fk_none_via_join (
  id                            bigint identity(1,1) not null,
  one_id                        bigint,
  constraint pk_migtest_fk_none_via_join primary key (id)
);

create table migtest_fk_one (
  id                            bigint identity(1,1) not null,
  constraint pk_migtest_fk_one primary key (id)
);

create table migtest_fk_set_null (
  id                            bigint identity(1,1) not null,
  one_id                        bigint,
  constraint pk_migtest_fk_set_null primary key (id)
);

create table migtest_e_basic (
  id                            integer identity(1,1) not null,
  status                        varchar(1),
  status2                       varchar(1) default 'N' not null,
  name                          varchar(127),
  description                   varchar(127),
  description_file              image,
  json_list                     text,
  a_lob                         varchar(255) default 'X' not null,
  some_date                     datetime2,
  old_boolean                   bit default 0 not null,
  old_boolean2                  bit,
  eref_id                       integer,
  indextest1                    varchar(127),
  indextest2                    varchar(127),
  indextest3                    varchar(127),
  indextest4                    varchar(127),
  indextest5                    varchar(127),
  indextest6                    varchar(127),
  indextest7                    varchar(127) not null,
  default_test                  integer default 0,
  user_id                       integer not null,
  constraint ck_migtest_e_basic_status check ( status in ('N','A','I')),
  constraint ck_migtest_e_basic_status2 check ( status2 in ('N','A','I')),
  constraint pk_migtest_e_basic primary key (id)
);

create table migtest_e_enum (
  id                            integer identity(1,1) not null,
  test_status                   varchar(1),
  constraint ck_migtest_e_enum_test_status check ( test_status in ('N','A','I')),
  constraint pk_migtest_e_enum primary key (id)
);

create table migtest_e_history (
  id                            integer identity(1,1) not null,
  test_string                   varchar(255),
  constraint pk_migtest_e_history primary key (id)
);

create table migtest_e_history2 (
  id                            integer identity(1,1) not null,
  test_string                   varchar(255),
  obsolete_string1              varchar(255),
  obsolete_string2              varchar(255),
  constraint pk_migtest_e_history2 primary key (id)
);

create table migtest_e_history3 (
  id                            integer identity(1,1) not null,
  test_string                   varchar(255),
  constraint pk_migtest_e_history3 primary key (id)
);

create table migtest_e_history4 (
  id                            integer identity(1,1) not null,
  test_number                   integer,
  constraint pk_migtest_e_history4 primary key (id)
);

create table migtest_e_history5 (
  id                            integer identity(1,1) not null,
  test_number                   integer,
  constraint pk_migtest_e_history5 primary key (id)
);

create table migtest_e_history6 (
  id                            integer identity(1,1) not null,
  test_number1                  integer,
  test_number2                  integer not null,
  constraint pk_migtest_e_history6 primary key (id)
);

create table [migtest_QuOtEd] (
  id                            varchar(255) not null,
  status1                       varchar(1),
  status2                       varchar(1),
  constraint ck_migtest_quoted_status1 check ( status1 in ('N','A','I')),
  constraint ck_migtest_quoted_status2 check ( status2 in ('N','A','I')),
  constraint pk_migtest_quoted primary key (id)
);

create table migtest_e_ref (
  id                            integer identity(1,1) not null,
  name                          varchar(127) not null,
  constraint pk_migtest_e_ref primary key (id)
);

create table migtest_e_softdelete (
  id                            integer identity(1,1) not null,
  test_string                   varchar(255),
  constraint pk_migtest_e_softdelete primary key (id)
);

create table [table] (
  [index]                       varchar(255) not null,
  [from]                        varchar(255),
  [to]                          varchar(255),
  [varchar]                     varchar(255),
  [foreign]                     varchar(255),
  textfield                     varchar(255) not null,
  constraint pk_table primary key ([index])
);

create table migtest_mtm_c (
  id                            integer identity(1,1) not null,
  name                          varchar(255),
  constraint pk_migtest_mtm_c primary key (id)
);

create table migtest_mtm_m (
  id                            bigint identity(1,1) not null,
  name                          varchar(255),
  constraint pk_migtest_mtm_m primary key (id)
);

create table migtest_oto_child (
  id                            integer identity(1,1) not null,
  name                          varchar(255),
  constraint pk_migtest_oto_child primary key (id)
);

create table migtest_oto_master (
  id                            bigint identity(1,1) not null,
  name                          varchar(255),
  constraint pk_migtest_oto_master primary key (id)
);

-- apply post alter
create unique nonclustered index uq_migtest_e_basic_indextest2 on migtest_e_basic(indextest2) where indextest2 is not null;
create unique nonclustered index uq_migtest_e_basic_indextest6 on migtest_e_basic(indextest6) where indextest6 is not null;
alter table migtest_e_basic add constraint uq_migtest_e_basic_indextest7 unique  (indextest7);
alter table migtest_e_history2
    add sys_periodFrom datetime2 GENERATED ALWAYS AS ROW START NOT NULL DEFAULT SYSUTCDATETIME(),
        sys_periodTo   datetime2 GENERATED ALWAYS AS ROW END   NOT NULL DEFAULT '9999-12-31T23:59:59.9999999',
period for system_time (sys_periodFrom, sys_periodTo);
alter table migtest_e_history2 set (system_versioning = on (history_table=dbo.migtest_e_history2_history));
alter table migtest_e_history3
    add sys_periodFrom datetime2 GENERATED ALWAYS AS ROW START NOT NULL DEFAULT SYSUTCDATETIME(),
        sys_periodTo   datetime2 GENERATED ALWAYS AS ROW END   NOT NULL DEFAULT '9999-12-31T23:59:59.9999999',
period for system_time (sys_periodFrom, sys_periodTo);
alter table migtest_e_history3 set (system_versioning = on (history_table=dbo.migtest_e_history3_history));
alter table migtest_e_history4
    add sys_periodFrom datetime2 GENERATED ALWAYS AS ROW START NOT NULL DEFAULT SYSUTCDATETIME(),
        sys_periodTo   datetime2 GENERATED ALWAYS AS ROW END   NOT NULL DEFAULT '9999-12-31T23:59:59.9999999',
period for system_time (sys_periodFrom, sys_periodTo);
alter table migtest_e_history4 set (system_versioning = on (history_table=dbo.migtest_e_history4_history));
alter table migtest_e_history5
    add sys_periodFrom datetime2 GENERATED ALWAYS AS ROW START NOT NULL DEFAULT SYSUTCDATETIME(),
        sys_periodTo   datetime2 GENERATED ALWAYS AS ROW END   NOT NULL DEFAULT '9999-12-31T23:59:59.9999999',
period for system_time (sys_periodFrom, sys_periodTo);
alter table migtest_e_history5 set (system_versioning = on (history_table=dbo.migtest_e_history5_history));
alter table migtest_e_history6
    add sys_periodFrom datetime2 GENERATED ALWAYS AS ROW START NOT NULL DEFAULT SYSUTCDATETIME(),
        sys_periodTo   datetime2 GENERATED ALWAYS AS ROW END   NOT NULL DEFAULT '9999-12-31T23:59:59.9999999',
period for system_time (sys_periodFrom, sys_periodTo);
alter table migtest_e_history6 set (system_versioning = on (history_table=dbo.migtest_e_history6_history));
create unique nonclustered index uq_migtest_quoted_status2 on "migtest_QuOtEd"(status2) where status2 is not null;
alter table migtest_e_ref add constraint uq_migtest_e_ref_name unique  (name);
create unique nonclustered index uq_table_to on "table"("to") where "to" is not null;
create unique nonclustered index uq_table_varchar on "table"("varchar") where "varchar" is not null;
alter table [table]
    add sys_periodFrom datetime2 GENERATED ALWAYS AS ROW START NOT NULL DEFAULT SYSUTCDATETIME(),
        sys_periodTo   datetime2 GENERATED ALWAYS AS ROW END   NOT NULL DEFAULT '9999-12-31T23:59:59.9999999',
period for system_time (sys_periodFrom, sys_periodTo);
alter table "table" set (system_versioning = on (history_table=dbo.table_history));
-- foreign keys and indices
create index ix_migtest_fk_cascade_one_id on migtest_fk_cascade (one_id);
alter table migtest_fk_cascade add constraint fk_migtest_fk_cascade_one_id foreign key (one_id) references migtest_fk_cascade_one (id) on delete cascade;

create index ix_migtest_fk_set_null_one_id on migtest_fk_set_null (one_id);
alter table migtest_fk_set_null add constraint fk_migtest_fk_set_null_one_id foreign key (one_id) references migtest_fk_one (id) on delete set null;

create index ix_migtest_e_basic_eref_id on migtest_e_basic (eref_id);
alter table migtest_e_basic add constraint fk_migtest_e_basic_eref_id foreign key (eref_id) references migtest_e_ref (id);

create index ix_table_foreign on [table] ([foreign]);
alter table [table] add constraint fk_table_foreign foreign key ([foreign]) references [table] ([index]);

create index ix_migtest_e_basic_indextest1 on migtest_e_basic (indextest1);
create index ix_migtest_e_basic_indextest5 on migtest_e_basic (indextest5);
create index ix_migtest_quoted_status1 on [migtest_QuOtEd] (status1);
create index ix_table_from on [table] ([from]);
