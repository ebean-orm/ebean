alter table bar drop foreign key fk_bar_foo_id;
drop index ix_bar_foo_id on bar;

alter table o_address drop foreign key fk_o_address_country_code;
drop index ix_o_address_country_code on o_address;

alter table animals drop foreign key fk_animals_shelter_id;
drop index ix_animals_shelter_id on animals;

alter table attribute drop foreign key fk_attribute_attribute_holder_id;
drop index ix_attribute_attribute_holder_id on attribute;

alter table bbookmark drop foreign key fk_bbookmark_user_id;
drop index ix_bbookmark_user_id on bbookmark;

alter table drel_booking drop foreign key fk_drel_booking_agent_invoice;

alter table drel_booking drop foreign key fk_drel_booking_client_invoice;

alter table ckey_detail drop foreign key fk_ckey_detail_ckey_parent;
drop index ix_ckey_detail_one_key_two_key on ckey_detail;

alter table ckey_parent drop foreign key fk_ckey_parent_assoc_id;
drop index ix_ckey_parent_assoc_id on ckey_parent;

alter table calculation_result drop foreign key fk_calculation_result_product_configuration_id;
drop index ix_calculation_result_product_configuration_id on calculation_result;

alter table calculation_result drop foreign key fk_calculation_result_group_configuration_id;
drop index ix_calculation_result_group_configuration_id on calculation_result;

alter table sp_car_car_wheels drop foreign key fk_sp_car_car_wheels_sp_car_car;
drop index ix_sp_car_car_wheels_car on sp_car_car_wheels;

alter table sp_car_car_wheels drop foreign key fk_sp_car_car_wheels_sp_car_wheel;
drop index ix_sp_car_car_wheels_wheel on sp_car_car_wheels;

alter table car_accessory drop foreign key fk_car_accessory_car_id;
drop index ix_car_accessory_car_id on car_accessory;

alter table configuration drop foreign key fk_configuration_configurations_id;
drop index ix_configuration_configurations_id on configuration;

alter table contact drop foreign key fk_contact_customer_id;
drop index ix_contact_customer_id on contact;

alter table contact drop foreign key fk_contact_group_id;
drop index ix_contact_group_id on contact;

alter table contact_note drop foreign key fk_contact_note_contact_id;
drop index ix_contact_note_contact_id on contact_note;

alter table c_conversation drop foreign key fk_c_conversation_group_id;
drop index ix_c_conversation_group_id on c_conversation;

alter table o_customer drop foreign key fk_o_customer_billing_address_id;
drop index ix_o_customer_billing_address_id on o_customer;

alter table o_customer drop foreign key fk_o_customer_shipping_address_id;
drop index ix_o_customer_shipping_address_id on o_customer;

alter table eemb_inner drop foreign key fk_eemb_inner_outer_id;
drop index ix_eemb_inner_outer_id on eemb_inner;

alter table einvoice drop foreign key fk_einvoice_person_id;
drop index ix_einvoice_person_id on einvoice;

alter table enull_collection_detail drop foreign key fk_enull_collection_detail_enull_collection_id;
drop index ix_enull_collection_detail_enull_collection_id on enull_collection_detail;

alter table eopt_one_a drop foreign key fk_eopt_one_a_b_id;
drop index ix_eopt_one_a_b_id on eopt_one_a;

alter table eopt_one_b drop foreign key fk_eopt_one_b_c_id;
drop index ix_eopt_one_b_c_id on eopt_one_b;

alter table evanilla_collection_detail drop foreign key fk_evanilla_collection_detail_evanilla_collection_id;
drop index ix_evanilla_collection_detail_evanilla_collection_id on evanilla_collection_detail;

alter table td_child drop foreign key fk_td_child_parent_id;
drop index ix_td_child_parent_id on td_child;

alter table imrelated drop foreign key fk_imrelated_owner_id;
drop index ix_imrelated_owner_id on imrelated;

alter table info_contact drop foreign key fk_info_contact_company_id;
drop index ix_info_contact_company_id on info_contact;

alter table info_customer drop foreign key fk_info_customer_company_id;

alter table inner_report drop foreign key fk_inner_report_forecast_id;

alter table drel_invoice drop foreign key fk_drel_invoice_booking;
drop index ix_drel_invoice_booking on drel_invoice;

alter table item drop foreign key fk_item_type;
drop index ix_item_customer_type on item;

alter table item drop foreign key fk_item_region;
drop index ix_item_customer_region on item;

alter table level1_level4 drop foreign key fk_level1_level4_level1;
drop index ix_level1_level4_level1_id on level1_level4;

