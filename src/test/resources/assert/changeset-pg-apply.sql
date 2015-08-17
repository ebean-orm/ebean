create table asimple_bean (
  id                            bigserial not null,
  name                          varchar(255),
  constraint pk_asimple_bean primary key (id)
);

create table bar (
  bar_type                      varchar(31) not null,
  bar_id                        serial not null,
  foo_id                        integer not null,
  version                       integer not null,
  constraint pk_bar primary key (bar_id)
);

create table oto_account (
  id                            bigserial not null,
  name                          varchar(255),
  version                       bigint not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_oto_account primary key (id)
);

create table o_address (
  id                            smallserial not null,
  line_1                        varchar(100),
  line_2                        varchar(100),
  city                          varchar(100),
  cretime                       timestamp,
  country_code                  varchar(2),
  updtime                       timestamp not null,
  constraint pk_o_address primary key (id)
);

create table address (
  oid                           bigserial not null,
  street                        varchar(255),
  version                       integer not null,
  constraint pk_address primary key (oid)
);

create table animals (
  species                       varchar(31) not null,
  id                            bigserial not null,
  shelter_id                    bigint,
  version                       bigint not null,
  name                          varchar(255),
  registration_number           varchar(255),
  date_of_birth                 date,
  constraint pk_animals primary key (id)
);

create table animal_shelter (
  id                            bigserial not null,
  name                          varchar(255),
  version                       bigint not null,
  constraint pk_animal_shelter primary key (id)
);

create table article (
  id                            serial not null,
  name                          varchar(255),
  author                        varchar(255),
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  version                       bigint not null,
  constraint pk_article primary key (id)
);

create table attribute (
  option_type                   integer not null,
  id                            serial not null,
  attribute_holder_id           integer,
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  version                       bigint not null,
  constraint pk_attribute primary key (id)
);

create table attribute_holder (
  id                            serial not null,
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  version                       bigint not null,
  constraint pk_attribute_holder primary key (id)
);

create table audit_log (
  id                            bigserial not null,
  description                   varchar(255),
  modified_description          varchar(255),
  constraint pk_audit_log primary key (id)
);

create table bbookmark (
  id                            serial not null,
  bookmark_reference            varchar(255),
  user_id                       integer,
  constraint pk_bbookmark primary key (id)
);

create table bbookmark_user (
  id                            serial not null,
  name                          varchar(255),
  password                      varchar(255),
  email_address                 varchar(255),
  country                       varchar(255),
  constraint pk_bbookmark_user primary key (id)
);

create table bsimple_with_gen (
  id                            serial not null,
  name                          varchar(255),
  constraint pk_bsimple_with_gen primary key (id)
);

create table bwith_qident (
  id                            serial not null,
  "Name"                        varchar(255),
  last_updated                  timestamp not null,
  constraint uq_bwith_qident_name unique ("Name"),
  constraint pk_bwith_qident primary key (id)
);

create table basic_joda_entity (
  id                            bigserial not null,
  name                          varchar(255),
  created                       timestamp not null,
  updated                       timestamp not null,
  version                       timestamp not null,
  constraint pk_basic_joda_entity primary key (id)
);

create table bean_with_time_zone (
  id                            bigserial not null,
  name                          varchar(255),
  timezone                      varchar(20),
  constraint pk_bean_with_time_zone primary key (id)
);

create table drel_booking (
  id                            bigserial not null,
  agent_invoice                 bigint,
  client_invoice                bigint,
  version                       integer not null,
  constraint uq_drel_booking_agent_invoice unique (agent_invoice),
  constraint uq_drel_booking_client_invoice unique (client_invoice),
  constraint pk_drel_booking primary key (id)
);

create table ckey_assoc (
  id                            serial not null,
  assoc_one                     varchar(255),
  constraint pk_ckey_assoc primary key (id)
);

create table ckey_detail (
  id                            serial not null,
  something                     varchar(255),
  one_key                       integer,
  two_key                       varchar(255),
  constraint pk_ckey_detail primary key (id)
);

create table ckey_parent (
  one_key                       integer not null,
  two_key                       varchar(255) not null,
  name                          varchar(255),
  assoc_id                      integer,
  version                       integer not null,
  constraint pk_ckey_parent primary key (one_key,two_key)
);

create table calculation_result (
  ID                            serial not null,
  charge                        float,
  product_configuration_ID      integer,
  group_configuration_ID        integer,
  constraint pk_calculation_result primary key (ID)
);

create table cao_bean (
  x_cust_id                     integer not null,
  x_type_id                     integer not null,
  description                   varchar(255),
  version                       bigint not null,
  constraint pk_cao_bean primary key (x_cust_id,x_type_id)
);

create table sa_car (
  id                            bigserial not null,
  version                       integer not null,
  constraint pk_sa_car primary key (id)
);

create table sp_car_car (
  id                            bigserial not null,
  version                       integer not null,
  constraint pk_sp_car_car primary key (id)
);

create table sp_car_car_wheels (
  car                           bigint not null,
  wheel                         bigint not null,
  constraint pk_sp_car_car_wheels primary key (car,wheel)
);

create table car_accessory (
  id                            serial not null,
  name                          varchar(255),
  car_id                        integer,
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  version                       bigint not null,
  constraint pk_car_accessory primary key (id)
);

