-- Migrationscripts for ebean unittest
-- drop dependencies
alter table migtest_fk_cascade drop constraint fk_migtest_fk_cascade_one_id;
alter table migtest_fk_set_null drop constraint fk_migtest_fk_set_null_one_id;
delimiter $$
declare
  expected_error exception;
  pragma exception_init(expected_error, -2443);
begin
  execute immediate 'alter table migtest_e_basic drop constraint ck_migtest_e_basic_status';
exception
  when expected_error then null;
end;
$$;
delimiter $$
declare
  expected_error exception;
  pragma exception_init(expected_error, -2443);
begin
  execute immediate 'alter table migtest_e_basic drop constraint ck_migtest_e_basic_status2';
exception
  when expected_error then null;
end;
$$;
delimiter $$
declare
  expected_error exception;
  pragma exception_init(expected_error, -2443);
begin
  execute immediate 'alter table migtest_e_basic drop constraint uq_migtest_e_basic_indextest2';
exception
  when expected_error then null;
end;
$$;
delimiter $$
declare
  expected_error exception;
  pragma exception_init(expected_error, -2443);
begin
  execute immediate 'alter table migtest_e_basic drop constraint uq_migtest_e_basic_indextest6';
exception
  when expected_error then null;
end;
$$;
delimiter $$
declare
  expected_error exception;
  pragma exception_init(expected_error, -2443);
begin
  execute immediate 'alter table migtest_e_basic drop constraint uq_migtest_e_basic_indextest7';
exception
  when expected_error then null;
end;
$$;
delimiter $$
declare
  expected_error exception;
  pragma exception_init(expected_error, -2443);
begin
  execute immediate 'alter table migtest_e_enum drop constraint ck_migtest_e_enum_test_status';
exception
  when expected_error then null;
end;
$$;
drop index ix_migtest_e_basic_indextest1;
drop index ix_migtest_e_basic_indextest5;
drop index ix_migtest_quoted_status1;
-- apply changes
create table drop_main (
  id                            number(10) not null,
  constraint pk_drop_main primary key (id)
);
create sequence drop_main_seq;

create table drop_main_drop_ref_many (
  drop_main_id                  number(10) not null,
  drop_ref_many_id              number(10) not null,
  constraint pk_drop_main_drop_ref_many primary key (drop_main_id,drop_ref_many_id)
);

create table drop_ref_many (
  id                            number(10) not null,
  constraint pk_drop_ref_many primary key (id)
);
create sequence drop_ref_many_seq;

create table drop_ref_one (
  id                            number(10) not null,
  parent_id                     number(10),
  constraint pk_drop_ref_one primary key (id)
);
create sequence drop_ref_one_seq;

create table drop_ref_one_to_one (
  id                            number(10) not null,
  parent_id                     number(10),
  constraint uq_drop_ref_one_to_one_parent_id unique (parent_id),
  constraint pk_drop_ref_one_to_one primary key (id)
);
create sequence drop_ref_one_to_one_seq;

create table migtest_e_test_binary (
  id                            number(10) not null,
  test_byte16                   raw(16),
  test_byte256                  raw(256),
  test_byte512                  raw(512),
  test_byte1k                   raw(1024),
  test_byte2k                   blob,
  test_byte4k                   blob,
  test_byte8k                   blob,
  test_byte16k                  blob,
  test_byte32k                  blob,
  test_byte64k                  blob,
  test_byte128k                 blob,
  test_byte256k                 blob,
  test_byte512k                 blob,
  test_byte1m                   blob,
  test_byte2m                   blob,
  test_byte4m                   blob,
  test_byte8m                   blob,
  test_byte16m                  blob,
  test_byte32m                  blob,
  constraint pk_migtest_e_test_binary primary key (id)
);
create sequence migtest_e_test_binary_seq;

create table migtest_e_test_json (
  id                            number(10) not null,
  json255                       varchar2(255),
  json256                       varchar2(256),
  json512                       varchar2(512),
  json1k                        varchar2(1024),
  json2k                        varchar2(2048),
  json4k                        clob,
  json8k                        clob,
  json16k                       clob,
  json32k                       clob,
  json64k                       clob,
  json128k                      clob,
  json256k                      clob,
  json512k                      clob,
  json1m                        clob,
  json2m                        clob,
  json4m                        clob,
  json8m                        clob,
  json16m                       clob,
  json32m                       clob,
  constraint pk_migtest_e_test_json primary key (id)
);
create sequence migtest_e_test_json_seq;

create table migtest_e_test_lob (
  id                            number(10) not null,
  lob255                        clob,
  lob256                        clob,
  lob512                        clob,
  lob1k                         clob,
  lob2k                         clob,
  lob4k                         clob,
  lob8k                         clob,
  lob16k                        clob,
  lob32k                        clob,
  lob64k                        clob,
  lob128k                       clob,
  lob256k                       clob,
  lob512k                       clob,
  lob1m                         clob,
  lob2m                         clob,
  lob4m                         clob,
  lob8m                         clob,
  lob16m                        clob,
  lob32m                        clob,
  constraint pk_migtest_e_test_lob primary key (id)
);
create sequence migtest_e_test_lob_seq;

