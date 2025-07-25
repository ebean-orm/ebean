-- Migrationscripts for ebean unittest
-- drop dependencies
alter table migtest_fk_cascade drop foreign key fk_migtest_fk_cascade_one_id;
alter table migtest_fk_set_null drop foreign key fk_migtest_fk_set_null_one_id;
alter table migtest_e_basic drop index uq_migtest_e_basic_indextest2;
alter table migtest_e_basic drop index uq_migtest_e_basic_indextest6;
alter table migtest_e_basic drop index uq_migtest_e_basic_indextest7;
drop index ix_migtest_e_basic_indextest1 on migtest_e_basic;
drop index ix_migtest_e_basic_indextest5 on migtest_e_basic;
drop index ix_migtest_quoted_status1 on `migtest_QuOtEd`;
-- apply changes
create table drop_main (
  id                            integer auto_increment not null,
  constraint pk_drop_main primary key (id)
);

create table drop_main_drop_ref_many (
  drop_main_id                  integer not null,
  drop_ref_many_id              integer not null,
  constraint pk_drop_main_drop_ref_many primary key (drop_main_id,drop_ref_many_id)
);

create table drop_ref_many (
  id                            integer auto_increment not null,
  constraint pk_drop_ref_many primary key (id)
);

create table drop_ref_one (
  id                            integer auto_increment not null,
  parent_id                     integer,
  constraint pk_drop_ref_one primary key (id)
);

create table drop_ref_one_to_one (
  id                            integer auto_increment not null,
  parent_id                     integer,
  constraint uq_drop_ref_one_to_one_parent_id unique (parent_id),
  constraint pk_drop_ref_one_to_one primary key (id)
);

create table migtest_e_test_binary (
  id                            integer auto_increment not null,
  test_byte16                   varbinary(16),
  test_byte256                  varbinary(256),
  test_byte512                  varbinary(512),
  test_byte1k                   varbinary(1024),
  test_byte2k                   varbinary(2048),
  test_byte4k                   varbinary(4096),
  test_byte8k                   blob,
  test_byte16k                  blob,
  test_byte32k                  blob,
  test_byte64k                  mediumblob,
  test_byte128k                 mediumblob,
  test_byte256k                 mediumblob,
  test_byte512k                 mediumblob,
  test_byte1m                   mediumblob,
  test_byte2m                   mediumblob,
  test_byte4m                   mediumblob,
  test_byte8m                   mediumblob,
  test_byte16m                  longblob,
  test_byte32m                  longblob,
  constraint pk_migtest_e_test_binary primary key (id)
);

create table migtest_e_test_json (
  id                            integer auto_increment not null,
  json255                       json,
  json256                       json,
  json512                       json,
  json1k                        json,
  json2k                        json,
  json4k                        json,
  json8k                        json,
  json16k                       json,
  json32k                       json,
  json64k                       json,
  json128k                      json,
  json256k                      json,
  json512k                      json,
  json1m                        json,
  json2m                        json,
  json4m                        json,
  json8m                        json,
  json16m                       json,
  json32m                       json,
  constraint pk_migtest_e_test_json primary key (id)
);

create table migtest_e_test_lob (
  id                            integer auto_increment not null,
  lob255                        longtext,
  lob256                        longtext,
  lob512                        longtext,
  lob1k                         longtext,
  lob2k                         longtext,
  lob4k                         longtext,
  lob8k                         longtext,
  lob16k                        longtext,
  lob32k                        longtext,
  lob64k                        longtext,
  lob128k                       longtext,
  lob256k                       longtext,
  lob512k                       longtext,
  lob1m                         longtext,
  lob2m                         longtext,
  lob4m                         longtext,
  lob8m                         longtext,
  lob16m                        longtext,
  lob32m                        longtext,
  constraint pk_migtest_e_test_lob primary key (id)
);