alter table level1_level4 drop foreign key fk_level1_level4_level4;
drop index ix_level1_level4_level4_id on level1_level4;

alter table level1_level2 drop foreign key fk_level1_level2_level1;
drop index ix_level1_level2_level1_id on level1_level2;

alter table level1_level2 drop foreign key fk_level1_level2_level2;
drop index ix_level1_level2_level2_id on level1_level2;

alter table level2_level3 drop foreign key fk_level2_level3_level2;
drop index ix_level2_level3_level2_id on level2_level3;

alter table level2_level3 drop foreign key fk_level2_level3_level3;
drop index ix_level2_level3_level3_id on level2_level3;

alter table la_attr_value_attribute drop foreign key fk_la_attr_value_attribute_la_attr_value;
drop index ix_la_attr_value_attribute_la_attr_value_id on la_attr_value_attribute;

alter table la_attr_value_attribute drop foreign key fk_la_attr_value_attribute_attribute;
drop index ix_la_attr_value_attribute_attribute_id on la_attr_value_attribute;

alter table mprinter drop foreign key fk_mprinter_current_state_id;
drop index ix_mprinter_current_state_id on mprinter;

alter table mprinter drop foreign key fk_mprinter_last_swap_cyan_id;

alter table mprinter drop foreign key fk_mprinter_last_swap_magenta_id;

alter table mprinter drop foreign key fk_mprinter_last_swap_yellow_id;

alter table mprinter drop foreign key fk_mprinter_last_swap_black_id;

alter table mprinter_state drop foreign key fk_mprinter_state_printer_id;
drop index ix_mprinter_state_printer_id on mprinter_state;

alter table mprofile drop foreign key fk_mprofile_picture_id;
drop index ix_mprofile_picture_id on mprofile;

alter table mrole_muser drop foreign key fk_mrole_muser_mrole;
drop index ix_mrole_muser_mrole_roleid on mrole_muser;

alter table mrole_muser drop foreign key fk_mrole_muser_muser;
drop index ix_mrole_muser_muser_userid on mrole_muser;

alter table muser drop foreign key fk_muser_user_type_id;
drop index ix_muser_user_type_id on muser;

alter table c_message drop foreign key fk_c_message_conversation_id;
drop index ix_c_message_conversation_id on c_message;

alter table c_message drop foreign key fk_c_message_user_id;
drop index ix_c_message_user_id on c_message;

alter table mnoc_user_mnoc_role drop foreign key fk_mnoc_user_mnoc_role_mnoc_user;
drop index ix_mnoc_user_mnoc_role_mnoc_user_user_id on mnoc_user_mnoc_role;

alter table mnoc_user_mnoc_role drop foreign key fk_mnoc_user_mnoc_role_mnoc_role;
drop index ix_mnoc_user_mnoc_role_mnoc_role_role_id on mnoc_user_mnoc_role;

alter table mp_role drop foreign key fk_mp_role_mp_user_id;
drop index ix_mp_role_mp_user_id on mp_role;

alter table my_lob_size_join_many drop foreign key fk_my_lob_size_join_many_parent_id;
drop index ix_my_lob_size_join_many_parent_id on my_lob_size_join_many;

alter table o_cached_bean_country drop foreign key fk_o_cached_bean_country_o_cached_bean;
drop index ix_o_cached_bean_country_o_cached_bean_id on o_cached_bean_country;

alter table o_cached_bean_country drop foreign key fk_o_cached_bean_country_o_country;
drop index ix_o_cached_bean_country_o_country_code on o_cached_bean_country;

alter table o_cached_bean_child drop foreign key fk_o_cached_bean_child_cached_bean_id;
drop index ix_o_cached_bean_child_cached_bean_id on o_cached_bean_child;

alter table oengine drop foreign key fk_oengine_car_id;

alter table ogear_box drop foreign key fk_ogear_box_car_id;

alter table o_order drop foreign key fk_o_order_kcustomer_id;
drop index ix_o_order_kcustomer_id on o_order;

alter table o_order_detail drop foreign key fk_o_order_detail_order_id;
drop index ix_o_order_detail_order_id on o_order_detail;

alter table o_order_detail drop foreign key fk_o_order_detail_product_id;
drop index ix_o_order_detail_product_id on o_order_detail;

alter table s_order_items drop foreign key fk_s_order_items_order_uuid;
drop index ix_s_order_items_order_uuid on s_order_items;

alter table or_order_ship drop foreign key fk_or_order_ship_order_id;
drop index ix_or_order_ship_order_id on or_order_ship;

