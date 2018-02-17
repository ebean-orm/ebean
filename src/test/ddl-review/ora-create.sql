create table asimple_bean (
  id                            number(19) not null,
  name                          varchar2(255),
  constraint pk_asimple_bean primary key (id)
);
create sequence asimple_bean_seq;

create table bar (
  bar_type                      varchar2(31) not null,
  bar_id                        number(10) not null,
  foo_id                        number(10) not null,
  version                       number(10) not null,
  constraint pk_bar primary key (bar_id)
);
create sequence bar_seq;

create table oto_account (
  id                            number(19) not null,
  name                          varchar2(255),
  version                       number(19) not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_oto_account primary key (id)
);
create sequence oto_account_seq;

create table o_address (
  id                            number(5) not null,
  line_1                        varchar2(100),
  line_2                        varchar2(100),
  city                          varchar2(100),
  cretime                       timestamp,
  country_code                  varchar2(2),
  updtime                       timestamp not null,
  constraint pk_o_address primary key (id)
);
create sequence o_address_seq;

create table address (
  oid                           number(19) not null,
  street                        varchar2(255),
  version                       number(10) not null,
  constraint pk_address primary key (oid)
);
create sequence address_seq;

create table animals (
  species                       varchar2(31) not null,
  id                            number(19) not null,
  shelter_id                    number(19),
  version                       number(19) not null,
  name                          varchar2(255),
  registration_number           varchar2(255),
  date_of_birth                 date,
  constraint pk_animals primary key (id)
);
create sequence animals_seq;

create table animal_shelter (
  id                            number(19) not null,
  name                          varchar2(255),
  version                       number(19) not null,
  constraint pk_animal_shelter primary key (id)
);
create sequence animal_shelter_seq;

create table article (
  id                            number(10) not null,
  name                          varchar2(255),
  author                        varchar2(255),
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  version                       number(19) not null,
  constraint pk_article primary key (id)
);
create sequence article_seq;

create table attribute (
  option_type                   number(31) not null,
  id                            number(10) not null,
  attribute_holder_id           number(10),
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  version                       number(19) not null,
  constraint pk_attribute primary key (id)
);
create sequence attribute_seq;

create table attribute_holder (
  id                            number(10) not null,
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  version                       number(19) not null,
  constraint pk_attribute_holder primary key (id)
);
create sequence attribute_holder_seq;

create table audit_log (
  id                            number(19) not null,
  description                   varchar2(255),
  modified_description          varchar2(255),
  constraint pk_audit_log primary key (id)
);
create sequence audit_log_seq;

create table bbookmark (
  id                            number(10) not null,
  bookmark_reference            varchar2(255),
  user_id                       number(10),
  constraint pk_bbookmark primary key (id)
);
create sequence bbookmark_seq;

create table bbookmark_user (
  id                            number(10) not null,
  name                          varchar2(255),
  password                      varchar2(255),
  email_address                 varchar2(255),
  country                       varchar2(255),
  constraint pk_bbookmark_user primary key (id)
);
create sequence bbookmark_user_seq;

create table bsimple_with_gen (
  id                            number(10) not null,
  name                          varchar2(255),
  constraint pk_bsimple_with_gen primary key (id)
);
create sequence bsimple_with_gen_seq;

create table bwith_qident (
  id                            number(10) not null,
  "Name"                        varchar2(255),
  last_updated                  timestamp not null,
  constraint uq_bwith_qident_name unique ("Name"),
  constraint pk_bwith_qident primary key (id)
);
create sequence bwith_qident_seq;

create table basic_joda_entity (
  id                            number(19) not null,
  name                          varchar2(255),
  created                       timestamp not null,
  updated                       timestamp not null,
  version                       timestamp not null,
  constraint pk_basic_joda_entity primary key (id)
);
create sequence basic_joda_entity_seq;

create table bean_with_time_zone (
  id                            number(19) not null,
  name                          varchar2(255),
  timezone                      varchar2(20),
  constraint pk_bean_with_time_zone primary key (id)
);
create sequence bean_with_time_zone_seq;

create table drel_booking (
  id                            number(19) not null,
  agent_invoice                 number(19),
  client_invoice                number(19),
  version                       number(10) not null,
  constraint uq_drel_booking_agent_invoice unique (agent_invoice),
  constraint uq_drel_booking_client_invo_2 unique (client_invoice),
  constraint pk_drel_booking primary key (id)
);
create sequence drel_booking_seq;

create table ckey_assoc (
  id                            number(10) not null,
  assoc_one                     varchar2(255),
  constraint pk_ckey_assoc primary key (id)
);
create sequence ckey_assoc_seq;

create table ckey_detail (
  id                            number(10) not null,
  something                     varchar2(255),
  one_key                       number(10),
  two_key                       varchar2(127),
  constraint pk_ckey_detail primary key (id)
);
create sequence ckey_detail_seq;

create table ckey_parent (
  one_key                       number(10) not null,
  two_key                       varchar2(127) not null,
  name                          varchar2(255),
  assoc_id                      number(10),
  version                       number(10) not null,
  constraint pk_ckey_parent primary key (one_key,two_key)
);

create table calculation_result (
  id                            number(10) not null,
  charge                        number(19,4),
  product_configuration_id      number(10),
  group_configuration_id        number(10),
  constraint pk_calculation_result primary key (id)
);
create sequence calculation_result_seq;

create table cao_bean (
  x_cust_id                     number(10) not null,
  x_type_id                     number(10) not null,
  description                   varchar2(255),
  version                       number(19) not null,
  constraint pk_cao_bean primary key (x_cust_id,x_type_id)
);

create table sa_car (
  id                            number(19) not null,
  version                       number(10) not null,
  constraint pk_sa_car primary key (id)
);
create sequence sa_car_seq;

create table sp_car_car (
  id                            number(19) not null,
  version                       number(10) not null,
  constraint pk_sp_car_car primary key (id)
);
create sequence sp_car_car_seq;

create table sp_car_car_wheels (
  car                           number(19) not null,
  wheel                         number(19) not null,
  constraint pk_sp_car_car_wheels primary key (car,wheel)
);

create table car_accessory (
  id                            number(10) not null,
  name                          varchar2(255),
  car_id                        number(10),
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  version                       number(19) not null,
  constraint pk_car_accessory primary key (id)
);
create sequence car_accessory_seq;

create table configuration (
  type                          varchar2(31) not null,
  id                            number(10) not null,
  name                          varchar2(255),
  configurations_id             number(10),
  group_name                    varchar2(255),
  product_name                  varchar2(255),
  constraint pk_configuration primary key (id)
);
create sequence configuration_seq;

create table configurations (
  id                            number(10) not null,
  name                          varchar2(255),
  constraint pk_configurations primary key (id)
);
create sequence configurations_seq;

create table contact (
  id                            number(10) not null,
  first_name                    varchar2(255),
  last_name                     varchar2(255),
  phone                         varchar2(255),
  mobile                        varchar2(255),
  email                         varchar2(255),
  customer_id                   number(10) not null,
  group_id                      number(10),
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  constraint pk_contact primary key (id)
);
create sequence contact_seq;

create table contact_group (
  id                            number(10) not null,
  name                          varchar2(255),
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  version                       number(19) not null,
  constraint pk_contact_group primary key (id)
);
create sequence contact_group_seq;

create table contact_note (
  id                            number(10) not null,
  contact_id                    number(10),
  title                         varchar2(255),
  note                          clob,
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  version                       number(19) not null,
  constraint pk_contact_note primary key (id)
);
create sequence contact_note_seq;

create table c_conversation (
  id                            number(19) not null,
  title                         varchar2(255),
  isopen                        number(1),
  group_id                      number(19),
  version                       number(19) not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_c_conversation primary key (id)
);
create sequence c_conversation_seq;

create table o_country (
  code                          varchar2(2) not null,
  name                          varchar2(60),
  constraint pk_o_country primary key (code)
);

create table o_customer (
  id                            number(10) not null,
  status                        varchar2(1),
  name                          varchar2(40) not null,
  smallnote                     varchar2(100),
  anniversary                   date,
  billing_address_id            number(5),
  shipping_address_id           number(5),
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  version                       number(19) not null,
  constraint ck_o_customer_status check (status in ('N','A','I')),
  constraint pk_o_customer primary key (id)
);
create sequence o_customer_seq;

create table dexh_entity (
  oid                           number(19) not null,
  exhange_rate                  number(38),
  exhange_cmoney_amount         number(38),
  exhange_cmoney_currency       varchar2(3),
  last_updated                  timestamp not null,
  constraint pk_dexh_entity primary key (oid)
);
create sequence dexh_entity_seq;

create table dperson (
  id                            number(19) not null,
  first_name                    varchar2(255),
  last_name                     varchar2(255),
  salary                        number(38),
  a_amt                         number(38),
  a_curr                        varchar2(3),
  i_start                       timestamp,
  i_end                         timestamp,
  constraint pk_dperson primary key (id)
);
create sequence dperson_seq;