create table migtest_e_test_varchar (
  id                            integer auto_increment not null,
  varchar255                    varchar(255),
  varchar256                    varchar(256),
  varchar512                    varchar(512),
  varchar1k                     varchar(1024),
  varchar2k                     varchar(2048),
  varchar4k                     text,
  varchar8k                     varchar(8193),
  varchar16k                    text,
  varchar32k                    text,
  varchar64k                    mediumtext,
  varchar128k                   mediumtext,
  varchar256k                   mediumtext,
  varchar512k                   mediumtext,
  varchar1m                     mediumtext,
  varchar2m                     mediumtext,
  varchar4m                     mediumtext,
  varchar8m                     mediumtext,
  varchar16m                    longtext,
  varchar32m                    longtext,
  constraint pk_migtest_e_test_varchar primary key (id)
);

create table migtest_e_user (
  id                            integer auto_increment not null,
  constraint pk_migtest_e_user primary key (id)
);

create table migtest_mtm_c_migtest_mtm_m (
  migtest_mtm_c_id              integer not null,
  migtest_mtm_m_id              bigint not null,
  constraint pk_migtest_mtm_c_migtest_mtm_m primary key (migtest_mtm_c_id,migtest_mtm_m_id)
);

create table migtest_mtm_m_migtest_mtm_c (
  migtest_mtm_m_id              bigint not null,
  migtest_mtm_c_id              integer not null,
  constraint pk_migtest_mtm_m_migtest_mtm_c primary key (migtest_mtm_m_id,migtest_mtm_c_id)
);

create table migtest_mtm_m_phone_numbers (
  migtest_mtm_m_id              bigint not null,
  value                         varchar(255) not null
);


update migtest_e_basic set status = 'A' where status is null;

-- rename all collisions;

update migtest_e_basic set default_test = 0 where default_test is null;

insert into migtest_e_user (id) select distinct user_id from migtest_e_basic;

-- NOTE: table has @History - special migration may be necessary
update migtest_e_history2 set test_string = 'unknown' where test_string is null;
drop trigger migtest_e_history2_history_upd;
drop trigger migtest_e_history2_history_del;
drop view migtest_e_history2_with_history;
drop trigger migtest_e_history3_history_upd;
drop trigger migtest_e_history3_history_del;
drop view migtest_e_history3_with_history;
drop trigger migtest_e_history4_history_upd;
drop trigger migtest_e_history4_history_del;
drop view migtest_e_history4_with_history;
drop trigger migtest_e_history5_history_upd;
drop trigger migtest_e_history5_history_del;
drop view migtest_e_history5_with_history;