create table configuration (
  type                          varchar(31) not null,
  ID                            serial not null,
  name                          varchar(255),
  configurations_ID             integer,
  group_name                    varchar(255),
  product_name                  varchar(255),
  constraint pk_configuration primary key (ID)
);

create table configurations (
  ID                            serial not null,
  constraint pk_configurations primary key (ID)
);

create table contact (
  id                            serial not null,
  first_name                    varchar(255),
  last_name                     varchar(255),
  phone                         varchar(255),
  mobile                        varchar(255),
  email                         varchar(255),
  customer_id                   integer not null,
  group_id                      integer,
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  constraint pk_contact primary key (id)
);

create table contact_group (
  id                            serial not null,
  name                          varchar(255),
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  version                       bigint not null,
  constraint pk_contact_group primary key (id)
);

create table contact_note (
  id                            serial not null,
  contact_id                    integer,
  title                         varchar(255),
  note                          text,
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  version                       bigint not null,
  constraint pk_contact_note primary key (id)
);

create table c_conversation (
  id                            bigserial not null,
  title                         varchar(255),
  isopen                        boolean,
  group_id                      bigint,
  version                       bigint not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_c_conversation primary key (id)
);

create table o_country (
  code                          varchar(2) not null,
  name                          varchar(60),
  constraint pk_o_country primary key (code)
);

create table o_customer (
  id                            serial not null,
  status                        varchar(1),
  name                          varchar(40) not null,
  smallnote                     varchar(100),
  anniversary                   date,
  billing_address_id            smallint,
  shipping_address_id           smallint,
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  version                       bigint not null,
  constraint ck_o_customer_status check (status in ('N','A','I')),
  constraint pk_o_customer primary key (id)
);

create table dexh_entity (
  oid                           bigserial not null,
  exhange_rate                  decimal(38),
  exhange_cmoney_amount         decimal(38),
  exhange_cmoney_currency       varchar(3),
  last_updated                  timestamp not null,
  constraint pk_dexh_entity primary key (oid)
);

create table dperson (
  id                            bigserial not null,
  first_name                    varchar(255),
  last_name                     varchar(255),
  salary                        decimal(38),
  a_amt                         decimal(38),
  a_curr                        varchar(3),
  i_start                       timestamp,
  i_end                         timestamp,
  constraint pk_dperson primary key (id)
);

create table rawinherit_data (
  id                            bigserial not null,
  number                        integer,
  constraint pk_rawinherit_data primary key (id)
);

create table e_basic (
  id                            serial not null,
  status                        varchar(1),
  name                          varchar(255),
  description                   varchar(255),
  some_date                     timestamp,
  constraint ck_e_basic_status check (status in ('N','A','I')),
  constraint pk_e_basic primary key (id)
);

create table ebasic_clob (
  id                            bigserial not null,
  name                          varchar(255),
  title                         varchar(255),
  description                   text,
  last_update                   timestamp not null,
  constraint pk_ebasic_clob primary key (id)
);

create table ebasic_clob_fetch_eager (
  id                            bigserial not null,
  name                          varchar(255),
  title                         varchar(255),
  description                   text,
  last_update                   timestamp not null,
  constraint pk_ebasic_clob_fetch_eager primary key (id)
);

create table ebasic_clob_no_ver (
  id                            bigserial not null,
  name                          varchar(255),
  description                   text,
  constraint pk_ebasic_clob_no_ver primary key (id)
);

create table e_basicenc (
  id                            serial not null,
  name                          varchar(255),
  description                   bytea,
  dob                           bytea,
  last_update                   timestamp,
  constraint pk_e_basicenc primary key (id)
);

create table e_basicenc_bin (
  id                            serial not null,
  name                          varchar(255),
  description                   varchar(255),
  data                          bytea,
  some_time                     bytea,
  last_update                   timestamp not null,
  constraint pk_e_basicenc_bin primary key (id)
);

create table e_basic_enum_id (
  status                        varchar(1) not null,
  name                          varchar(255),
  description                   varchar(255),
  constraint ck_e_basic_enum_id_status check (status in ('N','A','I')),
  constraint pk_e_basic_enum_id primary key (status)
);

create table ebasic_json_map (
  id                            bigserial not null,
  name                          varchar(255),
  content                       json,
  version                       bigint not null,
  constraint pk_ebasic_json_map primary key (id)
);

create table ebasic_json_map_blob (
  id                            bigserial not null,
  name                          varchar(255),
  content                       bytea,
  version                       bigint not null,
  constraint pk_ebasic_json_map_blob primary key (id)
);

create table ebasic_json_map_json_b (
  id                            bigserial not null,
  name                          varchar(255),
  content                       jsonb,
  version                       bigint not null,
  constraint pk_ebasic_json_map_json_b primary key (id)
);

create table ebasic_json_map_varchar (
  id                            bigserial not null,
  name                          varchar(255),
  content                       varchar(3000),
  version                       bigint not null,
  constraint pk_ebasic_json_map_varchar primary key (id)
);

create table ebasic_json_node (
  id                            bigserial not null,
  name                          varchar(255),
  content                       json,
  version                       bigint not null,
  constraint pk_ebasic_json_node primary key (id)
);

create table ebasic_json_node_blob (
  id                            bigserial not null,
  name                          varchar(255),
  content                       bytea,
  version                       bigint not null,
  constraint pk_ebasic_json_node_blob primary key (id)
);

