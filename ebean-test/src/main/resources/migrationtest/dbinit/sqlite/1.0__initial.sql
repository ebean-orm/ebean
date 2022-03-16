-- Migrationscripts for ebean unittest
-- apply changes
create table migtest_ckey_assoc (
  id                            integer not null,
  assoc_one                     varchar(255),
  constraint pk_migtest_ckey_assoc primary key (id)
);

create table migtest_ckey_detail (
  id                            integer not null,
  something                     varchar(255),
  one_key                       integer,
  two_key                       varchar(127),
  constraint pk_migtest_ckey_detail primary key (id),
  foreign key (one_key,two_key) references migtest_ckey_parent (one_key,two_key) on delete restrict on update restrict
);

create table migtest_ckey_parent (
  one_key                       integer not null,
  two_key                       varchar(127) not null,
  name                          varchar(255),
  assoc_id                      integer,
  version                       integer not null,
  constraint pk_migtest_ckey_parent primary key (one_key,two_key),
  foreign key (assoc_id) references migtest_ckey_assoc (id) on delete restrict on update restrict
);

create table migtest_fk_cascade (
  id                            integer not null,
  one_id                        integer,
  constraint pk_migtest_fk_cascade primary key (id),
  foreign key (one_id) references migtest_fk_cascade_one (id) on delete restrict on update restrict
);

create table migtest_fk_cascade_one (
  id                            integer not null,
  constraint pk_migtest_fk_cascade_one primary key (id)
);

create table migtest_fk_none (
  id                            integer not null,
  one_id                        integer,
  constraint pk_migtest_fk_none primary key (id),
  foreign key (one_id) references migtest_fk_one (id) on delete restrict on update restrict
);

create table migtest_fk_none_via_join (
  id                            integer not null,
  one_id                        integer,
  constraint pk_migtest_fk_none_via_join primary key (id),
  foreign key (one_id) references migtest_fk_one (id) on delete restrict on update restrict
);

create table migtest_fk_one (
  id                            integer not null,
  constraint pk_migtest_fk_one primary key (id)
);

create table migtest_fk_set_null (
  id                            integer not null,
  one_id                        integer,
  constraint pk_migtest_fk_set_null primary key (id),
  foreign key (one_id) references migtest_fk_one (id) on delete restrict on update restrict
);

create table migtest_e_basic (
  id                            integer not null,
  status                        varchar(1) default 'A' not null,
  status2                       varchar(127),
  name                          varchar(127),
  description                   varchar(127),
  some_date                     timestamp,
  new_string_field              varchar(255) default 'foo''bar' not null,
  new_boolean_field             int default 1 not null,
  new_boolean_field2            int default 1 not null,
  indextest1                    varchar(127),
  indextest2                    varchar(127),
  indextest3                    varchar(127),
  indextest4                    varchar(127),
  indextest5                    varchar(127),
  indextest6                    varchar(127),
  progress                      integer default 0 not null,
  new_integer                   integer default 42 not null,
  user_id                       integer,
  constraint ck_migtest_e_basic_status check ( status in ('N','A','I','?')),
  constraint ck_migtest_e_basic_progress check ( progress in (0,1,2)),
  constraint uq_migtest_e_basic_description unique (description),
  constraint uq_migtest_e_basic_status_indextest1 unique (status,indextest1),
  constraint uq_migtest_e_basic_name unique (name),
  constraint uq_migtest_e_basic_indextest4 unique (indextest4),
  constraint uq_migtest_e_basic_indextest5 unique (indextest5),
  constraint pk_migtest_e_basic primary key (id),
  foreign key (user_id) references migtest_e_user (id) on delete restrict on update restrict
);

create table migtest_e_enum (
  id                            integer not null,
  test_status                   varchar(1),
  constraint pk_migtest_e_enum primary key (id)
);

create table migtest_e_history (
  id                            integer not null,
  test_string                   integer,
  constraint pk_migtest_e_history primary key (id)
);

create table migtest_e_history2 (
  id                            integer not null,
  test_string                   varchar(255) default 'unknown' not null,
  test_string2                  varchar(255),
  test_string3                  varchar(255) default 'unknown' not null,
  new_column                    varchar(20),
  constraint pk_migtest_e_history2 primary key (id)
);

create table migtest_e_history3 (
  id                            integer not null,
  test_string                   varchar(255),
  constraint pk_migtest_e_history3 primary key (id)
);

create table migtest_e_history4 (
  id                            integer not null,
  test_number                   integer,
  constraint pk_migtest_e_history4 primary key (id)
);

create table migtest_e_history5 (
  id                            integer not null,
  test_number                   integer,
  test_boolean                  int default 0 not null,
  constraint pk_migtest_e_history5 primary key (id)
);

create table migtest_e_history6 (
  id                            integer not null,
  test_number1                  integer default 42 not null,
  test_number2                  integer,
  constraint pk_migtest_e_history6 primary key (id)
);

create table migtest_e_softdelete (
  id                            integer not null,
  test_string                   varchar(255),
  deleted                       int default 0 not null,
  constraint pk_migtest_e_softdelete primary key (id)
);

create table migtest_e_user (
  id                            integer not null,
  constraint pk_migtest_e_user primary key (id)
);

create table migtest_mtm_c (
  id                            integer not null,
  name                          varchar(255),
  constraint pk_migtest_mtm_c primary key (id)
);

create table migtest_mtm_c_migtest_mtm_m (
  migtest_mtm_c_id              integer not null,
  migtest_mtm_m_id              integer not null,
  constraint pk_migtest_mtm_c_migtest_mtm_m primary key (migtest_mtm_c_id,migtest_mtm_m_id),
  foreign key (migtest_mtm_c_id) references migtest_mtm_c (id) on delete restrict on update restrict,
  foreign key (migtest_mtm_m_id) references migtest_mtm_m (id) on delete restrict on update restrict
);

create table migtest_mtm_m (
  id                            integer not null,
  name                          varchar(255),
  constraint pk_migtest_mtm_m primary key (id)
);

create table migtest_mtm_m_migtest_mtm_c (
  migtest_mtm_m_id              integer not null,
  migtest_mtm_c_id              integer not null,
  constraint pk_migtest_mtm_m_migtest_mtm_c primary key (migtest_mtm_m_id,migtest_mtm_c_id),
  foreign key (migtest_mtm_m_id) references migtest_mtm_m (id) on delete restrict on update restrict,
  foreign key (migtest_mtm_c_id) references migtest_mtm_c (id) on delete restrict on update restrict
);

create table migtest_mtm_m_phone_numbers (
  migtest_mtm_m_id              integer not null,
  value                         varchar(255) not null,
  foreign key (migtest_mtm_m_id) references migtest_mtm_m (id) on delete restrict on update restrict
);

create table migtest_oto_child (
  id                            integer not null,
  name                          varchar(255),
  master_id                     integer,
  constraint uq_migtest_oto_child_master_id unique (master_id),
  constraint pk_migtest_oto_child primary key (id),
  foreign key (master_id) references migtest_oto_master (id) on delete restrict on update restrict
);

create table migtest_oto_master (
  id                            integer not null,
  name                          varchar(255),
  constraint pk_migtest_oto_master primary key (id)
);

-- foreign keys and indices
create index ix_migtest_e_basic_indextest3 on migtest_e_basic (indextest3);
create index ix_migtest_e_basic_indextest6 on migtest_e_basic (indextest6);