create table migtest_e_test_varchar (
  id                            number(10) not null,
  varchar255                    varchar2(255),
  varchar256                    varchar2(256),
  varchar512                    varchar2(512),
  varchar1k                     varchar2(1024),
  varchar2k                     varchar2(2048),
  varchar4k                     clob,
  varchar8k                     clob,
  varchar16k                    clob,
  varchar32k                    clob,
  varchar64k                    clob,
  varchar128k                   clob,
  varchar256k                   clob,
  varchar512k                   clob,
  varchar1m                     clob,
  varchar2m                     clob,
  varchar4m                     clob,
  varchar8m                     clob,
  varchar16m                    clob,
  varchar32m                    clob,
  constraint pk_migtest_e_test_varchar primary key (id)
);
create sequence migtest_e_test_varchar_seq;

create table migtest_e_user (
  id                            number(10) not null,
  constraint pk_migtest_e_user primary key (id)
);
create sequence migtest_e_user_seq;

create table migtest_mtm_c_migtest_mtm_m (
  migtest_mtm_c_id              number(10) not null,
  migtest_mtm_m_id              number(19) not null,
  constraint pk_migtest_mtm_c_migtest_mtm_m primary key (migtest_mtm_c_id,migtest_mtm_m_id)
);

create table migtest_mtm_m_migtest_mtm_c (
  migtest_mtm_m_id              number(19) not null,
  migtest_mtm_c_id              number(10) not null,
  constraint pk_migtest_mtm_m_migtest_mtm_c primary key (migtest_mtm_m_id,migtest_mtm_c_id)
);

create table migtest_mtm_m_phone_numbers (
  migtest_mtm_m_id              number(19) not null,
  value                         varchar2(255) not null
);


update migtest_e_basic set status = 'A' where status is null;

-- rename all collisions;

--;

insert into migtest_e_user (id) select distinct user_id from migtest_e_basic;

-- NOTE: table has @History - special migration may be necessary
update migtest_e_history2 set test_string = 'unknown' where test_string is null;

-- NOTE: table has @History - special migration may be necessary
update migtest_e_history6 set test_number1 = 42 where test_number1 is null;
-- apply alter tables
alter table "table" modify textfield null;
alter table "table" add "select" varchar2(255);
alter table "table" add textfield2 varchar2(255);
alter table migtest_ckey_detail add one_key number(10);
alter table migtest_ckey_detail add two_key varchar2(127);
alter table migtest_ckey_parent add assoc_id number(10);
alter table migtest_e_basic modify status default 'A';
alter table migtest_e_basic modify status not null;
alter table migtest_e_basic modify status2 varchar2(127);
alter table migtest_e_basic modify status2 default null;
alter table migtest_e_basic modify status2 null;
alter table migtest_e_basic modify a_lob default null;
alter table migtest_e_basic modify a_lob null;
alter table migtest_e_basic modify default_test not null;
alter table migtest_e_basic modify user_id null;
alter table migtest_e_basic add new_string_field varchar2(255) default 'foo''bar' not null;
alter table migtest_e_basic add new_boolean_field number(1) default 1 not null;
alter table migtest_e_basic add new_boolean_field2 number(1) default 1 not null;
alter table migtest_e_basic add progress number(10) default 0 not null;
alter table migtest_e_basic add new_integer number(10) default 42 not null;
alter table migtest_e_history modify test_string number(19);
alter table migtest_e_history2 modify test_string default 'unknown';
alter table migtest_e_history2 modify test_string not null;
alter table migtest_e_history2 add test_string2 varchar2(255);
alter table migtest_e_history2 add test_string3 varchar2(255) default 'unknown' not null;
alter table migtest_e_history2 add new_column varchar2(20);
alter table migtest_e_history4 modify test_number number(19);
alter table migtest_e_history5 add test_boolean number(1) default 0 not null;
alter table migtest_e_history6 modify test_number1 default 42;
alter table migtest_e_history6 modify test_number1 not null;
alter table migtest_e_history6 modify test_number2 null;
alter table migtest_e_softdelete add deleted number(1) default 0 not null;
alter table migtest_oto_child add master_id number(19);
-- apply post alter
alter table migtest_e_basic add constraint ck_migtest_e_basic_status check ( status in ('N','A','I','?'));
-- NOT YET IMPLEMENTED: alter table migtest_e_basic add constraint uq_migtest_e_basic_description unique  (description);
update migtest_e_basic set new_boolean_field = old_boolean;

