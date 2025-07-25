-- Migrationscripts for ebean unittest
-- drop dependencies
alter table migtest_ckey_detail drop foreign key fk_migtest_ckey_detail_parent;
alter table migtest_fk_cascade drop foreign key fk_migtest_fk_cascade_one_id;
alter table migtest_fk_none drop foreign key fk_migtest_fk_none_one_id;
alter table migtest_fk_none_via_join drop foreign key fk_migtest_fk_none_via_join_one_id;
alter table migtest_fk_set_null drop foreign key fk_migtest_fk_set_null_one_id;
alter table migtest_e_basic drop index uq_migtest_e_basic_description;
alter table migtest_e_basic drop foreign key fk_migtest_e_basic_user_id;
alter table migtest_e_basic drop index uq_migtest_e_basic_status_indextest1;
alter table migtest_e_basic drop index uq_migtest_e_basic_name;
alter table migtest_e_basic drop index uq_migtest_e_basic_indextest4;
alter table migtest_e_basic drop index uq_migtest_e_basic_indextest5;
alter table drop_main_drop_ref_many drop foreign key fk_drop_main_drop_ref_many_drop_main;
alter table drop_main_drop_ref_many drop foreign key fk_drop_main_drop_ref_many_drop_ref_many;
alter table drop_ref_one drop foreign key fk_drop_ref_one_parent_id;
alter table drop_ref_one_to_one drop foreign key fk_drop_ref_one_to_one_parent_id;
alter table migtest_mtm_c_migtest_mtm_m drop foreign key fk_migtest_mtm_c_migtest_mtm_m_migtest_mtm_c;
alter table migtest_mtm_c_migtest_mtm_m drop foreign key fk_migtest_mtm_c_migtest_mtm_m_migtest_mtm_m;
alter table migtest_mtm_m_migtest_mtm_c drop foreign key fk_migtest_mtm_m_migtest_mtm_c_migtest_mtm_m;
alter table migtest_mtm_m_migtest_mtm_c drop foreign key fk_migtest_mtm_m_migtest_mtm_c_migtest_mtm_c;
alter table migtest_mtm_m_phone_numbers drop foreign key fk_migtest_mtm_m_phone_numbers_migtest_mtm_m_id;
drop index ix_migtest_e_basic_indextest3 on migtest_e_basic;
drop index ix_migtest_e_basic_indextest6 on migtest_e_basic;
drop index ix_migtest_e_basic_indextest7 on migtest_e_basic;
drop index ix_table_textfield2 on `table`;
-- apply changes
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


update migtest_e_basic set status2 = 'N' where status2 is null;

update migtest_e_basic set a_lob = 'X' where a_lob is null;

update migtest_e_basic set user_id = 23 where user_id is null;
drop trigger migtest_e_history2_history_upd;
drop trigger migtest_e_history2_history_del;
drop view migtest_e_history2_with_history;
drop trigger migtest_e_history3_history_upd;
drop trigger migtest_e_history3_history_del;
drop view migtest_e_history3_with_history;
drop trigger migtest_e_history4_history_upd;
drop trigger migtest_e_history4_history_del;
drop view migtest_e_history4_with_history;
drop trigger migtest_e_history6_history_upd;
drop trigger migtest_e_history6_history_del;
drop view migtest_e_history6_with_history;

-- NOTE: table has @History - special migration may be necessary
update migtest_e_history6 set test_number2 = 7 where test_number2 is null;
-- apply alter tables
alter table migtest_e_basic modify status varchar(1);
alter table migtest_e_basic modify status2 varchar(1) not null default 'N';
alter table migtest_e_basic modify a_lob varchar(255) not null default 'X';
alter table migtest_e_basic modify user_id integer not null default 23;
alter table migtest_e_basic add column description_file longblob;
alter table migtest_e_basic add column old_boolean tinyint(1) default 0 not null;
alter table migtest_e_basic add column old_boolean2 tinyint(1);
alter table migtest_e_basic add column eref_id integer;
alter table migtest_e_history2 modify test_string varchar(255);
alter table migtest_e_history2 add column obsolete_string1 varchar(255);
alter table migtest_e_history2 add column obsolete_string2 varchar(255);
alter table migtest_e_history2_history modify test_string varchar(255);
alter table migtest_e_history2_history add column obsolete_string1 varchar(255);
alter table migtest_e_history2_history add column obsolete_string2 varchar(255);
alter table migtest_e_history4 modify test_number integer;
alter table migtest_e_history4_history modify test_number integer;
alter table migtest_e_history6 modify test_number1 integer;
alter table migtest_e_history6 modify test_number2 integer not null default 7;
alter table migtest_e_history6_history modify test_number1 integer;
-- apply post alter
alter table migtest_e_basic add constraint uq_migtest_e_basic_indextest2 unique  (indextest2);
alter table migtest_e_basic add constraint uq_migtest_e_basic_indextest6 unique  (indextest6);
alter table migtest_e_basic add constraint uq_migtest_e_basic_indextest7 unique  (indextest7);
alter table migtest_e_history comment = '';
create view migtest_e_history2_with_history as select * from migtest_e_history2 union all select * from migtest_e_history2_history;
lock tables migtest_e_history2 write;
delimiter $$
create trigger migtest_e_history2_history_upd before update on migtest_e_history2 for each row begin
    insert into migtest_e_history2_history (sys_period_start,sys_period_end,id, test_string, obsolete_string2, test_string2, test_string3, new_column) values (OLD.sys_period_start, now(6),OLD.id, OLD.test_string, OLD.obsolete_string2, OLD.test_string2, OLD.test_string3, OLD.new_column);
    set NEW.sys_period_start = now(6);
end$$
delimiter $$
create trigger migtest_e_history2_history_del before delete on migtest_e_history2 for each row begin
    insert into migtest_e_history2_history (sys_period_start,sys_period_end,id, test_string, obsolete_string2, test_string2, test_string3, new_column) values (OLD.sys_period_start, now(6),OLD.id, OLD.test_string, OLD.obsolete_string2, OLD.test_string2, OLD.test_string3, OLD.new_column);
end$$
unlock tables;
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
alter table migtest_oto_child add constraint uq_m12_otoc72 unique  (name);
alter table migtest_oto_master add constraint uq_migtest_oto_master_name unique  (name);
-- foreign keys and indices
alter table migtest_fk_cascade add constraint fk_migtest_fk_cascade_one_id foreign key (one_id) references migtest_fk_cascade_one (id) on delete cascade on update restrict;
alter table migtest_fk_set_null add constraint fk_migtest_fk_set_null_one_id foreign key (one_id) references migtest_fk_one (id) on delete set null on update restrict;
create index ix_migtest_e_basic_eref_id on migtest_e_basic (eref_id);
alter table migtest_e_basic add constraint fk_migtest_e_basic_eref_id foreign key (eref_id) references migtest_e_ref (id) on delete restrict on update restrict;

create index ix_migtest_e_basic_indextest1 on migtest_e_basic (indextest1);
create index ix_migtest_e_basic_indextest5 on migtest_e_basic (indextest5);
create index ix_migtest_quoted_status1 on `migtest_QuOtEd` (status1);
create index ix_m12_otoc72 on migtest_oto_child (name);
create index ix_migtest_oto_master_name on migtest_oto_master (name);
