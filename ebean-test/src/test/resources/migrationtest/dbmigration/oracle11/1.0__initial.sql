-- Migrationscripts for ebean unittest
-- apply changes
create table migtest_ckey_assoc (
  id                            number(10) not null,
  assoc_one                     varchar2(255),
  constraint pk_migtest_ckey_assoc primary key (id)
);
create sequence migtest_ckey_assoc_seq;

create table migtest_ckey_detail (
  id                            number(10) not null,
  something                     varchar2(255),
  constraint pk_migtest_ckey_detail primary key (id)
);
create sequence migtest_ckey_detail_seq;

create table migtest_ckey_parent (
  one_key                       number(10) not null,
  two_key                       varchar2(127) not null,
  name                          varchar2(255),
  version                       number(10) not null,
  constraint pk_migtest_ckey_parent primary key (one_key,two_key)
);

create table migtest_fk_cascade (
  id                            number(19) not null,
  one_id                        number(19),
  constraint pk_migtest_fk_cascade primary key (id)
);
create sequence migtest_fk_cascade_seq;

create table migtest_fk_cascade_one (
  id                            number(19) not null,
  constraint pk_migtest_fk_cascade_one primary key (id)
);
create sequence migtest_fk_cascade_one_seq;

create table migtest_fk_none (
  id                            number(19) not null,
  one_id                        number(19),
  constraint pk_migtest_fk_none primary key (id)
);
create sequence migtest_fk_none_seq;

create table migtest_fk_none_via_join (
  id                            number(19) not null,
  one_id                        number(19),
  constraint pk_migtest_fk_none_via_join primary key (id)
);
create sequence migtest_fk_none_via_join_seq;

create table migtest_fk_one (
  id                            number(19) not null,
  constraint pk_migtest_fk_one primary key (id)
);
create sequence migtest_fk_one_seq;

create table migtest_fk_set_null (
  id                            number(19) not null,
  one_id                        number(19),
  constraint pk_migtest_fk_set_null primary key (id)
);
create sequence migtest_fk_set_null_seq;

create table migtest_e_basic (
  id                            number(10) not null,
  status                        varchar2(1),
  status2                       varchar2(1) default 'N' not null,
  name                          varchar2(127),
  description                   varchar2(127),
  description_file              blob,
  json_list                     clob,
  a_lob                         varchar2(255) default 'X' not null,
  some_date                     timestamp,
  old_boolean                   number(1) default 0 not null,
  old_boolean2                  number(1),
  eref_id                       number(10),
  indextest1                    varchar2(127),
  indextest2                    varchar2(127),
  indextest3                    varchar2(127),
  indextest4                    varchar2(127),
  indextest5                    varchar2(127),
  indextest6                    varchar2(127),
  user_id                       number(10) not null,
  constraint ck_migtest_e_basic_status check ( status in ('N','A','I')),
  constraint ck_migtest_e_basic_status2 check ( status2 in ('N','A','I')),
  constraint uq_migtest_e_basic_indextest2 unique (indextest2),
  constraint uq_migtest_e_basic_indextest6 unique (indextest6),
  constraint pk_migtest_e_basic primary key (id)
);
create sequence migtest_e_basic_seq;

create table migtest_e_enum (
  id                            number(10) not null,
  test_status                   varchar2(1),
  constraint ck_migtest_e_enum_test_status check ( test_status in ('N','A','I')),
  constraint pk_migtest_e_enum primary key (id)
);
create sequence migtest_e_enum_seq;

create table migtest_e_history (
  id                            number(10) not null,
  test_string                   varchar2(255),
  constraint pk_migtest_e_history primary key (id)
);
create sequence migtest_e_history_seq;

create table migtest_e_history2 (
  id                            number(10) not null,
  test_string                   varchar2(255),
  obsolete_string1              varchar2(255),
  obsolete_string2              varchar2(255),
  constraint pk_migtest_e_history2 primary key (id)
);
create sequence migtest_e_history2_seq;

create table migtest_e_history3 (
  id                            number(10) not null,
  test_string                   varchar2(255),
  constraint pk_migtest_e_history3 primary key (id)
);
create sequence migtest_e_history3_seq;

