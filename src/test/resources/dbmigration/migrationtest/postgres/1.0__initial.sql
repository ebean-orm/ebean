-- apply changes
-- Migrationscript for postgres;
-- identity type: IDENTITY;

create table migtest_ckey_assoc (
  id                            serial not null,
  assoc_one                     varchar(255),
  constraint pk_migtest_ckey_assoc primary key (id)
);

create table migtest_ckey_detail (
  id                            serial not null,
  something                     varchar(255),
  constraint pk_migtest_ckey_detail primary key (id)
);

create table migtest_ckey_parent (
  one_key                       integer not null,
  two_key                       varchar(255) not null,
  name                          varchar(255),
  version                       integer not null,
  constraint pk_migtest_ckey_parent primary key (one_key,two_key)
);

create table migtest_e_basic (
  id                            serial not null,
  status                        varchar(1),
  name                          varchar(255),
  description                   varchar(255),
  some_date                     timestamptz,
  old_boolean                   boolean default false not null,
  old_boolean2                  boolean,
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
  id                            serial not null,
  test_string                   varchar(255),
  constraint pk_migtest_e_history primary key (id)
);

create table migtest_e_history2 (
  id                            serial not null,
  test_string                   varchar(255),
  constraint pk_migtest_e_history2 primary key (id)
);

create table migtest_e_ref (
  id                            serial not null,
  name                          varchar(255) not null,
  constraint uq_migtest_e_ref_name unique (name),
  constraint pk_migtest_e_ref primary key (id)
);

create table migtest_e_softdelete (
  id                            serial not null,
  test_string                   varchar(255),
  constraint pk_migtest_e_softdelete primary key (id)
);

create table migtest_mtm_c (
  id                            serial not null,
  name                          varchar(255),
  constraint pk_migtest_mtm_c primary key (id)
);

create table migtest_mtm_m (
  id                            bigserial not null,
  name                          varchar(255),
  constraint pk_migtest_mtm_m primary key (id)
);

create table migtest_oto_child (
  id                            serial not null,
  name                          varchar(255),
  constraint pk_migtest_oto_child primary key (id)
);

create table migtest_oto_master (
  id                            bigserial not null,
  name                          varchar(255),
  constraint pk_migtest_oto_master primary key (id)
);

create index ix_migtest_e_basic_indextest1 on migtest_e_basic (indextest1);
create index ix_migtest_e_basic_indextest5 on migtest_e_basic (indextest5);
alter table migtest_e_basic add constraint fk_migtest_e_basic_eref_id foreign key (eref_id) references migtest_e_ref (id) on delete restrict on update restrict;
create index ix_migtest_e_basic_eref_id on migtest_e_basic (eref_id);

alter table migtest_e_history2 add column sys_period tstzrange not null default tstzrange(current_timestamp, null);
create table migtest_e_history2_history(like migtest_e_history2);
create view migtest_e_history2_with_history as select * from migtest_e_history2 union all select * from migtest_e_history2_history;

create or replace function migtest_e_history2_history_version() returns trigger as $$
begin
  if (TG_OP = 'UPDATE') then
    insert into migtest_e_history2_history (sys_period,id, test_string) values (tstzrange(lower(OLD.sys_period), current_timestamp), OLD.id, OLD.test_string);
    NEW.sys_period = tstzrange(current_timestamp,null);
    return new;
  elsif (TG_OP = 'DELETE') then
    insert into migtest_e_history2_history (sys_period,id, test_string) values (tstzrange(lower(OLD.sys_period), current_timestamp), OLD.id, OLD.test_string);
    return old;
  end if;
end;
$$ LANGUAGE plpgsql;

create trigger migtest_e_history2_history_upd
  before update or delete on migtest_e_history2
  for each row execute procedure migtest_e_history2_history_version();