-- NOTE: table has @History - special migration may be necessary
update migtest_e_history6 set test_number1 = 42 where test_number1 is null;
drop trigger migtest_e_history6_history_upd;
drop trigger migtest_e_history6_history_del;
drop view migtest_e_history6_with_history;
drop trigger table_history_upd;
drop trigger table_history_del;
drop view table_with_history;
-- apply alter tables
alter table `table` modify textfield varchar(255);
alter table `table` add column `select` varchar(255);
alter table `table` add column textfield2 varchar(255);
alter table migtest_ckey_detail add column one_key integer;
alter table migtest_ckey_detail add column two_key varchar(127);
alter table migtest_ckey_parent add column assoc_id integer;
alter table migtest_e_basic modify status varchar(1) not null default 'A';
alter table migtest_e_basic modify status2 varchar(127);
alter table migtest_e_basic modify a_lob varchar(255);
alter table migtest_e_basic modify default_test integer not null default 0;
alter table migtest_e_basic modify user_id integer;
alter table migtest_e_basic add column new_string_field varchar(255) default 'foo''bar' not null;
alter table migtest_e_basic add column new_boolean_field tinyint(1) default 1 not null;
alter table migtest_e_basic add column new_boolean_field2 tinyint(1) default 1 not null;
alter table migtest_e_basic add column progress integer default 0 not null;
alter table migtest_e_basic add column new_integer integer default 42 not null;
alter table migtest_e_history add column sys_period_start datetime(6) default now(6);
alter table migtest_e_history add column sys_period_end datetime(6);
alter table migtest_e_history modify test_string bigint;
alter table migtest_e_history2 modify test_string varchar(255) not null default 'unknown';
alter table migtest_e_history2 add column test_string2 varchar(255);
alter table migtest_e_history2 add column test_string3 varchar(255) default 'unknown' not null;
alter table migtest_e_history2 add column new_column varchar(20);
alter table migtest_e_history2_history add column test_string2 varchar(255);
alter table migtest_e_history2_history add column test_string3 varchar(255) default 'unknown';
alter table migtest_e_history2_history add column new_column varchar(20);
alter table migtest_e_history4 modify test_number bigint;
alter table migtest_e_history4_history modify test_number bigint;
alter table migtest_e_history5 add column test_boolean tinyint(1) default 0 not null;
alter table migtest_e_history5_history add column test_boolean tinyint(1) default 0;
alter table migtest_e_history6 modify test_number1 integer not null default 42;
alter table migtest_e_history6 modify test_number2 integer;
alter table migtest_e_history6_history modify test_number2 integer;
alter table migtest_e_softdelete add column deleted tinyint(1) default 0 not null;
alter table migtest_oto_child add column master_id bigint;
alter table table_history modify textfield varchar(255);
alter table table_history add column `select` varchar(255);
alter table table_history add column textfield2 varchar(255);
-- apply post alter
alter table migtest_e_basic add constraint uq_migtest_e_basic_description unique  (description);
update migtest_e_basic set new_boolean_field = old_boolean;

alter table migtest_e_basic add constraint uq_migtest_e_basic_status_indextest1 unique  (status,indextest1);
alter table migtest_e_basic add constraint uq_migtest_e_basic_name unique  (name);
alter table migtest_e_basic add constraint uq_migtest_e_basic_indextest4 unique  (indextest4);
alter table migtest_e_basic add constraint uq_migtest_e_basic_indextest5 unique  (indextest5);
create table migtest_e_history_history(
  id                            integer,
  test_string                   bigint,
  sys_period_start              datetime(6),
  sys_period_end                datetime(6)
);
create view migtest_e_history_with_history as select * from migtest_e_history union all select * from migtest_e_history_history;
lock tables migtest_e_history write;
delimiter $$
create trigger migtest_e_history_history_upd before update on migtest_e_history for each row begin
    insert into migtest_e_history_history (sys_period_start,sys_period_end,id, test_string) values (OLD.sys_period_start, now(6),OLD.id, OLD.test_string);
    set NEW.sys_period_start = now(6);
end$$
delimiter $$
create trigger migtest_e_history_history_del before delete on migtest_e_history for each row begin
    insert into migtest_e_history_history (sys_period_start,sys_period_end,id, test_string) values (OLD.sys_period_start, now(6),OLD.id, OLD.test_string);
end$$
unlock tables;

alter table migtest_e_history comment = 'We have history now';
create view migtest_e_history2_with_history as select * from migtest_e_history2 union all select * from migtest_e_history2_history;
lock tables migtest_e_history2 write;
delimiter $$
create trigger migtest_e_history2_history_upd before update on migtest_e_history2 for each row begin
    insert into migtest_e_history2_history (sys_period_start,sys_period_end,id, test_string, test_string3, new_column, obsolete_string1, obsolete_string2) values (OLD.sys_period_start, now(6),OLD.id, OLD.test_string, OLD.test_string3, OLD.new_column, OLD.obsolete_string1, OLD.obsolete_string2);
    set NEW.sys_period_start = now(6);
end$$
delimiter $$
create trigger migtest_e_history2_history_del before delete on migtest_e_history2 for each row begin
    insert into migtest_e_history2_history (sys_period_start,sys_period_end,id, test_string, test_string3, new_column, obsolete_string1, obsolete_string2) values (OLD.sys_period_start, now(6),OLD.id, OLD.test_string, OLD.test_string3, OLD.new_column, OLD.obsolete_string1, OLD.obsolete_string2);