create table ebasic_json_node_json_b (
  id                            bigserial not null,
  name                          varchar(255),
  content                       jsonb,
  version                       bigint not null,
  constraint pk_ebasic_json_node_json_b primary key (id)
);

create table ebasic_json_node_varchar (
  id                            bigserial not null,
  name                          varchar(255),
  content                       varchar(1000),
  version                       bigint not null,
  constraint pk_ebasic_json_node_varchar primary key (id)
);

create table e_basic_ndc (
  id                            serial not null,
  name                          varchar(255),
  constraint pk_e_basic_ndc primary key (id)
);

create table e_basicver (
  id                            serial not null,
  name                          varchar(255),
  description                   varchar(255),
  other                         varchar(255),
  last_update                   timestamp not null,
  constraint pk_e_basicver primary key (id)
);

create table e_basic_withlife (
  id                            bigserial not null,
  name                          varchar(255),
  version                       bigint not null,
  constraint pk_e_basic_withlife primary key (id)
);

create table e_basicverucon (
  id                            serial not null,
  name                          varchar(255),
  other                         varchar(255),
  other_one                     varchar(255),
  description                   varchar(255),
  last_update                   timestamp not null,
  constraint uq_e_basicverucon_name unique (name),
  constraint pk_e_basicverucon primary key (id)
);

create table eemb_inner (
  id                            serial not null,
  nome_inner                    varchar(255),
  outer_id                      integer,
  update_count                  integer not null,
  constraint pk_eemb_inner primary key (id)
);

create table eemb_outer (
  id                            serial not null,
  nome_outer                    varchar(255),
  date1                         timestamp,
  date2                         timestamp,
  update_count                  integer not null,
  constraint pk_eemb_outer primary key (id)
);

create table egen_props (
  id                            bigserial not null,
  name                          varchar(255),
  version                       bigint not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  ts_created                    timestamp not null,
  ts_updated                    timestamp not null,
  ldt_created                   timestamp not null,
  ldt_updated                   timestamp not null,
  odt_created                   timestamp not null,
  odt_updated                   timestamp not null,
  zdt_created                   timestamp not null,
  zdt_updated                   timestamp not null,
  long_created                  bigint not null,
  long_updated                  bigint not null,
  constraint pk_egen_props primary key (id)
);

create table einvoice (
  id                            bigserial not null,
  date                          timestamp,
  state                         integer,
  person_id                     bigint,
  ship_street                   varchar(255),
  ship_suburb                   varchar(255),
  ship_city                     varchar(255),
  street                        varchar(255),
  suburb                        varchar(255),
  city                          varchar(255),
  version                       bigint not null,
  constraint ck_einvoice_state check (state in (0,1,2)),
  constraint pk_einvoice primary key (id)
);

create table e_main (
  id                            serial not null,
  name                          varchar(255),
  description                   varchar(255),
  version                       bigint not null,
  constraint pk_e_main primary key (id)
);

create table enull_collection (
  id                            serial not null,
  constraint pk_enull_collection primary key (id)
);

create table enull_collection_detail (
  id                            serial not null,
  enull_collection_id           integer not null,
  something                     varchar(255),
  constraint pk_enull_collection_detail primary key (id)
);

create table eopt_one_a (
  id                            serial not null,
  name_for_a                    varchar(255),
  b_id                          integer,
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  version                       bigint not null,
  constraint pk_eopt_one_a primary key (id)
);

create table eopt_one_b (
  id                            serial not null,
  name_for_b                    varchar(255),
  c_id                          integer not null,
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  version                       bigint not null,
  constraint pk_eopt_one_b primary key (id)
);

create table eopt_one_c (
  id                            serial not null,
  name_for_c                    varchar(255),
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  version                       bigint not null,
  constraint pk_eopt_one_c primary key (id)
);

create table eperson (
  id                            bigserial not null,
  name                          varchar(255),
  notes                         varchar(255),
  street                        varchar(255),
  suburb                        varchar(255),
  city                          varchar(255),
  version                       bigint not null,
  constraint pk_eperson primary key (id)
);

create table esimple (
  usertypeid                    serial not null,
  name                          varchar(255),
  constraint pk_esimple primary key (usertypeid)
);

create table esome_type (
  id                            serial not null,
  currency                      varchar(3),
  locale                        varchar(20),
  time_zone                     varchar(20),
  constraint pk_esome_type primary key (id)
);

create table etrans_many (
  id                            serial not null,
  name                          varchar(255),
  constraint pk_etrans_many primary key (id)
);

create table evanilla_collection (
  id                            serial not null,
  constraint pk_evanilla_collection primary key (id)
);

create table evanilla_collection_detail (
  id                            serial not null,
  evanilla_collection_id        integer not null,
  something                     varchar(255),
  constraint pk_evanilla_collection_detail primary key (id)
);

create table ewho_props (
  id                            bigserial not null,
  name                          varchar(255),
  who_created                   varchar(255) not null,
  who_modified                  varchar(255) not null,
  version                       bigint not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_ewho_props primary key (id)
);

create table e_withinet (
  id                            bigserial not null,
  name                          varchar(255),
  inet_address                  varchar(50),
  version                       bigint not null,
  constraint pk_e_withinet primary key (id)
);

