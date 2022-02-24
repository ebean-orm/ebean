-- Migrationscripts for ebean unittest
-- drop dependencies
drop view if exists migtest_e_history2_with_history;
drop view if exists migtest_e_history3_with_history;
drop view if exists migtest_e_history4_with_history;
drop view if exists migtest_e_history6_with_history;

-- apply changes
create table migtest_e_ref (
  id                            integer auto_increment not null,
  name                          varchar(127) not null,
  constraint uq_migtest_e_ref_name unique (name),
  constraint pk_migtest_e_ref primary key (id)
);

alter table migtest_ckey_detail drop foreign key fk_migtest_ckey_detail_parent;
alter table migtest_fk_cascade drop foreign key fk_migtest_fk_cascade_one_id;
alter table migtest_fk_cascade add constraint fk_migtest_fk_cascade_one_id foreign key (one_id) references migtest_fk_cascade_one (id) on delete cascade on update restrict;
alter table migtest_fk_none drop foreign key fk_migtest_fk_none_one_id;
alter table migtest_fk_none_via_join drop foreign key fk_migtest_fk_none_via_join_one_id;
alter table migtest_fk_set_null drop foreign key fk_migtest_fk_set_null_one_id;
alter table migtest_fk_set_null add constraint fk_migtest_fk_set_null_one_id foreign key (one_id) references migtest_fk_one (id) on delete set null on update restrict;
alter table migtest_e_basic modify status varchar(1);

update migtest_e_basic set status2 = 'N' where status2 is null;
alter table migtest_e_basic modify status2 varchar(1) not null default 'N';
alter table migtest_e_basic drop index uq_migtest_e_basic_description;

update migtest_e_basic set user_id = 23 where user_id is null;
alter table migtest_e_basic drop foreign key fk_migtest_e_basic_user_id;
alter table migtest_e_basic modify user_id integer not null default 23;
alter table migtest_e_basic add column description_file longblob;
alter table migtest_e_basic add column old_boolean tinyint(1) default 0 not null;
alter table migtest_e_basic add column old_boolean2 tinyint(1);
alter table migtest_e_basic add column eref_id integer;

alter table migtest_e_basic drop index uq_migtest_e_basic_status_indextest1;
alter table migtest_e_basic drop index uq_migtest_e_basic_name;
alter table migtest_e_basic drop index uq_migtest_e_basic_indextest4;
alter table migtest_e_basic drop index uq_migtest_e_basic_indextest5;
alter table migtest_e_basic add constraint uq_migtest_e_basic_indextest2 unique  (indextest2);
alter table migtest_e_basic add constraint uq_migtest_e_basic_indextest6 unique  (indextest6);
alter table migtest_e_history comment = '';
alter table migtest_e_history2 modify test_string varchar(255);
alter table migtest_e_history2_history modify test_string varchar(255);
alter table migtest_e_history2 add column obsolete_string1 varchar(255);
alter table migtest_e_history2 add column obsolete_string2 varchar(255);
alter table migtest_e_history2_history add column obsolete_string1 varchar(255);
alter table migtest_e_history2_history add column obsolete_string2 varchar(255);

alter table migtest_e_history4 modify test_number integer;
alter table migtest_e_history4_history modify test_number integer;
alter table migtest_e_history6 modify test_number1 integer;
alter table migtest_e_history6_history modify test_number1 integer;

-- NOTE: table has @History - special migration may be necessary
update migtest_e_history6 set test_number2 = 7 where test_number2 is null;
alter table migtest_e_history6 modify test_number2 integer not null default 7;
alter table migtest_oto_child add constraint uq_m12_otoc72 unique  (name);
alter table migtest_oto_master add constraint uq_migtest_oto_master_name unique  (name);
create index ix_migtest_e_basic_indextest1 on migtest_e_basic (indextest1);
create index ix_migtest_e_basic_indextest5 on migtest_e_basic (indextest5);
create index ix_m12_otoc72 on migtest_oto_child (name);
create index ix_migtest_oto_master_name on migtest_oto_master (name);
drop index ix_migtest_e_basic_indextest3 on migtest_e_basic;
drop index ix_migtest_e_basic_indextest6 on migtest_e_basic;
create index ix_migtest_e_basic_eref_id on migtest_e_basic (eref_id);
alter table migtest_e_basic add constraint fk_migtest_e_basic_eref_id foreign key (eref_id) references migtest_e_ref (id) on delete restrict on update restrict;

create view migtest_e_history2_with_history as select * from migtest_e_history2 union all select * from migtest_e_history2_history;

create view migtest_e_history3_with_history as select * from migtest_e_history3 union all select * from migtest_e_history3_history;

create view migtest_e_history4_with_history as select * from migtest_e_history4 union all select * from migtest_e_history4_history;

create view migtest_e_history6_with_history as select * from migtest_e_history6 union all select * from migtest_e_history6_history;

lock tables migtest_e_history2 write, migtest_e_history3 write, migtest_e_history4 write, migtest_e_history6 write;
-- changes: [alter test_string, add obsolete_string1, add obsolete_string2]
drop trigger migtest_e_history2_history_upd;
drop trigger migtest_e_history2_history_del;
delimiter $$
create trigger migtest_e_history2_history_upd before update on migtest_e_history2 for each row begin
    insert into migtest_e_history2_history (sys_period_start,sys_period_end,id, test_string, obsolete_string2, test_string2, test_string3, new_column) values (OLD.sys_period_start, now(6),OLD.id, OLD.test_string, OLD.obsolete_string2, OLD.test_string2, OLD.test_string3, OLD.new_column);
    set NEW.sys_period_start = now(6);
end$$
delimiter $$
create trigger migtest_e_history2_history_del before delete on migtest_e_history2 for each row begin
    insert into migtest_e_history2_history (sys_period_start,sys_period_end,id, test_string, obsolete_string2, test_string2, test_string3, new_column) values (OLD.sys_period_start, now(6),OLD.id, OLD.test_string, OLD.obsolete_string2, OLD.test_string2, OLD.test_string3, OLD.new_column);
end$$
-- changes: [include test_string]
drop trigger migtest_e_history3_history_upd;
drop trigger migtest_e_history3_history_del;
delimiter $$
create trigger migtest_e_history3_history_upd before update on migtest_e_history3 for each row begin
    insert into migtest_e_history3_history (sys_period_start,sys_period_end,id, test_string) values (OLD.sys_period_start, now(6),OLD.id, OLD.test_string);
    set NEW.sys_period_start = now(6);
end$$
delimiter $$
create trigger migtest_e_history3_history_del before delete on migtest_e_history3 for each row begin
    insert into migtest_e_history3_history (sys_period_start,sys_period_end,id, test_string) values (OLD.sys_period_start, now(6),OLD.id, OLD.test_string);
end$$
-- changes: [alter test_number]
drop trigger migtest_e_history4_history_upd;
drop trigger migtest_e_history4_history_del;
delimiter $$
create trigger migtest_e_history4_history_upd before update on migtest_e_history4 for each row begin
    insert into migtest_e_history4_history (sys_period_start,sys_period_end,id, test_number) values (OLD.sys_period_start, now(6),OLD.id, OLD.test_number);
    set NEW.sys_period_start = now(6);
end$$
delimiter $$
create trigger migtest_e_history4_history_del before delete on migtest_e_history4 for each row begin
    insert into migtest_e_history4_history (sys_period_start,sys_period_end,id, test_number) values (OLD.sys_period_start, now(6),OLD.id, OLD.test_number);
end$$
-- changes: [alter test_number1]
drop trigger migtest_e_history6_history_upd;
drop trigger migtest_e_history6_history_del;
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