create table rawinherit_data (
  id                            number(19) not null,
  val                           number(10),
  constraint pk_rawinherit_data primary key (id)
);
create sequence rawinherit_data_seq;

create table e_basic (
  id                            number(10) not null,
  status                        varchar2(1),
  name                          varchar2(255),
  description                   varchar2(255),
  some_date                     timestamp,
  constraint ck_e_basic_status check (status in ('N','A','I')),
  constraint pk_e_basic primary key (id)
);
create sequence e_basic_seq;

create table ebasic_clob (
  id                            number(19) not null,
  name                          varchar2(255),
  title                         varchar2(255),
  description                   clob,
  last_update                   timestamp not null,
  constraint pk_ebasic_clob primary key (id)
);
create sequence ebasic_clob_seq;

create table ebasic_clob_fetch_eager (
  id                            number(19) not null,
  name                          varchar2(255),
  title                         varchar2(255),
  description                   clob,
  last_update                   timestamp not null,
  constraint pk_ebasic_clob_fetch_eager primary key (id)
);
create sequence ebasic_clob_fetch_eager_seq;

create table ebasic_clob_no_ver (
  id                            number(19) not null,
  name                          varchar2(255),
  description                   clob,
  constraint pk_ebasic_clob_no_ver primary key (id)
);
create sequence ebasic_clob_no_ver_seq;

create table e_basicenc (
  id                            number(10) not null,
  name                          varchar2(255),
  description                   raw(80),
  dob                           raw(20),
  last_update                   timestamp,
  constraint pk_e_basicenc primary key (id)
);
create sequence e_basicenc_seq;

create table e_basicenc_bin (
  id                            number(10) not null,
  name                          varchar2(255),
  description                   varchar2(255),
  data                          blob,
  some_time                     raw(255),
  last_update                   timestamp not null,
  constraint pk_e_basicenc_bin primary key (id)
);
create sequence e_basicenc_bin_seq;

create table e_basic_enum_id (
  status                        varchar2(1) not null,
  name                          varchar2(255),
  description                   varchar2(255),
  constraint ck_e_basic_enum_id_status check (status in ('N','A','I')),
  constraint pk_e_basic_enum_id primary key (status)
);

create table ebasic_json_map (
  id                            number(19) not null,
  name                          varchar2(255),
  content                       clob,
  version                       number(19) not null,
  constraint pk_ebasic_json_map primary key (id)
);
create sequence ebasic_json_map_seq;

create table ebasic_json_map_blob (
  id                            number(19) not null,
  name                          varchar2(255),
  content                       blob,
  version                       number(19) not null,
  constraint pk_ebasic_json_map_blob primary key (id)
);
create sequence ebasic_json_map_blob_seq;

create table ebasic_json_map_clob (
  id                            number(19) not null,
  name                          varchar2(255),
  content                       clob,
  version                       number(19) not null,
  constraint pk_ebasic_json_map_clob primary key (id)
);
create sequence ebasic_json_map_clob_seq;

create table ebasic_json_map_json_b (
  id                            number(19) not null,
  name                          varchar2(255),
  content                       clob,
  version                       number(19) not null,
  constraint pk_ebasic_json_map_json_b primary key (id)
);
create sequence ebasic_json_map_json_b_seq;

create table ebasic_json_map_varchar (
  id                            number(19) not null,
  name                          varchar2(255),
  content                       varchar2(3000),
  version                       number(19) not null,
  constraint pk_ebasic_json_map_varchar primary key (id)
);
create sequence ebasic_json_map_varchar_seq;

create table ebasic_json_node (
  id                            number(19) not null,
  name                          varchar2(255),
  content                       clob,
  version                       number(19) not null,
  constraint pk_ebasic_json_node primary key (id)
);
create sequence ebasic_json_node_seq;

create table ebasic_json_node_blob (
  id                            number(19) not null,
  name                          varchar2(255),
  content                       blob,
  version                       number(19) not null,
  constraint pk_ebasic_json_node_blob primary key (id)
);
create sequence ebasic_json_node_blob_seq;

create table ebasic_json_node_json_b (
  id                            number(19) not null,
  name                          varchar2(255),
  content                       clob,
  version                       number(19) not null,
  constraint pk_ebasic_json_node_json_b primary key (id)
);
create sequence ebasic_json_node_json_b_seq;

create table ebasic_json_node_varchar (
  id                            number(19) not null,
  name                          varchar2(255),
  content                       varchar2(1000),
  version                       number(19) not null,
  constraint pk_ebasic_json_node_varchar primary key (id)
);
create sequence ebasic_json_node_varchar_seq;

create table e_basic_ndc (
  id                            number(10) not null,
  name                          varchar2(255),
  constraint pk_e_basic_ndc primary key (id)
);
create sequence e_basic_ndc_seq;

create table e_basicver (
  id                            number(10) not null,
  name                          varchar2(255),
  description                   varchar2(255),
  other                         varchar2(255),
  last_update                   timestamp not null,
  constraint pk_e_basicver primary key (id)
);
create sequence e_basicver_seq;

create table e_basic_withlife (
  id                            number(19) not null,
  name                          varchar2(255),
  version                       number(19) not null,
  constraint pk_e_basic_withlife primary key (id)
);
create sequence e_basic_withlife_seq;

create table e_basicverucon (
  id                            number(10) not null,
  name                          varchar2(255),
  other                         varchar2(255),
  other_one                     varchar2(255),
  description                   varchar2(255),
  last_update                   timestamp not null,
  constraint uq_e_basicverucon_name unique (name),
  constraint pk_e_basicverucon primary key (id)
);
create sequence e_basicverucon_seq;

create table eemb_inner (
  id                            number(10) not null,
  nome_inner                    varchar2(255),
  outer_id                      number(10),
  update_count                  number(10) not null,
  constraint pk_eemb_inner primary key (id)
);
create sequence eemb_inner_seq;

create table eemb_outer (
  id                            number(10) not null,
  nome_outer                    varchar2(255),
  date1                         timestamp,
  date2                         timestamp,
  update_count                  number(10) not null,
  constraint pk_eemb_outer primary key (id)
);
create sequence eemb_outer_seq;

create table egen_props (
  id                            number(19) not null,
  name                          varchar2(255),
  version                       number(19) not null,
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
  long_created                  number(19) not null,
  long_updated                  number(19) not null,
  constraint pk_egen_props primary key (id)
);
create sequence egen_props_seq;

create table einvoice (
  id                            number(19) not null,
  invoice_date                  timestamp,
  state                         number(10),
  person_id                     number(19),
  ship_street                   varchar2(255),
  ship_suburb                   varchar2(255),
  ship_city                     varchar2(255),
  street                        varchar2(255),
  suburb                        varchar2(255),
  city                          varchar2(255),
  version                       number(19) not null,
  constraint ck_einvoice_state check (state in (0,1,2)),
  constraint pk_einvoice primary key (id)
);
create sequence einvoice_seq;

create table e_main (
  id                            number(10) not null,
  name                          varchar2(255),
  description                   varchar2(255),
  version                       number(19) not null,
  constraint pk_e_main primary key (id)
);
create sequence e_main_seq;

create table enull_collection (
  id                            number(10) not null,
  name                          varchar2(255),
  constraint pk_enull_collection primary key (id)
);
create sequence enull_collection_seq;

create table enull_collection_detail (
  id                            number(10) not null,
  enull_collection_id           number(10) not null,
  something                     varchar2(255),
  constraint pk_enull_collection_detail primary key (id)
);
create sequence enull_collection_detail_seq;

create table eopt_one_a (
  id                            number(10) not null,
  name_for_a                    varchar2(255),
  b_id                          number(10),
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  version                       number(19) not null,
  constraint pk_eopt_one_a primary key (id)
);
create sequence eopt_one_a_seq;

create table eopt_one_b (
  id                            number(10) not null,
  name_for_b                    varchar2(255),
  c_id                          number(10) not null,
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  version                       number(19) not null,
  constraint pk_eopt_one_b primary key (id)
);
create sequence eopt_one_b_seq;

create table eopt_one_c (
  id                            number(10) not null,
  name_for_c                    varchar2(255),
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  version                       number(19) not null,
  constraint pk_eopt_one_c primary key (id)
);
create sequence eopt_one_c_seq;

create table eperson (
  id                            number(19) not null,
  name                          varchar2(255),
  notes                         varchar2(255),
  street                        varchar2(255),
  suburb                        varchar2(255),
  city                          varchar2(255),
  version                       number(19) not null,
  constraint pk_eperson primary key (id)
);
create sequence eperson_seq;

create table esimple (
  usertypeid                    number(10) not null,
  name                          varchar2(255),
  constraint pk_esimple primary key (usertypeid)
);
create sequence esimple_seq;

