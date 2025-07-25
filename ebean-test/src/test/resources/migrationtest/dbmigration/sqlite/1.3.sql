-- Migrationscripts for ebean unittest
-- drop dependencies
-- not supported: alter table migtest_ckey_detail drop constraint if exists fk_migtest_ckey_detail_parent;
-- not supported: alter table migtest_fk_cascade drop constraint if exists fk_migtest_fk_cascade_one_id;
-- not supported: alter table migtest_fk_none drop constraint if exists fk_migtest_fk_none_one_id;
-- not supported: alter table migtest_fk_none_via_join drop constraint if exists fk_migtest_fk_none_via_join_one_id;
-- not supported: alter table migtest_fk_set_null drop constraint if exists fk_migtest_fk_set_null_one_id;
-- not supported: alter table migtest_e_basic drop constraint if exists ck_migtest_e_basic_status;
-- not supported: alter table migtest_e_basic drop constraint if exists ck_migtest_e_basic_status2;
-- not supported: alter table migtest_e_basic drop constraint uq_migtest_e_basic_description;
-- not supported: alter table migtest_e_basic drop constraint if exists fk_migtest_e_basic_user_id;
-- not supported: alter table migtest_e_basic drop constraint uq_migtest_e_basic_status_indextest1;
-- not supported: alter table migtest_e_basic drop constraint uq_migtest_e_basic_name;
-- not supported: alter table migtest_e_basic drop constraint uq_migtest_e_basic_indextest4;
-- not supported: alter table migtest_e_basic drop constraint uq_migtest_e_basic_indextest5;
-- not supported: alter table migtest_e_enum drop constraint if exists ck_migtest_e_enum_test_status;
-- not supported: alter table drop_main_drop_ref_many drop constraint if exists fk_drop_main_drop_ref_many_drop_main;
-- not supported: alter table drop_main_drop_ref_many drop constraint if exists fk_drop_main_drop_ref_many_drop_ref_many;
-- not supported: alter table drop_ref_one drop constraint if exists fk_drop_ref_one_parent_id;
-- not supported: alter table drop_ref_one_to_one drop constraint if exists fk_drop_ref_one_to_one_parent_id;
-- not supported: alter table migtest_mtm_c_migtest_mtm_m drop constraint if exists fk_migtest_mtm_c_migtest_mtm_m_migtest_mtm_c;
-- not supported: alter table migtest_mtm_c_migtest_mtm_m drop constraint if exists fk_migtest_mtm_c_migtest_mtm_m_migtest_mtm_m;
-- not supported: alter table migtest_mtm_m_migtest_mtm_c drop constraint if exists fk_migtest_mtm_m_migtest_mtm_c_migtest_mtm_m;
-- not supported: alter table migtest_mtm_m_migtest_mtm_c drop constraint if exists fk_migtest_mtm_m_migtest_mtm_c_migtest_mtm_c;
-- not supported: alter table migtest_mtm_m_phone_numbers drop constraint if exists fk_migtest_mtm_m_phone_numbers_migtest_mtm_m_id;
drop index if exists ix_migtest_e_basic_indextest3;
drop index if exists ix_migtest_e_basic_indextest6;
drop index if exists ix_migtest_e_basic_indextest7;
drop index if exists ix_table_textfield2;
-- apply changes
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
  id                            integer not null,
  name                          varchar(127) not null,
  constraint uq_migtest_e_ref_name unique (name),
  constraint pk_migtest_e_ref primary key (id)
);


update migtest_e_basic set status2 = 'N' where status2 is null;

update migtest_e_basic set a_lob = 'X' where a_lob is null;

update migtest_e_basic set user_id = 23 where user_id is null;

-- NOTE: table has @History - special migration may be necessary
update migtest_e_history6 set test_number2 = 7 where test_number2 is null;
-- apply alter tables
-- not supported: alter table migtest_e_basic alter column status drop default;
-- not supported: alter table migtest_e_basic alter column status set null;
-- not supported: alter table migtest_e_basic alter column status2 varchar(1);
-- not supported: alter table migtest_e_basic alter column status2 set default 'N';
-- not supported: alter table migtest_e_basic alter column status2 set not null;
-- not supported: alter table migtest_e_basic alter column a_lob varchar(255);
-- not supported: alter table migtest_e_basic alter column a_lob set default 'X';
-- not supported: alter table migtest_e_basic alter column a_lob set not null;
-- not supported: alter table migtest_e_basic alter column default_test set null;
-- not supported: alter table migtest_e_basic alter column user_id set default 23;
-- not supported: alter table migtest_e_basic alter column user_id set not null;
alter table migtest_e_basic add column description_file blob;
alter table migtest_e_basic add column old_boolean int default 0 not null;
alter table migtest_e_basic add column old_boolean2 int;
alter table migtest_e_basic add column eref_id integer;
-- not supported: alter table migtest_e_history2 alter column test_string drop default;
-- not supported: alter table migtest_e_history2 alter column test_string set null;
alter table migtest_e_history2 add column obsolete_string1 varchar(255);
alter table migtest_e_history2 add column obsolete_string2 varchar(255);
-- not supported: alter table migtest_e_history6 alter column test_number1 drop default;
-- not supported: alter table migtest_e_history6 alter column test_number1 set null;
-- not supported: alter table migtest_e_history6 alter column test_number2 set default 7;
-- not supported: alter table migtest_e_history6 alter column test_number2 set not null;
-- apply post alter
-- not supported: alter table migtest_e_basic add constraint ck_migtest_e_basic_status check ( status in ('N','A','I'));
-- not supported: alter table migtest_e_basic add constraint ck_migtest_e_basic_status2 check ( status2 in ('N','A','I'));
-- not supported: alter table migtest_e_basic add constraint uq_migtest_e_basic_indextest2 unique  (indextest2);
-- not supported: alter table migtest_e_basic add constraint uq_migtest_e_basic_indextest6 unique  (indextest6);
-- not supported: alter table migtest_e_basic add constraint uq_migtest_e_basic_indextest7 unique  (indextest7);
-- not supported: alter table migtest_e_enum add constraint ck_migtest_e_enum_test_status check ( test_status in ('N','A','I'));
-- foreign keys and indices
-- not supported: alter table migtest_fk_cascade add constraint fk_migtest_fk_cascade_one_id foreign key (one_id) references migtest_fk_cascade_one (id) on delete cascade on update restrict;
-- not supported: alter table migtest_fk_set_null add constraint fk_migtest_fk_set_null_one_id foreign key (one_id) references migtest_fk_one (id) on delete set null on update restrict;
create index ix_migtest_e_basic_eref_id on migtest_e_basic (eref_id);
-- not supported: alter table migtest_e_basic add constraint fk_migtest_e_basic_eref_id foreign key (eref_id) references migtest_e_ref (id) on delete restrict on update restrict;

create index ix_migtest_e_basic_indextest1 on migtest_e_basic (indextest1);
create index ix_migtest_e_basic_indextest5 on migtest_e_basic (indextest5);
create index ix_migtest_quoted_status1 on "migtest_QuOtEd" (status1);
