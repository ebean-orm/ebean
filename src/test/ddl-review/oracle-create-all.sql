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

create table addr (
  id                            number(19) not null,
  employee_id                   number(19),
  name                          varchar2(255),
  address_line1                 varchar2(255),
  address_line2                 varchar2(255),
  city                          varchar2(255),
  version                       number(19) not null,
  constraint pk_addr primary key (id)
);
create sequence addr_seq;

create table address (
  oid                           number(19) not null,
  street                        varchar2(255),
  version                       number(10) not null,
  constraint pk_address primary key (oid)
);
create sequence address_seq;

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

create table album (
  id                            number(19) not null,
  name                          varchar2(255),
  cover_id                      number(19),
  deleted                       number(1) default 0 not null,
  created_at                    timestamp not null,
  last_update                   timestamp not null,
  constraint uq_album_cover_id unique (cover_id),
  constraint pk_album primary key (id)
);
create sequence album_seq;

create table animal (
  species                       varchar2(255) not null,
  id                            number(19) not null,
  shelter_id                    number(19),
  version                       number(19) not null,
  name                          varchar2(255),
  registration_number           varchar2(255),
  date_of_birth                 date,
  dog_size                      varchar2(255),
  constraint pk_animal primary key (id)
);
create sequence animal_seq;

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

create table bsite (
  id                            varchar2(40) not null,
  name                          varchar2(255),
  constraint pk_bsite primary key (id)
);

create table bsite_user (
  site_id                       varchar2(40) not null,
  user_id                       varchar2(40) not null,
  access_level                  number(10),
  constraint ck_bsite_user_access_level check ( access_level in (0,1,2)),
  constraint pk_bsite_user primary key (site_id,user_id)
);

create table bsite_user_b (
  site                          varchar2(40) not null,
  usr                           varchar2(40) not null,
  access_level                  number(10),
  constraint ck_bsite_user_b_access_level check ( access_level in (0,1,2)),
  constraint pk_bsite_user_b primary key (site,usr)
);

create table bsite_user_c (
  site_uid                      varchar2(40) not null,
  user_uid                      varchar2(40) not null,
  access_level                  number(10),
  constraint ck_bsite_user_c_access_level check ( access_level in (0,1,2)),
  constraint pk_bsite_user_c primary key (site_uid,user_uid)
);

create table buser (
  id                            varchar2(40) not null,
  name                          varchar2(255),
  constraint pk_buser primary key (id)
);

create table bwith_qident (
  id                            number(10) not null,
  "Name"                        varchar2(191),
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
  booking_uid                   number(19),
  agent_invoice                 number(19),
  client_invoice                number(19),
  version                       number(10) not null,
  constraint uq_drel_booking_booking_uid unique (booking_uid),
  constraint uq_drel_booking_agent_invoice unique (agent_invoice),
  constraint uq_drl_bkng_clnt_nvc unique (client_invoice),
  constraint pk_drel_booking primary key (id)
);
create sequence drel_booking_seq;

create table cinh_root (
  dtype                         varchar2(3) not null,
  id                            number(10) not null,
  license_number                varchar2(255),
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  version                       number(19) not null,
  driver                        varchar2(255),
  notes                         varchar2(255),
  action                        varchar2(255),
  constraint pk_cinh_root primary key (id)
);
create sequence cinh_root_seq;

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
  charge                        number(19,4) not null,
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
  fuse_id                       number(19) not null,
  car_id                        number(10),
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  version                       number(19) not null,
  constraint pk_car_accessory primary key (id)
);
create sequence car_accessory_seq;

create table car_fuse (
  id                            number(19) not null,
  location_code                 varchar2(255),
  constraint pk_car_fuse primary key (id)
);
create sequence car_fuse_seq;

create table category (
  id                            number(19) not null,
  name                          varchar2(255),
  surveyobjectid                number(19),
  sequence_number               number(10) not null,
  constraint pk_category primary key (id)
);
create sequence category_seq;

create table child_person (
  identifier                    number(10) not null,
  name                          varchar2(255),
  age                           number(10),
  some_bean_id                  number(10),
  parent_identifier             number(10),
  family_name                   varchar2(255),
  address                       varchar2(255),
  constraint pk_child_person primary key (identifier)
);
create sequence child_person_seq;

create table configuration (
  type                          varchar2(21) not null,
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
  first_name                    varchar2(127),
  last_name                     varchar2(127),
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
  isopen                        number(1) default 0 not null,
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

create table cover (
  id                            number(19) not null,
  s3url                         varchar2(255),
  deleted                       number(1) default 0 not null,
  constraint pk_cover primary key (id)
);
create sequence cover_seq;

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
  constraint ck_o_customer_status check ( status in ('N','A','I')),
  constraint pk_o_customer primary key (id)
);
comment on table o_customer is 'Holds external customers';
comment on column o_customer.status is 'status of the customer';
comment on column o_customer.smallnote is 'Short notes regarding the customer';
comment on column o_customer.anniversary is 'Join date of the customer';
create sequence o_customer_seq;

create table dcredit (
  id                            number(19) not null,
  credit                        varchar2(255),
  constraint pk_dcredit primary key (id)
);
create sequence dcredit_seq;

create table dcredit_drol (
  dcredit_id                    number(19) not null,
  drol_id                       number(19) not null,
  constraint pk_dcredit_drol primary key (dcredit_id,drol_id)
);

create table dexh_entity (
  oid                           number(19) not null,
  exhange                       varchar2(255),
  last_updated                  timestamp not null,
  constraint pk_dexh_entity primary key (oid)
);
create sequence dexh_entity_seq;

create table dperson (
  id                            number(19) not null,
  first_name                    varchar2(255),
  last_name                     varchar2(255),
  salary                        number(38),
  constraint pk_dperson primary key (id)
);
create sequence dperson_seq;

create table drol (
  id                            number(19) not null,
  name                          varchar2(255),
  constraint pk_drol primary key (id)
);
create sequence drol_seq;

create table drot (
  id                            number(19) not null,
  name                          varchar2(255),
  constraint pk_drot primary key (id)
);
create sequence drot_seq;

create table drot_drol (
  drot_id                       number(19) not null,
  drol_id                       number(19) not null,
  constraint pk_drot_drol primary key (drot_id,drol_id)
);

create table rawinherit_data (
  id                            number(19) not null,
  val                           number(10),
  constraint pk_rawinherit_data primary key (id)
);
create sequence rawinherit_data_seq;

create table dfk_cascade (
  id                            number(19) not null,
  name                          varchar2(255),
  one_id                        number(19),
  constraint pk_dfk_cascade primary key (id)
);
create sequence dfk_cascade_seq;

create table dfk_cascade_one (
  id                            number(19) not null,
  name                          varchar2(255),
  constraint pk_dfk_cascade_one primary key (id)
);
create sequence dfk_cascade_one_seq;

create table dfk_none (
  id                            number(19) not null,
  name                          varchar2(255),
  one_id                        number(19),
  constraint pk_dfk_none primary key (id)
);
create sequence dfk_none_seq;

create table dfk_none_via_join (
  id                            number(19) not null,
  name                          varchar2(255),
  one_id                        number(19),
  constraint pk_dfk_none_via_join primary key (id)
);
create sequence dfk_none_via_join_seq;

create table dfk_one (
  id                            number(19) not null,
  name                          varchar2(255),
  constraint pk_dfk_one primary key (id)
);
create sequence dfk_one_seq;

create table dfk_set_null (
  id                            number(19) not null,
  name                          varchar2(255),
  one_id                        number(19),
  constraint pk_dfk_set_null primary key (id)
);
create sequence dfk_set_null_seq;

create table doc (
  id                            number(19) not null,
  name                          varchar2(255),
  version                       number(19) not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_doc primary key (id)
);
create sequence doc_seq;

create table doc_link (
  doc_id                        number(19) not null,
  link_id                       number(19) not null,
  constraint pk_doc_link primary key (doc_id,link_id)
);

create table doc_link_draft (
  doc_id                        number(19) not null,
  link_id                       number(19) not null,
  constraint pk_doc_link_draft primary key (doc_id,link_id)
);

create table doc_draft (
  id                            number(19) not null,
  name                          varchar2(255),
  version                       number(19) not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_doc_draft primary key (id)
);
create sequence doc_draft_seq;

create table document (
  id                            number(19) not null,
  title                         varchar2(127),
  body                          varchar2(255),
  organisation_id               number(19),
  version                       number(19) not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint uq_document_title unique (title),
  constraint pk_document primary key (id)
);
create sequence document_seq;

create table document_draft (
  id                            number(19) not null,
  title                         varchar2(127),
  body                          varchar2(255),
  when_publish                  timestamp,
  organisation_id               number(19),
  version                       number(19) not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint uq_document_draft_title unique (title),
  constraint pk_document_draft primary key (id)
);
create sequence document_draft_seq;

create table document_media (
  id                            number(19) not null,
  document_id                   number(19),
  name                          varchar2(255),
  description                   varchar2(255),
  version                       number(19) not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_document_media primary key (id)
);
create sequence document_media_seq;

create table document_media_draft (
  id                            number(19) not null,
  document_id                   number(19),
  name                          varchar2(255),
  description                   varchar2(255),
  version                       number(19) not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_document_media_draft primary key (id)
);
create sequence document_media_draft_seq;

create table earray_bean (
  id                            number(19) not null,
  name                          varchar2(255),
  phone_numbers                 varchar(300),
  uids                          varchar(1000),
  other_ids                     varchar(1000),
  doubs                         varchar(1000),
  statuses                      varchar(1000),
  status2                       varchar(1000),
  version                       number(19) not null,
  constraint pk_earray_bean primary key (id)
);
create sequence earray_bean_seq;

create table earray_set_bean (
  id                            number(19) not null,
  name                          varchar2(255),
  phone_numbers                 varchar(300),
  uids                          varchar(1000),
  other_ids                     varchar(1000),
  doubs                         varchar(1000),
  version                       number(19) not null,
  constraint pk_earray_set_bean primary key (id)
);
create sequence earray_set_bean_seq;