create table esome_type (
  id                            number(10) not null,
  currency                      varchar2(3),
  locale                        varchar2(20),
  time_zone                     varchar2(20),
  constraint pk_esome_type primary key (id)
);
create sequence esome_type_seq;

create table etrans_many (
  id                            number(10) not null,
  name                          varchar2(255),
  constraint pk_etrans_many primary key (id)
);
create sequence etrans_many_seq;

create table evanilla_collection (
  id                            number(10) not null,
  name                          varchar2(255),
  constraint pk_evanilla_collection primary key (id)
);
create sequence evanilla_collection_seq;

create table evanilla_collection_detail (
  id                            number(10) not null,
  evanilla_collection_id        number(10) not null,
  something                     varchar2(255),
  constraint pk_evanilla_collection_detail primary key (id)
);
create sequence evanilla_collection_detail_seq;

create table ewho_props (
  id                            number(19) not null,
  name                          varchar2(255),
  who_created                   varchar2(255) not null,
  who_modified                  varchar2(255) not null,
  version                       number(19) not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_ewho_props primary key (id)
);
create sequence ewho_props_seq;

create table e_withinet (
  id                            number(19) not null,
  name                          varchar2(255),
  inet_address                  varchar2(50),
  version                       number(19) not null,
  constraint pk_e_withinet primary key (id)
);
create sequence e_withinet_seq;

create table td_child (
  child_id                      number(10) not null,
  child_name                    varchar2(255),
  parent_id                     number(10) not null,
  constraint pk_td_child primary key (child_id)
);
create sequence td_child_seq;

create table td_parent (
  parent_type                   varchar2(31) not null,
  parent_id                     number(10) not null,
  parent_name                   varchar2(255),
  extended_name                 varchar2(255),
  constraint pk_td_parent primary key (parent_id)
);
create sequence td_parent_seq;

create table feature_desc (
  id                            number(10) not null,
  name                          varchar2(255),
  description                   varchar2(255),
  constraint pk_feature_desc primary key (id)
);
create sequence feature_desc_seq;

create table f_first (
  id                            number(19) not null,
  name                          varchar2(255),
  constraint pk_f_first primary key (id)
);
create sequence f_first_seq;

create table foo (
  foo_id                        number(10) not null,
  important_text                varchar2(255),
  version                       number(10) not null,
  constraint pk_foo primary key (foo_id)
);
create sequence foo_seq;

create table gen_key_identity (
  id                            number(19) not null,
  description                   varchar2(255),
  constraint pk_gen_key_identity primary key (id)
);
create sequence gen_key_identity_seq;

create table gen_key_sequence (
  id                            number(19) not null,
  description                   varchar2(255),
  constraint pk_gen_key_sequence primary key (id)
);
create sequence seq;

create table gen_key_table (
  id                            number(19) not null,
  description                   varchar2(255),
  constraint pk_gen_key_table primary key (id)
);
create sequence gen_key_table_seq;

create table c_group (
  id                            number(19) not null,
  inactive                      number(1),
  name                          varchar2(255),
  version                       number(19) not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_c_group primary key (id)
);
create sequence c_group_seq;

create table imrelated (
  id                            number(19) not null,
  name                          varchar2(255),
  owner_id                      number(19) not null,
  constraint pk_imrelated primary key (id)
);
create sequence imrelated_seq;

create table imroot (
  dtype                         varchar2(10) not null,
  id                            number(19) not null,
  title                         varchar2(255),
  when_title                    timestamp,
  name                          varchar2(255),
  constraint pk_imroot primary key (id)
);
create sequence imroot_seq;

create table ixresource (
  dtype                         varchar2(255),
  id                            raw(16) not null,
  name                          varchar2(255),
  constraint pk_ixresource primary key (id)
);

create table info_company (
  id                            number(19) not null,
  name                          varchar2(255),
  version                       number(19) not null,
  constraint pk_info_company primary key (id)
);
create sequence info_company_seq;

create table info_contact (
  id                            number(19) not null,
  name                          varchar2(255),
  company_id                    number(19) not null,
  version                       number(19) not null,
  constraint pk_info_contact primary key (id)
);
create sequence info_contact_seq;

create table info_customer (
  id                            number(19) not null,
  name                          varchar2(255),
  company_id                    number(19),
  version                       number(19) not null,
  constraint uq_info_customer_company_id unique (company_id),
  constraint pk_info_customer primary key (id)
);
create sequence info_customer_seq;

create table inner_report (
  id                            number(19) not null,
  name                          varchar2(255),
  forecast_id                   number(19),
  constraint uq_inner_report_forecast_id unique (forecast_id),
  constraint pk_inner_report primary key (id)
);
create sequence inner_report_seq;

create table drel_invoice (
  id                            number(19) not null,
  booking                       number(19),
  version                       number(10) not null,
  constraint pk_drel_invoice primary key (id)
);
create sequence drel_invoice_seq;

create table item (
  customer                      number(10) not null,
  itemnumber                    varchar2(255) not null,
  description                   varchar2(255),
  units                         varchar2(255),
  type                          number(10),
  region                        number(10),
  date_modified                 timestamp,
  date_created                  timestamp,
  modified_by                   varchar2(255),
  created_by                    varchar2(255),
  version                       number(19) not null,
  constraint pk_item primary key (customer,itemnumber)
);

create table level1 (
  id                            number(19) not null,
  name                          varchar2(255),
  constraint pk_level1 primary key (id)
);
create sequence level1_seq;

create table level1_level4 (
  level1_id                     number(19) not null,
  level4_id                     number(19) not null,
  constraint pk_level1_level4 primary key (level1_id,level4_id)
);

create table level1_level2 (
  level1_id                     number(19) not null,
  level2_id                     number(19) not null,
  constraint pk_level1_level2 primary key (level1_id,level2_id)
);

create table level2 (
  id                            number(19) not null,
  name                          varchar2(255),
  constraint pk_level2 primary key (id)
);
create sequence level2_seq;

create table level2_level3 (
  level2_id                     number(19) not null,
  level3_id                     number(19) not null,
  constraint pk_level2_level3 primary key (level2_id,level3_id)
);

create table level3 (
  id                            number(19) not null,
  name                          varchar2(255),
  constraint pk_level3 primary key (id)
);
create sequence level3_seq;

create table level4 (
  id                            number(19) not null,
  name                          varchar2(255),
  constraint pk_level4 primary key (id)
);
create sequence level4_seq;

create table la_attr_value (
  id                            number(10) not null,
  name                          varchar2(255),
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  version                       number(19) not null,
  constraint pk_la_attr_value primary key (id)
);
create sequence la_attr_value_seq;

create table la_attr_value_attribute (
  la_attr_value_id              number(10) not null,
  attribute_id                  number(10) not null,
  constraint pk_la_attr_value_attribute primary key (la_attr_value_id,attribute_id)
);

create table mmedia (
  type                          varchar2(31) not null,
  id                            number(19) not null,
  url                           varchar2(255),
  note                          varchar2(255),
  constraint pk_mmedia primary key (id)
);
create sequence mmedia_seq;

create table non_updateprop (
  id                            number(10) not null,
  non_enum                      varchar2(5),
  name                          varchar2(255),
  note                          varchar2(255),
  constraint ck_non_updateprop_non_enum check (non_enum in ('BEGIN','END')),
  constraint pk_non_updateprop primary key (id)
);
create sequence non_updateprop_seq;

create table mprinter (
  id                            number(19) not null,
  name                          varchar2(255),
  flags                         number(19) not null,
  current_state_id              number(19),
  last_swap_cyan_id             number(19),
  last_swap_magenta_id          number(19),
  last_swap_yellow_id           number(19),
  last_swap_black_id            number(19),
  version                       number(19) not null,
  constraint uq_mprinter_last_swap_cyan_id unique (last_swap_cyan_id),
  constraint uq_mprinter_last_swap_magen_2 unique (last_swap_magenta_id),
  constraint uq_mprinter_last_swap_yello_3 unique (last_swap_yellow_id),
  constraint uq_mprinter_last_swap_black_4 unique (last_swap_black_id),
  constraint pk_mprinter primary key (id)
);
create sequence mprinter_seq;

create table mprinter_state (
  id                            number(19) not null,
  flags                         number(19),
  printer_id                    number(19),
  version                       number(19) not null,
  constraint pk_mprinter_state primary key (id)
);
create sequence mprinter_state_seq;

create table mprofile (
  id                            number(19) not null,
  picture_id                    number(19),
  name                          varchar2(255),
  constraint pk_mprofile primary key (id)
);
create sequence mprofile_seq;

create table mprotected_construct_bean (
  id                            number(19) not null,
  name                          varchar2(255),
  constraint pk_mprotected_construct_bean primary key (id)
);
create sequence mprotected_construct_bean_seq;

