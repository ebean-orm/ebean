-- Migrationscripts for ebean unittest
-- apply changes
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
  version                       integer not null,
  two_key                       varchar(127) not null,
  name                          varchar(255),
  constraint pk_migtest_ckey_parent primary key (one_key,two_key)
);

create table migtest_fk_cascade (
  id                            bigserial not null,
  one_id                        bigint,
  constraint pk_migtest_fk_cascade primary key (id)
);

create table migtest_fk_cascade_one (
  id                            bigserial not null,
  constraint pk_migtest_fk_cascade_one primary key (id)
);

create table migtest_fk_none (
  id                            bigserial not null,
  one_id                        bigint,
  constraint pk_migtest_fk_none primary key (id)
);

create table migtest_fk_none_via_join (
  id                            bigserial not null,
  one_id                        bigint,
  constraint pk_migtest_fk_none_via_join primary key (id)
);

create table migtest_fk_one (
  id                            bigserial not null,
  constraint pk_migtest_fk_one primary key (id)
);

create table migtest_fk_set_null (
  id                            bigserial not null,
  one_id                        bigint,
  constraint pk_migtest_fk_set_null primary key (id)
);

create table migtest_e_basic (
  id                            serial not null,
  description_file              bytea,
  json_list                     json,
  a_lob                         varchar(255) default 'X' not null,
  some_date                     timestamptz,
  old_boolean                   boolean default false not null,
  old_boolean2                  boolean,
  eref_id                       integer,
  user_id                       integer not null,
  status                        varchar(1),
  status2                       varchar(1) default 'N' not null,
  name                          varchar(127),
  description                   varchar(127),
  indextest1                    varchar(127),
  indextest2                    varchar(127),
  indextest3                    varchar(127),
  indextest4                    varchar(127),
  indextest5                    varchar(127),
  indextest6                    varchar(127),
  constraint ck_migtest_e_basic_status check ( status in ('N','A','I')),
  constraint ck_migtest_e_basic_status2 check ( status2 in ('N','A','I')),
  constraint uq_migtest_e_basic_indextest2 unique (indextest2),
  constraint uq_migtest_e_basic_indextest6 unique (indextest6),
  constraint pk_migtest_e_basic primary key (id)
);

create table migtest_e_enum (
  id                            serial not null,
  test_status                   varchar(1),
  constraint ck_migtest_e_enum_test_status check ( test_status in ('N','A','I')),
  constraint pk_migtest_e_enum primary key (id)
);

create table migtest_e_history (
  id                            serial not null,
  test_string                   varchar(255),
  constraint pk_migtest_e_history primary key (id)
);

create table migtest_e_history2 (
  id                            serial not null,
  test_string                   varchar(255),
  obsolete_string1              varchar(255),
  obsolete_string2              varchar(255),
  constraint pk_migtest_e_history2 primary key (id)
);

create table migtest_e_history3 (
  id                            serial not null,
  test_string                   varchar(255),
  constraint pk_migtest_e_history3 primary key (id)
);

create table migtest_e_history4 (
  id                            serial not null,
  test_number                   integer,
  constraint pk_migtest_e_history4 primary key (id)
);

create table migtest_e_history5 (
  id                            serial not null,
  test_number                   integer,
  constraint pk_migtest_e_history5 primary key (id)
);

create table migtest_e_history6 (
  id                            serial not null,
  test_number1                  integer,
  test_number2                  integer not null,
  constraint pk_migtest_e_history6 primary key (id)
);

create table "migtest_QuOtEd" (
  id                            varchar(255) not null,
  status1                       varchar(1),
  status2                       varchar(1),
  constraint ck_migtest_quoted_status1 check ( status1 in ('N','A','I')),
  constraint ck_migtest_quoted_status2 check ( status2 in ('N','A','I')),
  constraint uq_migtest_quoted_status2 unique (status2),
  constraint pk_migtest_quoted primary key (id)
);

create table migtest_e_ref (
  id                            serial not null,
  name                          varchar(127) not null,
  constraint uq_migtest_e_ref_name unique (name),
  constraint pk_migtest_e_ref primary key (id)
);

create table migtest_e_softdelete (
  id                            serial not null,
  test_string                   varchar(255),
  constraint pk_migtest_e_softdelete primary key (id)
);

create table "table" (
  "index"                       varchar(255) not null,
  "from"                        varchar(255),
  "to"                          varchar(255),
  "varchar"                     varchar(255),
  "foreign"                     varchar(255),
  textfield                     varchar(255) not null,
  constraint uq_table_to unique ("to"),
  constraint uq_table_varchar unique ("varchar"),
  constraint pk_table primary key ("index")
);
comment on column "table"."index" is 'this is a comment';

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

