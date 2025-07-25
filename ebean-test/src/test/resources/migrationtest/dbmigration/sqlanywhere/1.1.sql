-- Migrationscripts for ebean unittest
-- drop dependencies
alter table migtest_fk_cascade drop constraint if exists fk_migtest_fk_cascade_one_id;
alter table migtest_fk_set_null drop constraint if exists fk_migtest_fk_set_null_one_id;
alter table migtest_e_basic drop constraint if exists ck_migtest_e_basic_status;
alter table migtest_e_basic drop constraint if exists ck_migtest_e_basic_status2;
alter table migtest_e_basic drop constraint uq_migtest_e_basic_indextest2;
alter table migtest_e_basic drop constraint uq_migtest_e_basic_indextest6;
alter table migtest_e_basic drop constraint uq_migtest_e_basic_indextest7;
alter table migtest_e_enum drop constraint if exists ck_migtest_e_enum_test_status;
drop index if exists ix_migtest_e_basic_indextest1;
drop index if exists ix_migtest_e_basic_indextest5;
drop index if exists ix_migtest_quoted_status1;
-- apply changes
create table drop_main (
  id                            integer auto_increment not null,
  constraint pk_drop_main primary key (id)
);

create table drop_main_drop_ref_many (
  drop_main_id                  integer not null,
  drop_ref_many_id              integer not null,
  constraint pk_drop_main_drop_ref_many primary key (drop_main_id,drop_ref_many_id)
);

create table drop_ref_many (
  id                            integer auto_increment not null,
  constraint pk_drop_ref_many primary key (id)
);

create table drop_ref_one (
  id                            integer auto_increment not null,
  parent_id                     integer,
  constraint pk_drop_ref_one primary key (id)
);

create table drop_ref_one_to_one (
  id                            integer auto_increment not null,
  parent_id                     integer,
  constraint uq_drop_ref_one_to_one_parent_id unique (parent_id),
  constraint pk_drop_ref_one_to_one primary key (id)
);

create table migtest_e_test_binary (
  id                            integer auto_increment not null,
  test_byte16                   varbinary(16),
  test_byte256                  varbinary(256),
  test_byte512                  varbinary(512),
  test_byte1k                   varbinary(1024),
  test_byte2k                   varbinary(2048),
  test_byte4k                   varbinary(4096),
  test_byte8k                   varbinary(8192),
  test_byte16k                  varbinary(16384),
  test_byte32k                  long binary,
  test_byte64k                  long binary,
  test_byte128k                 long binary,
  test_byte256k                 long binary,
  test_byte512k                 long binary,
  test_byte1m                   long binary,
  test_byte2m                   long binary,
  test_byte4m                   long binary,
  test_byte8m                   long binary,
  test_byte16m                  long binary,
  test_byte32m                  long binary,
  constraint pk_migtest_e_test_binary primary key (id)
);

create table migtest_e_test_json (
  id                            integer auto_increment not null,
  json255                       varchar(255),
  json256                       varchar(256),
  json512                       varchar(512),
  json1k                        varchar(1024),
  json2k                        varchar(2048),
  json4k                        varchar(4096),
  json8k                        varchar(8192),
  json16k                       varchar(16384),
  json32k                       long varchar,
  json64k                       long varchar,
  json128k                      long varchar,
  json256k                      long varchar,
  json512k                      long varchar,
  json1m                        long varchar,
  json2m                        long varchar,
  json4m                        long varchar,
  json8m                        long varchar,
  json16m                       long varchar,
  json32m                       long varchar,
  constraint pk_migtest_e_test_json primary key (id)
);

create table migtest_e_test_lob (
  id                            integer auto_increment not null,
  lob255                        long varchar,
  lob256                        long varchar,
  lob512                        long varchar,
  lob1k                         long varchar,
  lob2k                         long varchar,
  lob4k                         long varchar,
  lob8k                         long varchar,
  lob16k                        long varchar,
  lob32k                        long varchar,
  lob64k                        long varchar,
  lob128k                       long varchar,
  lob256k                       long varchar,
  lob512k                       long varchar,
  lob1m                         long varchar,
  lob2m                         long varchar,
  lob4m                         long varchar,
  lob8m                         long varchar,
  lob16m                        long varchar,
  lob32m                        long varchar,
  constraint pk_migtest_e_test_lob primary key (id)
);