create table migtest_e_history4 (
  id                            number(10) not null,
  test_number                   number(10),
  constraint pk_migtest_e_history4 primary key (id)
);
create sequence migtest_e_history4_seq;

create table migtest_e_history5 (
  id                            number(10) not null,
  test_number                   number(10),
  constraint pk_migtest_e_history5 primary key (id)
);
create sequence migtest_e_history5_seq;

create table migtest_e_history6 (
  id                            number(10) not null,
  test_number1                  number(10),
  test_number2                  number(10) not null,
  constraint pk_migtest_e_history6 primary key (id)
);
create sequence migtest_e_history6_seq;

create table "migtest_QuOtEd" (
  id                            varchar2(255) not null,
  status1                       varchar2(1),
  status2                       varchar2(1),
  constraint ck_migtest_quoted_status1 check ( status1 in ('N','A','I')),
  constraint ck_migtest_quoted_status2 check ( status2 in ('N','A','I')),
  constraint uq_migtest_quoted_status2 unique (status2),
  constraint pk_migtest_quoted primary key (id)
);

create table migtest_e_ref (
  id                            number(10) not null,
  name                          varchar2(127) not null,
  constraint uq_migtest_e_ref_name unique (name),
  constraint pk_migtest_e_ref primary key (id)
);
create sequence migtest_e_ref_seq;

create table migtest_e_softdelete (
  id                            number(10) not null,
  test_string                   varchar2(255),
  constraint pk_migtest_e_softdelete primary key (id)
);
create sequence migtest_e_softdelete_seq;

create table "table" (
  "index"                       varchar2(255) not null,
  "from"                        varchar2(255),
  "to"                          varchar2(255),
  "varchar"                     varchar2(255),
  "foreign"                     varchar2(255),
  textfield                     varchar2(255) not null,
  constraint uq_table_to unique ("to"),
  constraint uq_table_varchar unique ("varchar"),
  constraint pk_table primary key ("index")
);
comment on column "table"."index" is 'this is a comment';

create table migtest_mtm_c (
  id                            number(10) not null,
  name                          varchar2(255),
  constraint pk_migtest_mtm_c primary key (id)
);
create sequence migtest_mtm_c_seq;

create table migtest_mtm_m (
  id                            number(19) not null,
  name                          varchar2(255),
  constraint pk_migtest_mtm_m primary key (id)
);
create sequence migtest_mtm_m_seq;

create table migtest_oto_child (
  id                            number(10) not null,
  name                          varchar2(255),
  constraint pk_migtest_oto_child primary key (id)
);
create sequence migtest_oto_child_seq;

create table migtest_oto_master (
  id                            number(19) not null,
  name                          varchar2(255),
  constraint pk_migtest_oto_master primary key (id)
);
create sequence migtest_oto_master_seq;

-- foreign keys and indices
create index ix_migtest_fk_cascade_one_id on migtest_fk_cascade (one_id);
alter table migtest_fk_cascade add constraint fk_migtest_fk_cascade_one_id foreign key (one_id) references migtest_fk_cascade_one (id) on delete cascade;

create index ix_migtest_fk_set_null_one_id on migtest_fk_set_null (one_id);
alter table migtest_fk_set_null add constraint fk_migtest_fk_set_null_one_id foreign key (one_id) references migtest_fk_one (id) on delete set null;

create index ix_migtest_e_basic_eref_id on migtest_e_basic (eref_id);
alter table migtest_e_basic add constraint fk_migtest_e_basic_eref_id foreign key (eref_id) references migtest_e_ref (id);

create index ix_table_foreign on "table" ("foreign");
alter table "table" add constraint fk_table_foreign foreign key ("foreign") references "table" ("index");

create index ix_migtest_e_basic_indextest1 on migtest_e_basic (indextest1);
create index ix_migtest_e_basic_indextest5 on migtest_e_basic (indextest5);
create index ix_migtest_quoted_status1 on "migtest_QuOtEd" (status1);
create index ix_table_from on "table" ("from");
