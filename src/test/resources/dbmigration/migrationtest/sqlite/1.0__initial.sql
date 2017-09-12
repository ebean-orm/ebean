-- apply changes
-- Migrationscript for sqlite;
-- identity type: IDENTITY;

create table migtest_ckey_assoc (
  id                            integer not null,
  assoc_one                     varchar(255),
  constraint pk_migtest_ckey_assoc primary key (id)
);

create table migtest_ckey_detail (
  id                            integer not null,
  something                     varchar(255),
  constraint pk_migtest_ckey_detail primary key (id)
);

create table migtest_ckey_parent (
  one_key                       integer not null,
  two_key                       varchar(255) not null,
  name                          varchar(255),
  version                       integer not null,
  constraint pk_migtest_ckey_parent primary key (one_key,two_key)
);

create table migtest_e_basic (
  id                            integer not null,
  status                        varchar(1),
  name                          varchar(255),
  description                   varchar(255),
  some_date                     timestamp,
  old_boolean                   int default 0 not null,
  old_boolean2                  int,
  eref_id                       integer,
  indextest1                    varchar(255),
  indextest2                    varchar(255),
  indextest3                    varchar(255),
  indextest4                    varchar(255),
  indextest5                    varchar(255),
  indextest6                    varchar(255),
  user_id                       integer not null,
  constraint ck_migtest_e_basic_status check ( status in ('N','A','I')),
  constraint uq_migtest_e_basic_indextest2 unique (indextest2),
  constraint uq_migtest_e_basic_indextest6 unique (indextest6),
  constraint pk_migtest_e_basic primary key (id),
  foreign key (eref_id) references migtest_e_ref (id) on delete restrict on update restrict
);

create table migtest_e_history (
  id                            integer not null,
  test_string                   varchar(255),
  constraint pk_migtest_e_history primary key (id)
);

create table migtest_e_history2 (
  id                            integer not null,
  test_string                   varchar(255),
  constraint pk_migtest_e_history2 primary key (id)
);

create table migtest_e_ref (
  id                            integer not null,
  name                          varchar(255) not null,
  constraint uq_migtest_e_ref_name unique (name),
  constraint pk_migtest_e_ref primary key (id)
);

create table migtest_e_softdelete (
  id                            integer not null,
  test_string                   varchar(255),
  constraint pk_migtest_e_softdelete primary key (id)
);

create table migtest_mtm_c (
  id                            integer not null,
  name                          varchar(255),
  constraint pk_migtest_mtm_c primary key (id)
);

create table migtest_mtm_m (
  id                            integer not null,
  name                          varchar(255),
  constraint pk_migtest_mtm_m primary key (id)
);

create table migtest_oto_child (
  id                            integer not null,
  name                          varchar(255),
  constraint pk_migtest_oto_child primary key (id)
);

create table migtest_oto_master (
  id                            integer not null,
  name                          varchar(255),
  constraint pk_migtest_oto_master primary key (id)
);

create index ix_migtest_e_basic_indextest1 on migtest_e_basic (indextest1);
create index ix_migtest_e_basic_indextest5 on migtest_e_basic (indextest5);