create table migtest_e_test_varchar (
  id                            integer auto_increment not null,
  varchar255                    varchar(255),
  varchar256                    varchar(256),
  varchar512                    varchar(512),
  varchar1k                     varchar(1024),
  varchar2k                     varchar(2048),
  varchar4k                     varchar(4096),
  varchar8k                     varchar(8192),
  varchar16k                    varchar(16384),
  varchar32k                    long varchar,
  varchar64k                    long varchar,
  varchar128k                   long varchar,
  varchar256k                   long varchar,
  varchar512k                   long varchar,
  varchar1m                     long varchar,
  varchar2m                     long varchar,
  varchar4m                     long varchar,
  varchar8m                     long varchar,
  varchar16m                    long varchar,
  varchar32m                    long varchar,
  constraint pk_migtest_e_test_varchar primary key (id)
);

create table migtest_e_user (
  id                            integer auto_increment not null,
  constraint pk_migtest_e_user primary key (id)
);

create table migtest_mtm_c_migtest_mtm_m (
  migtest_mtm_c_id              integer not null,
  migtest_mtm_m_id              numeric(19) not null,
  constraint pk_migtest_mtm_c_migtest_mtm_m primary key (migtest_mtm_c_id,migtest_mtm_m_id)
);

create table migtest_mtm_m_migtest_mtm_c (
  migtest_mtm_m_id              numeric(19) not null,
  migtest_mtm_c_id              integer not null,
  constraint pk_migtest_mtm_m_migtest_mtm_c primary key (migtest_mtm_m_id,migtest_mtm_c_id)
);

create table migtest_mtm_m_phone_numbers (
  migtest_mtm_m_id              numeric(19) not null,
  value                         varchar(255) not null
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
alter table "table" alter column textfield set null;
alter table "table" add column "select" varchar(255);
alter table "table" add column textfield2 varchar(255);
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
alter table migtest_e_basic alter column default_test set not null;
alter table migtest_e_basic alter column user_id set null;
alter table migtest_e_basic add column new_string_field varchar(255) default 'foo''bar' not null;
alter table migtest_e_basic add column new_boolean_field bit default true not null;
alter table migtest_e_basic add column new_boolean_field2 bit default true not null;
alter table migtest_e_basic add column progress integer default 0 not null;
alter table migtest_e_basic add column new_integer integer default 42 not null;
alter table migtest_e_history alter column test_string numeric(19);
alter table migtest_e_history2 alter column test_string set default 'unknown';
alter table migtest_e_history2 alter column test_string set not null;
alter table migtest_e_history2 add column test_string2 varchar(255);
alter table migtest_e_history2 add column test_string3 varchar(255) default 'unknown' not null;
alter table migtest_e_history2 add column new_column varchar(20);
alter table migtest_e_history4 alter column test_number numeric(19);
alter table migtest_e_history5 add column test_boolean bit default false not null;
alter table migtest_e_history6 alter column test_number1 set default 42;
alter table migtest_e_history6 alter column test_number1 set not null;
alter table migtest_e_history6 alter column test_number2 set null;
alter table migtest_e_softdelete add column deleted bit default false not null;
alter table migtest_oto_child add column master_id numeric(19);
-- apply post alter
alter table migtest_e_basic add constraint ck_migtest_e_basic_status check ( status in ('N','A','I','?'));
alter table migtest_e_basic add constraint uq_migtest_e_basic_description unique  (description);
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
create index ix_drop_main_drop_ref_many_drop_main on drop_main_drop_ref_many (drop_main_id);
alter table drop_main_drop_ref_many add constraint fk_drop_main_drop_ref_many_drop_main foreign key (drop_main_id) references drop_main (id) on delete restrict on update restrict;

create index ix_drop_main_drop_ref_many_drop_ref_many on drop_main_drop_ref_many (drop_ref_many_id);
alter table drop_main_drop_ref_many add constraint fk_drop_main_drop_ref_many_drop_ref_many foreign key (drop_ref_many_id) references drop_ref_many (id) on delete restrict on update restrict;

create index ix_drop_ref_one_parent_id on drop_ref_one (parent_id);
alter table drop_ref_one add constraint fk_drop_ref_one_parent_id foreign key (parent_id) references drop_main (id) on delete restrict on update restrict;

alter table drop_ref_one_to_one add constraint fk_drop_ref_one_to_one_parent_id foreign key (parent_id) references drop_main (id) on delete restrict on update restrict;

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
create index ix_migtest_e_basic_indextest7 on migtest_e_basic (indextest7);
create index ix_table_textfield2 on "table" (textfield2);
