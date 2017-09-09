-- apply changes
create table migtest_e_basic (
  id                            integer auto_increment not null,
  status                        varchar(1),
  name                          varchar(255),
  description                   varchar(255),
  some_date                     datetime(6),
  old_boolean                   tinyint(1) default 0 not null,
  old_boolean2                  tinyint(1) default 0,
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
  constraint pk_migtest_e_basic primary key (id)
);

create table migtest_e_history (
  id                            integer auto_increment not null,
  test_string                   varchar(255),
  constraint pk_migtest_e_history primary key (id)
);

create table migtest_e_history2 (
  id                            integer auto_increment not null,
  test_string                   varchar(255),
  constraint pk_migtest_e_history2 primary key (id)
);

create table migtest_e_ref (
  id                            integer auto_increment not null,
  constraint pk_migtest_e_ref primary key (id)
);

create table migtest_e_softdelete (
  id                            integer auto_increment not null,
  test_string                   varchar(255),
  constraint pk_migtest_e_softdelete primary key (id)
);

create index ix_migtest_e_basic_indextest1 on migtest_e_basic (indextest1);
create index ix_migtest_e_basic_indextest5 on migtest_e_basic (indextest5);
alter table migtest_e_basic add constraint fk_migtest_e_basic_eref_id foreign key (eref_id) references migtest_e_ref (id) on delete restrict on update restrict;
create index ix_migtest_e_basic_eref_id on migtest_e_basic (eref_id);

alter table migtest_e_history2 add column sys_period_start datetime(6) default now(6);
alter table migtest_e_history2 add column sys_period_end datetime(6);
create table migtest_e_history2_history(
  id                            integer,
  test_string                   varchar(255),
  sys_period_start              datetime(6),
  sys_period_end                datetime(6)
);
create view migtest_e_history2_with_history as select * from migtest_e_history2 union all select * from migtest_e_history2_history;

delimiter $$
create trigger migtest_e_history2_history_upd before update on migtest_e_history2 for each row begin
    insert into migtest_e_history2_history (sys_period_start,sys_period_end,id, test_string) values (OLD.sys_period_start, now(6),OLD.id, OLD.test_string);
    set NEW.sys_period_start = now(6);
end$$
delimiter $$
create trigger migtest_e_history2_history_del before delete on migtest_e_history2 for each row begin
    insert into migtest_e_history2_history (sys_period_start,sys_period_end,id, test_string) values (OLD.sys_period_start, now(6),OLD.id, OLD.test_string);
end$$
