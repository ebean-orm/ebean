-- Migrationscripts for ebean unittest
-- apply changes
create table migtest_ckey_assoc (
  id                            integer not null,
  assoc_one                     nvarchar(255),
  constraint pk_migtest_ckey_assoc primary key (id)
);
create sequence migtest_ckey_assoc_seq as bigint start with 1;

create table migtest_ckey_detail (
  id                            integer not null,
  something                     nvarchar(255),
  one_key                       integer,
  two_key                       nvarchar(127),
  constraint pk_migtest_ckey_detail primary key (id)
);
create sequence migtest_ckey_detail_seq as bigint start with 1;

create table migtest_ckey_parent (
  one_key                       integer not null,
  two_key                       nvarchar(127) not null,
  name                          nvarchar(255),
  assoc_id                      integer,
  version                       integer not null,
  constraint pk_migtest_ckey_parent primary key (one_key,two_key)
);

create table migtest_fk_cascade (
  id                            numeric(19) not null,
  one_id                        numeric(19),
  constraint pk_migtest_fk_cascade primary key (id)
);
create sequence migtest_fk_cascade_seq as bigint start with 1;

create table migtest_fk_cascade_one (
  id                            numeric(19) not null,
  constraint pk_migtest_fk_cascade_one primary key (id)
);
create sequence migtest_fk_cascade_one_seq as bigint start with 1;

create table migtest_fk_none (
  id                            numeric(19) not null,
  one_id                        numeric(19),
  constraint pk_migtest_fk_none primary key (id)
);
create sequence migtest_fk_none_seq as bigint start with 1;

create table migtest_fk_none_via_join (
  id                            numeric(19) not null,
  one_id                        numeric(19),
  constraint pk_migtest_fk_none_via_join primary key (id)
);
create sequence migtest_fk_none_via_join_seq as bigint start with 1;

create table migtest_fk_one (
  id                            numeric(19) not null,
  constraint pk_migtest_fk_one primary key (id)
);
create sequence migtest_fk_one_seq as bigint start with 1;

create table migtest_fk_set_null (
  id                            numeric(19) not null,
  one_id                        numeric(19),
  constraint pk_migtest_fk_set_null primary key (id)
);
create sequence migtest_fk_set_null_seq as bigint start with 1;

create table migtest_e_basic (
  id                            integer not null,
  status                        nvarchar(1) default 'A' not null,
  status2                       nvarchar(127),
  name                          nvarchar(127),
  description                   nvarchar(127),
  some_date                     datetime2,
  new_string_field              nvarchar(255) default 'foo''bar' not null,
  new_boolean_field             bit default 1 not null,
  new_boolean_field2            bit default 1 not null,
  indextest1                    nvarchar(127),
  indextest2                    nvarchar(127),
  indextest3                    nvarchar(127),
  indextest4                    nvarchar(127),
  indextest5                    nvarchar(127),
  indextest6                    nvarchar(127),
  progress                      integer default 0 not null,
  new_integer                   integer default 42 not null,
  user_id                       integer,
  constraint ck_migtest_e_basic_status check ( status in ('N','A','I','?')),
  constraint ck_migtest_e_basic_progress check ( progress in (0,1,2)),
  constraint pk_migtest_e_basic primary key (id)
);
create sequence migtest_e_basic_seq as bigint start with 1;

create table migtest_e_enum (
  id                            integer not null,
  test_status                   nvarchar(1),
  constraint pk_migtest_e_enum primary key (id)
);
create sequence migtest_e_enum_seq as bigint start with 1;

create table migtest_e_history (
  id                            integer not null,
  test_string                   numeric(19),
  constraint pk_migtest_e_history primary key (id)
);
create sequence migtest_e_history_seq as bigint start with 1;

create table migtest_e_history2 (
  id                            integer not null,
  test_string                   nvarchar(255) default 'unknown' not null,
  test_string2                  nvarchar(255),
  test_string3                  nvarchar(255) default 'unknown' not null,
  new_column                    nvarchar(20),
  constraint pk_migtest_e_history2 primary key (id)
);
create sequence migtest_e_history2_seq as bigint start with 1;