-- apply alter tables
alter table "table" add column if not exists sys_period tstzrange not null default tstzrange(current_timestamp, null);
alter table migtest_e_history2 add column if not exists sys_period tstzrange not null default tstzrange(current_timestamp, null);
alter table migtest_e_history3 add column if not exists sys_period tstzrange not null default tstzrange(current_timestamp, null);
alter table migtest_e_history4 add column if not exists sys_period tstzrange not null default tstzrange(current_timestamp, null);
alter table migtest_e_history5 add column if not exists sys_period tstzrange not null default tstzrange(current_timestamp, null);
alter table migtest_e_history6 add column if not exists sys_period tstzrange not null default tstzrange(current_timestamp, null);
-- apply post alter
create table migtest_e_history2_history(like migtest_e_history2);
create view migtest_e_history2_with_history as select * from migtest_e_history2 union all select * from migtest_e_history2_history;
create or replace function migtest_e_history2_history_version() returns trigger as $$
-- play-ebean-start
declare
  lowerTs timestamptz;
  upperTs timestamptz;
begin
  lowerTs = lower(OLD.sys_period);
  upperTs = greatest(lowerTs + '1 microsecond',current_timestamp);
  if (TG_OP = 'UPDATE') then
    insert into migtest_e_history2_history (sys_period,id, test_string, obsolete_string2) values (tstzrange(lowerTs,upperTs), OLD.id, OLD.test_string, OLD.obsolete_string2);
    NEW.sys_period = tstzrange(upperTs,null);
    return new;
  elsif (TG_OP = 'DELETE') then
    insert into migtest_e_history2_history (sys_period,id, test_string, obsolete_string2) values (tstzrange(lowerTs,upperTs), OLD.id, OLD.test_string, OLD.obsolete_string2);
    return old;
  end if;
end;
-- play-ebean-end
$$ LANGUAGE plpgsql;

create trigger migtest_e_history2_history_upd
  before update or delete on migtest_e_history2
  for each row execute procedure migtest_e_history2_history_version();


create table migtest_e_history3_history(like migtest_e_history3);
create view migtest_e_history3_with_history as select * from migtest_e_history3 union all select * from migtest_e_history3_history;
create or replace function migtest_e_history3_history_version() returns trigger as $$
-- play-ebean-start
declare
  lowerTs timestamptz;
  upperTs timestamptz;
begin
  lowerTs = lower(OLD.sys_period);
  upperTs = greatest(lowerTs + '1 microsecond',current_timestamp);
  if (TG_OP = 'UPDATE') then
    insert into migtest_e_history3_history (sys_period,id, test_string) values (tstzrange(lowerTs,upperTs), OLD.id, OLD.test_string);
    NEW.sys_period = tstzrange(upperTs,null);
    return new;
  elsif (TG_OP = 'DELETE') then
    insert into migtest_e_history3_history (sys_period,id, test_string) values (tstzrange(lowerTs,upperTs), OLD.id, OLD.test_string);
    return old;
  end if;
end;
-- play-ebean-end
$$ LANGUAGE plpgsql;

create trigger migtest_e_history3_history_upd
  before update or delete on migtest_e_history3
  for each row execute procedure migtest_e_history3_history_version();


create table migtest_e_history4_history(like migtest_e_history4);
create view migtest_e_history4_with_history as select * from migtest_e_history4 union all select * from migtest_e_history4_history;
create or replace function migtest_e_history4_history_version() returns trigger as $$
-- play-ebean-start
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
-- play-ebean-end
$$ LANGUAGE plpgsql;

create trigger migtest_e_history4_history_upd
  before update or delete on migtest_e_history4
  for each row execute procedure migtest_e_history4_history_version();


create table migtest_e_history5_history(like migtest_e_history5);
create view migtest_e_history5_with_history as select * from migtest_e_history5 union all select * from migtest_e_history5_history;
create or replace function migtest_e_history5_history_version() returns trigger as $$
-- play-ebean-start
declare
  lowerTs timestamptz;
  upperTs timestamptz;
begin
  lowerTs = lower(OLD.sys_period);
  upperTs = greatest(lowerTs + '1 microsecond',current_timestamp);
  if (TG_OP = 'UPDATE') then
    insert into migtest_e_history5_history (sys_period,id, test_number) values (tstzrange(lowerTs,upperTs), OLD.id, OLD.test_number);
    NEW.sys_period = tstzrange(upperTs,null);
    return new;
  elsif (TG_OP = 'DELETE') then
    insert into migtest_e_history5_history (sys_period,id, test_number) values (tstzrange(lowerTs,upperTs), OLD.id, OLD.test_number);
    return old;
  end if;