alter table oto_child drop foreign key fk_oto_child_master_id;

alter table pfile drop foreign key fk_pfile_file_content_id;

alter table pfile drop foreign key fk_pfile_file_content2_id;

alter table paggview drop foreign key fk_paggview_pview_id;

alter table pallet_location drop foreign key fk_pallet_location_zone_sid;
drop index ix_pallet_location_zone_sid on pallet_location;

alter table parcel_location drop foreign key fk_parcel_location_parcelid;

alter table rawinherit_parent_rawinherit_data drop foreign key fk_rawinherit_parent_rawinherit_data_rawinherit_parent;
drop index ix_rawinherit_parent_rawinherit_data_rawinherit_parent_id on rawinherit_parent_rawinherit_data;

alter table rawinherit_parent_rawinherit_data drop foreign key fk_rawinherit_parent_rawinherit_data_rawinherit_data;
drop index ix_rawinherit_parent_rawinherit_data_rawinherit_data_id on rawinherit_parent_rawinherit_data;

alter table c_participation drop foreign key fk_c_participation_conversation_id;
drop index ix_c_participation_conversation_id on c_participation;

alter table c_participation drop foreign key fk_c_participation_user_id;
drop index ix_c_participation_user_id on c_participation;

alter table persistent_file_content drop foreign key fk_persistent_file_content_persistent_file_id;

alter table person drop foreign key fk_person_default_address_oid;
drop index ix_person_default_address_oid on person;

alter table phones drop foreign key fk_phones_person_id;
drop index ix_phones_person_id on phones;

alter table pp_to_ww drop foreign key fk_pp_to_ww_pp;
drop index ix_pp_to_ww_pp_id on pp_to_ww;

alter table pp_to_ww drop foreign key fk_pp_to_ww_wview;
drop index ix_pp_to_ww_ww_id on pp_to_ww;

alter table r_orders drop foreign key fk_r_orders_rcustomer;
drop index ix_r_orders_company_customername on r_orders;

alter table resourcefile drop foreign key fk_resourcefile_parentresourcefileid;
drop index ix_resourcefile_parentresourcefileid on resourcefile;

alter table mt_role drop foreign key fk_mt_role_tenant_id;
drop index ix_mt_role_tenant_id on mt_role;

alter table mt_role_permission drop foreign key fk_mt_role_permission_mt_role;
drop index ix_mt_role_permission_mt_role_id on mt_role_permission;

alter table mt_role_permission drop foreign key fk_mt_role_permission_mt_permission;
drop index ix_mt_role_permission_mt_permission_id on mt_role_permission;

alter table f_second drop foreign key fk_f_second_first;

alter table section drop foreign key fk_section_article_id;
drop index ix_section_article_id on section;

alter table self_parent drop foreign key fk_self_parent_parent_id;
drop index ix_self_parent_parent_id on self_parent;

alter table self_ref_customer drop foreign key fk_self_ref_customer_referred_by_id;
drop index ix_self_ref_customer_referred_by_id on self_ref_customer;

alter table self_ref_example drop foreign key fk_self_ref_example_parent_id;
drop index ix_self_ref_example_parent_id on self_ref_example;

alter table stockforecast drop foreign key fk_stockforecast_inner_report_id;
drop index ix_stockforecast_inner_report_id on stockforecast;

alter table sub_section drop foreign key fk_sub_section_section_id;
drop index ix_sub_section_section_id on sub_section;

alter table t_detail_with_other_namexxxyy drop foreign key fk_t_detail_with_other_namexxxyy_master_id;
drop index ix_t_detail_with_other_namexxxyy_master_id on t_detail_with_other_namexxxyy;

alter table ts_detail_two drop foreign key fk_ts_detail_two_master_id;
drop index ix_ts_detail_two_master_id on ts_detail_two;

alter table twheel drop foreign key fk_twheel_owner_plate_no;
drop index ix_twheel_owner_plate_no on twheel;

alter table tire drop foreign key fk_tire_wheel;

alter table trip drop foreign key fk_trip_vehicle_driver_id;
drop index ix_trip_vehicle_driver_id on trip;

alter table trip drop foreign key fk_trip_address_id;
drop index ix_trip_address_id on trip;

alter table type drop foreign key fk_type_sub_type_id;
drop index ix_type_sub_type_id on type;

alter table ut_detail drop foreign key fk_ut_detail_utmaster_id;
drop index ix_ut_detail_utmaster_id on ut_detail;

alter table uutwo drop foreign key fk_uutwo_master_id;
drop index ix_uutwo_master_id on uutwo;