create table mrole (
  roleid                        number(10) not null,
  role_name                     varchar2(255),
  constraint pk_mrole primary key (roleid)
);
create sequence mrole_seq;

create table mrole_muser (
  mrole_roleid                  number(10) not null,
  muser_userid                  number(10) not null,
  constraint pk_mrole_muser primary key (mrole_roleid,muser_userid)
);

create table msome_other (
  id                            number(19) not null,
  name                          varchar2(255),
  constraint pk_msome_other primary key (id)
);
create sequence msome_other_seq;

create table muser (
  userid                        number(10) not null,
  user_name                     varchar2(255),
  user_type_id                  number(10),
  constraint pk_muser primary key (userid)
);
create sequence muser_seq;

create table muser_type (
  id                            number(10) not null,
  name                          varchar2(255),
  constraint pk_muser_type primary key (id)
);
create sequence muser_type_seq;

create table map_super_actual (
  id                            number(19) not null,
  name                          varchar2(255),
  when_created                  timestamp not null,
  when_updated                  timestamp not null,
  constraint pk_map_super_actual primary key (id)
);
create sequence map_super_actual_seq;

create table c_message (
  id                            number(19) not null,
  title                         varchar2(255),
  body                          varchar2(255),
  conversation_id               number(19),
  user_id                       number(19),
  version                       number(19) not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_c_message primary key (id)
);
create sequence c_message_seq;

create table mnoc_role (
  role_id                       number(10) not null,
  role_name                     varchar2(255),
  version                       number(10) not null,
  constraint pk_mnoc_role primary key (role_id)
);
create sequence mnoc_role_seq;

create table mnoc_user (
  user_id                       number(10) not null,
  user_name                     varchar2(255),
  version                       number(10) not null,
  constraint pk_mnoc_user primary key (user_id)
);
create sequence mnoc_user_seq;

create table mnoc_user_mnoc_role (
  mnoc_user_user_id             number(10) not null,
  mnoc_role_role_id             number(10) not null,
  constraint pk_mnoc_user_mnoc_role primary key (mnoc_user_user_id,mnoc_role_role_id)
);

create table mp_role (
  id                            number(19) not null,
  mp_user_id                    number(19) not null,
  code                          varchar2(255),
  organization_id               number(19),
  constraint pk_mp_role primary key (id)
);
create sequence mp_role_seq;

create table mp_user (
  id                            number(19) not null,
  name                          varchar2(255),
  constraint pk_mp_user primary key (id)
);
create sequence mp_user_seq;

create table my_lob_size (
  id                            number(10) not null,
  name                          varchar2(255),
  my_count                      number(10),
  my_lob                        clob,
  constraint pk_my_lob_size primary key (id)
);
create sequence my_lob_size_seq;

create table my_lob_size_join_many (
  id                            number(10) not null,
  something                     varchar2(255),
  other                         varchar2(255),
  parent_id                     number(10),
  constraint pk_my_lob_size_join_many primary key (id)
);
create sequence my_lob_size_join_many_seq;

create table noidbean (
  name                          varchar2(255),
  subject                       varchar2(255),
  when_created                  timestamp not null
);

create table o_cached_bean (
  id                            number(19) not null,
  name                          varchar2(255),
  constraint pk_o_cached_bean primary key (id)
);
create sequence o_cached_bean_seq;

create table o_cached_bean_country (
  o_cached_bean_id              number(19) not null,
  o_country_code                varchar2(2) not null,
  constraint pk_o_cached_bean_country primary key (o_cached_bean_id,o_country_code)
);

create table o_cached_bean_child (
  id                            number(19) not null,
  cached_bean_id                number(19),
  constraint pk_o_cached_bean_child primary key (id)
);
create sequence o_cached_bean_child_seq;

create table ocar (
  id                            number(10) not null,
  vin                           varchar2(255),
  name                          varchar2(255),
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  version                       number(19) not null,
  constraint pk_ocar primary key (id)
);
create sequence ocar_seq;

create table oengine (
  engine_id                     raw(16) not null,
  short_desc                    varchar2(255),
  car_id                        number(10),
  version                       number(10) not null,
  constraint uq_oengine_car_id unique (car_id),
  constraint pk_oengine primary key (engine_id)
);

create table ogear_box (
  id                            raw(16) not null,
  box_desc                      varchar2(255),
  box_size                      number(10),
  car_id                        number(10),
  version                       number(10) not null,
  constraint uq_ogear_box_car_id unique (car_id),
  constraint pk_ogear_box primary key (id)
);

create table o_order (
  id                            number(10) not null,
  status                        number(10),
  order_date                    date,
  ship_date                     date,
  kcustomer_id                  number(10) not null,
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  constraint ck_o_order_status check (status in (0,1,2,3)),
  constraint pk_o_order primary key (id)
);
create sequence o_order_seq;

create table o_order_detail (
  id                            number(10) not null,
  order_id                      number(10),
  order_qty                     number(10),
  ship_qty                      number(10),
  unit_price                    number(19,4),
  product_id                    number(10),
  cretime                       timestamp,
  updtime                       timestamp not null,
  constraint pk_o_order_detail primary key (id)
);
create sequence o_order_detail_seq;

create table s_orders (
  uuid                          varchar2(255) not null,
  constraint pk_s_orders primary key (uuid)
);

create table s_order_items (
  uuid                          varchar2(255) not null,
  product_variant_uuid          varchar2(255),
  order_uuid                    varchar2(255),
  quantity                      number(10),
  amount                        number(38),
  constraint pk_s_order_items primary key (uuid)
);

create table or_order_ship (
  id                            number(10) not null,
  order_id                      number(10),
  ship_time                     timestamp,
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  version                       number(19) not null,
  constraint pk_or_order_ship primary key (id)
);
create sequence or_order_ship_seq;

create table oto_child (
  id                            number(10) not null,
  name                          varchar2(255),
  master_id                     number(19),
  constraint uq_oto_child_master_id unique (master_id),
  constraint pk_oto_child primary key (id)
);
create sequence oto_child_seq;

create table oto_master (
  id                            number(19) not null,
  name                          varchar2(255),
  constraint pk_oto_master primary key (id)
);
create sequence oto_master_seq;

create table pfile (
  id                            number(10) not null,
  name                          varchar2(255),
  file_content_id               number(10),
  file_content2_id              number(10),
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  version                       number(19) not null,
  constraint uq_pfile_file_content_id unique (file_content_id),
  constraint uq_pfile_file_content2_id unique (file_content2_id),
  constraint pk_pfile primary key (id)
);
create sequence pfile_seq;

create table pfile_content (
  id                            number(10) not null,
  content                       blob,
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  version                       number(19) not null,
  constraint pk_pfile_content primary key (id)
);
create sequence pfile_content_seq;

create table paggview (
  pview_id                      raw(16),
  amount                        number(10) not null,
  constraint uq_paggview_pview_id unique (pview_id)
);

create table pallet_location (
  type                          varchar2(31) not null,
  id                            number(10) not null,
  zone_sid                      number(10) not null,
  attribute                     varchar2(255),
  constraint pk_pallet_location primary key (id)
);
create sequence pallet_location_seq;

create table parcel (
  parcelid                      number(19) not null,
  description                   varchar2(255),
  constraint pk_parcel primary key (parcelid)
);
create sequence parcel_seq;

create table parcel_location (
  parcellocid                   number(19) not null,
  location                      varchar2(255),
  parcelid                      number(19),
  constraint uq_parcel_location_parcelid unique (parcelid),
  constraint pk_parcel_location primary key (parcellocid)
);
create sequence parcel_location_seq;

create table rawinherit_parent (
  type                          varchar2(31) not null,
  id                            number(19) not null,
  val                           number(10),
  constraint pk_rawinherit_parent primary key (id)
);
create sequence rawinherit_parent_seq;

create table rawinherit_parent_rawinherit_d (
  rawinherit_parent_id          number(19) not null,
  rawinherit_data_id            number(19) not null,
  constraint pk_rawinherit_parent_rawinh_0 primary key (rawinherit_parent_id,rawinherit_data_id)
);

create table c_participation (
  id                            number(19) not null,
  rating                        number(10),
  type                          number(10),
  conversation_id               number(19) not null,
  user_id                       number(19) not null,
  version                       number(19) not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint ck_c_participation_type check (type in (0,1)),
  constraint pk_c_participation primary key (id)
);
create sequence c_participation_seq;

create table mt_permission (
  id                            raw(16) not null,
  name                          varchar2(255),
  constraint pk_mt_permission primary key (id)
);

create table persistent_file (
  id                            number(10) not null,
  name                          varchar2(255),
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  version                       number(19) not null,
  constraint pk_persistent_file primary key (id)
);
create sequence persistent_file_seq;

create table persistent_file_content (
  id                            number(10) not null,
  persistent_file_id            number(10),
  content                       blob,
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  version                       number(19) not null,
  constraint uq_persistent_file_content__1 unique (persistent_file_id),
  constraint pk_persistent_file_content primary key (id)
);
create sequence persistent_file_content_seq;

