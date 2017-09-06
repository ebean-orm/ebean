-- apply changes
create table migtest_e_basic (
  id                            integer auto_increment not null,
  status                        varchar(1),
  name                          varchar(255),
  description                   varchar(255),
  some_date                     datetime(6),
  old_boolean                   tinyint(1) default 0 not null,
  old_boolean2                  tinyint(1) default 0,
  eref_id                       bigint,
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
  constraint pk_migtest_e_basic primary key (id)
);

create table migtest_e_history (
  id                            integer auto_increment not null,
  test_string                   varchar(255),
  constraint pk_migtest_e_history primary key (id)
);

create table migtest_e_ref (
  id                            bigint auto_increment not null,
  constraint pk_migtest_e_ref primary key (id)
);

create index ix_migtest_e_basic_indextest1 on migtest_e_basic (indextest1);
create index ix_migtest_e_basic_indextest5 on migtest_e_basic (indextest5);
alter table migtest_e_basic add constraint fk_migtest_e_basic_eref_id foreign key (eref_id) references migtest_e_ref (id) on delete restrict on update restrict;
create index ix_migtest_e_basic_eref_id on migtest_e_basic (eref_id);