alter table c_user drop foreign key fk_c_user_group_id;
drop index ix_c_user_group_id on c_user;

alter table oto_user drop foreign key fk_oto_user_account_id;

alter table em_user_role drop foreign key fk_em_user_role_user_id;
drop index ix_em_user_role_user_id on em_user_role;

alter table em_user_role drop foreign key fk_em_user_role_role_id;
drop index ix_em_user_role_role_id on em_user_role;

alter table vehicle drop foreign key fk_vehicle_truck_ref_id;
drop index ix_vehicle_truck_ref_id on vehicle;

alter table vehicle drop foreign key fk_vehicle_car_ref_id;
drop index ix_vehicle_car_ref_id on vehicle;

alter table vehicle_driver drop foreign key fk_vehicle_driver_vehicle_id;
drop index ix_vehicle_driver_vehicle_id on vehicle_driver;

alter table vehicle_driver drop foreign key fk_vehicle_driver_address_id;
drop index ix_vehicle_driver_address_id on vehicle_driver;

alter table warehouses drop foreign key fk_warehouses_officezoneid;
drop index ix_warehouses_officezoneid on warehouses;

alter table warehousesshippingzones drop foreign key fk_warehousesshippingzones_warehouses;
drop index ix_warehousesshippingzones_warehouseid on warehousesshippingzones;

alter table warehousesshippingzones drop foreign key fk_warehousesshippingzones_zones;
drop index ix_warehousesshippingzones_shippingzoneid on warehousesshippingzones;

alter table sa_wheel drop foreign key fk_sa_wheel_tire;
drop index ix_sa_wheel_tire on sa_wheel;

alter table sa_wheel drop foreign key fk_sa_wheel_car;
drop index ix_sa_wheel_car on sa_wheel;

alter table with_zero drop foreign key fk_with_zero_parent_id;
drop index ix_with_zero_parent_id on with_zero;

drop table if exists asimple_bean;

drop table if exists bar;

drop table if exists oto_account;

drop table if exists o_address;

drop table if exists address;

drop table if exists animals;

drop table if exists animal_shelter;

drop table if exists article;

drop table if exists attribute;

drop table if exists attribute_holder;

drop table if exists audit_log;

drop table if exists bbookmark;

drop table if exists bbookmark_user;

drop table if exists bsimple_with_gen;

drop table if exists bwith_qident;

drop table if exists basic_joda_entity;

drop table if exists bean_with_time_zone;

drop table if exists drel_booking;

drop table if exists ckey_assoc;

drop table if exists ckey_detail;

drop table if exists ckey_parent;

drop table if exists calculation_result;

drop table if exists cao_bean;

drop table if exists sa_car;

drop table if exists sp_car_car;

drop table if exists sp_car_car_wheels;

drop table if exists car_accessory;

drop table if exists configuration;

drop table if exists configurations;

drop table if exists contact;

drop table if exists contact_group;

drop table if exists contact_note;

drop table if exists c_conversation;

drop table if exists o_country;

drop table if exists o_customer;

drop table if exists dexh_entity;

drop table if exists dperson;

drop table if exists rawinherit_data;

drop table if exists e_basic;

drop table if exists ebasic_clob;

drop table if exists ebasic_clob_fetch_eager;

drop table if exists ebasic_clob_no_ver;

drop table if exists e_basicenc;

drop table if exists e_basicenc_bin;

drop table if exists e_basic_enum_id;

drop table if exists ebasic_json_map;

drop table if exists ebasic_json_map_blob;

drop table if exists ebasic_json_map_clob;

drop table if exists ebasic_json_map_json_b;

drop table if exists ebasic_json_map_varchar;

drop table if exists ebasic_json_node;

drop table if exists ebasic_json_node_blob;

drop table if exists ebasic_json_node_json_b;

drop table if exists ebasic_json_node_varchar;

drop table if exists e_basic_ndc;

drop table if exists e_basicver;

drop table if exists e_basic_withlife;

drop table if exists e_basicverucon;

drop table if exists eemb_inner;

drop table if exists eemb_outer;

drop table if exists egen_props;

drop table if exists einvoice;

drop table if exists e_main;

drop table if exists enull_collection;

drop table if exists enull_collection_detail;

drop table if exists eopt_one_a;

drop table if exists eopt_one_b;

drop table if exists eopt_one_c;

drop table if exists eperson;

drop table if exists esimple;

drop table if exists esome_type;

drop table if exists etrans_many;

drop table if exists evanilla_collection;