create table td_child (
  child_id                      serial not null,
  child_name                    varchar(255),
  parent_id                     integer not null,
  constraint pk_td_child primary key (child_id)
);

create table td_parent (
  parent_type                   varchar(31) not null,
  parent_id                     serial not null,
  parent_name                   varchar(255),
  extended_name                 varchar(255),
  constraint pk_td_parent primary key (parent_id)
);

create table feature_desc (
  id                            serial not null,
  name                          varchar(255),
  description                   varchar(255),
  constraint pk_feature_desc primary key (id)
);

create table f_first (
  id                            bigserial not null,
  name                          varchar(255),
  constraint pk_f_first primary key (id)
);

create table foo (
  foo_id                        serial not null,
  important_text                varchar(255),
  version                       integer not null,
  constraint pk_foo primary key (foo_id)
);

create table gen_key_identity (
  id                            bigserial not null,
  description                   varchar(255),
  constraint pk_gen_key_identity primary key (id)
);

create table gen_key_sequence (
  id                            bigserial not null,
  description                   varchar(255),
  constraint pk_gen_key_sequence primary key (id)
);

create table gen_key_table (
  id                            bigserial not null,
  description                   varchar(255),
  constraint pk_gen_key_table primary key (id)
);

create table c_group (
  id                            bigserial not null,
  inactive                      boolean,
  name                          varchar(255),
  version                       bigint not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_c_group primary key (id)
);

create table imrelated (
  id                            bigserial not null,
  name                          varchar(255),
  owner_id                      bigint not null,
  constraint pk_imrelated primary key (id)
);

create table imroot (
  dtype                         varchar(10) not null,
  id                            bigserial not null,
  title                         varchar(255),
  when_title                    timestamp,
  name                          varchar(255),
  constraint pk_imroot primary key (id)
);

create table ixresource (
  dtype                         varchar(255),
  id                            bytea not null,
  name                          varchar(255),
  constraint pk_ixresource primary key (id)
);

create table info_company (
  id                            bigserial not null,
  name                          varchar(255),
  version                       bigint not null,
  constraint pk_info_company primary key (id)
);

create table info_contact (
  id                            bigserial not null,
  name                          varchar(255),
  company_id                    bigint not null,
  version                       bigint not null,
  constraint pk_info_contact primary key (id)
);

create table info_customer (
  id                            bigserial not null,
  name                          varchar(255),
  company_id                    bigint,
  version                       bigint not null,
  constraint uq_info_customer_company_id unique (company_id),
  constraint pk_info_customer primary key (id)
);

create table inner_report (
  id                            bigserial not null,
  name                          varchar(255),
  forecast_id                   bigint,
  constraint uq_inner_report_forecast_id unique (forecast_id),
  constraint pk_inner_report primary key (id)
);

create table drel_invoice (
  id                            bigserial not null,
  booking                       bigint,
  version                       integer not null,
  constraint pk_drel_invoice primary key (id)
);

create table item (
  customer                      integer not null,
  itemNumber                    varchar(255) not null,
  description                   varchar(255),
  units                         varchar(255),
  type                          integer,
  region                        integer,
  DATE_MODIFIED                 timestamp,
  DATE_CREATED                  timestamp,
  MODIFIED_BY                   varchar(255),
  CREATED_BY                    varchar(255),
  version                       bigint not null,
  constraint pk_item primary key (customer,itemNumber)
);

create table level1 (
  id                            bigserial not null,
  name                          varchar(255),
  constraint pk_level1 primary key (id)
);

create table level1_level4 (
  level1_id                     bigint not null,
  level4_id                     bigint not null,
  constraint pk_level1_level4 primary key (level1_id,level4_id)
);

create table level1_level2 (
  level1_id                     bigint not null,
  level2_id                     bigint not null,
  constraint pk_level1_level2 primary key (level1_id,level2_id)
);

create table level2 (
  id                            bigserial not null,
  name                          varchar(255),
  constraint pk_level2 primary key (id)
);

create table level2_level3 (
  level2_id                     bigint not null,
  level3_id                     bigint not null,
  constraint pk_level2_level3 primary key (level2_id,level3_id)
);

create table level3 (
  id                            bigserial not null,
  name                          varchar(255),
  constraint pk_level3 primary key (id)
);

create table level4 (
  id                            bigserial not null,
  name                          varchar(255),
  constraint pk_level4 primary key (id)
);

create table la_attr_value (
  id                            serial not null,
  name                          varchar(255),
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  version                       bigint not null,
  constraint pk_la_attr_value primary key (id)
);

create table la_attr_value_attribute (
  la_attr_value_id              integer not null,
  attribute_id                  integer not null,
  constraint pk_la_attr_value_attribute primary key (la_attr_value_id,attribute_id)
);

create table mmedia (
  type                          varchar(31) not null,
  id                            bigserial not null,
  url                           varchar(255),
  note                          varchar(255),
  constraint pk_mmedia primary key (id)
);

create table non_updateprop (
  id                            serial not null,
  non_enum                      varchar(5),
  name                          varchar(255),
  note                          varchar(255),
  constraint ck_non_updateprop_non_enum check (non_enum in ('BEGIN','END')),
  constraint pk_non_updateprop primary key (id)
);

