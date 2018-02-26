-- Migrationscripts for ebean unittest
-- apply changes
create table migtest_e_user (
  id                            integer auto_increment not null,
  constraint pk_migtest_e_user primary key (id)
);

alter table migtest_fk_cascade drop foreign key fk_migtest_fk_cascade_one_id;
alter table migtest_fk_cascade add constraint fk_migtest_fk_cascade_one_id foreign key (one_id) references migtest_fk_cascade_one (id) on delete restrict on update restrict;
alter table migtest_fk_none add constraint fk_migtest_fk_none_one_id foreign key (one_id) references migtest_fk_one (id) on delete restrict on update restrict;
alter table migtest_fk_none_via_join add constraint fk_migtest_fk_none_via_join_one_id foreign key (one_id) references migtest_fk_one (id) on delete restrict on update restrict;
alter table migtest_fk_set_null drop foreign key fk_migtest_fk_set_null_one_id;
alter table migtest_fk_set_null add constraint fk_migtest_fk_set_null_one_id foreign key (one_id) references migtest_fk_one (id) on delete restrict on update restrict;

update migtest_e_basic set status = 'A' where status is null;
alter table migtest_e_basic alter status set default 'A';
alter table migtest_e_basic modify status varchar(1) not null;
alter table migtest_e_basic add constraint ck_migtest_e_basic_status check ( status in ('N','A','I','?'));

-- rename all collisions;
alter table migtest_e_basic add constraint uq_migtest_e_basic_description unique  (description);

update migtest_e_basic set some_date = '2000-01-01T00:00:00' where some_date is null;
alter table migtest_e_basic alter some_date set default '2000-01-01T00:00:00';
alter table migtest_e_basic modify some_date datetime(6) not null;

insert into migtest_e_user (id) select distinct user_id from migtest_e_basic;
alter table migtest_e_basic add constraint fk_migtest_e_basic_user_id foreign key (user_id) references migtest_e_user (id) on delete restrict on update restrict;
alter table migtest_e_basic modify user_id integer;
alter table migtest_e_basic add column new_string_field varchar(255) default 'foo''bar' not null;
alter table migtest_e_basic add column new_boolean_field tinyint(1) default 1 not null;
update migtest_e_basic set new_boolean_field = old_boolean;

alter table migtest_e_basic add column new_boolean_field2 tinyint(1) default 1 not null;
alter table migtest_e_basic add column progress integer default 0 not null;
alter table migtest_e_basic add constraint ck_migtest_e_basic_progress check ( progress in (0,1,2));
alter table migtest_e_basic add column new_integer integer default 42 not null;

alter table migtest_e_basic drop index uq_migtest_e_basic_indextest2;
alter table migtest_e_basic drop index uq_migtest_e_basic_indextest6;
alter table migtest_e_basic add constraint uq_migtest_e_basic_name unique  (name);
alter table migtest_e_basic add constraint uq_migtest_e_basic_indextest4 unique  (indextest4);
alter table migtest_e_basic add constraint uq_migtest_e_basic_indextest5 unique  (indextest5);
alter table migtest_e_history modify test_string bigint;
alter table migtest_e_history comment = 'We have history now';

update migtest_e_history2 set test_string = 'unknown' where test_string is null;
alter table migtest_e_history2 alter test_string set default 'unknown';
alter table migtest_e_history2 modify test_string varchar(255) not null;
alter table migtest_e_history2 add column test_string2 varchar(255);
alter table migtest_e_history2 add column test_string3 varchar(255) default 'unknown' not null;
alter table migtest_e_history2_history add column test_string2 varchar(255);
alter table migtest_e_history2_history add column test_string3 varchar(255);

alter table migtest_e_softdelete add column deleted tinyint(1) default 0 not null;

create index ix_migtest_e_basic_indextest3 on migtest_e_basic (indextest3);
create index ix_migtest_e_basic_indextest6 on migtest_e_basic (indextest6);
drop index ix_migtest_e_basic_indextest1 on migtest_e_basic;
drop index ix_migtest_e_basic_indextest5 on migtest_e_basic;
alter table migtest_e_history add column sys_period_start datetime(6) default now(6);
alter table migtest_e_history add column sys_period_end datetime(6);
create table migtest_e_history_history(
  id                            integer,
  test_string                   bigint,
  sys_period_start              datetime(6),
  sys_period_end                datetime(6)
);
create view migtest_e_history_with_history as select * from migtest_e_history union all select * from migtest_e_history_history;

delimiter $$
create trigger migtest_e_history_history_upd before update on migtest_e_history for each row begin
    insert into migtest_e_history_history (sys_period_start,sys_period_end,id, test_string) values (OLD.sys_period_start, now(6),OLD.id, OLD.test_string);
    set NEW.sys_period_start = now(6);
end$$
delimiter $$
create trigger migtest_e_history_history_del before delete on migtest_e_history for each row begin
    insert into migtest_e_history_history (sys_period_start,sys_period_end,id, test_string) values (OLD.sys_period_start, now(6),OLD.id, OLD.test_string);
end$$
-- changes: [add test_string2, add test_string3]
lock tables migtest_e_history2 write;
drop trigger migtest_e_history2_history_upd;
drop trigger migtest_e_history2_history_del;
delimiter $$
create trigger migtest_e_history2_history_upd before update on migtest_e_history2 for each row begin
    insert into migtest_e_history2_history (sys_period_start,sys_period_end,id, test_string, test_string2, test_string3) values (OLD.sys_period_start, now(6),OLD.id, OLD.test_string, OLD.test_string2, OLD.test_string3);
    set NEW.sys_period_start = now(6);
end$$
delimiter $$
create trigger migtest_e_history2_history_del before delete on migtest_e_history2 for each row begin
    insert into migtest_e_history2_history (sys_period_start,sys_period_end,id, test_string, test_string2, test_string3) values (OLD.sys_period_start, now(6),OLD.id, OLD.test_string, OLD.test_string2, OLD.test_string3);
end$$
unlock tables;
