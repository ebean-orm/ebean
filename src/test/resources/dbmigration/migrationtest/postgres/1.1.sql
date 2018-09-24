-- Migrationscripts for ebean unittest
-- drop dependencies
drop view if exists migtest_e_history2_with_history;
drop view if exists migtest_e_history3_with_history;
drop view if exists migtest_e_history4_with_history;
drop view if exists migtest_e_history5_with_history;

-- apply changes
create table migtest_e_user (
  id                            serial not null,
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

alter table migtest_ckey_detail add column one_key integer;
alter table migtest_ckey_detail add column two_key varchar(127);

alter table migtest_ckey_detail add constraint fk_migtest_ckey_detail_parent foreign key (one_key,two_key) references migtest_ckey_parent (one_key,two_key) on delete restrict on update restrict;
alter table migtest_ckey_parent add column assoc_id integer;

alter table if exists migtest_fk_cascade drop constraint if exists fk_migtest_fk_cascade_one_id;
alter table migtest_fk_cascade add constraint fk_migtest_fk_cascade_one_id foreign key (one_id) references migtest_fk_cascade_one (id) on delete restrict on update restrict;
alter table migtest_fk_none add constraint fk_migtest_fk_none_one_id foreign key (one_id) references migtest_fk_one (id) on delete restrict on update restrict;
alter table migtest_fk_none_via_join add constraint fk_migtest_fk_none_via_join_one_id foreign key (one_id) references migtest_fk_one (id) on delete restrict on update restrict;
alter table if exists migtest_fk_set_null drop constraint if exists fk_migtest_fk_set_null_one_id;
alter table migtest_fk_set_null add constraint fk_migtest_fk_set_null_one_id foreign key (one_id) references migtest_fk_one (id) on delete restrict on update restrict;

update migtest_e_basic set status = 'A' where status is null;
alter table migtest_e_basic drop constraint if exists ck_migtest_e_basic_status;
alter table migtest_e_basic alter column status set default 'A';
alter table migtest_e_basic alter column status set not null;
alter table migtest_e_basic add constraint ck_migtest_e_basic_status check ( status in ('N','A','I','?'));

-- rename all collisions;
alter table migtest_e_basic add constraint uq_migtest_e_basic_description unique  (description);

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
alter table migtest_e_basic add constraint uq_migtest_e_basic_status_indextest1 unique  (status,indextest1);
alter table migtest_e_basic add constraint uq_migtest_e_basic_name unique  (name);
alter table migtest_e_basic add constraint uq_migtest_e_basic_indextest4 unique  (indextest4);
alter table migtest_e_basic add constraint uq_migtest_e_basic_indextest5 unique  (indextest5);
alter table migtest_e_enum drop constraint if exists ck_migtest_e_enum_test_status;

alter table migtest_e_history alter column test_string TYPE bigint USING (test_string::integer);
comment on column migtest_e_history.test_string is 'Column altered to long now';
alter table migtest_e_history alter column test_string type bigint;
comment on table migtest_e_history is 'We have history now';

-- NOTE: table has @History - special migration may be necessary
update migtest_e_history2 set test_string = 'unknown' where test_string is null;
alter table migtest_e_history2 alter column test_string set default 'unknown';
alter table migtest_e_history2 alter column test_string set not null;
alter table migtest_e_history2 add column test_string2 varchar(255);
alter table migtest_e_history2 add column test_string3 varchar(255) default 'unknown' not null;
alter table migtest_e_history2 add column new_column varchar(20);
alter table migtest_e_history2_history add column test_string2 varchar(255);
alter table migtest_e_history2_history add column test_string3 varchar(255) default 'unknown';
alter table migtest_e_history2_history add column new_column varchar(20);

alter table migtest_e_history4 alter column test_number type bigint;
alter table migtest_e_history4_history alter column test_number type bigint;
alter table migtest_e_history5 add column test_boolean boolean default false not null;
alter table migtest_e_history5_history add column test_boolean boolean default false;


-- NOTE: table has @History - special migration may be necessary
update migtest_e_history6 set test_number1 = 42 where test_number1 is null;
alter table migtest_e_history6 alter column test_number1 set default 42;
alter table migtest_e_history6 alter column test_number1 set not null;
alter table migtest_e_history6 alter column test_number2 drop not null;
alter table migtest_e_softdelete add column deleted boolean default false not null;

alter table migtest_oto_child add column master_id bigint;

create index ix_migtest_e_basic_indextest3 on migtest_e_basic (indextest3);
create index ix_migtest_e_basic_indextest6 on migtest_e_basic (indextest6);
drop index if exists ix_migtest_e_basic_indextest1;
drop index if exists ix_migtest_e_basic_indextest5;
create index ix_migtest_mtm_c_migtest_mtm_m_migtest_mtm_c on migtest_mtm_c_migtest_mtm_m (migtest_mtm_c_id);
alter table migtest_mtm_c_migtest_mtm_m add constraint fk_migtest_mtm_c_migtest_mtm_m_migtest_mtm_c foreign key (migtest_mtm_c_id) references migtest_mtm_c (id) on delete restrict on update restrict;

create index ix_migtest_mtm_c_migtest_mtm_m_migtest_mtm_m on migtest_mtm_c_migtest_mtm_m (migtest_mtm_m_id);
alter table migtest_mtm_c_migtest_mtm_m add constraint fk_migtest_mtm_c_migtest_mtm_m_migtest_mtm_m foreign key (migtest_mtm_m_id) references migtest_mtm_m (id) on delete restrict on update restrict;

create index ix_migtest_mtm_m_migtest_mtm_c_migtest_mtm_m on migtest_mtm_m_migtest_mtm_c (migtest_mtm_m_id);
alter table migtest_mtm_m_migtest_mtm_c add constraint fk_migtest_mtm_m_migtest_mtm_c_migtest_mtm_m foreign key (migtest_mtm_m_id) references migtest_mtm_m (id) on delete restrict on update restrict;

create index ix_migtest_mtm_m_migtest_mtm_c_migtest_mtm_c on migtest_mtm_m_migtest_mtm_c (migtest_mtm_c_id);
alter table migtest_mtm_m_migtest_mtm_c add constraint fk_migtest_mtm_m_migtest_mtm_c_migtest_mtm_c foreign key (migtest_mtm_c_id) references migtest_mtm_c (id) on delete restrict on update restrict;

create index ix_migtest_ckey_parent_assoc_id on migtest_ckey_parent (assoc_id);
alter table migtest_ckey_parent add constraint fk_migtest_ckey_parent_assoc_id foreign key (assoc_id) references migtest_ckey_assoc (id) on delete restrict on update restrict;

alter table migtest_oto_child add constraint fk_migtest_oto_child_master_id foreign key (master_id) references migtest_oto_master (id) on delete restrict on update restrict;

alter table migtest_e_history add column sys_period tstzrange not null default tstzrange(current_timestamp, null);
create table migtest_e_history_history(like migtest_e_history);
create view migtest_e_history_with_history as select * from migtest_e_history union all select * from migtest_e_history_history;

create view migtest_e_history2_with_history as select * from migtest_e_history2 union all select * from migtest_e_history2_history;

create view migtest_e_history3_with_history as select * from migtest_e_history3 union all select * from migtest_e_history3_history;

create view migtest_e_history4_with_history as select * from migtest_e_history4 union all select * from migtest_e_history4_history;

create view migtest_e_history5_with_history as select * from migtest_e_history5 union all select * from migtest_e_history5_history;

create or replace function migtest_e_history_history_version() returns trigger as $$
declare
  lowerTs timestamptz;
  upperTs timestamptz;
begin
  lowerTs = lower(OLD.sys_period);
  upperTs = greatest(lowerTs + '1 microsecond',current_timestamp);
  if (TG_OP = 'UPDATE') then
    insert into migtest_e_history_history (sys_period,id, test_string) values (tstzrange(lowerTs,upperTs), OLD.id, OLD.test_string);
    NEW.sys_period = tstzrange(upperTs,null);
    return new;
  elsif (TG_OP = 'DELETE') then
    insert into migtest_e_history_history (sys_period,id, test_string) values (tstzrange(lowerTs,upperTs), OLD.id, OLD.test_string);
    return old;
  end if;
end;
$$ LANGUAGE plpgsql;

create trigger migtest_e_history_history_upd
  before update or delete on migtest_e_history
  for each row execute procedure migtest_e_history_history_version();

-- changes: [add test_string2, add test_string3, add new_column]
create or replace function migtest_e_history2_history_version() returns trigger as $$
declare
  lowerTs timestamptz;
  upperTs timestamptz;
begin
  lowerTs = lower(OLD.sys_period);
  upperTs = greatest(lowerTs + '1 microsecond',current_timestamp);
  if (TG_OP = 'UPDATE') then
    insert into migtest_e_history2_history (sys_period,id, test_string, test_string3, new_column, obsolete_string1, obsolete_string2) values (tstzrange(lowerTs,upperTs), OLD.id, OLD.test_string, OLD.test_string3, OLD.new_column, OLD.obsolete_string1, OLD.obsolete_string2);
    NEW.sys_period = tstzrange(upperTs,null);
    return new;
  elsif (TG_OP = 'DELETE') then
    insert into migtest_e_history2_history (sys_period,id, test_string, test_string3, new_column, obsolete_string1, obsolete_string2) values (tstzrange(lowerTs,upperTs), OLD.id, OLD.test_string, OLD.test_string3, OLD.new_column, OLD.obsolete_string1, OLD.obsolete_string2);
    return old;
  end if;
end;
$$ LANGUAGE plpgsql;

-- changes: [exclude test_string]
create or replace function migtest_e_history3_history_version() returns trigger as $$
declare
  lowerTs timestamptz;
  upperTs timestamptz;
begin
  lowerTs = lower(OLD.sys_period);
  upperTs = greatest(lowerTs + '1 microsecond',current_timestamp);
  if (TG_OP = 'UPDATE') then
    insert into migtest_e_history3_history (sys_period,id) values (tstzrange(lowerTs,upperTs), OLD.id);
    NEW.sys_period = tstzrange(upperTs,null);
    return new;
  elsif (TG_OP = 'DELETE') then
    insert into migtest_e_history3_history (sys_period,id) values (tstzrange(lowerTs,upperTs), OLD.id);
    return old;
  end if;
end;
$$ LANGUAGE plpgsql;

-- changes: [alter test_number]
create or replace function migtest_e_history4_history_version() returns trigger as $$
declare
  lowerTs timestamptz;
  upperTs timestamptz;
begin
  lowerTs = lower(OLD.sys_period);
  upperTs = greatest(lowerTs + '1 microsecond',current_timestamp);
  if (TG_OP = 'UPDATE') then
    insert into migtest_e_history4_history (sys_period,id, test_number) values (tstzrange(lowerTs,upperTs), OLD.id, OLD.test_number);
    NEW.sys_period = tstzrange(upperTs,null);
    return new;
  elsif (TG_OP = 'DELETE') then
    insert into migtest_e_history4_history (sys_period,id, test_number) values (tstzrange(lowerTs,upperTs), OLD.id, OLD.test_number);
    return old;
  end if;
end;
$$ LANGUAGE plpgsql;

-- changes: [add test_boolean]
create or replace function migtest_e_history5_history_version() returns trigger as $$
declare
  lowerTs timestamptz;
  upperTs timestamptz;
begin
  lowerTs = lower(OLD.sys_period);
  upperTs = greatest(lowerTs + '1 microsecond',current_timestamp);
  if (TG_OP = 'UPDATE') then
    insert into migtest_e_history5_history (sys_period,id, test_number, test_boolean) values (tstzrange(lowerTs,upperTs), OLD.id, OLD.test_number, OLD.test_boolean);
    NEW.sys_period = tstzrange(upperTs,null);
    return new;
  elsif (TG_OP = 'DELETE') then
    insert into migtest_e_history5_history (sys_period,id, test_number, test_boolean) values (tstzrange(lowerTs,upperTs), OLD.id, OLD.test_number, OLD.test_boolean);
    return old;
  end if;
end;
$$ LANGUAGE plpgsql;