alter table migtest_e_basic add constraint ck_migtest_e_basic_progress check ( progress in (0,1,2));
-- NOT YET IMPLEMENTED: alter table migtest_e_basic add constraint uq_mgtst__bsc_stts_ndxtst1 unique  (status,indextest1);
-- NOT YET IMPLEMENTED: alter table migtest_e_basic add constraint uq_migtest_e_basic_name unique  (name);
-- NOT YET IMPLEMENTED: alter table migtest_e_basic add constraint uq_migtest_e_basic_indextest4 unique  (indextest4);
-- NOT YET IMPLEMENTED: alter table migtest_e_basic add constraint uq_migtest_e_basic_indextest5 unique  (indextest5);
comment on column migtest_e_history.test_string is 'Column altered to long now';
comment on table migtest_e_history is 'We have history now';
comment on column "table"."index" is 'this is an other comment';
-- NOT YET IMPLEMENTED: alter table "table" add constraint uq_table_select unique  ("select");
-- foreign keys and indices
create index ix_drp_mn_drp_rf_mny_drp_mn on drop_main_drop_ref_many (drop_main_id);
alter table drop_main_drop_ref_many add constraint fk_drp_mn_drp_rf_mny_drp_mn foreign key (drop_main_id) references drop_main (id);

create index ix_drp_mn_drp_rf_mny_dr_d70vl on drop_main_drop_ref_many (drop_ref_many_id);
alter table drop_main_drop_ref_many add constraint fk_drp_mn_drp_rf_mny_dr_joeslj foreign key (drop_ref_many_id) references drop_ref_many (id);

create index ix_drop_ref_one_parent_id on drop_ref_one (parent_id);
alter table drop_ref_one add constraint fk_drop_ref_one_parent_id foreign key (parent_id) references drop_main (id);

alter table drop_ref_one_to_one add constraint fk_drp_rf_n_t_n_prnt_d foreign key (parent_id) references drop_main (id);

create index ix_mgtst_mtm_c_mgtst_mt_3ug4ok on migtest_mtm_c_migtest_mtm_m (migtest_mtm_c_id);
alter table migtest_mtm_c_migtest_mtm_m add constraint fk_mgtst_mtm_c_mgtst_mt_93awga foreign key (migtest_mtm_c_id) references migtest_mtm_c (id);

create index ix_mgtst_mtm_c_mgtst_mt_3ug4ou on migtest_mtm_c_migtest_mtm_m (migtest_mtm_m_id);
alter table migtest_mtm_c_migtest_mtm_m add constraint fk_mgtst_mtm_c_mgtst_mt_93awgk foreign key (migtest_mtm_m_id) references migtest_mtm_m (id);

create index ix_mgtst_mtm_m_mgtst_mt_b7nbcu on migtest_mtm_m_migtest_mtm_c (migtest_mtm_m_id);
alter table migtest_mtm_m_migtest_mtm_c add constraint fk_mgtst_mtm_m_mgtst_mt_ggi34k foreign key (migtest_mtm_m_id) references migtest_mtm_m (id);

create index ix_mgtst_mtm_m_mgtst_mt_b7nbck on migtest_mtm_m_migtest_mtm_c (migtest_mtm_c_id);
alter table migtest_mtm_m_migtest_mtm_c add constraint fk_mgtst_mtm_m_mgtst_mt_ggi34a foreign key (migtest_mtm_c_id) references migtest_mtm_c (id);

create index ix_mgtst_mtm_m_phn_nmbr_do9ma3 on migtest_mtm_m_phone_numbers (migtest_mtm_m_id);
alter table migtest_mtm_m_phone_numbers add constraint fk_mgtst_mtm_m_phn_nmbr_s8neid foreign key (migtest_mtm_m_id) references migtest_mtm_m (id);

alter table migtest_ckey_detail add constraint fk_migtest_ckey_detail_parent foreign key (one_key,two_key) references migtest_ckey_parent (one_key,two_key);
create index ix_mgtst_cky_prnt_ssc_d on migtest_ckey_parent (assoc_id);
alter table migtest_ckey_parent add constraint fk_mgtst_cky_prnt_ssc_d foreign key (assoc_id) references migtest_ckey_assoc (id);

alter table migtest_fk_cascade add constraint fk_migtest_fk_cascade_one_id foreign key (one_id) references migtest_fk_cascade_one (id);
alter table migtest_fk_none add constraint fk_migtest_fk_none_one_id foreign key (one_id) references migtest_fk_one (id);
alter table migtest_fk_none_via_join add constraint fk_mgtst_fk_nn_v_jn_n_d foreign key (one_id) references migtest_fk_one (id);
alter table migtest_fk_set_null add constraint fk_migtest_fk_set_null_one_id foreign key (one_id) references migtest_fk_one (id);
alter table migtest_e_basic add constraint fk_migtest_e_basic_user_id foreign key (user_id) references migtest_e_user (id);
alter table migtest_oto_child add constraint fk_migtest_oto_child_master_id foreign key (master_id) references migtest_oto_master (id);

create index ix_migtest_e_basic_indextest3 on migtest_e_basic (indextest3);
create index ix_migtest_e_basic_indextest6 on migtest_e_basic (indextest6);
create index ix_migtest_e_basic_indextest7 on migtest_e_basic (indextest7);
create index ix_table_textfield2 on "table" (textfield2);
