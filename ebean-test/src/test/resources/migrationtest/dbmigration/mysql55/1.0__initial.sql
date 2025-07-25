-- Migrationscripts for ebean unittest
-- apply changes
create table migtest_ckey_assoc (
  id                            integer auto_increment not null,
  assoc_one                     varchar(255),
  constraint pk_migtest_ckey_assoc primary key (id)
);

create table migtest_ckey_detail (
  id                            integer auto_increment not null,
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
  id                            bigint auto_increment not null,
  one_id                        bigint,
  constraint pk_migtest_fk_cascade primary key (id)
);

create table migtest_fk_cascade_one (
  id                            bigint auto_increment not null,
  constraint pk_migtest_fk_cascade_one primary key (id)
);

create table migtest_fk_none (
  id                            bigint auto_increment not null,
  one_id                        bigint,
  constraint pk_migtest_fk_none primary key (id)
);

create table migtest_fk_none_via_join (
  id                            bigint auto_increment not null,
  one_id                        bigint,
  constraint pk_migtest_fk_none_via_join primary key (id)
);

create table migtest_fk_one (
  id                            bigint auto_increment not null,
  constraint pk_migtest_fk_one primary key (id)
);

create table migtest_fk_set_null (
  id                            bigint auto_increment not null,
  one_id                        bigint,
  constraint pk_migtest_fk_set_null primary key (id)
);

create table migtest_e_basic (
  id                            integer auto_increment not null,
  status                        varchar(1),
  status2                       varchar(1) default 'N' not null,
  name                          varchar(127),
  description                   varchar(127),
  description_file              longblob,
  json_list                     json,
  a_lob                         varchar(255) default 'X' not null,
  some_date                     datetime,
  old_boolean                   tinyint(1) default 0 not null,
  old_boolean2                  tinyint(1),
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
  constraint uq_migtest_e_basic_indextest2 unique (indextest2),
  constraint uq_migtest_e_basic_indextest6 unique (indextest6),
  constraint uq_migtest_e_basic_indextest7 unique (indextest7),
  constraint pk_migtest_e_basic primary key (id)
);

create table migtest_e_enum (
  id                            integer auto_increment not null,
  test_status                   varchar(1),
  constraint pk_migtest_e_enum primary key (id)
);

create table migtest_e_history (
  id                            integer auto_increment not null,
  test_string                   varchar(255),
  constraint pk_migtest_e_history primary key (id)
);

create table migtest_e_history2 (
  id                            integer auto_increment not null,
  test_string                   varchar(255),
  obsolete_string1              varchar(255),
  obsolete_string2              varchar(255),
  constraint pk_migtest_e_history2 primary key (id)
);

create table migtest_e_history3 (
  id                            integer auto_increment not null,
  test_string                   varchar(255),
  constraint pk_migtest_e_history3 primary key (id)
);

create table migtest_e_history4 (
  id                            integer auto_increment not null,
  test_number                   integer,
  constraint pk_migtest_e_history4 primary key (id)
);

create table migtest_e_history5 (
  id                            integer auto_increment not null,
  test_number                   integer,
  constraint pk_migtest_e_history5 primary key (id)
);

create table migtest_e_history6 (
  id                            integer auto_increment not null,
  test_number1                  integer,
  test_number2                  integer not null,
  constraint pk_migtest_e_history6 primary key (id)
);

create table `migtest_QuOtEd` (
  id                            varchar(255) not null,
  status1                       varchar(1),
  status2                       varchar(1),
  constraint uq_migtest_quoted_status2 unique (status2),
  constraint pk_migtest_quoted primary key (id)
);

create table migtest_e_ref (
  id                            integer auto_increment not null,
  name                          varchar(127) not null,
  constraint uq_migtest_e_ref_name unique (name),
  constraint pk_migtest_e_ref primary key (id)
);

create table migtest_e_softdelete (
  id                            integer auto_increment not null,
  test_string                   varchar(255),
  constraint pk_migtest_e_softdelete primary key (id)
);

create table `table` (
  `index`                       varchar(255) not null comment 'this is a comment',
  `from`                        varchar(255),
  `to`                          varchar(255),
  `varchar`                     varchar(255),
  `foreign`                     varchar(255),
  textfield                     varchar(255) not null,
  constraint uq_table_to unique (`to`),
  constraint uq_table_varchar unique (`varchar`),
  constraint pk_table primary key (`index`)
);

create table migtest_mtm_c (
  id                            integer auto_increment not null,
  name                          varchar(255),
  constraint pk_migtest_mtm_c primary key (id)
);