create table migtest_e_history3 (
  id                            integer not null,
  test_string                   nvarchar(255),
  constraint pk_migtest_e_history3 primary key (id)
);
create sequence migtest_e_history3_seq as bigint start with 1;

create table migtest_e_history4 (
  id                            integer not null,
  test_number                   numeric(19),
  constraint pk_migtest_e_history4 primary key (id)
);
create sequence migtest_e_history4_seq as bigint start with 1;

create table migtest_e_history5 (
  id                            integer not null,
  test_number                   integer,
  test_boolean                  bit default 0 not null,
  constraint pk_migtest_e_history5 primary key (id)
);
create sequence migtest_e_history5_seq as bigint start with 1;

create table migtest_e_history6 (
  id                            integer not null,
  test_number1                  integer default 42 not null,
  test_number2                  integer,
  constraint pk_migtest_e_history6 primary key (id)
);
create sequence migtest_e_history6_seq as bigint start with 1;

create table migtest_e_softdelete (
  id                            integer not null,
  test_string                   nvarchar(255),
  deleted                       bit default 0 not null,
  constraint pk_migtest_e_softdelete primary key (id)
);
create sequence migtest_e_softdelete_seq as bigint start with 1;

create table migtest_e_user (
  id                            integer not null,
  constraint pk_migtest_e_user primary key (id)
);
create sequence migtest_e_user_seq as bigint start with 1;

create table migtest_mtm_c (
  id                            integer not null,
  name                          nvarchar(255),
  constraint pk_migtest_mtm_c primary key (id)
);
create sequence migtest_mtm_c_seq as bigint start with 1;

create table migtest_mtm_c_migtest_mtm_m (
  migtest_mtm_c_id              integer not null,
  migtest_mtm_m_id              numeric(19) not null,
  constraint pk_migtest_mtm_c_migtest_mtm_m primary key (migtest_mtm_c_id,migtest_mtm_m_id)
);

create table migtest_mtm_m (
  id                            numeric(19) not null,
  name                          nvarchar(255),
  constraint pk_migtest_mtm_m primary key (id)
);
create sequence migtest_mtm_m_seq as bigint start with 1;

create table migtest_mtm_m_migtest_mtm_c (
  migtest_mtm_m_id              numeric(19) not null,
  migtest_mtm_c_id              integer not null,
  constraint pk_migtest_mtm_m_migtest_mtm_c primary key (migtest_mtm_m_id,migtest_mtm_c_id)
);

create table migtest_mtm_m_phone_numbers (
  migtest_mtm_m_id              numeric(19) not null,
  value                         nvarchar(255) not null
);

create table migtest_oto_child (
  id                            integer not null,
  name                          nvarchar(255),
  master_id                     numeric(19),
  constraint pk_migtest_oto_child primary key (id)
);
create sequence migtest_oto_child_seq as bigint start with 1;

create table migtest_oto_master (
  id                            numeric(19) not null,
  name                          nvarchar(255),
  constraint pk_migtest_oto_master primary key (id)
);
create sequence migtest_oto_master_seq as bigint start with 1;

-- apply post alter
create unique nonclustered index uq_migtest_e_basic_description on migtest_e_basic(description) where description is not null;
create unique nonclustered index uq_migtest_e_basic_status_indextest1 on migtest_e_basic(status,indextest1) where indextest1 is not null;
create unique nonclustered index uq_migtest_e_basic_name on migtest_e_basic(name) where name is not null;
create unique nonclustered index uq_migtest_e_basic_indextest4 on migtest_e_basic(indextest4) where indextest4 is not null;
create unique nonclustered index uq_migtest_e_basic_indextest5 on migtest_e_basic(indextest5) where indextest5 is not null;
alter table migtest_e_history
    add sys_periodFrom datetime2 GENERATED ALWAYS AS ROW START NOT NULL DEFAULT SYSUTCDATETIME(),
        sys_periodTo   datetime2 GENERATED ALWAYS AS ROW END   NOT NULL DEFAULT '9999-12-31T23:59:59.9999999',