create table mprinter (
  id                            bigserial not null,
  name                          varchar(255),
  flags                         bigint not null,
  current_state_id              bigint,
  last_swap_cyan_id             bigint,
  last_swap_magenta_id          bigint,
  last_swap_yellow_id           bigint,
  last_swap_black_id            bigint,
  version                       bigint not null,
  constraint uq_mprinter_last_swap_cyan_id unique (last_swap_cyan_id),
  constraint uq_mprinter_last_swap_magenta_id unique (last_swap_magenta_id),
  constraint uq_mprinter_last_swap_yellow_id unique (last_swap_yellow_id),
  constraint uq_mprinter_last_swap_black_id unique (last_swap_black_id),
  constraint pk_mprinter primary key (id)
);

create table mprinter_state (
  id                            bigserial not null,
  flags                         bigint,
  printer_id                    bigint,
  version                       bigint not null,
  constraint pk_mprinter_state primary key (id)
);

create table mprofile (
  id                            bigserial not null,
  picture_id                    bigint,
  name                          varchar(255),
  constraint pk_mprofile primary key (id)
);

create table mprotected_construct_bean (
  id                            bigserial not null,
  name                          varchar(255),
  constraint pk_mprotected_construct_bean primary key (id)
);

create table mrole (
  roleid                        serial not null,
  role_name                     varchar(255),
  constraint pk_mrole primary key (roleid)
);

create table mrole_muser (
  mrole_roleid                  integer not null,
  muser_userid                  integer not null,
  constraint pk_mrole_muser primary key (mrole_roleid,muser_userid)
);

create table msome_other (
  id                            bigserial not null,
  name                          varchar(255),
  constraint pk_msome_other primary key (id)
);

create table muser (
  userid                        serial not null,
  user_name                     varchar(255),
  user_type_id                  integer,
  constraint pk_muser primary key (userid)
);

create table muser_type (
  id                            serial not null,
  name                          varchar(255),
  constraint pk_muser_type primary key (id)
);

create table map_super_actual (
  id                            bigserial not null,
  name                          varchar(255),
  when_created                  timestamp not null,
  when_updated                  timestamp not null,
  constraint pk_map_super_actual primary key (id)
);

create table c_message (
  id                            bigserial not null,
  title                         varchar(255),
  body                          varchar(255),
  conversation_id               bigint,
  user_id                       bigint,
  version                       bigint not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_c_message primary key (id)
);

create table mnoc_role (
  role_id                       serial not null,
  role_name                     varchar(255),
  version                       integer not null,
  constraint pk_mnoc_role primary key (role_id)
);

create table mnoc_user (
  user_id                       serial not null,
  user_name                     varchar(255),
  version                       integer not null,
  constraint pk_mnoc_user primary key (user_id)
);

create table mnoc_user_mnoc_role (
  mnoc_user_user_id             integer not null,
  mnoc_role_role_id             integer not null,
  constraint pk_mnoc_user_mnoc_role primary key (mnoc_user_user_id,mnoc_role_role_id)
);

create table mp_role (
  id                            bigserial not null,
  mp_user_id                    bigint not null,
  code                          varchar(255),
  organization_id               bigint,
  constraint pk_mp_role primary key (id)
);

create table mp_user (
  id                            bigserial not null,
  name                          varchar(255),
  constraint pk_mp_user primary key (id)
);

create table my_lob_size (
  id                            serial not null,
  name                          varchar(255),
  my_count                      integer,
  my_lob                        text,
  constraint pk_my_lob_size primary key (id)
);

create table my_lob_size_join_many (
  id                            serial not null,
  something                     varchar(255),
  other                         varchar(255),
  parent_id                     integer,
  constraint pk_my_lob_size_join_many primary key (id)
);

create table noidbean (
  name                          varchar(255),
  subject                       varchar(255),
  when_created                  timestamp not null
);

create table o_cached_bean (
  id                            bigserial not null,
  name                          varchar(255),
  constraint pk_o_cached_bean primary key (id)
);

create table o_cached_bean_country (
  o_cached_bean_id              bigint not null,
  o_country_code                varchar(2) not null,
  constraint pk_o_cached_bean_country primary key (o_cached_bean_id,o_country_code)
);

create table o_cached_bean_child (
  id                            bigserial not null,
  cached_bean_id                bigint,
  constraint pk_o_cached_bean_child primary key (id)
);

create table ocar (
  id                            serial not null,
  vin                           varchar(255),
  name                          varchar(255),
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  version                       bigint not null,
  constraint pk_ocar primary key (id)
);

create table oengine (
  engine_id                     bytea not null,
  short_desc                    varchar(255),
  car_id                        integer,
  version                       integer not null,
  constraint uq_oengine_car_id unique (car_id),
  constraint pk_oengine primary key (engine_id)
);

create table ogear_box (
  id                            bytea not null,
  box_desc                      varchar(255),
  box_size                      integer,
  car_id                        integer,
  version                       integer not null,
  constraint uq_ogear_box_car_id unique (car_id),
  constraint pk_ogear_box primary key (id)
);

create table o_order (
  id                            serial not null,
  status                        integer,
  order_date                    date,
  ship_date                     date,
  kcustomer_id                  integer not null,
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  constraint ck_o_order_status check (status in (0,1,2,3)),
  constraint pk_o_order primary key (id)
);