end;
-- play-ebean-end
$$ LANGUAGE plpgsql;

create trigger migtest_e_history5_history_upd
  before update or delete on migtest_e_history5
  for each row execute procedure migtest_e_history5_history_version();


create table migtest_e_history6_history(like migtest_e_history6);
create view migtest_e_history6_with_history as select * from migtest_e_history6 union all select * from migtest_e_history6_history;
create or replace function migtest_e_history6_history_version() returns trigger as $$
-- play-ebean-start
declare
  lowerTs timestamptz;
  upperTs timestamptz;
begin
  lowerTs = lower(OLD.sys_period);
  upperTs = greatest(lowerTs + '1 microsecond',current_timestamp);
  if (TG_OP = 'UPDATE') then
    insert into migtest_e_history6_history (sys_period,id, test_number1, test_number2) values (tstzrange(lowerTs,upperTs), OLD.id, OLD.test_number1, OLD.test_number2);
    NEW.sys_period = tstzrange(upperTs,null);
    return new;
  elsif (TG_OP = 'DELETE') then
    insert into migtest_e_history6_history (sys_period,id, test_number1, test_number2) values (tstzrange(lowerTs,upperTs), OLD.id, OLD.test_number1, OLD.test_number2);
    return old;
  end if;
end;
-- play-ebean-end
$$ LANGUAGE plpgsql;

create trigger migtest_e_history6_history_upd
  before update or delete on migtest_e_history6
  for each row execute procedure migtest_e_history6_history_version();


create table table_history(like "table");
create view table_with_history as select * from "table" union all select * from table_history;
create or replace function table_history_version() returns trigger as $$
-- play-ebean-start
declare
  lowerTs timestamptz;
  upperTs timestamptz;
begin
  lowerTs = lower(OLD.sys_period);
  upperTs = greatest(lowerTs + '1 microsecond',current_timestamp);
  if (TG_OP = 'UPDATE') then
    insert into table_history (sys_period,"index", "from", "to", "varchar", "foreign", textfield) values (tstzrange(lowerTs,upperTs), OLD."index", OLD."from", OLD."to", OLD."varchar", OLD."foreign", OLD.textfield);
    NEW.sys_period = tstzrange(upperTs,null);
    return new;
  elsif (TG_OP = 'DELETE') then
    insert into table_history (sys_period,"index", "from", "to", "varchar", "foreign", textfield) values (tstzrange(lowerTs,upperTs), OLD."index", OLD."from", OLD."to", OLD."varchar", OLD."foreign", OLD.textfield);
    return old;
  end if;
end;
-- play-ebean-end
$$ LANGUAGE plpgsql;

create trigger table_history_upd
  before update or delete on "table"
  for each row execute procedure table_history_version();


-- foreign keys and indices
create index ix_migtest_fk_cascade_one_id on migtest_fk_cascade (one_id);
alter table migtest_fk_cascade add constraint fk_migtest_fk_cascade_one_id foreign key (one_id) references migtest_fk_cascade_one (id) on delete cascade on update restrict;

create index ix_migtest_fk_set_null_one_id on migtest_fk_set_null (one_id);
alter table migtest_fk_set_null add constraint fk_migtest_fk_set_null_one_id foreign key (one_id) references migtest_fk_one (id) on delete set null on update restrict;

create index ix_migtest_e_basic_eref_id on migtest_e_basic (eref_id);
alter table migtest_e_basic add constraint fk_migtest_e_basic_eref_id foreign key (eref_id) references migtest_e_ref (id) on delete restrict on update restrict;

create index ix_table_foreign on "table" ("foreign");
alter table "table" add constraint fk_table_foreign foreign key ("foreign") references "table" ("index") on delete restrict on update restrict;

create index if not exists ix_migtest_e_basic_indextest1 on migtest_e_basic (indextest1);
create index if not exists ix_migtest_e_basic_indextest5 on migtest_e_basic (indextest5);
create index if not exists ix_migtest_quoted_status1 on "migtest_QuOtEd" (status1);
create index if not exists ix_table_from on "table" ("from");
create index idxd_migtest_0 on migtest_oto_child using hash (upper(name)) where upper(name) = 'JIM';
create index concurrently if not exists ix_migtest_oto_child_lowername_id on migtest_oto_child (lower(name),id);
create index if not exists ix_migtest_oto_child_lowername on migtest_oto_child (lower(name));