create table e_basic (
  id                            number(10) not null,
  status                        varchar2(1),
  name                          varchar2(127),
  description                   varchar2(255),
  some_date                     timestamp,
  constraint ck_e_basic_status check ( status in ('N','A','I')),
  constraint pk_e_basic primary key (id)
);
create sequence e_basic_seq;

create table ebasic_change_log (
  id                            number(19) not null,
  name                          varchar2(20),
  short_description             varchar2(50),
  long_description              varchar2(100),
  who_created                   varchar2(255) not null,
  who_modified                  varchar2(255) not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  version                       number(19) not null,
  constraint pk_ebasic_change_log primary key (id)
);
create sequence ebasic_change_log_seq;

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
  status                        raw(20),
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
  constraint ck_e_basic_enum_id_status check ( status in ('N','A','I')),
  constraint pk_e_basic_enum_id primary key (status)
);

create table e_basic_eni (
  id                            number(10) not null,
  status                        number(10),
  name                          varchar2(255),
  description                   varchar2(255),
  some_date                     timestamp,
  constraint ck_e_basic_eni_status check ( status in (1,2,3)),
  constraint pk_e_basic_eni primary key (id)
);
create sequence e_basic_eni_seq;

create table ebasic_hstore (
  id                            number(19) not null,
  name                          varchar2(255),
  map                           varchar2(800),
  version                       number(19) not null,
  constraint pk_ebasic_hstore primary key (id)
);
create sequence ebasic_hstore_seq;

create table ebasic_json_list (
  id                            number(19) not null,
  name                          varchar2(255),
  bean_set                      varchar2(700),
  bean_list                     clob,
  bean_map                      varchar2(700),
  plain_bean                    varchar2(500),
  flags                         varchar2(50),
  tags                          varchar2(100),
  version                       number(19) not null,
  constraint pk_ebasic_json_list primary key (id)
);
create sequence ebasic_json_list_seq;

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

create table ebasic_json_map_detail (
  id                            number(19) not null,
  owner_id                      number(19),
  name                          varchar2(255),
  content                       clob,
  version                       number(19) not null,
  constraint pk_ebasic_json_map_detail primary key (id)
);
create sequence ebasic_json_map_detail_seq;

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

create table ebasic_json_unmapped (
  id                            number(19) not null,
  name                          varchar2(255),
  version                       number(19) not null,
  constraint pk_ebasic_json_unmapped primary key (id)
);
create sequence ebasic_json_unmapped_seq;

create table e_basic_ndc (
  id                            number(10) not null,
  name                          varchar2(255),
  constraint pk_e_basic_ndc primary key (id)
);
create sequence e_basic_ndc_seq;

create table ebasic_no_sdchild (
  id                            number(19) not null,
  owner_id                      number(19) not null,
  child_name                    varchar2(255),
  amount                        number(19) not null,
  version                       number(19) not null,
  constraint pk_ebasic_no_sdchild primary key (id)
);
create sequence ebasic_no_sdchild_seq;

create table ebasic_sdchild (
  id                            number(19) not null,
  owner_id                      number(19) not null,
  child_name                    varchar2(255),
  amount                        number(19) not null,
  version                       number(19) not null,
  deleted                       number(1) default 0 not null,
  constraint pk_ebasic_sdchild primary key (id)
);
create sequence ebasic_sdchild_seq;

create table ebasic_soft_delete (
  id                            number(19) not null,
  name                          varchar2(255),
  description                   varchar2(255),
  version                       number(19) not null,
  deleted                       number(1) default 0 not null,
  constraint pk_ebasic_soft_delete primary key (id)
);
create sequence ebasic_soft_delete_seq;

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
  deleted                       number(1) default 0 not null,
  version                       number(19) not null,
  constraint pk_e_basic_withlife primary key (id)
);
create sequence e_basic_withlife_seq;

create table e_basicverucon (
  id                            number(10) not null,
  name                          varchar2(127),
  other                         varchar2(127),
  other_one                     varchar2(127),
  description                   varchar2(255),
  last_update                   timestamp not null,
  constraint uq_e_basicverucon_name unique (name),
  constraint uq_e_bscvrcn_thr_thr_n unique (other,other_one),
  constraint pk_e_basicverucon primary key (id)
);
create sequence e_basicverucon_seq;

create table e_col_ab (
  id                            number(19) not null,
  column_a                      varchar2(255),
  column_b                      varchar2(255),
  constraint pk_e_col_ab primary key (id)
);
create sequence e_col_ab_seq;

create table ecustom_id (
  id                            varchar2(127) not null,
  name                          varchar2(255),
  constraint pk_ecustom_id primary key (id)
);

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
  instant_created               timestamp not null,
  instant_updated               timestamp not null,
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
  ship_status                   varchar2(3),
  bill_street                   varchar2(255),
  bill_suburb                   varchar2(255),
  bill_city                     varchar2(255),
  bill_status                   varchar2(3),
  version                       number(19) not null,
  constraint ck_einvoice_state check ( state in (0,1,2)),
  constraint ck_einvoice_ship_status check ( ship_status in ('ONE','TWO')),
  constraint ck_einvoice_bill_status check ( bill_status in ('ONE','TWO')),
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
  addr_city                     varchar2(255),
  addr_status                   varchar2(3),
  version                       number(19) not null,
  constraint ck_eperson_addr_status check ( addr_status in ('ONE','TWO')),
  constraint pk_eperson primary key (id)
);
create sequence eperson_seq;

create table e_person_online (
  id                            number(19) not null,
  email                         varchar2(127),
  online_status                 number(1) default 0 not null,
  when_updated                  timestamp not null,
  constraint uq_e_person_online_email unique (email),
  constraint pk_e_person_online primary key (id)
);
create sequence e_person_online_seq;

create table esimple (
  usertypeid                    number(10) generated always as identity not null,
  name                          varchar2(255),
  constraint pk_esimple primary key (usertypeid)
);

create table esoft_del_book (
  id                            number(19) not null,
  book_title                    varchar2(255),
  lend_by_id                    number(19),
  version                       number(19) not null,
  deleted                       number(1) default 0 not null,
  constraint pk_esoft_del_book primary key (id)
);
create sequence esoft_del_book_seq;

create table esoft_del_book_esoft_del_user (
  esoft_del_book_id             number(19) not null,
  esoft_del_user_id             number(19) not null,
  constraint pk_esft_dl_bk_sft_dl_sr primary key (esoft_del_book_id,esoft_del_user_id)
);

create table esoft_del_down (
  id                            number(19) not null,
  esoft_del_mid_id              number(19) not null,
  down                          varchar2(255),
  version                       number(19) not null,
  deleted                       number(1) default 0 not null,
  constraint pk_esoft_del_down primary key (id)
);
create sequence esoft_del_down_seq;

create table esoft_del_mid (
  id                            number(19) not null,
  top_id                        number(19),
  mid                           varchar2(255),
  up_id                         number(19),
  version                       number(19) not null,
  deleted                       number(1) default 0 not null,
  constraint pk_esoft_del_mid primary key (id)
);
create sequence esoft_del_mid_seq;

create table esoft_del_role (
  id                            number(19) not null,
  role_name                     varchar2(255),
  version                       number(19) not null,
  deleted                       number(1) default 0 not null,
  constraint pk_esoft_del_role primary key (id)
);
create sequence esoft_del_role_seq;

create table esoft_del_role_esoft_del_user (
  esoft_del_role_id             number(19) not null,
  esoft_del_user_id             number(19) not null,
  constraint pk_esft_dl_rl_sft_dl_sr primary key (esoft_del_role_id,esoft_del_user_id)
);

create table esoft_del_top (
  id                            number(19) not null,
  name                          varchar2(255),
  version                       number(19) not null,
  deleted                       number(1) default 0 not null,
  constraint pk_esoft_del_top primary key (id)
);
create sequence esoft_del_top_seq;

create table esoft_del_up (
  id                            number(19) not null,
  up                            varchar2(255),
  version                       number(19) not null,
  deleted                       number(1) default 0 not null,
  constraint pk_esoft_del_up primary key (id)
);
create sequence esoft_del_up_seq;

create table esoft_del_user (
  id                            number(19) not null,
  user_name                     varchar2(255),
  version                       number(19) not null,
  deleted                       number(1) default 0 not null,
  constraint pk_esoft_del_user primary key (id)
);
create sequence esoft_del_user_seq;

create table esoft_del_user_esoft_del_role (
  esoft_del_user_id             number(19) not null,
  esoft_del_role_id             number(19) not null,
  constraint pk_esft_dl_sr_sft_dl_rl primary key (esoft_del_user_id,esoft_del_role_id)
);

create table esome_convert_type (
  id                            number(19) not null,
  name                          varchar2(255),
  money                         number(38),
  constraint pk_esome_convert_type primary key (id)
);
create sequence esome_convert_type_seq;

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

create table rawinherit_uncle (
  id                            number(10) not null,
  name                          varchar2(255),
  parent_id                     number(19) not null,
  version                       number(19) not null,
  constraint pk_rawinherit_uncle primary key (id)
);
create sequence rawinherit_uncle_seq;

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
  version                       number(19) not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  who_created                   varchar2(255) not null,
  who_modified                  varchar2(255) not null,
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

create table empl (
  id                            number(19) not null,
  name                          varchar2(255),
  age                           number(10),
  default_address_id            number(19),
  constraint pk_empl primary key (id)
);
create sequence empl_seq;

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
  id                            number(19) generated always as identity not null,
  description                   varchar2(255),
  constraint pk_gen_key_identity primary key (id)
);

create table gen_key_sequence (
  id                            number(19) not null,
  description                   varchar2(255),
  constraint pk_gen_key_sequence primary key (id)
);
create sequence SEQ;

create table gen_key_table (
  id                            number(19) not null,
  description                   varchar2(255),
  constraint pk_gen_key_table primary key (id)
);
create sequence gen_key_table_seq;