create table persons (
  id                            number(19) not null,
  surname                       varchar2(64) not null,
  name                          varchar2(64) not null,
  constraint pk_persons primary key (id)
);
create sequence persons_seq start with 1000 increment by 40;

create table person (
  oid                           number(19) not null,
  default_address_oid           number(19),
  version                       number(10) not null,
  constraint pk_person primary key (oid)
);
create sequence person_seq;

create table phones (
  id                            number(19) not null,
  phone_number                  varchar2(7) not null,
  person_id                     number(19) not null,
  constraint uq_phones_phone_number unique (phone_number),
  constraint pk_phones primary key (id)
);
create sequence phones_seq;

create table o_product (
  id                            number(10) not null,
  sku                           varchar2(20),
  name                          varchar2(255),
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  constraint pk_o_product primary key (id)
);
create sequence o_product_seq;

create table pp (
  id                            raw(16) not null,
  name                          varchar2(255),
  value                         varchar2(100) not null,
  constraint pk_pp primary key (id)
);

create table pp_to_ww (
  pp_id                         raw(16) not null,
  ww_id                         raw(16) not null,
  constraint pk_pp_to_ww primary key (pp_id,ww_id)
);

create table rcustomer (
  company                       varchar2(255) not null,
  name                          varchar2(255) not null,
  description                   varchar2(255),
  constraint pk_rcustomer primary key (company,name)
);

create table r_orders (
  company                       varchar2(255) not null,
  order_number                  number(10) not null,
  customername                  varchar2(255),
  item                          varchar2(255),
  constraint pk_r_orders primary key (company,order_number)
);

create table region (
  customer                      number(10) not null,
  type                          number(10) not null,
  description                   varchar2(255),
  version                       number(19) not null,
  constraint pk_region primary key (customer,type)
);

create table resourcefile (
  id                            varchar2(64) not null,
  parentresourcefileid          varchar2(64),
  name                          varchar2(128) not null,
  constraint pk_resourcefile primary key (id)
);

create table mt_role (
  id                            raw(16) not null,
  name                          varchar2(50),
  tenant_id                     raw(16),
  version                       number(19) not null,
  constraint pk_mt_role primary key (id)
);

create table mt_role_permission (
  mt_role_id                    raw(16) not null,
  mt_permission_id              raw(16) not null,
  constraint pk_mt_role_permission primary key (mt_role_id,mt_permission_id)
);

create table em_role (
  id                            number(19) not null,
  name                          varchar2(255),
  constraint pk_em_role primary key (id)
);
create sequence em_role_seq;

create table f_second (
  id                            number(19) not null,
  first                         number(19),
  title                         varchar2(255),
  constraint uq_f_second_first unique (first),
  constraint pk_f_second primary key (id)
);
create sequence f_second_seq;

create table section (
  id                            number(10) not null,
  article_id                    number(10),
  type                          number(10),
  content                       clob,
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  version                       number(19) not null,
  constraint ck_section_type check (type in (0,1)),
  constraint pk_section primary key (id)
);
create sequence section_seq;

create table self_parent (
  id                            number(19) not null,
  name                          varchar2(255),
  parent_id                     number(19),
  version                       number(19) not null,
  constraint pk_self_parent primary key (id)
);
create sequence self_parent_seq;

create table self_ref_customer (
  id                            number(19) not null,
  name                          varchar2(255),
  referred_by_id                number(19),
  constraint pk_self_ref_customer primary key (id)
);
create sequence self_ref_customer_seq;

create table self_ref_example (
  id                            number(19) not null,
  name                          varchar2(255) not null,
  parent_id                     number(19),
  constraint pk_self_ref_example primary key (id)
);
create sequence self_ref_example_seq;

create table some_enum_bean (
  id                            number(19) not null,
  some_enum                     number(10),
  name                          varchar2(255),
  constraint ck_some_enum_bean_some_enum check (some_enum in (0,1)),
  constraint pk_some_enum_bean primary key (id)
);
create sequence some_enum_bean_seq;

create table some_file_bean (
  id                            number(19) not null,
  name                          varchar2(255),
  content                       blob,
  version                       number(19) not null,
  constraint pk_some_file_bean primary key (id)
);
create sequence some_file_bean_seq;

create table some_new_types_bean (
  id                            number(19) not null,
  dow                           number(1),
  mth                           number(1),
  yr                            number(10),
  yr_mth                        date,
  local_date                    date,
  local_date_time               timestamp,
  offset_date_time              timestamp,
  zoned_date_time               timestamp,
  instant                       timestamp,
  zone_id                       varchar2(60),
  zone_offset                   varchar2(60),
  version                       number(19) not null,
  constraint ck_some_new_types_bean_dow check (dow in ('1','2','3','4','5','6','7')),
  constraint ck_some_new_types_bean_mth check (mth in ('1','2','3','4','5','6','7','8','9','10','11','12')),
  constraint pk_some_new_types_bean primary key (id)
);
create sequence some_new_types_bean_seq;

create table some_period_bean (
  id                            number(19) not null,
  period_years                  number(10),
  period_months                 number(10),
  period_days                   number(10),
  anniversary                   date,
  version                       number(19) not null,
  constraint pk_some_period_bean primary key (id)
);
create sequence some_period_bean_seq;

create table stockforecast (
  type                          varchar2(31) not null,
  id                            number(19) not null,
  inner_report_id               number(19),
  constraint pk_stockforecast primary key (id)
);
create sequence stockforecast_seq;

create table sub_section (
  id                            number(10) not null,
  section_id                    number(10),
  title                         varchar2(255),
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  version                       number(19) not null,
  constraint pk_sub_section primary key (id)
);
create sequence sub_section_seq;

create table sub_type (
  sub_type_id                   number(10) not null,
  description                   varchar2(255),
  version                       number(19) not null,
  constraint pk_sub_type primary key (sub_type_id)
);
create sequence sub_type_seq;

create table tbytes_only (
  id                            number(10) not null,
  content                       blob,
  constraint pk_tbytes_only primary key (id)
);
create sequence tbytes_only_seq;

create table tcar (
  type                          varchar2(31) not null,
  plate_no                      varchar2(255) not null,
  truckload                     number(19),
  constraint pk_tcar primary key (plate_no)
);

create table tint_root (
  my_type                       number(3) not null,
  id                            number(10) not null,
  name                          varchar2(255),
  child_property                varchar2(255),
  constraint pk_tint_root primary key (id)
);
create sequence tint_root_seq;

create table tjoda_entity (
  id                            number(10) not null,
  local_time                    timestamp,
  constraint pk_tjoda_entity primary key (id)
);
create sequence tjoda_entity_seq;

create table t_mapsuper1 (
  id                            number(10) not null,
  something                     varchar2(255),
  name                          varchar2(255),
  version                       number(10) not null,
  constraint pk_t_mapsuper1 primary key (id)
);
create sequence t_mapsuper1_seq;

create table t_oneb (
  id                            number(10) not null,
  name                          varchar2(255),
  description                   varchar2(255),
  active                        number(1),
  constraint pk_t_oneb primary key (id)
);
create sequence t_oneb_seq;

create table t_detail_with_other_namexxxyy (
  id                            number(10) not null,
  name                          varchar2(255),
  description                   varchar2(255),
  active                        number(1),
  master_id                     number(10),
  constraint pk_t_detail_with_other_name_0 primary key (id)
);
create sequence t_atable_detail_seq;

create table ts_detail_two (
  id                            number(10) not null,
  name                          varchar2(255),
  description                   varchar2(255),
  active                        number(1),
  master_id                     number(10),
  constraint pk_ts_detail_two primary key (id)
);
create sequence ts_detail_two_seq;

create table t_atable_thatisrelatively (
  id                            number(10) not null,
  name                          varchar2(255),
  description                   varchar2(255),
  active                        number(1),
  constraint pk_t_atable_thatisrelatively primary key (id)
);
create sequence t_atable_master_seq;

create table ts_master_two (
  id                            number(10) not null,
  name                          varchar2(255),
  description                   varchar2(255),
  active                        number(1),
  constraint pk_ts_master_two primary key (id)
);
create sequence ts_master_two_seq;

create table tuuid_entity (
  id                            raw(16) not null,
  name                          varchar2(255),
  constraint pk_tuuid_entity primary key (id)
);

create table twheel (
  id                            number(19) not null,
  owner_plate_no                varchar2(255) not null,
  constraint pk_twheel primary key (id)
);
create sequence twheel_seq;

create table twith_pre_insert (
  id                            number(10) not null,
  name                          varchar2(255) not null,
  title                         varchar2(255),
  constraint pk_twith_pre_insert primary key (id)
);
create sequence twith_pre_insert_seq;

