-- Migrationscripts for ebean unittest
-- drop dependencies
alter table migtest_fk_cascade drop constraint if exists fk_migtest_fk_cascade_one_id;
alter table migtest_fk_set_null drop constraint if exists fk_migtest_fk_set_null_one_id;
alter table migtest_e_basic drop constraint if exists ck_migtest_e_basic_status;
alter table migtest_e_basic drop constraint if exists ck_migtest_e_basic_status2;
alter table migtest_e_basic drop constraint uq_migtest_e_basic_indextest2;
alter table migtest_e_basic drop constraint uq_migtest_e_basic_indextest6;
alter table migtest_e_enum drop constraint if exists ck_migtest_e_enum_test_status;
drop index if exists ix_migtest_e_basic_indextest1;
drop index if exists ix_migtest_e_basic_indextest5;
drop index if exists ix_migtest_quoted_status1;
-- apply changes
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

insert into migtest_e_user (id) select distinct user_id from migtest_e_basic;

-- NOTE: table has @History - special migration may be necessary
update migtest_e_history2 set test_string = 'unknown' where test_string is null;

-- NOTE: table has @History - special migration may be necessary
update migtest_e_history6 set test_number1 = 42 where test_number1 is null;
-- apply alter tables
alter table "table" add column "select" varchar(255);
alter table migtest_ckey_detail add column one_key integer;
alter table migtest_ckey_detail add column two_key varchar(127);
alter table migtest_ckey_parent add column assoc_id integer;
alter table migtest_e_basic alter column status set default 'A';
alter table migtest_e_basic alter column status set not null;
alter table migtest_e_basic alter column status2 varchar(127);
alter table migtest_e_basic alter column status2 drop default;
alter table migtest_e_basic alter column status2 set null;
alter table migtest_e_basic alter column a_lob drop default;
alter table migtest_e_basic alter column a_lob set null;
alter table migtest_e_basic alter column user_id set null;
alter table migtest_e_basic add column new_string_field varchar(255) default 'foo''bar' not null;
alter table migtest_e_basic add column new_boolean_field boolean default true not null;
alter table migtest_e_basic add column new_boolean_field2 boolean default true not null;
alter table migtest_e_basic add column progress integer default 0 not null;
alter table migtest_e_basic add column new_integer integer default 42 not null;
alter table migtest_e_history alter column test_string bigint;
alter table migtest_e_history2 alter column test_string set default 'unknown';
alter table migtest_e_history2 alter column test_string set not null;
alter table migtest_e_history2 add column test_string2 varchar(255);
alter table migtest_e_history2 add column test_string3 varchar(255) default 'unknown' not null;
alter table migtest_e_history2 add column new_column varchar(20);
alter table migtest_e_history4 alter column test_number bigint;
alter table migtest_e_history5 add column test_boolean boolean default false not null;
alter table migtest_e_history6 alter column test_number1 set default 42;
alter table migtest_e_history6 alter column test_number1 set not null;
alter table migtest_e_history6 alter column test_number2 set null;
alter table migtest_e_softdelete add column deleted boolean default false not null;
alter table migtest_oto_child add column master_id bigint;
-- apply post alter
alter table migtest_e_basic add constraint ck_migtest_e_basic_status check ( status in ('N','A','I','?'));
alter table migtest_e_basic add constraint uq_migtest_e_basic_description unique  (description);
-- NOTE: table has @History - special migration may be necessary
update migtest_e_basic set new_boolean_field = old_boolean;

alter table migtest_e_basic add constraint ck_migtest_e_basic_progress check ( progress in (0,1,2));
alter table migtest_e_basic add constraint uq_migtest_e_basic_status_indextest1 unique  (status,indextest1);
alter table migtest_e_basic add constraint uq_migtest_e_basic_name unique  (name);
alter table migtest_e_basic add constraint uq_migtest_e_basic_indextest4 unique  (indextest4);
alter table migtest_e_basic add constraint uq_migtest_e_basic_indextest5 unique  (indextest5);
comment on column migtest_e_history.test_string is 'Column altered to long now';
comment on table migtest_e_history is 'We have history now';
comment on column "table"."index" is 'this is an other comment';
alter table "table" add constraint uq_table_select unique  ("select");
-- foreign keys and indices
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