create table grand_parent_person (
  identifier                    number(10) not null,
  name                          varchar2(255),
  age                           number(10),
  some_bean_id                  number(10),
  family_name                   varchar2(255),
  address                       varchar2(255),
  constraint pk_grand_parent_person primary key (identifier)
);
create sequence grand_parent_person_seq;

create table survey_group (
  id                            number(19) not null,
  name                          varchar2(255),
  categoryobjectid              number(19),
  sequence_number               number(10) not null,
  constraint pk_survey_group primary key (id)
);
create sequence survey_group_seq;

create table c_group (
  id                            number(19) not null,
  inactive                      number(1) default 0 not null,
  name                          varchar2(255),
  version                       number(19) not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_c_group primary key (id)
);
create sequence c_group_seq;

create table he_doc (
  id                            number(19) not null,
  name                          varchar2(255),
  version                       number(19) not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_he_doc primary key (id)
);
create sequence he_doc_seq;

create table hx_link (
  id                            number(19) not null,
  name                          varchar2(255),
  location                      varchar2(255),
  comments                      varchar2(255),
  version                       number(19) not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_hx_link primary key (id)
);
create sequence hx_link_seq;

create table hx_link_doc (
  hx_link_id                    number(19) not null,
  he_doc_id                     number(19) not null,
  constraint pk_hx_link_doc primary key (hx_link_id,he_doc_id)
);

create table hi_doc (
  id                            number(19) not null,
  name                          varchar2(255),
  version                       number(19) not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_hi_doc primary key (id)
);
create sequence hi_doc_seq;

create table hi_link (
  id                            number(19) not null,
  name                          varchar2(255),
  location                      varchar2(255),
  comments                      varchar2(255),
  version                       number(19) not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_hi_link primary key (id)
);
create sequence hi_link_seq;

create table hi_link_doc (
  hi_link_id                    number(19) not null,
  hi_doc_id                     number(19) not null,
  constraint pk_hi_link_doc primary key (hi_link_id,hi_doc_id)
);

create table imrelated (
  id                            number(19) not null,
  name                          varchar2(255),
  owner_id                      number(19) not null,
  constraint pk_imrelated primary key (id)
);
create sequence imrelated_seq;

create table imroot (
  dtype                         varchar2(31) not null,
  id                            number(19) not null,
  name                          varchar2(255),
  title                         varchar2(255),
  when_title                    timestamp,
  constraint pk_imroot primary key (id)
);
create sequence imroot_seq;

create table ixresource (
  dtype                         varchar2(255),
  id                            varchar2(40) not null,
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
  itemnumber                    varchar2(127) not null,
  description                   varchar2(255),
  units                         varchar2(255),
  type                          number(10) not null,
  region                        number(10) not null,
  date_modified                 timestamp,
  date_created                  timestamp,
  modified_by                   varchar2(255),
  created_by                    varchar2(255),
  version                       number(19) not null,
  constraint pk_item primary key (customer,itemnumber)
);

create table monkey (
  mid                           number(19) not null,
  name                          varchar2(255),
  food_preference               varchar2(255),
  version                       number(19) not null,
  constraint pk_monkey primary key (mid)
);
create sequence monkey_seq;

create table trainer (
  tid                           number(19) not null,
  name                          varchar2(255),
  version                       number(19) not null,
  constraint pk_trainer primary key (tid)
);
create sequence trainer_seq;

create table trainer_monkey (
  trainer_tid                   number(19) not null,
  monkey_mid                    number(19) not null,
  constraint uq_trainer_monkey_mid unique (monkey_mid),
  constraint pk_trainer_monkey primary key (trainer_tid,monkey_mid)
);

create table troop (
  pid                           number(19) not null,
  name                          varchar2(255),
  version                       number(19) not null,
  constraint pk_troop primary key (pid)
);
create sequence troop_seq;

create table troop_monkey (
  troop_pid                     number(19) not null,
  monkey_mid                    number(19) not null,
  constraint uq_troop_monkey_mid unique (monkey_mid),
  constraint pk_troop_monkey primary key (troop_pid,monkey_mid)
);

create table l2_cldf_reset_bean (
  id                            number(19) not null,
  name                          varchar2(255),
  constraint pk_l2_cldf_reset_bean primary key (id)
);
create sequence l2_cldf_reset_bean_seq;

create table l2_cldf_reset_bean_child (
  id                            number(19) not null,
  parent_id                     number(19),
  constraint pk_l2_cldf_reset_bean_child primary key (id)
);
create sequence l2_cldf_reset_bean_child_seq;

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

create table link (
  id                            number(19) not null,
  name                          varchar2(255),
  location                      varchar2(255),
  when_publish                  timestamp,
  link_comment                  varchar2(255),
  version                       number(19) not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  deleted                       number(1) default 0 not null,
  constraint pk_link primary key (id)
);
create sequence link_seq;

create table link_draft (
  id                            number(19) not null,
  name                          varchar2(255),
  location                      varchar2(255),
  when_publish                  timestamp,
  link_comment                  varchar2(255),
  dirty                         number(1) default 0 not null,
  version                       number(19) not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  deleted                       number(1) default 0 not null,
  constraint pk_link_draft primary key (id)
);
create sequence link_draft_seq;

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
  constraint ck_non_updateprop_non_enum check ( non_enum in ('BEGIN','END')),
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
  constraint uq_mprntr_lst_swp_mgnt_d unique (last_swap_magenta_id),
  constraint uq_mprntr_lst_swp_yllw_d unique (last_swap_yellow_id),
  constraint uq_mprntr_lst_swp_blck_d unique (last_swap_black_id),
  constraint pk_mprinter primary key (id)
);
create sequence mprinter_seq;