create table mt_tenant (
  id                            raw(16) not null,
  name                          varchar2(255),
  version                       number(19) not null,
  constraint pk_mt_tenant primary key (id)
);

create table sa_tire (
  id                            number(19) not null,
  version                       number(10) not null,
  constraint pk_sa_tire primary key (id)
);
create sequence sa_tire_seq;

create table tire (
  id                            number(19) not null,
  wheel                         number(19),
  version                       number(10) not null,
  constraint uq_tire_wheel unique (wheel),
  constraint pk_tire primary key (id)
);
create sequence tire_seq;

create table trip (
  id                            number(10) not null,
  vehicle_driver_id             number(10),
  destination                   varchar2(255),
  address_id                    number(5),
  star_date                     timestamp,
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  version                       number(19) not null,
  constraint pk_trip primary key (id)
);
create sequence trip_seq;

create table truck_ref (
  id                            number(10) not null,
  something                     varchar2(255),
  constraint pk_truck_ref primary key (id)
);
create sequence truck_ref_seq;

create table type (
  customer                      number(10) not null,
  type                          number(10) not null,
  description                   varchar2(255),
  sub_type_id                   number(10),
  version                       number(19) not null,
  constraint pk_type primary key (customer,type)
);

create table ut_detail (
  id                            number(10) not null,
  utmaster_id                   number(10) not null,
  name                          varchar2(255),
  qty                           number(10),
  amount                        number(19,4),
  version                       number(10) not null,
  constraint pk_ut_detail primary key (id)
);
create sequence ut_detail_seq;

create table ut_master (
  id                            number(10) not null,
  name                          varchar2(255),
  description                   varchar2(255),
  version                       number(10) not null,
  constraint pk_ut_master primary key (id)
);
create sequence ut_master_seq;

create table uuone (
  id                            raw(16) not null,
  name                          varchar2(255),
  constraint pk_uuone primary key (id)
);

create table uutwo (
  id                            raw(16) not null,
  name                          varchar2(255),
  master_id                     raw(16),
  constraint pk_uutwo primary key (id)
);

create table em_user (
  id                            number(19) not null,
  name                          varchar2(255),
  constraint pk_em_user primary key (id)
);
create sequence em_user_seq;

create table tx_user (
  id                            number(19) not null,
  name                          varchar2(255),
  constraint pk_tx_user primary key (id)
);
create sequence tx_user_seq;

create table c_user (
  id                            number(19) not null,
  inactive                      number(1),
  name                          varchar2(255),
  email                         varchar2(255),
  group_id                      number(19),
  version                       number(19) not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_c_user primary key (id)
);
create sequence c_user_seq;

create table oto_user (
  id                            number(19) not null,
  name                          varchar2(255),
  account_id                    number(19) not null,
  version                       number(19) not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint uq_oto_user_account_id unique (account_id),
  constraint pk_oto_user primary key (id)
);
create sequence oto_user_seq;

create table em_user_role (
  user_id                       number(19) not null,
  role_id                       number(19) not null,
  constraint pk_em_user_role primary key (user_id,role_id)
);

create table vehicle (
  dtype                         varchar2(3) not null,
  id                            number(10) not null,
  license_number                varchar2(255),
  registration_date             timestamp,
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  version                       number(19) not null,
  truck_ref_id                  number(10),
  capacity                      number(19,4),
  driver                        varchar2(255),
  car_ref_id                    number(10),
  constraint pk_vehicle primary key (id)
);
create sequence vehicle_seq;

create table vehicle_driver (
  id                            number(10) not null,
  name                          varchar2(255),
  vehicle_id                    number(10),
  address_id                    number(5),
  license_issued_on             timestamp,
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  version                       number(19) not null,
  constraint pk_vehicle_driver primary key (id)
);
create sequence vehicle_driver_seq;

create table warehouses (
  id                            number(10) not null,
  officezoneid                  number(10),
  constraint pk_warehouses primary key (id)
);
create sequence warehouses_seq;

create table warehousesshippingzones (
  warehouseid                   number(10) not null,
  shippingzoneid                number(10) not null,
  constraint pk_warehousesshippingzones primary key (warehouseid,shippingzoneid)
);

create table sa_wheel (
  id                            number(19) not null,
  tire                          number(19),
  car                           number(19),
  version                       number(10) not null,
  constraint pk_sa_wheel primary key (id)
);
create sequence sa_wheel_seq;

create table sp_car_wheel (
  id                            number(19) not null,
  version                       number(10) not null,
  constraint pk_sp_car_wheel primary key (id)
);
create sequence sp_car_wheel_seq;

create table wheel (
  id                            number(19) not null,
  version                       number(10) not null,
  constraint pk_wheel primary key (id)
);
create sequence wheel_seq;

create table with_zero (
  id                            number(19) not null,
  name                          varchar2(255),
  parent_id                     number(10),
  version                       number(19) not null,
  constraint pk_with_zero primary key (id)
);
create sequence with_zero_seq;

create table parent (
  id                            number(10) not null,
  name                          varchar2(255),
  constraint pk_parent primary key (id)
);
create sequence parent_seq;

create table wview (
  id                            raw(16) not null,
  name                          varchar2(255) not null,
  constraint uq_wview_name unique (name),
  constraint pk_wview primary key (id)
);

create table zones (
  type                          varchar2(31) not null,
  id                            number(10) not null,
  attribute                     varchar2(255),
  constraint pk_zones primary key (id)
);
create sequence zones_seq;

alter table bar add constraint fk_bar_foo_id foreign key (foo_id) references foo (foo_id);
create index ix_bar_foo_id on bar (foo_id);

alter table o_address add constraint fk_o_address_country_code foreign key (country_code) references o_country (code);
create index ix_o_address_country_code on o_address (country_code);

alter table animals add constraint fk_animals_shelter_id foreign key (shelter_id) references animal_shelter (id);
create index ix_animals_shelter_id on animals (shelter_id);

alter table attribute add constraint fk_attribute_attribute_hold_1 foreign key (attribute_holder_id) references attribute_holder (id);
create index ix_attribute_attribute_hold_1 on attribute (attribute_holder_id);

alter table bbookmark add constraint fk_bbookmark_user_id foreign key (user_id) references bbookmark_user (id);
create index ix_bbookmark_user_id on bbookmark (user_id);

alter table drel_booking add constraint fk_drel_booking_agent_invoice foreign key (agent_invoice) references drel_invoice (id);

alter table drel_booking add constraint fk_drel_booking_client_invo_2 foreign key (client_invoice) references drel_invoice (id);

alter table ckey_detail add constraint fk_ckey_detail_ckey_parent foreign key (one_key,two_key) references ckey_parent (one_key,two_key);
create index ix_ckey_detail_one_key_two__1 on ckey_detail (one_key,two_key);

alter table ckey_parent add constraint fk_ckey_parent_assoc_id foreign key (assoc_id) references ckey_assoc (id);
create index ix_ckey_parent_assoc_id on ckey_parent (assoc_id);

alter table calculation_result add constraint fk_calculation_result_produ_1 foreign key (product_configuration_id) references configuration (id);
create index ix_calculation_result_produ_1 on calculation_result (product_configuration_id);

alter table calculation_result add constraint fk_calculation_result_group_2 foreign key (group_configuration_id) references configuration (id);
create index ix_calculation_result_group_2 on calculation_result (group_configuration_id);

alter table sp_car_car_wheels add constraint fk_sp_car_car_wheels_sp_car_1 foreign key (car) references sp_car_car (id);
create index ix_sp_car_car_wheels_car on sp_car_car_wheels (car);

alter table sp_car_car_wheels add constraint fk_sp_car_car_wheels_sp_car_2 foreign key (wheel) references sp_car_wheel (id);
create index ix_sp_car_car_wheels_wheel on sp_car_car_wheels (wheel);

alter table car_accessory add constraint fk_car_accessory_car_id foreign key (car_id) references vehicle (id);
create index ix_car_accessory_car_id on car_accessory (car_id);

alter table configuration add constraint fk_configuration_configurat_1 foreign key (configurations_id) references configurations (id);
create index ix_configuration_configurat_1 on configuration (configurations_id);

alter table contact add constraint fk_contact_customer_id foreign key (customer_id) references o_customer (id);
create index ix_contact_customer_id on contact (customer_id);

alter table contact add constraint fk_contact_group_id foreign key (group_id) references contact_group (id);
create index ix_contact_group_id on contact (group_id);

alter table contact_note add constraint fk_contact_note_contact_id foreign key (contact_id) references contact (id);
create index ix_contact_note_contact_id on contact_note (contact_id);

alter table c_conversation add constraint fk_c_conversation_group_id foreign key (group_id) references c_group (id);
create index ix_c_conversation_group_id on c_conversation (group_id);

alter table o_customer add constraint fk_o_customer_billing_addre_1 foreign key (billing_address_id) references o_address (id);
create index ix_o_customer_billing_addre_1 on o_customer (billing_address_id);