create table migtest_mtm_m (
  id                            bigint auto_increment not null,
  name                          varchar(255),
  constraint pk_migtest_mtm_m primary key (id)
);

create table migtest_oto_child (
  id                            integer auto_increment not null,
  name                          varchar(255),
  constraint pk_migtest_oto_child primary key (id)
);

create table migtest_oto_master (
  id                            bigint auto_increment not null,
  name                          varchar(255),
  constraint pk_migtest_oto_master primary key (id)
);

-- apply alter tables
alter table `table` add column sys_period_start datetime(6) default now(6);
alter table `table` add column sys_period_end datetime(6);
alter table migtest_e_history2 add column sys_period_start datetime(6) default now(6);
alter table migtest_e_history2 add column sys_period_end datetime(6);
alter table migtest_e_history3 add column sys_period_start datetime(6) default now(6);
alter table migtest_e_history3 add column sys_period_end datetime(6);
alter table migtest_e_history4 add column sys_period_start datetime(6) default now(6);
alter table migtest_e_history4 add column sys_period_end datetime(6);
alter table migtest_e_history5 add column sys_period_start datetime(6) default now(6);
alter table migtest_e_history5 add column sys_period_end datetime(6);
alter table migtest_e_history6 add column sys_period_start datetime(6) default now(6);
alter table migtest_e_history6 add column sys_period_end datetime(6);
-- apply post alter
create table migtest_e_history2_history(
  id                            integer,
  test_string                   varchar(255),
  obsolete_string1              varchar(255),
  obsolete_string2              varchar(255),
  sys_period_start              datetime(6),
  sys_period_end                datetime(6)
);
create view migtest_e_history2_with_history as select * from migtest_e_history2 union all select * from migtest_e_history2_history;
lock tables migtest_e_history2 write;
delimiter $$
create trigger migtest_e_history2_history_upd before update on migtest_e_history2 for each row begin
    insert into migtest_e_history2_history (sys_period_start,sys_period_end,id, test_string, obsolete_string2) values (OLD.sys_period_start, now(6),OLD.id, OLD.test_string, OLD.obsolete_string2);
    set NEW.sys_period_start = now(6);
end$$
delimiter $$
create trigger migtest_e_history2_history_del before delete on migtest_e_history2 for each row begin
    insert into migtest_e_history2_history (sys_period_start,sys_period_end,id, test_string, obsolete_string2) values (OLD.sys_period_start, now(6),OLD.id, OLD.test_string, OLD.obsolete_string2);
end$$
unlock tables;

create table migtest_e_history3_history(
  id                            integer,
  test_string                   varchar(255),
  sys_period_start              datetime(6),
  sys_period_end                datetime(6)
);
create view migtest_e_history3_with_history as select * from migtest_e_history3 union all select * from migtest_e_history3_history;
lock tables migtest_e_history3 write;
delimiter $$
create trigger migtest_e_history3_history_upd before update on migtest_e_history3 for each row begin
    insert into migtest_e_history3_history (sys_period_start,sys_period_end,id, test_string) values (OLD.sys_period_start, now(6),OLD.id, OLD.test_string);
    set NEW.sys_period_start = now(6);
end$$
delimiter $$
create trigger migtest_e_history3_history_del before delete on migtest_e_history3 for each row begin
    insert into migtest_e_history3_history (sys_period_start,sys_period_end,id, test_string) values (OLD.sys_period_start, now(6),OLD.id, OLD.test_string);
end$$
unlock tables;

create table migtest_e_history4_history(
  id                            integer,
  test_number                   integer,
  sys_period_start              datetime(6),
  sys_period_end                datetime(6)
);
create view migtest_e_history4_with_history as select * from migtest_e_history4 union all select * from migtest_e_history4_history;
lock tables migtest_e_history4 write;
delimiter $$
create trigger migtest_e_history4_history_upd before update on migtest_e_history4 for each row begin
    insert into migtest_e_history4_history (sys_period_start,sys_period_end,id, test_number) values (OLD.sys_period_start, now(6),OLD.id, OLD.test_number);
    set NEW.sys_period_start = now(6);
end$$
delimiter $$
create trigger migtest_e_history4_history_del before delete on migtest_e_history4 for each row begin
    insert into migtest_e_history4_history (sys_period_start,sys_period_end,id, test_number) values (OLD.sys_period_start, now(6),OLD.id, OLD.test_number);
end$$
unlock tables;