create table o_order_detail (
  id                            serial not null,
  order_id                      integer,
  order_qty                     integer,
  ship_qty                      integer,
  unit_price                    float,
  product_id                    integer,
  cretime                       timestamp,
  updtime                       timestamp not null,
  constraint pk_o_order_detail primary key (id)
);

create table s_orders (
  uuid                          varchar(255) not null,
  constraint pk_s_orders primary key (uuid)
);

create table s_order_items (
  uuid                          varchar(255) not null,
  product_variant_uuid          varchar(255),
  order_uuid                    varchar(255),
  quantity                      integer,
  amount                        decimal(38),
  constraint pk_s_order_items primary key (uuid)
);

create table or_order_ship (
  id                            serial not null,
  order_id                      integer,
  ship_time                     timestamp,
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  version                       bigint not null,
  constraint pk_or_order_ship primary key (id)
);

create table oto_child (
  id                            serial not null,
  name                          varchar(255),
  master_id                     bigint,
  constraint uq_oto_child_master_id unique (master_id),
  constraint pk_oto_child primary key (id)
);

create table oto_master (
  id                            bigserial not null,
  name                          varchar(255),
  constraint pk_oto_master primary key (id)
);

create table pfile (
  id                            serial not null,
  name                          varchar(255),
  file_content_id               integer,
  file_content2_id              integer,
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  version                       bigint not null,
  constraint uq_pfile_file_content_id unique (file_content_id),
  constraint uq_pfile_file_content2_id unique (file_content2_id),
  constraint pk_pfile primary key (id)
);

create table pfile_content (
  id                            serial not null,
  content                       bytea,
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  version                       bigint not null,
  constraint pk_pfile_content primary key (id)
);

create table paggview (
  pview_id                      bytea,
  amount                        integer not null,
  constraint uq_paggview_pview_id unique (pview_id)
);

create table pallet_location (
  type                          varchar(31) not null,
  ID                            serial not null,
  ZONE_SID                      integer not null,
  attribute                     varchar(255),
  constraint pk_pallet_location primary key (ID)
);

create table parcel (
  parcelId                      bigserial not null,
  description                   varchar(255),
  constraint pk_parcel primary key (parcelId)
);

create table parcel_location (
  parcelLocId                   bigserial not null,
  location                      varchar(255),
  parcelId                      bigint,
  constraint uq_parcel_location_parcelid unique (parcelId),
  constraint pk_parcel_location primary key (parcelLocId)
);

create table rawinherit_parent (
  type                          varchar(31) not null,
  id                            bigserial not null,
  number                        integer,
  constraint pk_rawinherit_parent primary key (id)
);

create table rawinherit_parent_rawinherit_dat (
  rawinherit_parent_id          bigint not null,
  rawinherit_data_id            bigint not null,
  constraint pk_rawinherit_parent_rawinherit_dat primary key (rawinherit_parent_id,rawinherit_data_id)
);

create table c_participation (
  id                            bigserial not null,
  rating                        integer,
  type                          integer,
  conversation_id               bigint not null,
  user_id                       bigint not null,
  version                       bigint not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint ck_c_participation_type check (type in (0,1)),
  constraint pk_c_participation primary key (id)
);

create table mt_permission (
  id                            bytea not null,
  name                          varchar(255),
  constraint pk_mt_permission primary key (id)
);

create table persistent_file (
  id                            serial not null,
  name                          varchar(255),
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  version                       bigint not null,
  constraint pk_persistent_file primary key (id)
);

create table persistent_file_content (
  id                            serial not null,
  persistent_file_id            integer,
  content                       bytea,
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  version                       bigint not null,
  constraint uq_persistent_file_content_persistent_file_id unique (persistent_file_id),
  constraint pk_persistent_file_content primary key (id)
);

create table PERSONS (
  ID                            bigserial not null,
  SURNAME                       varchar(64) not null,
  NAME                          varchar(64) not null,
  constraint pk_persons primary key (ID)
);

create table person (
  oid                           bigserial not null,
  default_address_oid           bigint,
  version                       integer not null,
  constraint pk_person primary key (oid)
);

create table PHONES (
  ID                            bigserial not null,
  PHONE_NUMBER                  varchar(7) not null,
  PERSON_ID                     bigint not null,
  constraint uq_phones_phone_number unique (PHONE_NUMBER),
  constraint pk_phones primary key (ID)
);

create table o_product (
  id                            serial not null,
  sku                           varchar(20),
  name                          varchar(255),
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  constraint pk_o_product primary key (id)
);

create table pp (
  id                            bytea not null,
  name                          varchar(255),
  value                         varchar(100) not null,
  constraint pk_pp primary key (id)
);

create table pp_to_ww (
  pp_id                         bytea not null,
  ww_id                         bytea not null,
  constraint pk_pp_to_ww primary key (pp_id,ww_id)
);

create table rcustomer (
  company                       varchar(255) not null,
  name                          varchar(255) not null,
  description                   varchar(255),
  constraint pk_rcustomer primary key (company,name)
);

create table r_orders (
  company                       varchar(255) not null,
  order_number                  integer not null,
  customerName                  varchar(255),
  item                          varchar(255),
  constraint pk_r_orders primary key (company,order_number)
);