alter table o_customer add constraint fk_o_customer_shipping_addr_2 foreign key (shipping_address_id) references o_address (id);
create index ix_o_customer_shipping_addr_2 on o_customer (shipping_address_id);

alter table eemb_inner add constraint fk_eemb_inner_outer_id foreign key (outer_id) references eemb_outer (id);
create index ix_eemb_inner_outer_id on eemb_inner (outer_id);

alter table einvoice add constraint fk_einvoice_person_id foreign key (person_id) references eperson (id);
create index ix_einvoice_person_id on einvoice (person_id);

alter table enull_collection_detail add constraint fk_enull_collection_detail__1 foreign key (enull_collection_id) references enull_collection (id);
create index ix_enull_collection_detail__1 on enull_collection_detail (enull_collection_id);

alter table eopt_one_a add constraint fk_eopt_one_a_b_id foreign key (b_id) references eopt_one_b (id);
create index ix_eopt_one_a_b_id on eopt_one_a (b_id);

alter table eopt_one_b add constraint fk_eopt_one_b_c_id foreign key (c_id) references eopt_one_c (id);
create index ix_eopt_one_b_c_id on eopt_one_b (c_id);

alter table evanilla_collection_detail add constraint fk_evanilla_collection_deta_1 foreign key (evanilla_collection_id) references evanilla_collection (id);
create index ix_evanilla_collection_deta_1 on evanilla_collection_detail (evanilla_collection_id);

alter table td_child add constraint fk_td_child_parent_id foreign key (parent_id) references td_parent (parent_id);
create index ix_td_child_parent_id on td_child (parent_id);

alter table imrelated add constraint fk_imrelated_owner_id foreign key (owner_id) references imroot (id);
create index ix_imrelated_owner_id on imrelated (owner_id);

alter table info_contact add constraint fk_info_contact_company_id foreign key (company_id) references info_company (id);
create index ix_info_contact_company_id on info_contact (company_id);

alter table info_customer add constraint fk_info_customer_company_id foreign key (company_id) references info_company (id);

alter table inner_report add constraint fk_inner_report_forecast_id foreign key (forecast_id) references stockforecast (id);

alter table drel_invoice add constraint fk_drel_invoice_booking foreign key (booking) references drel_booking (id);
create index ix_drel_invoice_booking on drel_invoice (booking);

alter table item add constraint fk_item_type foreign key (customer,type) references type (customer,type);
create index ix_item_customer_type on item (customer,type);

alter table item add constraint fk_item_region foreign key (customer,region) references region (customer,type);
create index ix_item_customer_region on item (customer,region);

alter table level1_level4 add constraint fk_level1_level4_level1 foreign key (level1_id) references level1 (id);
create index ix_level1_level4_level1_id on level1_level4 (level1_id);

alter table level1_level4 add constraint fk_level1_level4_level4 foreign key (level4_id) references level4 (id);
create index ix_level1_level4_level4_id on level1_level4 (level4_id);

alter table level1_level2 add constraint fk_level1_level2_level1 foreign key (level1_id) references level1 (id);
create index ix_level1_level2_level1_id on level1_level2 (level1_id);

alter table level1_level2 add constraint fk_level1_level2_level2 foreign key (level2_id) references level2 (id);
create index ix_level1_level2_level2_id on level1_level2 (level2_id);

alter table level2_level3 add constraint fk_level2_level3_level2 foreign key (level2_id) references level2 (id);
create index ix_level2_level3_level2_id on level2_level3 (level2_id);

alter table level2_level3 add constraint fk_level2_level3_level3 foreign key (level3_id) references level3 (id);
create index ix_level2_level3_level3_id on level2_level3 (level3_id);

alter table la_attr_value_attribute add constraint fk_la_attr_value_attribute__1 foreign key (la_attr_value_id) references la_attr_value (id);
create index ix_la_attr_value_attribute__1 on la_attr_value_attribute (la_attr_value_id);

alter table la_attr_value_attribute add constraint fk_la_attr_value_attribute__2 foreign key (attribute_id) references attribute (id);
create index ix_la_attr_value_attribute__2 on la_attr_value_attribute (attribute_id);

alter table mprinter add constraint fk_mprinter_current_state_id foreign key (current_state_id) references mprinter_state (id);
create index ix_mprinter_current_state_id on mprinter (current_state_id);

alter table mprinter add constraint fk_mprinter_last_swap_cyan_id foreign key (last_swap_cyan_id) references mprinter_state (id);

alter table mprinter add constraint fk_mprinter_last_swap_magen_3 foreign key (last_swap_magenta_id) references mprinter_state (id);

alter table mprinter add constraint fk_mprinter_last_swap_yello_4 foreign key (last_swap_yellow_id) references mprinter_state (id);

alter table mprinter add constraint fk_mprinter_last_swap_black_5 foreign key (last_swap_black_id) references mprinter_state (id);

alter table mprinter_state add constraint fk_mprinter_state_printer_id foreign key (printer_id) references mprinter (id);
create index ix_mprinter_state_printer_id on mprinter_state (printer_id);

alter table mprofile add constraint fk_mprofile_picture_id foreign key (picture_id) references mmedia (id);
create index ix_mprofile_picture_id on mprofile (picture_id);

alter table mrole_muser add constraint fk_mrole_muser_mrole foreign key (mrole_roleid) references mrole (roleid);
create index ix_mrole_muser_mrole_roleid on mrole_muser (mrole_roleid);

alter table mrole_muser add constraint fk_mrole_muser_muser foreign key (muser_userid) references muser (userid);
create index ix_mrole_muser_muser_userid on mrole_muser (muser_userid);

alter table muser add constraint fk_muser_user_type_id foreign key (user_type_id) references muser_type (id);
create index ix_muser_user_type_id on muser (user_type_id);

alter table c_message add constraint fk_c_message_conversation_id foreign key (conversation_id) references c_conversation (id);
create index ix_c_message_conversation_id on c_message (conversation_id);

alter table c_message add constraint fk_c_message_user_id foreign key (user_id) references c_user (id);
create index ix_c_message_user_id on c_message (user_id);

alter table mnoc_user_mnoc_role add constraint fk_mnoc_user_mnoc_role_mnoc_1 foreign key (mnoc_user_user_id) references mnoc_user (user_id);
create index ix_mnoc_user_mnoc_role_mnoc_1 on mnoc_user_mnoc_role (mnoc_user_user_id);

alter table mnoc_user_mnoc_role add constraint fk_mnoc_user_mnoc_role_mnoc_2 foreign key (mnoc_role_role_id) references mnoc_role (role_id);
create index ix_mnoc_user_mnoc_role_mnoc_2 on mnoc_user_mnoc_role (mnoc_role_role_id);

alter table mp_role add constraint fk_mp_role_mp_user_id foreign key (mp_user_id) references mp_user (id);
create index ix_mp_role_mp_user_id on mp_role (mp_user_id);

alter table my_lob_size_join_many add constraint fk_my_lob_size_join_many_pa_1 foreign key (parent_id) references my_lob_size (id);
create index ix_my_lob_size_join_many_pa_1 on my_lob_size_join_many (parent_id);

alter table o_cached_bean_country add constraint fk_o_cached_bean_country_o__1 foreign key (o_cached_bean_id) references o_cached_bean (id);
create index ix_o_cached_bean_country_o__1 on o_cached_bean_country (o_cached_bean_id);

alter table o_cached_bean_country add constraint fk_o_cached_bean_country_o__2 foreign key (o_country_code) references o_country (code);
create index ix_o_cached_bean_country_o__2 on o_cached_bean_country (o_country_code);

alter table o_cached_bean_child add constraint fk_o_cached_bean_child_cach_1 foreign key (cached_bean_id) references o_cached_bean (id);
create index ix_o_cached_bean_child_cach_1 on o_cached_bean_child (cached_bean_id);

alter table oengine add constraint fk_oengine_car_id foreign key (car_id) references ocar (id);

alter table ogear_box add constraint fk_ogear_box_car_id foreign key (car_id) references ocar (id);

alter table o_order add constraint fk_o_order_kcustomer_id foreign key (kcustomer_id) references o_customer (id);
create index ix_o_order_kcustomer_id on o_order (kcustomer_id);

alter table o_order_detail add constraint fk_o_order_detail_order_id foreign key (order_id) references o_order (id);
create index ix_o_order_detail_order_id on o_order_detail (order_id);

alter table o_order_detail add constraint fk_o_order_detail_product_id foreign key (product_id) references o_product (id);
create index ix_o_order_detail_product_id on o_order_detail (product_id);

alter table s_order_items add constraint fk_s_order_items_order_uuid foreign key (order_uuid) references s_orders (uuid);
create index ix_s_order_items_order_uuid on s_order_items (order_uuid);