create table migtest_e_history5_history(
  id                            integer,
  test_number                   integer,
  sys_period_start              datetime(6),
  sys_period_end                datetime(6)
);
create view migtest_e_history5_with_history as select * from migtest_e_history5 union all select * from migtest_e_history5_history;
lock tables migtest_e_history5 write;
delimiter $$
create trigger migtest_e_history5_history_upd before update on migtest_e_history5 for each row begin
    insert into migtest_e_history5_history (sys_period_start,sys_period_end,id, test_number) values (OLD.sys_period_start, now(6),OLD.id, OLD.test_number);
    set NEW.sys_period_start = now(6);
end$$
delimiter $$
create trigger migtest_e_history5_history_del before delete on migtest_e_history5 for each row begin
    insert into migtest_e_history5_history (sys_period_start,sys_period_end,id, test_number) values (OLD.sys_period_start, now(6),OLD.id, OLD.test_number);
end$$
unlock tables;

create table migtest_e_history6_history(
  id                            integer,
  test_number1                  integer,
  test_number2                  integer,
  sys_period_start              datetime(6),
  sys_period_end                datetime(6)
);
create view migtest_e_history6_with_history as select * from migtest_e_history6 union all select * from migtest_e_history6_history;
lock tables migtest_e_history6 write;
delimiter $$
create trigger migtest_e_history6_history_upd before update on migtest_e_history6 for each row begin
    insert into migtest_e_history6_history (sys_period_start,sys_period_end,id, test_number1, test_number2) values (OLD.sys_period_start, now(6),OLD.id, OLD.test_number1, OLD.test_number2);
    set NEW.sys_period_start = now(6);
end$$
delimiter $$
create trigger migtest_e_history6_history_del before delete on migtest_e_history6 for each row begin
    insert into migtest_e_history6_history (sys_period_start,sys_period_end,id, test_number1, test_number2) values (OLD.sys_period_start, now(6),OLD.id, OLD.test_number1, OLD.test_number2);
end$$
unlock tables;

create table table_history(
  `index`                       varchar(255),
  `from`                        varchar(255),
  `to`                          varchar(255),
  `varchar`                     varchar(255),
  `foreign`                     varchar(255),
  textfield                     varchar(255),
  sys_period_start              datetime(6),
  sys_period_end                datetime(6)
);
create view table_with_history as select * from `table` union all select * from table_history;
lock tables `table` write;
delimiter $$
create trigger table_history_upd before update on `table` for each row begin
    insert into table_history (sys_period_start,sys_period_end,`index`, `from`, `to`, `varchar`, `foreign`, textfield) values (OLD.sys_period_start, now(6),OLD.`index`, OLD.`from`, OLD.`to`, OLD.`varchar`, OLD.`foreign`, OLD.textfield);
    set NEW.sys_period_start = now(6);
end$$
delimiter $$
create trigger table_history_del before delete on `table` for each row begin
    insert into table_history (sys_period_start,sys_period_end,`index`, `from`, `to`, `varchar`, `foreign`, textfield) values (OLD.sys_period_start, now(6),OLD.`index`, OLD.`from`, OLD.`to`, OLD.`varchar`, OLD.`foreign`, OLD.textfield);
end$$
unlock tables;

-- foreign keys and indices
create index ix_migtest_fk_cascade_one_id on migtest_fk_cascade (one_id);
alter table migtest_fk_cascade add constraint fk_migtest_fk_cascade_one_id foreign key (one_id) references migtest_fk_cascade_one (id) on delete cascade on update restrict;

create index ix_migtest_fk_set_null_one_id on migtest_fk_set_null (one_id);
alter table migtest_fk_set_null add constraint fk_migtest_fk_set_null_one_id foreign key (one_id) references migtest_fk_one (id) on delete set null on update restrict;

create index ix_migtest_e_basic_eref_id on migtest_e_basic (eref_id);
alter table migtest_e_basic add constraint fk_migtest_e_basic_eref_id foreign key (eref_id) references migtest_e_ref (id) on delete restrict on update restrict;

create index ix_table_foreign on `table` (`foreign`);
alter table `table` add constraint fk_table_foreign foreign key (`foreign`) references `table` (`index`) on delete restrict on update restrict;

create index ix_migtest_e_basic_indextest1 on migtest_e_basic (indextest1);
create index ix_migtest_e_basic_indextest5 on migtest_e_basic (indextest5);
create index ix_migtest_quoted_status1 on `migtest_QuOtEd` (status1);
create index ix_table_from on `table` (`from`);