create table region (
  customer                      integer not null,
  type                          integer not null,
  description                   varchar(255),
  version                       bigint not null,
  constraint pk_region primary key (customer,type)
);

create table ResourceFile (
  id                            varchar(64) not null,
  parentResourceFileId          varchar(64),
  name                          varchar(128) not null,
  constraint pk_resourcefile primary key (id)
);

create table mt_role (
  id                            bytea not null,
  name                          varchar(50),
  tenant_id                     bytea,
  version                       bigint not null,
  constraint pk_mt_role primary key (id)
);

create table mt_role_permission (
  mt_role_id                    bytea not null,
  mt_permission_id              bytea not null,
  constraint pk_mt_role_permission primary key (mt_role_id,mt_permission_id)
);

create table em_role (
  id                            bigserial not null,
  name                          varchar(255),
  constraint pk_em_role primary key (id)
);

create table f_second (
  id                            bigserial not null,
  first                         bigint,
  title                         varchar(255),
  constraint uq_f_second_first unique (first),
  constraint pk_f_second primary key (id)
);

create table section (
  id                            serial not null,
  article_id                    integer,
  type                          integer,
  content                       text,
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  version                       bigint not null,
  constraint ck_section_type check (type in (0,1)),
  constraint pk_section primary key (id)
);

create table self_parent (
  id                            bigserial not null,
  name                          varchar(255),
  parent_id                     bigint,
  version                       bigint not null,
  constraint pk_self_parent primary key (id)
);

create table self_ref_customer (
  id                            bigserial not null,
  name                          varchar(255),
  referred_by_id                bigint,
  constraint pk_self_ref_customer primary key (id)
);

create table self_ref_example (
  id                            bigserial not null,
  name                          varchar(255) not null,
  parent_id                     bigint,
  constraint pk_self_ref_example primary key (id)
);

create table some_enum_bean (
  id                            bigserial not null,
  some_enum                     integer,
  name                          varchar(255),
  constraint ck_some_enum_bean_some_enum check (some_enum in (0,1)),
  constraint pk_some_enum_bean primary key (id)
);

create table some_file_bean (
  id                            bigserial not null,
  name                          varchar(255),
  content                       bytea,
  version                       bigint not null,
  constraint pk_some_file_bean primary key (id)
);

create table some_new_types_bean (
  id                            bigserial not null,
  dow                           integer,
  mth                           integer,
  yr                            integer,
  yr_mth                        date,
  local_date                    date,
  local_date_time               timestamp,
  offset_date_time              timestamp,
  zoned_date_time               timestamp,
  instant                       timestamp,
  zone_id                       varchar(60),
  zone_offset                   varchar(60),
  version                       bigint not null,
  constraint ck_some_new_types_bean_dow check (dow in ('1','2','3','4','5','6','7')),
  constraint ck_some_new_types_bean_mth check (mth in ('1','2','3','4','5','6','7','8','9','10','11','12')),
  constraint pk_some_new_types_bean primary key (id)
);

create table some_period_bean (
  id                            bigserial not null,
  period_years                  integer,
  period_months                 integer,
  period_days                   integer,
  anniversary                   date,
  version                       bigint not null,
  constraint pk_some_period_bean primary key (id)
);

create table stockforecast (
  type                          varchar(31) not null,
  id                            bigserial not null,
  inner_report_id               bigint,
  constraint pk_stockforecast primary key (id)
);

create table sub_section (
  id                            serial not null,
  section_id                    integer,
  title                         varchar(255),
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  version                       bigint not null,
  constraint pk_sub_section primary key (id)
);

create table sub_type (
  sub_type_id                   serial not null,
  description                   varchar(255),
  version                       bigint not null,
  constraint pk_sub_type primary key (sub_type_id)
);

create table tbytes_only (
  id                            serial not null,
  content                       bytea,
  constraint pk_tbytes_only primary key (id)
);

create table tcar (
  type                          varchar(31) not null,
  plate_no                      varchar(255) not null,
  truckLoad                     bigint,
  constraint pk_tcar primary key (plate_no)
);

create table tint_root (
  my_type                       integer not null,
  id                            serial not null,
  name                          varchar(255),
  child_property                varchar(255),
  constraint pk_tint_root primary key (id)
);

create table tjoda_entity (
  id                            serial not null,
  local_time                    time,
  constraint pk_tjoda_entity primary key (id)
);

create table t_mapsuper1 (
  id                            serial not null,
  something                     varchar(255),
  name                          varchar(255),
  version                       integer not null,
  constraint pk_t_mapsuper1 primary key (id)
);

create table t_oneb (
  id                            serial not null,
  name                          varchar(255),
  description                   varchar(255),
  active                        boolean,
  constraint pk_t_oneb primary key (id)
);

create table t_detail_with_other_namexxxyy (
  id                            serial not null,
  name                          varchar(255),
  description                   varchar(255),
  active                        boolean,
  master_id                     integer,
  constraint pk_t_detail_with_other_namexxxyy primary key (id)
);

create table ts_detail_two (
  id                            serial not null,
  name                          varchar(255),
  description                   varchar(255),
  active                        boolean,
  master_id                     integer,
  constraint pk_ts_detail_two primary key (id)
);

create table t_atable_thatisrelatively (
  id                            serial not null,
  name                          varchar(255),
  description                   varchar(255),
  active                        boolean,
  constraint pk_t_atable_thatisrelatively primary key (id)
);

