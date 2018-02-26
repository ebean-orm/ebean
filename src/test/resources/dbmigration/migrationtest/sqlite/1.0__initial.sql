-- Migrationscripts for ebean unittest
-- apply changes
create table migtest_fk_cascade (
  id                            integer not null,
  one_id                        integer,
  constraint pk_migtest_fk_cascade primary key (id),
  foreign key (one_id) references migtest_fk_cascade_one (id) on delete cascade on update cascade
);

create table migtest_fk_cascade_one (
  id                            integer not null,
  constraint pk_migtest_fk_cascade_one primary key (id)
);

create table migtest_fk_none (
  id                            integer not null,
  one_id                        integer,
  constraint pk_migtest_fk_none primary key (id)
);

create table migtest_fk_none_via_join (
  id                            integer not null,
  one_id                        integer,
  constraint pk_migtest_fk_none_via_join primary key (id)
);

create table migtest_fk_one (
  id                            integer not null,
  constraint pk_migtest_fk_one primary key (id)
);

create table migtest_fk_set_null (
  id                            integer not null,
  one_id                        integer,
  constraint pk_migtest_fk_set_null primary key (id),
  foreign key (one_id) references migtest_fk_one (id) on delete set null on update set null
);

create table migtest_e_basic (
  id                            integer not null,
  status                        varchar(1),
  name                          varchar(127),
  description                   varchar(127),
  some_date                     timestamp,
  old_boolean                   int default 0 not null,
  old_boolean2                  int,
  eref_id                       integer,
  indextest1                    varchar(127),
  indextest2                    varchar(127),
  indextest3                    varchar(127),
  indextest4                    varchar(127),
  indextest5                    varchar(127),
  indextest6                    varchar(127),
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
  constraint pk_migtest_e_ref primary key (id)
);

create table migtest_e_softdelete (
  id                            integer not null,
  test_string                   varchar(255),
  constraint pk_migtest_e_softdelete primary key (id)
);

create index ix_migtest_e_basic_indextest1 on migtest_e_basic (indextest1);
create index ix_migtest_e_basic_indextest5 on migtest_e_basic (indextest5);