end$$
unlock tables;
create view migtest_e_history3_with_history as select * from migtest_e_history3 union all select * from migtest_e_history3_history;
lock tables migtest_e_history3 write;
delimiter $$
create trigger migtest_e_history3_history_upd before update on migtest_e_history3 for each row begin
    insert into migtest_e_history3_history (sys_period_start,sys_period_end,id) values (OLD.sys_period_start, now(6),OLD.id);
    set NEW.sys_period_start = now(6);
end$$
delimiter $$
create trigger migtest_e_history3_history_del before delete on migtest_e_history3 for each row begin
    insert into migtest_e_history3_history (sys_period_start,sys_period_end,id) values (OLD.sys_period_start, now(6),OLD.id);
end$$
unlock tables;
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
create view migtest_e_history5_with_history as select * from migtest_e_history5 union all select * from migtest_e_history5_history;
lock tables migtest_e_history5 write;
delimiter $$
create trigger migtest_e_history5_history_upd before update on migtest_e_history5 for each row begin
    insert into migtest_e_history5_history (sys_period_start,sys_period_end,id, test_number, test_boolean) values (OLD.sys_period_start, now(6),OLD.id, OLD.test_number, OLD.test_boolean);
    set NEW.sys_period_start = now(6);
end$$
delimiter $$
create trigger migtest_e_history5_history_del before delete on migtest_e_history5 for each row begin
    insert into migtest_e_history5_history (sys_period_start,sys_period_end,id, test_number, test_boolean) values (OLD.sys_period_start, now(6),OLD.id, OLD.test_number, OLD.test_boolean);
end$$
unlock tables;
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
create view table_with_history as select * from `table` union all select * from table_history;
lock tables `table` write;
delimiter $$
create trigger table_history_upd before update on `table` for each row begin
    insert into table_history (sys_period_start,sys_period_end,`index`, `from`, `to`, `varchar`, `select`, `foreign`, textfield, textfield2) values (OLD.sys_period_start, now(6),OLD.`index`, OLD.`from`, OLD.`to`, OLD.`varchar`, OLD.`select`, OLD.`foreign`, OLD.textfield, OLD.textfield2);
    set NEW.sys_period_start = now(6);
end$$
delimiter $$
create trigger table_history_del before delete on `table` for each row begin
    insert into table_history (sys_period_start,sys_period_end,`index`, `from`, `to`, `varchar`, `select`, `foreign`, textfield, textfield2) values (OLD.sys_period_start, now(6),OLD.`index`, OLD.`from`, OLD.`to`, OLD.`varchar`, OLD.`select`, OLD.`foreign`, OLD.textfield, OLD.textfield2);
end$$
unlock tables;
alter table `table` add constraint uq_table_select unique  (`select`);
-- foreign keys and indices
create index ix_drop_main_drop_ref_many_drop_main on drop_main_drop_ref_many (drop_main_id);
alter table drop_main_drop_ref_many add constraint fk_drop_main_drop_ref_many_drop_main foreign key (drop_main_id) references drop_main (id) on delete restrict on update restrict;

create index ix_drop_main_drop_ref_many_drop_ref_many on drop_main_drop_ref_many (drop_ref_many_id);
alter table drop_main_drop_ref_many add constraint fk_drop_main_drop_ref_many_drop_ref_many foreign key (drop_ref_many_id) references drop_ref_many (id) on delete restrict on update restrict;

create index ix_drop_ref_one_parent_id on drop_ref_one (parent_id);
alter table drop_ref_one add constraint fk_drop_ref_one_parent_id foreign key (parent_id) references drop_main (id) on delete restrict on update restrict;

alter table drop_ref_one_to_one add constraint fk_drop_ref_one_to_one_parent_id foreign key (parent_id) references drop_main (id) on delete restrict on update restrict;