create table ts_master_two (
  id                            serial not null,
  name                          varchar(255),
  description                   varchar(255),
  active                        boolean,
  constraint pk_ts_master_two primary key (id)
);

create table tuuid_entity (
  id                            bytea not null,
  name                          varchar(255),
  constraint pk_tuuid_entity primary key (id)
);

create table twheel (
  id                            bigserial not null,
  owner_plate_no                varchar(255) not null,
  constraint pk_twheel primary key (id)
);

create table twith_pre_insert (
  id                            serial not null,
  name                          varchar(255) not null,
  title                         varchar(255),
  constraint pk_twith_pre_insert primary key (id)
);

create table mt_tenant (
  id                            bytea not null,
  name                          varchar(255),
  version                       bigint not null,
  constraint pk_mt_tenant primary key (id)
);

create table sa_tire (
  id                            bigserial not null,
  version                       integer not null,
  constraint pk_sa_tire primary key (id)
);

create table tire (
  id                            bigserial not null,
  wheel                         bigint,
  version                       integer not null,
  constraint uq_tire_wheel unique (wheel),
  constraint pk_tire primary key (id)
);

create table trip (
  id                            serial not null,
  vehicle_driver_id             integer,
  destination                   varchar(255),
  address_id                    smallint,
  star_date                     timestamp,
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  version                       bigint not null,
  constraint pk_trip primary key (id)
);

create table truck_ref (
  id                            serial not null,
  something                     varchar(255),
  constraint pk_truck_ref primary key (id)
);

create table type (
  customer                      integer not null,
  type                          integer not null,
  description                   varchar(255),
  sub_type_id                   integer,
  version                       bigint not null,
  constraint pk_type primary key (customer,type)
);

create table ut_detail (
  id                            serial not null,
  utmaster_id                   integer not null,
  name                          varchar(255),
  qty                           integer,
  amount                        float,
  version                       integer not null,
  constraint pk_ut_detail primary key (id)
);

create table ut_master (
  id                            serial not null,
  name                          varchar(255),
  description                   varchar(255),
  version                       integer not null,
  constraint pk_ut_master primary key (id)
);

create table uuone (
  id                            bytea not null,
  name                          varchar(255),
  constraint pk_uuone primary key (id)
);

create table uutwo (
  id                            bytea not null,
  name                          varchar(255),
  master_id                     bytea,
  constraint pk_uutwo primary key (id)
);

create table em_user (
  id                            bigserial not null,
  name                          varchar(255),
  constraint pk_em_user primary key (id)
);

create table tx_user (
  id                            bigserial not null,
  name                          varchar(255),
  constraint pk_tx_user primary key (id)
);

create table c_user (
  id                            bigserial not null,
  inactive                      boolean,
  name                          varchar(255),
  email                         varchar(255),
  group_id                      bigint,
  version                       bigint not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_c_user primary key (id)
);

create table oto_user (
  id                            bigserial not null,
  name                          varchar(255),
  account_id                    bigint not null,
  version                       bigint not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint uq_oto_user_account_id unique (account_id),
  constraint pk_oto_user primary key (id)
);

create table em_user_role (
  user_id                       bigint not null,
  role_id                       bigint not null,
  constraint pk_em_user_role primary key (user_id,role_id)
);

create table vehicle (
  dtype                         varchar(3) not null,
  id                            serial not null,
  license_number                varchar(255),
  registration_date             timestamp,
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  version                       bigint not null,
  truck_ref_id                  integer,
  capacity                      float,
  driver                        varchar(255),
  car_ref_id                    integer,
  constraint pk_vehicle primary key (id)
);

create table vehicle_driver (
  id                            serial not null,
  name                          varchar(255),
  vehicle_id                    integer,
  address_id                    smallint,
  license_issued_on             timestamp,
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  version                       bigint not null,
  constraint pk_vehicle_driver primary key (id)
);

create table warehouses (
  ID                            serial not null,
  officeZoneId                  integer,
  constraint pk_warehouses primary key (ID)
);

create table WarehousesShippingZones (
  warehouseId                   integer not null,
  shippingZoneId                integer not null,
  constraint pk_warehousesshippingzones primary key (warehouseId,shippingZoneId)
);

create table sa_wheel (
  id                            bigserial not null,
  tire                          bigint,
  car                           bigint,
  version                       integer not null,
  constraint pk_sa_wheel primary key (id)
);

create table sp_car_wheel (
  id                            bigserial not null,
  version                       integer not null,
  constraint pk_sp_car_wheel primary key (id)
);

create table wheel (
  id                            bigserial not null,
  version                       integer not null,
  constraint pk_wheel primary key (id)
);

create table with_zero (
  id                            bigserial not null,
  name                          varchar(255),
  parent_id                     integer,
  version                       bigint not null,
  constraint pk_with_zero primary key (id)
);

create table parent (
  id                            serial not null,
  constraint pk_parent primary key (id)
);

create table wview (
  id                            bytea not null,
  name                          varchar(255) not null,
  constraint uq_wview_name unique (name),
  constraint pk_wview primary key (id)
);

create table zones (
  type                          varchar(31) not null,
  ID                            serial not null,
  attribute                     varchar(255),
  constraint pk_zones primary key (ID)
);