drop table if exists evanilla_collection_detail;

drop table if exists ewho_props;

drop table if exists e_withinet;

drop table if exists td_child;

drop table if exists td_parent;

drop table if exists feature_desc;

drop table if exists f_first;

drop table if exists foo;

drop table if exists gen_key_identity;

drop table if exists gen_key_sequence;

drop table if exists gen_key_table;

drop table if exists c_group;

drop table if exists imrelated;

drop table if exists imroot;

drop table if exists ixresource;

drop table if exists info_company;

drop table if exists info_contact;

drop table if exists info_customer;

drop table if exists inner_report;

drop table if exists drel_invoice;

drop table if exists item;

drop table if exists level1;

drop table if exists level1_level4;

drop table if exists level1_level2;

drop table if exists level2;

drop table if exists level2_level3;

drop table if exists level3;

drop table if exists level4;

drop table if exists la_attr_value;

drop table if exists la_attr_value_attribute;

drop table if exists mmedia;

drop table if exists non_updateprop;

drop table if exists mprinter;

drop table if exists mprinter_state;

drop table if exists mprofile;

drop table if exists mprotected_construct_bean;

drop table if exists mrole;

drop table if exists mrole_muser;

drop table if exists msome_other;

drop table if exists muser;

drop table if exists muser_type;

drop table if exists map_super_actual;

drop table if exists c_message;

drop table if exists mnoc_role;

drop table if exists mnoc_user;

drop table if exists mnoc_user_mnoc_role;

drop table if exists mp_role;

drop table if exists mp_user;

drop table if exists my_lob_size;

drop table if exists my_lob_size_join_many;

drop table if exists noidbean;

drop table if exists o_cached_bean;

drop table if exists o_cached_bean_country;

drop table if exists o_cached_bean_child;

drop table if exists ocar;

drop table if exists oengine;

drop table if exists ogear_box;

drop table if exists o_order;

drop table if exists o_order_detail;

drop table if exists s_orders;

drop table if exists s_order_items;

drop table if exists or_order_ship;

drop table if exists oto_child;

drop table if exists oto_master;

drop table if exists pfile;

drop table if exists pfile_content;

drop table if exists paggview;

drop table if exists pallet_location;

drop table if exists parcel;

drop table if exists parcel_location;

drop table if exists rawinherit_parent;

drop table if exists rawinherit_parent_rawinherit_data;

drop table if exists c_participation;

drop table if exists mt_permission;

drop table if exists persistent_file;

drop table if exists persistent_file_content;

drop table if exists persons;

drop table if exists person;

drop table if exists phones;

drop table if exists o_product;

drop table if exists pp;

drop table if exists pp_to_ww;

drop table if exists rcustomer;

drop table if exists r_orders;

drop table if exists region;

drop table if exists resourcefile;

drop table if exists mt_role;

drop table if exists mt_role_permission;

drop table if exists em_role;

drop table if exists f_second;

drop table if exists section;

drop table if exists self_parent;

drop table if exists self_ref_customer;

drop table if exists self_ref_example;

drop table if exists some_enum_bean;

drop table if exists some_file_bean;

drop table if exists some_new_types_bean;

drop table if exists some_period_bean;

drop table if exists stockforecast;

drop table if exists sub_section;

drop table if exists sub_type;

drop table if exists tbytes_only;

drop table if exists tcar;

drop table if exists tint_root;

drop table if exists tjoda_entity;

drop table if exists t_mapsuper1;

drop table if exists t_oneb;

drop table if exists t_detail_with_other_namexxxyy;

drop table if exists ts_detail_two;

drop table if exists t_atable_thatisrelatively;

drop table if exists ts_master_two;

drop table if exists tuuid_entity;

drop table if exists twheel;

drop table if exists twith_pre_insert;

drop table if exists mt_tenant;

drop table if exists sa_tire;

drop table if exists tire;

drop table if exists trip;

drop table if exists truck_ref;

drop table if exists type;

drop table if exists ut_detail;

drop table if exists ut_master;

drop table if exists uuone;

drop table if exists uutwo;

drop table if exists em_user;

drop table if exists tx_user;

drop table if exists c_user;

drop table if exists oto_user;

drop table if exists em_user_role;

drop table if exists vehicle;

drop table if exists vehicle_driver;

drop table if exists warehouses;

drop table if exists warehousesshippingzones;

drop table if exists sa_wheel;

drop table if exists sp_car_wheel;

drop table if exists wheel;

drop table if exists with_zero;

drop table if exists parent;

drop table if exists wview;

drop table if exists zones;