create index ix_migtest_mtm_c_migtest_mtm_m_migtest_mtm_c on migtest_mtm_c_migtest_mtm_m (migtest_mtm_c_id);
alter table migtest_mtm_c_migtest_mtm_m add constraint fk_migtest_mtm_c_migtest_mtm_m_migtest_mtm_c foreign key (migtest_mtm_c_id) references migtest_mtm_c (id) on delete restrict on update restrict;

create index ix_migtest_mtm_c_migtest_mtm_m_migtest_mtm_m on migtest_mtm_c_migtest_mtm_m (migtest_mtm_m_id);
alter table migtest_mtm_c_migtest_mtm_m add constraint fk_migtest_mtm_c_migtest_mtm_m_migtest_mtm_m foreign key (migtest_mtm_m_id) references migtest_mtm_m (id) on delete restrict on update restrict;

create index ix_migtest_mtm_m_migtest_mtm_c_migtest_mtm_m on migtest_mtm_m_migtest_mtm_c (migtest_mtm_m_id);
alter table migtest_mtm_m_migtest_mtm_c add constraint fk_migtest_mtm_m_migtest_mtm_c_migtest_mtm_m foreign key (migtest_mtm_m_id) references migtest_mtm_m (id) on delete restrict on update restrict;

create index ix_migtest_mtm_m_migtest_mtm_c_migtest_mtm_c on migtest_mtm_m_migtest_mtm_c (migtest_mtm_c_id);
alter table migtest_mtm_m_migtest_mtm_c add constraint fk_migtest_mtm_m_migtest_mtm_c_migtest_mtm_c foreign key (migtest_mtm_c_id) references migtest_mtm_c (id) on delete restrict on update restrict;

create index ix_migtest_mtm_m_phone_numbers_migtest_mtm_m_id on migtest_mtm_m_phone_numbers (migtest_mtm_m_id);
alter table migtest_mtm_m_phone_numbers add constraint fk_migtest_mtm_m_phone_numbers_migtest_mtm_m_id foreign key (migtest_mtm_m_id) references migtest_mtm_m (id) on delete restrict on update restrict;

alter table migtest_ckey_detail add constraint fk_migtest_ckey_detail_parent foreign key (one_key,two_key) references migtest_ckey_parent (one_key,two_key) on delete restrict on update restrict;
create index ix_migtest_ckey_parent_assoc_id on migtest_ckey_parent (assoc_id);
alter table migtest_ckey_parent add constraint fk_migtest_ckey_parent_assoc_id foreign key (assoc_id) references migtest_ckey_assoc (id) on delete restrict on update restrict;

alter table migtest_fk_cascade add constraint fk_migtest_fk_cascade_one_id foreign key (one_id) references migtest_fk_cascade_one (id) on delete restrict on update restrict;
alter table migtest_fk_none add constraint fk_migtest_fk_none_one_id foreign key (one_id) references migtest_fk_one (id) on delete restrict on update restrict;
alter table migtest_fk_none_via_join add constraint fk_migtest_fk_none_via_join_one_id foreign key (one_id) references migtest_fk_one (id) on delete restrict on update restrict;
alter table migtest_fk_set_null add constraint fk_migtest_fk_set_null_one_id foreign key (one_id) references migtest_fk_one (id) on delete restrict on update restrict;
alter table migtest_e_basic add constraint fk_migtest_e_basic_user_id foreign key (user_id) references migtest_e_user (id) on delete restrict on update restrict;
alter table migtest_oto_child add constraint fk_migtest_oto_child_master_id foreign key (master_id) references migtest_oto_master (id) on delete restrict on update restrict;

create index ix_migtest_e_basic_indextest3 on migtest_e_basic (indextest3);
create index ix_migtest_e_basic_indextest6 on migtest_e_basic (indextest6);
create index ix_migtest_e_basic_indextest7 on migtest_e_basic (indextest7);
create index ix_table_textfield2 on `table` (textfield2);