period for system_time (sys_periodFrom, sys_periodTo);
alter table migtest_e_history set (system_versioning = on (history_table=dbo.migtest_e_history_history));
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
create unique nonclustered index uq_migtest_oto_child_master_id on migtest_oto_child(master_id) where master_id is not null;
-- foreign keys and indices
create index ix_migtest_ckey_detail_parent on migtest_ckey_detail (one_key,two_key);
alter table migtest_ckey_detail add constraint fk_migtest_ckey_detail_parent foreign key (one_key,two_key) references migtest_ckey_parent (one_key,two_key);

create index ix_migtest_ckey_parent_assoc_id on migtest_ckey_parent (assoc_id);
alter table migtest_ckey_parent add constraint fk_migtest_ckey_parent_assoc_id foreign key (assoc_id) references migtest_ckey_assoc (id);

create index ix_migtest_fk_cascade_one_id on migtest_fk_cascade (one_id);
alter table migtest_fk_cascade add constraint fk_migtest_fk_cascade_one_id foreign key (one_id) references migtest_fk_cascade_one (id);

create index ix_migtest_fk_none_one_id on migtest_fk_none (one_id);
alter table migtest_fk_none add constraint fk_migtest_fk_none_one_id foreign key (one_id) references migtest_fk_one (id);

create index ix_migtest_fk_none_via_join_one_id on migtest_fk_none_via_join (one_id);
alter table migtest_fk_none_via_join add constraint fk_migtest_fk_none_via_join_one_id foreign key (one_id) references migtest_fk_one (id);

create index ix_migtest_fk_set_null_one_id on migtest_fk_set_null (one_id);
alter table migtest_fk_set_null add constraint fk_migtest_fk_set_null_one_id foreign key (one_id) references migtest_fk_one (id);

create index ix_migtest_e_basic_user_id on migtest_e_basic (user_id);
alter table migtest_e_basic add constraint fk_migtest_e_basic_user_id foreign key (user_id) references migtest_e_user (id);

create index ix_migtest_mtm_c_migtest_mtm_m_migtest_mtm_c on migtest_mtm_c_migtest_mtm_m (migtest_mtm_c_id);
alter table migtest_mtm_c_migtest_mtm_m add constraint fk_migtest_mtm_c_migtest_mtm_m_migtest_mtm_c foreign key (migtest_mtm_c_id) references migtest_mtm_c (id);

create index ix_migtest_mtm_c_migtest_mtm_m_migtest_mtm_m on migtest_mtm_c_migtest_mtm_m (migtest_mtm_m_id);
alter table migtest_mtm_c_migtest_mtm_m add constraint fk_migtest_mtm_c_migtest_mtm_m_migtest_mtm_m foreign key (migtest_mtm_m_id) references migtest_mtm_m (id);

create index ix_migtest_mtm_m_migtest_mtm_c_migtest_mtm_m on migtest_mtm_m_migtest_mtm_c (migtest_mtm_m_id);
alter table migtest_mtm_m_migtest_mtm_c add constraint fk_migtest_mtm_m_migtest_mtm_c_migtest_mtm_m foreign key (migtest_mtm_m_id) references migtest_mtm_m (id);

create index ix_migtest_mtm_m_migtest_mtm_c_migtest_mtm_c on migtest_mtm_m_migtest_mtm_c (migtest_mtm_c_id);
alter table migtest_mtm_m_migtest_mtm_c add constraint fk_migtest_mtm_m_migtest_mtm_c_migtest_mtm_c foreign key (migtest_mtm_c_id) references migtest_mtm_c (id);

create index ix_migtest_mtm_m_phone_numbers_migtest_mtm_m_id on migtest_mtm_m_phone_numbers (migtest_mtm_m_id);
alter table migtest_mtm_m_phone_numbers add constraint fk_migtest_mtm_m_phone_numbers_migtest_mtm_m_id foreign key (migtest_mtm_m_id) references migtest_mtm_m (id);

alter table migtest_oto_child add constraint fk_migtest_oto_child_master_id foreign key (master_id) references migtest_oto_master (id);

create index ix_migtest_e_basic_indextest3 on migtest_e_basic (indextest3);
create index ix_migtest_e_basic_indextest6 on migtest_e_basic (indextest6);
