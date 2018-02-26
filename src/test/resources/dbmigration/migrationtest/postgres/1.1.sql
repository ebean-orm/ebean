-- Migrationscripts for ebean unittest
-- drop dependencies
drop view if exists migtest_e_history2_with_history;

-- apply changes
create table migtest_e_user (
  id                            serial not null,
  constraint pk_migtest_e_user primary key (id)
);

alter table if exists migtest_fk_cascade drop constraint if exists fk_migtest_fk_cascade_one_id;
alter table migtest_fk_cascade add constraint fk_migtest_fk_cascade_one_id foreign key (one_id) references migtest_fk_cascade_one (id) on delete restrict on update restrict;
alter table migtest_fk_none add constraint fk_migtest_fk_none_one_id foreign key (one_id) references migtest_fk_one (id) on delete restrict on update restrict;
alter table migtest_fk_none_via_join add constraint fk_migtest_fk_none_via_join_one_id foreign key (one_id) references migtest_fk_one (id) on delete restrict on update restrict;
alter table if exists migtest_fk_set_null drop constraint if exists fk_migtest_fk_set_null_one_id;
alter table migtest_fk_set_null add constraint fk_migtest_fk_set_null_one_id foreign key (one_id) references migtest_fk_one (id) on delete restrict on update restrict;

update migtest_e_basic set status = 'A' where status is null;
alter table migtest_e_basic drop constraint ck_migtest_e_basic_status;
alter table migtest_e_basic alter column status set default 'A';
alter table migtest_e_basic alter column status set not null;
alter table migtest_e_basic add constraint ck_migtest_e_basic_status check ( status in ('N','A','I','?'));

-- rename all collisions;
alter table migtest_e_basic add constraint uq_migtest_e_basic_description unique  (description);

update migtest_e_basic set some_date = '2000-01-01T00:00:00' where some_date is null;
alter table migtest_e_basic alter column some_date set default '2000-01-01T00:00:00';
alter table migtest_e_basic alter column some_date set not null;

insert into migtest_e_user (id) select distinct user_id from migtest_e_basic;
alter table migtest_e_basic add constraint fk_migtest_e_basic_user_id foreign key (user_id) references migtest_e_user (id) on delete restrict on update restrict;
alter table migtest_e_basic alter column user_id drop not null;
alter table migtest_e_basic add column new_string_field varchar(255) default 'foo''bar' not null;
alter table migtest_e_basic add column new_boolean_field boolean default true not null;
update migtest_e_basic set new_boolean_field = old_boolean;

alter table migtest_e_basic add column new_boolean_field2 boolean default true not null;
alter table migtest_e_basic add column progress integer default 0 not null;
alter table migtest_e_basic add constraint ck_migtest_e_basic_progress check ( progress in (0,1,2));
alter table migtest_e_basic add column new_integer integer default 42 not null;

alter table migtest_e_basic drop constraint uq_migtest_e_basic_indextest2;
alter table migtest_e_basic drop constraint uq_migtest_e_basic_indextest6;
alter table migtest_e_basic add constraint uq_migtest_e_basic_name unique  (name);
alter table migtest_e_basic add constraint uq_migtest_e_basic_indextest4 unique  (indextest4);
alter table migtest_e_basic add constraint uq_migtest_e_basic_indextest5 unique  (indextest5);

alter table migtest_e_history alter column test_string TYPE bigint USING (test_string::integer);
comment on column migtest_e_history.test_string is 'Column altered to long now';
alter table migtest_e_history alter column test_string type bigint;
comment on table migtest_e_history is 'We have history now';

update migtest_e_history2 set test_string = 'unknown' where test_string is null;
alter table migtest_e_history2 alter column test_string set default 'unknown';
alter table migtest_e_history2 alter column test_string set not null;
alter table migtest_e_history2 add column test_string2 varchar(255);
alter table migtest_e_history2 add column test_string3 varchar(255) default 'unknown' not null;
alter table migtest_e_history2_history add column test_string2 varchar(255);
alter table migtest_e_history2_history add column test_string3 varchar(255);

alter table migtest_e_softdelete add column deleted boolean default false not null;

create index ix_migtest_e_basic_indextest3 on migtest_e_basic (indextest3);
create index ix_migtest_e_basic_indextest6 on migtest_e_basic (indextest6);
drop index if exists ix_migtest_e_basic_indextest1;
drop index if exists ix_migtest_e_basic_indextest5;
alter table migtest_e_history add column sys_period tstzrange not null default tstzrange(current_timestamp, null);
create table migtest_e_history_history(like migtest_e_history);
create view migtest_e_history_with_history as select * from migtest_e_history union all select * from migtest_e_history_history;

create or replace function migtest_e_history_history_version() returns trigger as $$
begin
  if (TG_OP = 'UPDATE') then
    insert into migtest_e_history_history (sys_period,id, test_string) values (tstzrange(lower(OLD.sys_period), current_timestamp), OLD.id, OLD.test_string);
    NEW.sys_period = tstzrange(current_timestamp,null);
    return new;
  elsif (TG_OP = 'DELETE') then
    insert into migtest_e_history_history (sys_period,id, test_string) values (tstzrange(lower(OLD.sys_period), current_timestamp), OLD.id, OLD.test_string);
    return old;
  end if;
end;
$$ LANGUAGE plpgsql;

create trigger migtest_e_history_history_upd
  before update or delete on migtest_e_history
  for each row execute procedure migtest_e_history_history_version();

-- changes: [add test_string2, add test_string3]
create or replace view migtest_e_history2_with_history as select id, test_string, test_string2, test_string3, sys_period from migtest_e_history2 union all select id, test_string, test_string2, test_string3, sys_period from migtest_e_history2_history;

create or replace function migtest_e_history2_history_version() returns trigger as $$
begin
  if (TG_OP = 'UPDATE') then
    insert into migtest_e_history2_history (sys_period,id, test_string, test_string2, test_string3) values (tstzrange(lower(OLD.sys_period), current_timestamp), OLD.id, OLD.test_string, OLD.test_string2, OLD.test_string3);
    NEW.sys_period = tstzrange(current_timestamp,null);
    return new;
  elsif (TG_OP = 'DELETE') then
    insert into migtest_e_history2_history (sys_period,id, test_string, test_string2, test_string3) values (tstzrange(lower(OLD.sys_period), current_timestamp), OLD.id, OLD.test_string, OLD.test_string2, OLD.test_string3);
    return old;
  end if;
end;
$$ LANGUAGE plpgsql;