alter table or_order_ship add constraint fk_or_order_ship_order_id foreign key (order_id) references o_order (id);
create index ix_or_order_ship_order_id on or_order_ship (order_id);

alter table oto_child add constraint fk_oto_child_master_id foreign key (master_id) references oto_master (id);

alter table pfile add constraint fk_pfile_file_content_id foreign key (file_content_id) references pfile_content (id);

alter table pfile add constraint fk_pfile_file_content2_id foreign key (file_content2_id) references pfile_content (id);

alter table paggview add constraint fk_paggview_pview_id foreign key (pview_id) references pp (id);

alter table pallet_location add constraint fk_pallet_location_zone_sid foreign key (zone_sid) references zones (id);
create index ix_pallet_location_zone_sid on pallet_location (zone_sid);

alter table parcel_location add constraint fk_parcel_location_parcelid foreign key (parcelid) references parcel (parcelid);

alter table rawinherit_parent_rawinherit_d add constraint fk_rawinherit_parent_rawinh_1 foreign key (rawinherit_parent_id) references rawinherit_parent (id);
create index ix_rawinherit_parent_rawinh_1 on rawinherit_parent_rawinherit_d (rawinherit_parent_id);

alter table rawinherit_parent_rawinherit_d add constraint fk_rawinherit_parent_rawinh_2 foreign key (rawinherit_data_id) references rawinherit_data (id);
create index ix_rawinherit_parent_rawinh_2 on rawinherit_parent_rawinherit_d (rawinherit_data_id);

alter table c_participation add constraint fk_c_participation_conversa_1 foreign key (conversation_id) references c_conversation (id);
create index ix_c_participation_conversa_1 on c_participation (conversation_id);

alter table c_participation add constraint fk_c_participation_user_id foreign key (user_id) references c_user (id);
create index ix_c_participation_user_id on c_participation (user_id);

alter table persistent_file_content add constraint fk_persistent_file_content__1 foreign key (persistent_file_id) references persistent_file (id);

alter table person add constraint fk_person_default_address_oid foreign key (default_address_oid) references address (oid);
create index ix_person_default_address_oid on person (default_address_oid);

alter table phones add constraint fk_phones_person_id foreign key (person_id) references persons (id);
create index ix_phones_person_id on phones (person_id);

alter table pp_to_ww add constraint fk_pp_to_ww_pp foreign key (pp_id) references pp (id);
create index ix_pp_to_ww_pp_id on pp_to_ww (pp_id);

alter table pp_to_ww add constraint fk_pp_to_ww_wview foreign key (ww_id) references wview (id);
create index ix_pp_to_ww_ww_id on pp_to_ww (ww_id);

alter table r_orders add constraint fk_r_orders_rcustomer foreign key (company,customername) references rcustomer (company,name);
create index ix_r_orders_company_custome_1 on r_orders (company,customername);

alter table resourcefile add constraint fk_resourcefile_parentresou_1 foreign key (parentresourcefileid) references resourcefile (id);
create index ix_resourcefile_parentresou_1 on resourcefile (parentresourcefileid);

alter table mt_role add constraint fk_mt_role_tenant_id foreign key (tenant_id) references mt_tenant (id);
create index ix_mt_role_tenant_id on mt_role (tenant_id);

alter table mt_role_permission add constraint fk_mt_role_permission_mt_role foreign key (mt_role_id) references mt_role (id);
create index ix_mt_role_permission_mt_ro_1 on mt_role_permission (mt_role_id);

alter table mt_role_permission add constraint fk_mt_role_permission_mt_pe_2 foreign key (mt_permission_id) references mt_permission (id);
create index ix_mt_role_permission_mt_pe_2 on mt_role_permission (mt_permission_id);

alter table f_second add constraint fk_f_second_first foreign key (first) references f_first (id);

alter table section add constraint fk_section_article_id foreign key (article_id) references article (id);
create index ix_section_article_id on section (article_id);

alter table self_parent add constraint fk_self_parent_parent_id foreign key (parent_id) references self_parent (id);
create index ix_self_parent_parent_id on self_parent (parent_id);

alter table self_ref_customer add constraint fk_self_ref_customer_referr_1 foreign key (referred_by_id) references self_ref_customer (id);
create index ix_self_ref_customer_referr_1 on self_ref_customer (referred_by_id);

alter table self_ref_example add constraint fk_self_ref_example_parent_id foreign key (parent_id) references self_ref_example (id);
create index ix_self_ref_example_parent_id on self_ref_example (parent_id);

alter table stockforecast add constraint fk_stockforecast_inner_repo_1 foreign key (inner_report_id) references inner_report (id);
create index ix_stockforecast_inner_repo_1 on stockforecast (inner_report_id);

alter table sub_section add constraint fk_sub_section_section_id foreign key (section_id) references section (id);
create index ix_sub_section_section_id on sub_section (section_id);

alter table t_detail_with_other_namexxxyy add constraint fk_t_detail_with_other_name_1 foreign key (master_id) references t_atable_thatisrelatively (id);
create index ix_t_detail_with_other_name_1 on t_detail_with_other_namexxxyy (master_id);

alter table ts_detail_two add constraint fk_ts_detail_two_master_id foreign key (master_id) references ts_master_two (id);
create index ix_ts_detail_two_master_id on ts_detail_two (master_id);

alter table twheel add constraint fk_twheel_owner_plate_no foreign key (owner_plate_no) references tcar (plate_no);
create index ix_twheel_owner_plate_no on twheel (owner_plate_no);

alter table tire add constraint fk_tire_wheel foreign key (wheel) references wheel (id);

alter table trip add constraint fk_trip_vehicle_driver_id foreign key (vehicle_driver_id) references vehicle_driver (id);
create index ix_trip_vehicle_driver_id on trip (vehicle_driver_id);

alter table trip add constraint fk_trip_address_id foreign key (address_id) references o_address (id);
create index ix_trip_address_id on trip (address_id);

alter table type add constraint fk_type_sub_type_id foreign key (sub_type_id) references sub_type (sub_type_id);
create index ix_type_sub_type_id on type (sub_type_id);

alter table ut_detail add constraint fk_ut_detail_utmaster_id foreign key (utmaster_id) references ut_master (id);
create index ix_ut_detail_utmaster_id on ut_detail (utmaster_id);

alter table uutwo add constraint fk_uutwo_master_id foreign key (master_id) references uuone (id);
create index ix_uutwo_master_id on uutwo (master_id);

alter table c_user add constraint fk_c_user_group_id foreign key (group_id) references c_group (id);
create index ix_c_user_group_id on c_user (group_id);

alter table oto_user add constraint fk_oto_user_account_id foreign key (account_id) references oto_account (id);

alter table em_user_role add constraint fk_em_user_role_user_id foreign key (user_id) references em_user (id);
create index ix_em_user_role_user_id on em_user_role (user_id);

alter table em_user_role add constraint fk_em_user_role_role_id foreign key (role_id) references em_role (id);
create index ix_em_user_role_role_id on em_user_role (role_id);

alter table vehicle add constraint fk_vehicle_truck_ref_id foreign key (truck_ref_id) references truck_ref (id);
create index ix_vehicle_truck_ref_id on vehicle (truck_ref_id);

alter table vehicle add constraint fk_vehicle_car_ref_id foreign key (car_ref_id) references truck_ref (id);
create index ix_vehicle_car_ref_id on vehicle (car_ref_id);

alter table vehicle_driver add constraint fk_vehicle_driver_vehicle_id foreign key (vehicle_id) references vehicle (id);
create index ix_vehicle_driver_vehicle_id on vehicle_driver (vehicle_id);

alter table vehicle_driver add constraint fk_vehicle_driver_address_id foreign key (address_id) references o_address (id);
create index ix_vehicle_driver_address_id on vehicle_driver (address_id);

alter table warehouses add constraint fk_warehouses_officezoneid foreign key (officezoneid) references zones (id);
create index ix_warehouses_officezoneid on warehouses (officezoneid);

alter table warehousesshippingzones add constraint fk_warehousesshippingzones__1 foreign key (warehouseid) references warehouses (id);
create index ix_warehousesshippingzones__1 on warehousesshippingzones (warehouseid);

alter table warehousesshippingzones add constraint fk_warehousesshippingzones__2 foreign key (shippingzoneid) references zones (id);
create index ix_warehousesshippingzones__2 on warehousesshippingzones (shippingzoneid);

alter table sa_wheel add constraint fk_sa_wheel_tire foreign key (tire) references sa_tire (id);
create index ix_sa_wheel_tire on sa_wheel (tire);

alter table sa_wheel add constraint fk_sa_wheel_car foreign key (car) references sa_car (id);
create index ix_sa_wheel_car on sa_wheel (car);

alter table with_zero add constraint fk_with_zero_parent_id foreign key (parent_id) references parent (id);
create index ix_with_zero_parent_id on with_zero (parent_id);