create table mprinter_state (
  id                            number(19) not null,
  flags                         number(19) not null,
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

create table mail_box (
  id                            number(19) not null,
  name                          varchar2(255),
  version                       number(19) not null,
  constraint pk_mail_box primary key (id)
);
create sequence mail_box_seq;

create table mail_user (
  id                            number(19) not null,
  name                          varchar2(255),
  version                       number(19) not null,
  constraint pk_mail_user primary key (id)
);
create sequence mail_user_seq;

create table mail_user_inbox (
  mail_user_id                  number(19) not null,
  mail_box_id                   number(19) not null,
  constraint pk_mail_user_inbox primary key (mail_user_id,mail_box_id)
);

create table mail_user_outbox (
  mail_user_id                  number(19) not null,
  mail_box_id                   number(19) not null,
  constraint pk_mail_user_outbox primary key (mail_user_id,mail_box_id)
);

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

create table mny_a (
  id                            number(19) not null,
  name                          varchar2(255),
  version                       number(19) not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_mny_a primary key (id)
);
create sequence mny_a_seq;

create table mny_b (
  id                            number(19) not null,
  name                          varchar2(255),
  a_id                          number(19),
  version                       number(19) not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_mny_b primary key (id)
);
create sequence mny_b_seq;

create table mny_b_mny_c (
  mny_b_id                      number(19) not null,
  mny_c_id                      number(19) not null,
  constraint pk_mny_b_mny_c primary key (mny_b_id,mny_c_id)
);

create table mny_c (
  id                            number(19) not null,
  name                          varchar2(255),
  version                       number(19) not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_mny_c primary key (id)
);
create sequence mny_c_seq;

create table mny_topic (
  id                            number(19) not null,
  name                          varchar2(255),
  version                       number(19) not null,
  constraint pk_mny_topic primary key (id)
);
create sequence mny_topic_seq;

create table subtopics (
  topic                         number(19) not null,
  subtopic                      number(19) not null,
  constraint pk_subtopics primary key (topic,subtopic)
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

create table ms_many_a (
  aid                           number(19) not null,
  name                          varchar2(255),
  ms_many_a_many_b              number(1) default 0 not null,
  ms_many_b                     number(1) default 0 not null,
  deleted                       number(1) default 0 not null,
  constraint pk_ms_many_a primary key (aid)
);
create sequence ms_many_a_seq;

create table ms_many_a_many_b (
  ms_many_a_aid                 number(19) not null,
  ms_many_b_bid                 number(19) not null,
  constraint pk_ms_many_a_many_b primary key (ms_many_a_aid,ms_many_b_bid)
);

create table ms_many_b (
  bid                           number(19) not null,
  name                          varchar2(255),
  deleted                       number(1) default 0 not null,
  constraint pk_ms_many_b primary key (bid)
);
create sequence ms_many_b_seq;

create table ms_many_b_many_a (
  ms_many_b_bid                 number(19) not null,
  ms_many_a_aid                 number(19) not null,
  constraint pk_ms_many_b_many_a primary key (ms_many_b_bid,ms_many_a_aid)
);

create table my_lob_size (
  id                            number(10) not null,
  name                          varchar2(255),
  my_count                      number(10) not null,
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

create table o_cached_natkey (
  id                            number(19) not null,
  store                         varchar2(255),
  sku                           varchar2(255),
  description                   varchar2(255),
  constraint pk_o_cached_natkey primary key (id)
);
create sequence o_cached_natkey_seq;

create table o_cached_natkey3 (
  id                            number(19) not null,
  store                         varchar2(255),
  code                          number(10) not null,
  sku                           varchar2(255),
  description                   varchar2(255),
  constraint pk_o_cached_natkey3 primary key (id)
);
create sequence o_cached_natkey3_seq;

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

create table ocompany (
  id                            number(10) not null,
  corp_id                       varchar2(50),
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  version                       number(19) not null,
  constraint uq_ocompany_corp_id unique (corp_id),
  constraint pk_ocompany primary key (id)
);
create sequence ocompany_seq;

create table oengine (
  engine_id                     varchar2(40) not null,
  short_desc                    varchar2(255),
  car_id                        number(10),
  version                       number(10) not null,
  constraint uq_oengine_car_id unique (car_id),
  constraint pk_oengine primary key (engine_id)
);

create table ogear_box (
  id                            varchar2(40) not null,
  box_desc                      varchar2(255),
  box_size                      number(10),
  car_id                        number(10),
  version                       number(10) not null,
  constraint uq_ogear_box_car_id unique (car_id),
  constraint pk_ogear_box primary key (id)
);

create table oroad_show_msg (
  id                            number(10) not null,
  company_id                    number(10) not null,
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  version                       number(19) not null,
  constraint uq_oroad_show_msg_company_id unique (company_id),
  constraint pk_oroad_show_msg primary key (id)
);
create sequence oroad_show_msg_seq;

create table om_ordered_detail (
  id                            number(19) not null,
  name                          varchar2(255),
  master_id                     number(19),
  version                       number(19) not null,
  sort_order                    number(10),
  constraint pk_om_ordered_detail primary key (id)
);
create sequence om_ordered_detail_seq;

create table om_ordered_master (
  id                            number(19) not null,
  name                          varchar2(255),
  version                       number(19) not null,
  constraint pk_om_ordered_master primary key (id)
);
create sequence om_ordered_master_seq;

create table o_order (
  id                            number(10) not null,
  status                        number(10),
  order_date                    date,
  ship_date                     date,
  kcustomer_id                  number(10) not null,
  cretime                       timestamp not null,
  updtime                       timestamp not null,
  constraint ck_o_order_status check ( status in (0,1,2,3)),
  constraint pk_o_order primary key (id)
);
create sequence o_order_seq;

create table o_order_detail (
  id                            number(10) not null,
  order_id                      number(10) not null,
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
  uuid                          varchar2(40) not null,
  constraint pk_s_orders primary key (uuid)
);

create table s_order_items (
  uuid                          varchar2(40) not null,
  product_variant_uuid          varchar2(255),
  order_uuid                    varchar2(40),
  quantity                      number(10) not null,
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

create table organisation (
  id                            number(19) not null,
  name                          varchar2(255),
  version                       number(19) not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_organisation primary key (id)
);
create sequence organisation_seq;

create table oto_bchild (
  master_id                     number(19) not null,
  child                         varchar2(255),
  constraint pk_oto_bchild primary key (master_id)
);
create sequence oto_bchild_seq;

create table oto_bmaster (
  id                            number(19) not null,
  name                          varchar2(255),
  constraint pk_oto_bmaster primary key (id)
);
create sequence oto_bmaster_seq;

create table oto_child (
  id                            number(10) not null,
  name                          varchar2(255),
  master_id                     number(19),
  constraint uq_oto_child_master_id unique (master_id),
  constraint pk_oto_child primary key (id)
);
create sequence oto_child_seq;

create table oto_cust (
  cid                           number(19) not null,
  name                          varchar2(255),
  version                       number(19) not null,
  constraint pk_oto_cust primary key (cid)
);
create sequence oto_cust_seq;

create table oto_cust_address (
  aid                           number(19) not null,
  line1                         varchar2(255),
  line2                         varchar2(255),
  line3                         varchar2(255),
  customer_cid                  number(19),
  version                       number(19) not null,
  constraint uq_ot_cst_ddrss_cstmr_cd unique (customer_cid),
  constraint pk_oto_cust_address primary key (aid)
);
create sequence oto_cust_address_seq;

create table oto_master (
  id                            number(19) not null,
  name                          varchar2(255),
  constraint pk_oto_master primary key (id)
);
create sequence oto_master_seq;

create table oto_prime (
  pid                           number(19) not null,
  name                          varchar2(255),
  version                       number(19) not null,
  constraint pk_oto_prime primary key (pid)
);
create sequence oto_prime_seq;

create table oto_prime_extra (
  eid                           number(19) not null,
  extra                         varchar2(255),
  version                       number(19) not null,
  constraint pk_oto_prime_extra primary key (eid)
);

create table oto_th_many (
  id                            number(19) not null,
  oto_th_top_id                 number(19) not null,
  many                          varchar2(255),
  version                       number(19) not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_oto_th_many primary key (id)
);
create sequence oto_th_many_seq;

create table oto_th_one (
  id                            number(19) not null,
  one                           number(1) default 0 not null,
  many_id                       number(19),
  version                       number(19) not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint uq_oto_th_one_many_id unique (many_id),
  constraint pk_oto_th_one primary key (id)
);
create sequence oto_th_one_seq;

create table oto_th_top (
  id                            number(19) not null,
  topp                          varchar2(255),
  version                       number(19) not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_oto_th_top primary key (id)
);
create sequence oto_th_top_seq;

create table oto_ubprime (
  pid                           varchar2(40) not null,
  name                          varchar2(255),
  version                       number(19) not null,
  constraint pk_oto_ubprime primary key (pid)
);

create table oto_ubprime_extra (
  eid                           varchar2(40) not null,
  extra                         varchar2(255),
  version                       number(19) not null,
  constraint pk_oto_ubprime_extra primary key (eid)
);

create table oto_uprime (
  pid                           varchar2(40) not null,
  name                          varchar2(255),
  version                       number(19) not null,
  constraint pk_oto_uprime primary key (pid)
);

create table oto_uprime_extra (
  eid                           varchar2(40) not null,
  extra                         varchar2(255),
  version                       number(19) not null,
  constraint pk_oto_uprime_extra primary key (eid)
);

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
  pview_id                      varchar2(40),
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
  more                          varchar2(255),
  constraint pk_rawinherit_parent primary key (id)
);
create sequence rawinherit_parent_seq;

create table rawinherit_parent_rawinherit_d (
  rawinherit_parent_id          number(19) not null,
  rawinherit_data_id            number(19) not null,
  constraint pk_rwnhrt_prnt_rwnhrt_d primary key (rawinherit_parent_id,rawinherit_data_id)
);

create table parent_person (
  identifier                    number(10) not null,
  name                          varchar2(255),
  age                           number(10),
  some_bean_id                  number(10),
  parent_identifier             number(10),
  family_name                   varchar2(255),
  address                       varchar2(255),
  constraint pk_parent_person primary key (identifier)
);
create sequence parent_person_seq;

create table c_participation (
  id                            number(19) not null,
  rating                        number(10),
  type                          number(10),
  conversation_id               number(19) not null,
  user_id                       number(19) not null,
  version                       number(19) not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint ck_c_participation_type check ( type in (0,1)),
  constraint pk_c_participation primary key (id)
);
create sequence c_participation_seq;

create table mt_permission (
  id                            varchar2(40) not null,
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
  constraint uq_prsstnt_fl_cntnt_prsstnt_1 unique (persistent_file_id),
  constraint pk_persistent_file_content primary key (id)
);
create sequence persistent_file_content_seq;

create table persons (
  id                            number(19) not null,
  surname                       varchar2(64) not null,
  name                          varchar2(64) not null,
  constraint pk_persons primary key (id)
);
create sequence PERSONS_seq start with 1000;

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
create sequence PHONES_seq;

create table primary_revision (
  id                            number(19) not null,
  revision                      number(10) not null,
  name                          varchar2(255),
  version                       number(19) not null,
  constraint pk_primary_revision primary key (id,revision)
);

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
  id                            varchar2(40) not null,
  name                          varchar2(255),
  value                         varchar2(100) not null,
  constraint pk_pp primary key (id)
);

create table pp_to_ww (
  pp_id                         varchar2(40) not null,
  ww_id                         varchar2(40) not null,
  constraint pk_pp_to_ww primary key (pp_id,ww_id)
);

create table question (
  id                            number(19) not null,
  name                          varchar2(255),
  groupobjectid                 number(19),
  sequence_number               number(10) not null,
  constraint pk_question primary key (id)
);
create sequence question_seq;

create table rcustomer (
  company                       varchar2(127) not null,
  name                          varchar2(127) not null,
  description                   varchar2(255),
  constraint pk_rcustomer primary key (company,name)
);

create table r_orders (
  company                       varchar2(127) not null,
  order_number                  number(10) not null,
  customername                  varchar2(127),
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

create table em_role (
  id                            number(19) not null,
  name                          varchar2(255),
  constraint pk_em_role primary key (id)
);
create sequence em_role_seq;

create table mt_role (
  id                            varchar2(40) not null,
  name                          varchar2(50),
  tenant_id                     varchar2(40),
  version                       number(19) not null,
  constraint pk_mt_role primary key (id)
);

create table mt_role_permission (
  mt_role_id                    varchar2(40) not null,
  mt_permission_id              varchar2(40) not null,
  constraint pk_mt_role_permission primary key (mt_role_id,mt_permission_id)
);

create table f_second (
  id                            number(19) not null,
  mod_name                      varchar2(255),
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
  constraint ck_section_type check ( type in (0,1)),
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
  constraint ck_some_enum_bean_some_enum check ( some_enum in (0,1)),
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
  path                          varchar2(255),
  version                       number(19) not null,
  constraint ck_some_new_types_bean_dow check ( dow in (1,2,3,4,5,6,7)),
  constraint ck_some_new_types_bean_mth check ( mth in (1,2,3,4,5,6,7,8,9,10,11,12)),
  constraint pk_some_new_types_bean primary key (id)
);
create sequence some_new_types_bean_seq;

create table some_period_bean (
  id                            number(19) not null,
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

create table survey (
  id                            number(19) not null,
  name                          varchar2(255),
  constraint pk_survey primary key (id)
);
create sequence survey_seq;

create table tbytes_only (
  id                            number(10) not null,
  content                       blob,
  constraint pk_tbytes_only primary key (id)
);
create sequence tbytes_only_seq;

create table tcar (
  type                          varchar2(31) not null,
  plate_no                      varchar2(32) not null,
  truckload                     number(19),
  constraint pk_tcar primary key (plate_no)
);

create table tevent (
  id                            number(19) not null,
  name                          varchar2(255),
  version                       number(19) not null,
  constraint pk_tevent primary key (id)
);
create sequence tevent_seq;

create table tevent_many (
  id                            number(19) not null,
  description                   varchar2(255),
  event_id                      number(19),
  units                         number(10) not null,
  amount                        number(19,4) not null,
  version                       number(19) not null,
  constraint pk_tevent_many primary key (id)
);
create sequence tevent_many_seq;

create table tevent_one (
  id                            number(19) not null,
  name                          varchar2(255),
  event_id                      number(19),
  version                       number(19) not null,
  constraint uq_tevent_one_event_id unique (event_id),
  constraint pk_tevent_one primary key (id)
);
create sequence tevent_one_seq;

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
  active                        number(1) default 0 not null,
  constraint pk_t_oneb primary key (id)
);
create sequence t_oneb_seq;

create table t_detail_with_other_namexxxyy (
  id                            number(10) not null,
  name                          varchar2(255),
  description                   varchar2(255),
  some_unique_value             varchar2(127),
  active                        number(1) default 0 not null,
  master_id                     number(10),
  constraint uq_t_dtl_wth_thr_nmxxxyy_sm_1 unique (some_unique_value),
  constraint pk_t_dtl_wth_thr_nmxxxyy primary key (id)
);
create sequence t_atable_detail_seq;

create table ts_detail_two (
  id                            number(10) not null,
  name                          varchar2(255),
  description                   varchar2(255),
  active                        number(1) default 0 not null,
  master_id                     number(10),
  constraint pk_ts_detail_two primary key (id)
);
create sequence ts_detail_two_seq;

create table t_atable_thatisrelatively (
  id                            number(10) not null,
  name                          varchar2(255),
  description                   varchar2(255),
  active                        number(1) default 0 not null,
  constraint pk_t_atable_thatisrelatively primary key (id)
);
create sequence t_atable_master_seq;

create table ts_master_two (
  id                            number(10) not null,
  name                          varchar2(255),
  description                   varchar2(255),
  active                        number(1) default 0 not null,
  constraint pk_ts_master_two primary key (id)
);
create sequence ts_master_two_seq;

create table tuuid_entity (
  id                            varchar2(40) not null,
  name                          varchar2(255),
  constraint pk_tuuid_entity primary key (id)
);

create table twheel (
  id                            number(19) not null,
  owner_plate_no                varchar2(32) not null,
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
  id                            varchar2(40) not null,
  name                          varchar2(255),
  version                       number(19) not null,
  constraint pk_mt_tenant primary key (id)
);

create table test_annotation_base_entity (
  direct                        varchar2(255),
  meta                          varchar2(255),
  mixed                         varchar2(255),
  constraint_annotation         varchar2(40),
  null1                         varchar2(255) not null,
  null2                         varchar2(255),
  null3                         varchar2(255)
);

create table tire (
  id                            number(19) not null,
  wheel                         number(19),
  version                       number(10) not null,
  constraint uq_tire_wheel unique (wheel),
  constraint pk_tire primary key (id)
);
create sequence tire_seq;

create table sa_tire (
  id                            number(19) not null,
  version                       number(10) not null,
  constraint pk_sa_tire primary key (id)
);
create sequence sa_tire_seq;

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

create table tz_bean (
  id                            number(19) not null,
  moda                          varchar2(255),
  ts                            timestamp,
  tstz                          timestamp,
  constraint pk_tz_bean primary key (id)
);
create sequence tz_bean_seq;

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
  id                            varchar2(40) not null,
  name                          varchar2(255),
  constraint pk_uuone primary key (id)
);

create table uutwo (
  id                            varchar2(40) not null,
  name                          varchar2(255),
  master_id                     varchar2(40),
  constraint pk_uutwo primary key (id)
);

create table c_user (
  id                            number(19) not null,
  inactive                      number(1) default 0 not null,
  name                          varchar2(255),
  email                         varchar2(255),
  password_hash                 varchar2(255),
  group_id                      number(19),
  version                       number(19) not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_c_user primary key (id)
);
create sequence c_user_seq;

create table tx_user (
  id                            number(19) not null,
  name                          varchar2(255),
  constraint pk_tx_user primary key (id)
);
create sequence tx_user_seq;

create table g_user (
  id                            number(19) not null,
  username                      varchar2(255),
  version                       number(19) not null,
  constraint pk_g_user primary key (id)
);
create sequence g_user_seq;

create table em_user (
  id                            number(19) not null,
  name                          varchar2(255),
  constraint pk_em_user primary key (id)
);
create sequence em_user_seq;

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
  siz                           varchar2(1),
  driver                        varchar2(255),
  car_ref_id                    number(10),
  notes                         varchar2(255),
  truck_ref_id                  number(10),
  capacity                      number(19,4),
  constraint ck_vehicle_siz check ( siz in ('S','M','L','H')),
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

create table wheel (
  id                            number(19) not null,
  version                       number(10) not null,
  constraint pk_wheel primary key (id)
);
create sequence wheel_seq;

create table sp_car_wheel (
  id                            number(19) not null,
  version                       number(10) not null,
  constraint pk_sp_car_wheel primary key (id)
);
create sequence sp_car_wheel_seq;

create table g_who_props_otm (
  id                            number(19) not null,
  name                          varchar2(255),
  version                       number(19) not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  who_created_id                number(19),
  who_modified_id               number(19),
  constraint pk_g_who_props_otm primary key (id)
);
create sequence g_who_props_otm_seq;

create table with_zero (
  id                            number(19) not null,
  name                          varchar2(255),
  parent_id                     number(10),
  lang                          varchar2(2) default 'en' not null,
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
  id                            varchar2(40) not null,
  name                          varchar2(127) not null,
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

create index ix_cntct_lst_nm_frst_nm on contact (last_name,first_name);
create index ix_e_basic_name on e_basic (name);
alter table bar add constraint fk_bar_foo_id foreign key (foo_id) references foo (foo_id);
create index ix_bar_foo_id on bar (foo_id);

alter table addr add constraint fk_addr_employee_id foreign key (employee_id) references empl (id);
create index ix_addr_employee_id on addr (employee_id);

alter table o_address add constraint fk_o_address_country_code foreign key (country_code) references o_country (code);
create index ix_o_address_country_code on o_address (country_code);

alter table album add constraint fk_album_cover_id foreign key (cover_id) references cover (id);

alter table animal add constraint fk_animal_shelter_id foreign key (shelter_id) references animal_shelter (id);
create index ix_animal_shelter_id on animal (shelter_id);

alter table attribute add constraint fk_attrbt_ttrbt_hldr_d foreign key (attribute_holder_id) references attribute_holder (id);
create index ix_attrbt_ttrbt_hldr_d on attribute (attribute_holder_id);

alter table bbookmark add constraint fk_bbookmark_user_id foreign key (user_id) references bbookmark_user (id);
create index ix_bbookmark_user_id on bbookmark (user_id);

alter table bsite_user add constraint fk_bsite_user_site_id foreign key (site_id) references bsite (id);
create index ix_bsite_user_site_id on bsite_user (site_id);

alter table bsite_user add constraint fk_bsite_user_user_id foreign key (user_id) references buser (id);
create index ix_bsite_user_user_id on bsite_user (user_id);

alter table bsite_user_b add constraint fk_bsite_user_b_site foreign key (site) references bsite (id);
create index ix_bsite_user_b_site on bsite_user_b (site);

alter table bsite_user_b add constraint fk_bsite_user_b_usr foreign key (usr) references buser (id);
create index ix_bsite_user_b_usr on bsite_user_b (usr);

alter table bsite_user_c add constraint fk_bsite_user_c_site_uid foreign key (site_uid) references bsite (id);
create index ix_bsite_user_c_site_uid on bsite_user_c (site_uid);

alter table bsite_user_c add constraint fk_bsite_user_c_user_uid foreign key (user_uid) references buser (id);
create index ix_bsite_user_c_user_uid on bsite_user_c (user_uid);

alter table drel_booking add constraint fk_drel_booking_agent_invoice foreign key (agent_invoice) references drel_invoice (id);

alter table drel_booking add constraint fk_drl_bkng_clnt_nvc foreign key (client_invoice) references drel_invoice (id);

alter table ckey_detail add constraint fk_ckey_detail_parent foreign key (one_key,two_key) references ckey_parent (one_key,two_key);
create index ix_ckey_detail_parent on ckey_detail (one_key,two_key);

alter table ckey_parent add constraint fk_ckey_parent_assoc_id foreign key (assoc_id) references ckey_assoc (id);
create index ix_ckey_parent_assoc_id on ckey_parent (assoc_id);

alter table calculation_result add constraint fk_clcltn_rslt_prdct_cnfgrt_1 foreign key (product_configuration_id) references configuration (id);
create index ix_clcltn_rslt_prdct_cnfgrt_1 on calculation_result (product_configuration_id);

alter table calculation_result add constraint fk_clcltn_rslt_grp_cnfgrtn_d foreign key (group_configuration_id) references configuration (id);
create index ix_clcltn_rslt_grp_cnfgrtn_d on calculation_result (group_configuration_id);

alter table sp_car_car_wheels add constraint fk_sp_cr_cr_whls_sp_cr_cr foreign key (car) references sp_car_car (id);
create index ix_sp_cr_cr_whls_sp_cr_cr on sp_car_car_wheels (car);

alter table sp_car_car_wheels add constraint fk_sp_cr_cr_whls_sp_cr_whl foreign key (wheel) references sp_car_wheel (id);
create index ix_sp_cr_cr_whls_sp_cr_whl on sp_car_car_wheels (wheel);

alter table car_accessory add constraint fk_car_accessory_fuse_id foreign key (fuse_id) references car_fuse (id);
create index ix_car_accessory_fuse_id on car_accessory (fuse_id);

alter table car_accessory add constraint fk_car_accessory_car_id foreign key (car_id) references vehicle (id);
create index ix_car_accessory_car_id on car_accessory (car_id);

alter table category add constraint fk_category_surveyobjectid foreign key (surveyobjectid) references survey (id);
create index ix_category_surveyobjectid on category (surveyobjectid);

alter table child_person add constraint fk_child_person_some_bean_id foreign key (some_bean_id) references e_basic (id);
create index ix_child_person_some_bean_id on child_person (some_bean_id);

alter table child_person add constraint fk_chld_prsn_prnt_dntfr foreign key (parent_identifier) references parent_person (identifier);
create index ix_chld_prsn_prnt_dntfr on child_person (parent_identifier);

alter table configuration add constraint fk_cnfgrtn_cnfgrtns_d foreign key (configurations_id) references configurations (id);
create index ix_cnfgrtn_cnfgrtns_d on configuration (configurations_id);

alter table contact add constraint fk_contact_customer_id foreign key (customer_id) references o_customer (id);
create index ix_contact_customer_id on contact (customer_id);

alter table contact add constraint fk_contact_group_id foreign key (group_id) references contact_group (id);
create index ix_contact_group_id on contact (group_id);

alter table contact_note add constraint fk_contact_note_contact_id foreign key (contact_id) references contact (id);
create index ix_contact_note_contact_id on contact_note (contact_id);

alter table c_conversation add constraint fk_c_conversation_group_id foreign key (group_id) references c_group (id);
create index ix_c_conversation_group_id on c_conversation (group_id);

alter table o_customer add constraint fk_o_cstmr_bllng_ddrss_d foreign key (billing_address_id) references o_address (id);
create index ix_o_cstmr_bllng_ddrss_d on o_customer (billing_address_id);

alter table o_customer add constraint fk_o_cstmr_shppng_ddrss_d foreign key (shipping_address_id) references o_address (id);
create index ix_o_cstmr_shppng_ddrss_d on o_customer (shipping_address_id);

alter table dcredit_drol add constraint fk_dcredit_drol_dcredit foreign key (dcredit_id) references dcredit (id);
create index ix_dcredit_drol_dcredit on dcredit_drol (dcredit_id);

alter table dcredit_drol add constraint fk_dcredit_drol_drol foreign key (drol_id) references drol (id);
create index ix_dcredit_drol_drol on dcredit_drol (drol_id);

alter table drot_drol add constraint fk_drot_drol_drot foreign key (drot_id) references drot (id);
create index ix_drot_drol_drot on drot_drol (drot_id);

alter table drot_drol add constraint fk_drot_drol_drol foreign key (drol_id) references drol (id);
create index ix_drot_drol_drol on drot_drol (drol_id);

alter table dfk_cascade add constraint fk_dfk_cascade_one_id foreign key (one_id) references dfk_cascade_one (id) on delete cascade;
create index ix_dfk_cascade_one_id on dfk_cascade (one_id);

alter table dfk_set_null add constraint fk_dfk_set_null_one_id foreign key (one_id) references dfk_one (id) on delete set null;
create index ix_dfk_set_null_one_id on dfk_set_null (one_id);

alter table doc add constraint fk_doc_id foreign key (id) references doc_draft (id);

alter table doc_link add constraint fk_doc_link_doc foreign key (doc_id) references doc (id);
create index ix_doc_link_doc on doc_link (doc_id);

alter table doc_link add constraint fk_doc_link_link foreign key (link_id) references link (id);
create index ix_doc_link_link on doc_link (link_id);

alter table document add constraint fk_document_id foreign key (id) references document_draft (id);

alter table document add constraint fk_document_organisation_id foreign key (organisation_id) references organisation (id);
create index ix_document_organisation_id on document (organisation_id);

alter table document_draft add constraint fk_dcmnt_drft_rgnstn_d foreign key (organisation_id) references organisation (id);
create index ix_dcmnt_drft_rgnstn_d on document_draft (organisation_id);

alter table document_media add constraint fk_document_media_document_id foreign key (document_id) references document (id);
create index ix_document_media_document_id on document_media (document_id);

alter table document_media_draft add constraint fk_dcmnt_md_drft_dcmnt_d foreign key (document_id) references document_draft (id);
create index ix_dcmnt_md_drft_dcmnt_d on document_media_draft (document_id);

alter table ebasic_json_map_detail add constraint fk_ebsc_jsn_mp_dtl_wnr_d foreign key (owner_id) references ebasic_json_map (id);
create index ix_ebsc_jsn_mp_dtl_wnr_d on ebasic_json_map_detail (owner_id);

alter table ebasic_no_sdchild add constraint fk_ebasic_no_sdchild_owner_id foreign key (owner_id) references ebasic_soft_delete (id);
create index ix_ebasic_no_sdchild_owner_id on ebasic_no_sdchild (owner_id);

alter table ebasic_sdchild add constraint fk_ebasic_sdchild_owner_id foreign key (owner_id) references ebasic_soft_delete (id);
create index ix_ebasic_sdchild_owner_id on ebasic_sdchild (owner_id);

alter table eemb_inner add constraint fk_eemb_inner_outer_id foreign key (outer_id) references eemb_outer (id);
create index ix_eemb_inner_outer_id on eemb_inner (outer_id);

alter table einvoice add constraint fk_einvoice_person_id foreign key (person_id) references eperson (id);
create index ix_einvoice_person_id on einvoice (person_id);

alter table enull_collection_detail add constraint fk_enll_cllctn_dtl_nll_cllc_1 foreign key (enull_collection_id) references enull_collection (id);
create index ix_enll_cllctn_dtl_nll_cllc_1 on enull_collection_detail (enull_collection_id);

alter table eopt_one_a add constraint fk_eopt_one_a_b_id foreign key (b_id) references eopt_one_b (id);
create index ix_eopt_one_a_b_id on eopt_one_a (b_id);

alter table eopt_one_b add constraint fk_eopt_one_b_c_id foreign key (c_id) references eopt_one_c (id);
create index ix_eopt_one_b_c_id on eopt_one_b (c_id);

alter table esoft_del_book add constraint fk_esoft_del_book_lend_by_id foreign key (lend_by_id) references esoft_del_user (id);
create index ix_esoft_del_book_lend_by_id on esoft_del_book (lend_by_id);

alter table esoft_del_book_esoft_del_user add constraint fk_esft_dl_bk_sft_dl_sr_sft_1 foreign key (esoft_del_book_id) references esoft_del_book (id);
create index ix_esft_dl_bk_sft_dl_sr_sft_1 on esoft_del_book_esoft_del_user (esoft_del_book_id);

alter table esoft_del_book_esoft_del_user add constraint fk_esft_dl_bk_sft_dl_sr_sft_2 foreign key (esoft_del_user_id) references esoft_del_user (id);
create index ix_esft_dl_bk_sft_dl_sr_sft_2 on esoft_del_book_esoft_del_user (esoft_del_user_id);

alter table esoft_del_down add constraint fk_esft_dl_dwn_sft_dl_md_d foreign key (esoft_del_mid_id) references esoft_del_mid (id);
create index ix_esft_dl_dwn_sft_dl_md_d on esoft_del_down (esoft_del_mid_id);

alter table esoft_del_mid add constraint fk_esoft_del_mid_top_id foreign key (top_id) references esoft_del_top (id);
create index ix_esoft_del_mid_top_id on esoft_del_mid (top_id);

alter table esoft_del_mid add constraint fk_esoft_del_mid_up_id foreign key (up_id) references esoft_del_up (id);
create index ix_esoft_del_mid_up_id on esoft_del_mid (up_id);

alter table esoft_del_role_esoft_del_user add constraint fk_esft_dl_rl_sft_dl_sr_sft_1 foreign key (esoft_del_role_id) references esoft_del_role (id);
create index ix_esft_dl_rl_sft_dl_sr_sft_1 on esoft_del_role_esoft_del_user (esoft_del_role_id);

alter table esoft_del_role_esoft_del_user add constraint fk_esft_dl_rl_sft_dl_sr_sft_2 foreign key (esoft_del_user_id) references esoft_del_user (id);
create index ix_esft_dl_rl_sft_dl_sr_sft_2 on esoft_del_role_esoft_del_user (esoft_del_user_id);

alter table esoft_del_user_esoft_del_role add constraint fk_esft_dl_sr_sft_dl_rl_sft_1 foreign key (esoft_del_user_id) references esoft_del_user (id);
create index ix_esft_dl_sr_sft_dl_rl_sft_1 on esoft_del_user_esoft_del_role (esoft_del_user_id);

alter table esoft_del_user_esoft_del_role add constraint fk_esft_dl_sr_sft_dl_rl_sft_2 foreign key (esoft_del_role_id) references esoft_del_role (id);
create index ix_esft_dl_sr_sft_dl_rl_sft_2 on esoft_del_user_esoft_del_role (esoft_del_role_id);

alter table rawinherit_uncle add constraint fk_rawinherit_uncle_parent_id foreign key (parent_id) references rawinherit_parent (id);
create index ix_rawinherit_uncle_parent_id on rawinherit_uncle (parent_id);

alter table evanilla_collection_detail add constraint fk_evnll_cllctn_dtl_vnll_cl_1 foreign key (evanilla_collection_id) references evanilla_collection (id);
create index ix_evnll_cllctn_dtl_vnll_cl_1 on evanilla_collection_detail (evanilla_collection_id);

alter table td_child add constraint fk_td_child_parent_id foreign key (parent_id) references td_parent (parent_id);
create index ix_td_child_parent_id on td_child (parent_id);

alter table empl add constraint fk_empl_default_address_id foreign key (default_address_id) references addr (id);
create index ix_empl_default_address_id on empl (default_address_id);

alter table grand_parent_person add constraint fk_grnd_prnt_prsn_sm_bn_d foreign key (some_bean_id) references e_basic (id);
create index ix_grnd_prnt_prsn_sm_bn_d on grand_parent_person (some_bean_id);

alter table survey_group add constraint fk_srvy_grp_ctgrybjctd foreign key (categoryobjectid) references category (id);
create index ix_srvy_grp_ctgrybjctd on survey_group (categoryobjectid);

alter table hx_link_doc add constraint fk_hx_link_doc_hx_link foreign key (hx_link_id) references hx_link (id);
create index ix_hx_link_doc_hx_link on hx_link_doc (hx_link_id);

alter table hx_link_doc add constraint fk_hx_link_doc_he_doc foreign key (he_doc_id) references he_doc (id);
create index ix_hx_link_doc_he_doc on hx_link_doc (he_doc_id);

alter table hi_link_doc add constraint fk_hi_link_doc_hi_link foreign key (hi_link_id) references hi_link (id);
create index ix_hi_link_doc_hi_link on hi_link_doc (hi_link_id);

alter table hi_link_doc add constraint fk_hi_link_doc_hi_doc foreign key (hi_doc_id) references hi_doc (id);
create index ix_hi_link_doc_hi_doc on hi_link_doc (hi_doc_id);

alter table imrelated add constraint fk_imrelated_owner_id foreign key (owner_id) references imroot (id);
create index ix_imrelated_owner_id on imrelated (owner_id);

alter table info_contact add constraint fk_info_contact_company_id foreign key (company_id) references info_company (id);
create index ix_info_contact_company_id on info_contact (company_id);

alter table info_customer add constraint fk_info_customer_company_id foreign key (company_id) references info_company (id);

alter table inner_report add constraint fk_inner_report_forecast_id foreign key (forecast_id) references stockforecast (id);

alter table drel_invoice add constraint fk_drel_invoice_booking foreign key (booking) references drel_booking (id);
create index ix_drel_invoice_booking on drel_invoice (booking);

alter table item add constraint fk_item_etype foreign key (customer,type) references type (customer,type);
create index ix_item_etype on item (customer,type);

alter table item add constraint fk_item_eregion foreign key (customer,region) references region (customer,type);
create index ix_item_eregion on item (customer,region);

alter table trainer_monkey add constraint fk_trainer_monkey_trainer foreign key (trainer_tid) references trainer (tid);

alter table trainer_monkey add constraint fk_trainer_monkey_monkey foreign key (monkey_mid) references monkey (mid);

alter table troop_monkey add constraint fk_troop_monkey_troop foreign key (troop_pid) references troop (pid);

alter table troop_monkey add constraint fk_troop_monkey_monkey foreign key (monkey_mid) references monkey (mid);

alter table l2_cldf_reset_bean_child add constraint fk_l2_cldf_rst_bn_chld_prnt_d foreign key (parent_id) references l2_cldf_reset_bean (id);
create index ix_l2_cldf_rst_bn_chld_prnt_d on l2_cldf_reset_bean_child (parent_id);

alter table level1_level4 add constraint fk_level1_level4_level1 foreign key (level1_id) references level1 (id);
create index ix_level1_level4_level1 on level1_level4 (level1_id);

alter table level1_level4 add constraint fk_level1_level4_level4 foreign key (level4_id) references level4 (id);
create index ix_level1_level4_level4 on level1_level4 (level4_id);

alter table level1_level2 add constraint fk_level1_level2_level1 foreign key (level1_id) references level1 (id);
create index ix_level1_level2_level1 on level1_level2 (level1_id);

alter table level1_level2 add constraint fk_level1_level2_level2 foreign key (level2_id) references level2 (id);
create index ix_level1_level2_level2 on level1_level2 (level2_id);

alter table level2_level3 add constraint fk_level2_level3_level2 foreign key (level2_id) references level2 (id);
create index ix_level2_level3_level2 on level2_level3 (level2_id);

alter table level2_level3 add constraint fk_level2_level3_level3 foreign key (level3_id) references level3 (id);
create index ix_level2_level3_level3 on level2_level3 (level3_id);

alter table link add constraint fk_link_id foreign key (id) references link_draft (id);

alter table la_attr_value_attribute add constraint fk_l_ttr_vl_ttrbt_l_ttr_vl foreign key (la_attr_value_id) references la_attr_value (id);
create index ix_l_ttr_vl_ttrbt_l_ttr_vl on la_attr_value_attribute (la_attr_value_id);

alter table la_attr_value_attribute add constraint fk_l_ttr_vl_ttrbt_ttrbt foreign key (attribute_id) references attribute (id);
create index ix_l_ttr_vl_ttrbt_ttrbt on la_attr_value_attribute (attribute_id);

alter table mprinter add constraint fk_mprinter_current_state_id foreign key (current_state_id) references mprinter_state (id);
create index ix_mprinter_current_state_id on mprinter (current_state_id);

alter table mprinter add constraint fk_mprinter_last_swap_cyan_id foreign key (last_swap_cyan_id) references mprinter_state (id);

alter table mprinter add constraint fk_mprntr_lst_swp_mgnt_d foreign key (last_swap_magenta_id) references mprinter_state (id);

alter table mprinter add constraint fk_mprntr_lst_swp_yllw_d foreign key (last_swap_yellow_id) references mprinter_state (id);

alter table mprinter add constraint fk_mprntr_lst_swp_blck_d foreign key (last_swap_black_id) references mprinter_state (id);

alter table mprinter_state add constraint fk_mprinter_state_printer_id foreign key (printer_id) references mprinter (id);
create index ix_mprinter_state_printer_id on mprinter_state (printer_id);

alter table mprofile add constraint fk_mprofile_picture_id foreign key (picture_id) references mmedia (id);
create index ix_mprofile_picture_id on mprofile (picture_id);

alter table mrole_muser add constraint fk_mrole_muser_mrole foreign key (mrole_roleid) references mrole (roleid);
create index ix_mrole_muser_mrole on mrole_muser (mrole_roleid);

alter table mrole_muser add constraint fk_mrole_muser_muser foreign key (muser_userid) references muser (userid);
create index ix_mrole_muser_muser on mrole_muser (muser_userid);

alter table muser add constraint fk_muser_user_type_id foreign key (user_type_id) references muser_type (id);
create index ix_muser_user_type_id on muser (user_type_id);

alter table mail_user_inbox add constraint fk_mail_user_inbox_mail_user foreign key (mail_user_id) references mail_user (id);
create index ix_mail_user_inbox_mail_user on mail_user_inbox (mail_user_id);

alter table mail_user_inbox add constraint fk_mail_user_inbox_mail_box foreign key (mail_box_id) references mail_box (id);
create index ix_mail_user_inbox_mail_box on mail_user_inbox (mail_box_id);

alter table mail_user_outbox add constraint fk_mail_user_outbox_mail_user foreign key (mail_user_id) references mail_user (id);
create index ix_mail_user_outbox_mail_user on mail_user_outbox (mail_user_id);

alter table mail_user_outbox add constraint fk_mail_user_outbox_mail_box foreign key (mail_box_id) references mail_box (id);
create index ix_mail_user_outbox_mail_box on mail_user_outbox (mail_box_id);

alter table c_message add constraint fk_c_message_conversation_id foreign key (conversation_id) references c_conversation (id);
create index ix_c_message_conversation_id on c_message (conversation_id);

alter table c_message add constraint fk_c_message_user_id foreign key (user_id) references c_user (id);
create index ix_c_message_user_id on c_message (user_id);

alter table mnoc_user_mnoc_role add constraint fk_mnc_sr_mnc_rl_mnc_sr foreign key (mnoc_user_user_id) references mnoc_user (user_id);
create index ix_mnc_sr_mnc_rl_mnc_sr on mnoc_user_mnoc_role (mnoc_user_user_id);

alter table mnoc_user_mnoc_role add constraint fk_mnc_sr_mnc_rl_mnc_rl foreign key (mnoc_role_role_id) references mnoc_role (role_id);
create index ix_mnc_sr_mnc_rl_mnc_rl on mnoc_user_mnoc_role (mnoc_role_role_id);

alter table mny_b add constraint fk_mny_b_a_id foreign key (a_id) references mny_a (id);
create index ix_mny_b_a_id on mny_b (a_id);

alter table mny_b_mny_c add constraint fk_mny_b_mny_c_mny_b foreign key (mny_b_id) references mny_b (id);
create index ix_mny_b_mny_c_mny_b on mny_b_mny_c (mny_b_id);

alter table mny_b_mny_c add constraint fk_mny_b_mny_c_mny_c foreign key (mny_c_id) references mny_c (id);
create index ix_mny_b_mny_c_mny_c on mny_b_mny_c (mny_c_id);

alter table subtopics add constraint fk_subtopics_mny_topic_1 foreign key (topic) references mny_topic (id);
create index ix_subtopics_mny_topic_1 on subtopics (topic);

alter table subtopics add constraint fk_subtopics_mny_topic_2 foreign key (subtopic) references mny_topic (id);
create index ix_subtopics_mny_topic_2 on subtopics (subtopic);

alter table mp_role add constraint fk_mp_role_mp_user_id foreign key (mp_user_id) references mp_user (id);
create index ix_mp_role_mp_user_id on mp_role (mp_user_id);

alter table ms_many_a_many_b add constraint fk_ms_many_a_many_b_ms_many_a foreign key (ms_many_a_aid) references ms_many_a (aid);
create index ix_ms_many_a_many_b_ms_many_a on ms_many_a_many_b (ms_many_a_aid);

alter table ms_many_a_many_b add constraint fk_ms_many_a_many_b_ms_many_b foreign key (ms_many_b_bid) references ms_many_b (bid);
create index ix_ms_many_a_many_b_ms_many_b on ms_many_a_many_b (ms_many_b_bid);

alter table ms_many_b_many_a add constraint fk_ms_many_b_many_a_ms_many_b foreign key (ms_many_b_bid) references ms_many_b (bid);
create index ix_ms_many_b_many_a_ms_many_b on ms_many_b_many_a (ms_many_b_bid);

alter table ms_many_b_many_a add constraint fk_ms_many_b_many_a_ms_many_a foreign key (ms_many_a_aid) references ms_many_a (aid);
create index ix_ms_many_b_many_a_ms_many_a on ms_many_b_many_a (ms_many_a_aid);

alter table my_lob_size_join_many add constraint fk_my_lb_sz_jn_mny_prnt_d foreign key (parent_id) references my_lob_size (id);
create index ix_my_lb_sz_jn_mny_prnt_d on my_lob_size_join_many (parent_id);

alter table o_cached_bean_country add constraint fk_o_cchd_bn_cntry__cchd_bn foreign key (o_cached_bean_id) references o_cached_bean (id);
create index ix_o_cchd_bn_cntry__cchd_bn on o_cached_bean_country (o_cached_bean_id);

alter table o_cached_bean_country add constraint fk_o_cchd_bn_cntry__cntry foreign key (o_country_code) references o_country (code);
create index ix_o_cchd_bn_cntry__cntry on o_cached_bean_country (o_country_code);

alter table o_cached_bean_child add constraint fk_o_cchd_bn_chld_cchd_bn_d foreign key (cached_bean_id) references o_cached_bean (id);
create index ix_o_cchd_bn_chld_cchd_bn_d on o_cached_bean_child (cached_bean_id);

alter table oengine add constraint fk_oengine_car_id foreign key (car_id) references ocar (id);

alter table ogear_box add constraint fk_ogear_box_car_id foreign key (car_id) references ocar (id);

alter table oroad_show_msg add constraint fk_oroad_show_msg_company_id foreign key (company_id) references ocompany (id);

alter table om_ordered_detail add constraint fk_om_rdrd_dtl_mstr_d foreign key (master_id) references om_ordered_master (id);
create index ix_om_rdrd_dtl_mstr_d on om_ordered_detail (master_id);

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

alter table oto_bchild add constraint fk_oto_bchild_master_id foreign key (master_id) references oto_bmaster (id);

alter table oto_child add constraint fk_oto_child_master_id foreign key (master_id) references oto_master (id);

alter table oto_cust_address add constraint fk_ot_cst_ddrss_cstmr_cd foreign key (customer_cid) references oto_cust (cid);

alter table oto_prime_extra add constraint fk_oto_prime_extra_eid foreign key (eid) references oto_prime (pid);

alter table oto_th_many add constraint fk_oto_th_many_oto_th_top_id foreign key (oto_th_top_id) references oto_th_top (id);
create index ix_oto_th_many_oto_th_top_id on oto_th_many (oto_th_top_id);

alter table oto_th_one add constraint fk_oto_th_one_many_id foreign key (many_id) references oto_th_many (id);

alter table oto_ubprime_extra add constraint fk_oto_ubprime_extra_eid foreign key (eid) references oto_ubprime (pid);

alter table oto_uprime_extra add constraint fk_oto_uprime_extra_eid foreign key (eid) references oto_uprime (pid);

alter table pfile add constraint fk_pfile_file_content_id foreign key (file_content_id) references pfile_content (id);

alter table pfile add constraint fk_pfile_file_content2_id foreign key (file_content2_id) references pfile_content (id);

alter table paggview add constraint fk_paggview_pview_id foreign key (pview_id) references pp (id);

alter table pallet_location add constraint fk_pallet_location_zone_sid foreign key (zone_sid) references zones (id);
create index ix_pallet_location_zone_sid on pallet_location (zone_sid);

alter table parcel_location add constraint fk_parcel_location_parcelid foreign key (parcelid) references parcel (parcelid);

alter table rawinherit_parent_rawinherit_d add constraint fk_rwnhrt_prnt_rwnhrt_d_rwn_1 foreign key (rawinherit_parent_id) references rawinherit_parent (id);
create index ix_rwnhrt_prnt_rwnhrt_d_rwn_1 on rawinherit_parent_rawinherit_d (rawinherit_parent_id);

alter table rawinherit_parent_rawinherit_d add constraint fk_rwnhrt_prnt_rwnhrt_d_rwn_2 foreign key (rawinherit_data_id) references rawinherit_data (id);
create index ix_rwnhrt_prnt_rwnhrt_d_rwn_2 on rawinherit_parent_rawinherit_d (rawinherit_data_id);

alter table parent_person add constraint fk_parent_person_some_bean_id foreign key (some_bean_id) references e_basic (id);
create index ix_parent_person_some_bean_id on parent_person (some_bean_id);

alter table parent_person add constraint fk_prnt_prsn_prnt_dntfr foreign key (parent_identifier) references grand_parent_person (identifier);
create index ix_prnt_prsn_prnt_dntfr on parent_person (parent_identifier);

alter table c_participation add constraint fk_c_prtcptn_cnvrstn_d foreign key (conversation_id) references c_conversation (id);
create index ix_c_prtcptn_cnvrstn_d on c_participation (conversation_id);

alter table c_participation add constraint fk_c_participation_user_id foreign key (user_id) references c_user (id);
create index ix_c_participation_user_id on c_participation (user_id);

alter table persistent_file_content add constraint fk_prsstnt_fl_cntnt_prsstnt_1 foreign key (persistent_file_id) references persistent_file (id);

alter table person add constraint fk_person_default_address_oid foreign key (default_address_oid) references address (oid);
create index ix_person_default_address_oid on person (default_address_oid);

alter table phones add constraint fk_phones_person_id foreign key (person_id) references persons (id);
create index ix_phones_person_id on phones (person_id);

alter table pp_to_ww add constraint fk_pp_to_ww_pp foreign key (pp_id) references pp (id);
create index ix_pp_to_ww_pp on pp_to_ww (pp_id);

alter table pp_to_ww add constraint fk_pp_to_ww_wview foreign key (ww_id) references wview (id);
create index ix_pp_to_ww_wview on pp_to_ww (ww_id);

alter table question add constraint fk_question_groupobjectid foreign key (groupobjectid) references survey_group (id);
create index ix_question_groupobjectid on question (groupobjectid);

alter table r_orders add constraint fk_r_orders_customer foreign key (company,customername) references rcustomer (company,name);
create index ix_r_orders_customer on r_orders (company,customername);

alter table resourcefile add constraint fk_rsrcfl_prntrsrcfld foreign key (parentresourcefileid) references resourcefile (id);
create index ix_rsrcfl_prntrsrcfld on resourcefile (parentresourcefileid);

alter table mt_role add constraint fk_mt_role_tenant_id foreign key (tenant_id) references mt_tenant (id);
create index ix_mt_role_tenant_id on mt_role (tenant_id);

alter table mt_role_permission add constraint fk_mt_role_permission_mt_role foreign key (mt_role_id) references mt_role (id);
create index ix_mt_role_permission_mt_role on mt_role_permission (mt_role_id);

alter table mt_role_permission add constraint fk_mt_rl_prmssn_mt_prmssn foreign key (mt_permission_id) references mt_permission (id);
create index ix_mt_rl_prmssn_mt_prmssn on mt_role_permission (mt_permission_id);

alter table f_second add constraint fk_f_second_first foreign key (first) references f_first (id);

alter table section add constraint fk_section_article_id foreign key (article_id) references article (id);
create index ix_section_article_id on section (article_id);

alter table self_parent add constraint fk_self_parent_parent_id foreign key (parent_id) references self_parent (id);
create index ix_self_parent_parent_id on self_parent (parent_id);

alter table self_ref_customer add constraint fk_slf_rf_cstmr_rfrrd_by_d foreign key (referred_by_id) references self_ref_customer (id);
create index ix_slf_rf_cstmr_rfrrd_by_d on self_ref_customer (referred_by_id);

alter table self_ref_example add constraint fk_self_ref_example_parent_id foreign key (parent_id) references self_ref_example (id);
create index ix_self_ref_example_parent_id on self_ref_example (parent_id);

alter table stockforecast add constraint fk_stckfrcst_nnr_rprt_d foreign key (inner_report_id) references inner_report (id);
create index ix_stckfrcst_nnr_rprt_d on stockforecast (inner_report_id);

alter table sub_section add constraint fk_sub_section_section_id foreign key (section_id) references section (id);
create index ix_sub_section_section_id on sub_section (section_id);

alter table tevent_many add constraint fk_tevent_many_event_id foreign key (event_id) references tevent_one (id);
create index ix_tevent_many_event_id on tevent_many (event_id);

alter table tevent_one add constraint fk_tevent_one_event_id foreign key (event_id) references tevent (id);

alter table t_detail_with_other_namexxxyy add constraint fk_t_dtl_wth_thr_nmxxxyy_ms_1 foreign key (master_id) references t_atable_thatisrelatively (id);
create index ix_t_dtl_wth_thr_nmxxxyy_ms_1 on t_detail_with_other_namexxxyy (master_id);

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

alter table vehicle add constraint fk_vehicle_car_ref_id foreign key (car_ref_id) references truck_ref (id);
create index ix_vehicle_car_ref_id on vehicle (car_ref_id);

alter table vehicle add constraint fk_vehicle_truck_ref_id foreign key (truck_ref_id) references truck_ref (id);
create index ix_vehicle_truck_ref_id on vehicle (truck_ref_id);

alter table vehicle_driver add constraint fk_vehicle_driver_vehicle_id foreign key (vehicle_id) references vehicle (id);
create index ix_vehicle_driver_vehicle_id on vehicle_driver (vehicle_id);

alter table vehicle_driver add constraint fk_vehicle_driver_address_id foreign key (address_id) references o_address (id);
create index ix_vehicle_driver_address_id on vehicle_driver (address_id);

alter table warehouses add constraint fk_warehouses_officezoneid foreign key (officezoneid) references zones (id);
create index ix_warehouses_officezoneid on warehouses (officezoneid);

alter table warehousesshippingzones add constraint fk_wrhssshppngzns_wrhss foreign key (warehouseid) references warehouses (id);
create index ix_wrhssshppngzns_wrhss on warehousesshippingzones (warehouseid);

alter table warehousesshippingzones add constraint fk_wrhssshppngzns_zns foreign key (shippingzoneid) references zones (id);
create index ix_wrhssshppngzns_zns on warehousesshippingzones (shippingzoneid);

alter table sa_wheel add constraint fk_sa_wheel_tire foreign key (tire) references sa_tire (id);
create index ix_sa_wheel_tire on sa_wheel (tire);

alter table sa_wheel add constraint fk_sa_wheel_car foreign key (car) references sa_car (id);
create index ix_sa_wheel_car on sa_wheel (car);

alter table g_who_props_otm add constraint fk_g_wh_prps_tm_wh_crtd_d foreign key (who_created_id) references g_user (id);
create index ix_g_wh_prps_tm_wh_crtd_d on g_who_props_otm (who_created_id);

alter table g_who_props_otm add constraint fk_g_wh_prps_tm_wh_mdfd_d foreign key (who_modified_id) references g_user (id);
create index ix_g_wh_prps_tm_wh_mdfd_d on g_who_props_otm (who_modified_id);

alter table with_zero add constraint fk_with_zero_parent_id foreign key (parent_id) references parent (id);
create index ix_with_zero_parent_id on with_zero (parent_id);

