alter table bar drop constraint fk_bar_foo_id;
drop index ix_bar_foo_id;

alter table o_address drop constraint fk_o_address_country_code;
drop index ix_o_address_country_code;

alter table animals drop constraint fk_animals_shelter_id;
drop index ix_animals_shelter_id;

alter table attribute drop constraint fk_attribute_attribute_holder_1;
drop index ix_attribute_attribute_holder_1;

alter table bbookmark drop constraint fk_bbookmark_user_id;
drop index ix_bbookmark_user_id;

alter table drel_booking drop constraint fk_drel_booking_agent_invoice;

alter table drel_booking drop constraint fk_drel_booking_client_invoice;

alter table ckey_detail drop constraint fk_ckey_detail_ckey_parent;
drop index ix_ckey_detail_one_key_two_key;

alter table ckey_parent drop constraint fk_ckey_parent_assoc_id;
drop index ix_ckey_parent_assoc_id;

alter table calculation_result drop constraint fk_calculation_result_product_1;
drop index ix_calculation_result_product_1;

alter table calculation_result drop constraint fk_calculation_result_group_c_2;
drop index ix_calculation_result_group_c_2;

alter table sp_car_car_wheels drop constraint fk_sp_car_car_wheels_sp_car_car;
drop index ix_sp_car_car_wheels_car;

alter table sp_car_car_wheels drop constraint fk_sp_car_car_wheels_sp_car_w_2;
drop index ix_sp_car_car_wheels_wheel;

alter table car_accessory drop constraint fk_car_accessory_car_id;
drop index ix_car_accessory_car_id;

alter table configuration drop constraint fk_configuration_configuratio_1;
drop index ix_configuration_configuratio_1;

alter table contact drop constraint fk_contact_customer_id;
drop index ix_contact_customer_id;

alter table contact drop constraint fk_contact_group_id;
drop index ix_contact_group_id;

alter table contact_note drop constraint fk_contact_note_contact_id;
drop index ix_contact_note_contact_id;

alter table c_conversation drop constraint fk_c_conversation_group_id;
drop index ix_c_conversation_group_id;

alter table o_customer drop constraint fk_o_customer_billing_address_1;
drop index ix_o_customer_billing_address_1;

alter table o_customer drop constraint fk_o_customer_shipping_addres_2;
drop index ix_o_customer_shipping_addres_2;

alter table eemb_inner drop constraint fk_eemb_inner_outer_id;
drop index ix_eemb_inner_outer_id;

alter table einvoice drop constraint fk_einvoice_person_id;
drop index ix_einvoice_person_id;

alter table enull_collection_detail drop constraint fk_enull_collection_detail_en_1;
drop index ix_enull_collection_detail_en_1;

alter table eopt_one_a drop constraint fk_eopt_one_a_b_id;
drop index ix_eopt_one_a_b_id;

alter table eopt_one_b drop constraint fk_eopt_one_b_c_id;
drop index ix_eopt_one_b_c_id;

alter table evanilla_collection_detail drop constraint fk_evanilla_collection_detail_1;
drop index ix_evanilla_collection_detail_1;

alter table td_child drop constraint fk_td_child_parent_id;
drop index ix_td_child_parent_id;

alter table imrelated drop constraint fk_imrelated_owner_id;
drop index ix_imrelated_owner_id;

alter table info_contact drop constraint fk_info_contact_company_id;
drop index ix_info_contact_company_id;

alter table info_customer drop constraint fk_info_customer_company_id;

alter table inner_report drop constraint fk_inner_report_forecast_id;

alter table drel_invoice drop constraint fk_drel_invoice_booking;
drop index ix_drel_invoice_booking;

alter table item drop constraint fk_item_type;
drop index ix_item_customer_type;

alter table item drop constraint fk_item_region;
drop index ix_item_customer_region;

alter table level1_level4 drop constraint fk_level1_level4_level1;
drop index ix_level1_level4_level1_id;

alter table level1_level4 drop constraint fk_level1_level4_level4;
drop index ix_level1_level4_level4_id;

alter table level1_level2 drop constraint fk_level1_level2_level1;
drop index ix_level1_level2_level1_id;

alter table level1_level2 drop constraint fk_level1_level2_level2;
drop index ix_level1_level2_level2_id;

alter table level2_level3 drop constraint fk_level2_level3_level2;
drop index ix_level2_level3_level2_id;

alter table level2_level3 drop constraint fk_level2_level3_level3;
drop index ix_level2_level3_level3_id;

alter table la_attr_value_attribute drop constraint fk_la_attr_value_attribute_la_1;
drop index ix_la_attr_value_attribute_la_1;

alter table la_attr_value_attribute drop constraint fk_la_attr_value_attribute_at_2;
drop index ix_la_attr_value_attribute_at_2;

alter table mprinter drop constraint fk_mprinter_current_state_id;
drop index ix_mprinter_current_state_id;

alter table mprinter drop constraint fk_mprinter_last_swap_cyan_id;

alter table mprinter drop constraint fk_mprinter_last_swap_magenta_3;

alter table mprinter drop constraint fk_mprinter_last_swap_yellow_id;

alter table mprinter drop constraint fk_mprinter_last_swap_black_id;

alter table mprinter_state drop constraint fk_mprinter_state_printer_id;
drop index ix_mprinter_state_printer_id;

alter table mprofile drop constraint fk_mprofile_picture_id;
drop index ix_mprofile_picture_id;

alter table mrole_muser drop constraint fk_mrole_muser_mrole;
drop index ix_mrole_muser_mrole_roleid;

alter table mrole_muser drop constraint fk_mrole_muser_muser;
drop index ix_mrole_muser_muser_userid;

alter table muser drop constraint fk_muser_user_type_id;
drop index ix_muser_user_type_id;

alter table c_message drop constraint fk_c_message_conversation_id;
drop index ix_c_message_conversation_id;

alter table c_message drop constraint fk_c_message_user_id;
drop index ix_c_message_user_id;

alter table mnoc_user_mnoc_role drop constraint fk_mnoc_user_mnoc_role_mnoc_u_1;
drop index ix_mnoc_user_mnoc_role_mnoc_u_1;

alter table mnoc_user_mnoc_role drop constraint fk_mnoc_user_mnoc_role_mnoc_r_2;
drop index ix_mnoc_user_mnoc_role_mnoc_r_2;

alter table mp_role drop constraint fk_mp_role_mp_user_id;
drop index ix_mp_role_mp_user_id;

alter table my_lob_size_join_many drop constraint fk_my_lob_size_join_many_pare_1;
drop index ix_my_lob_size_join_many_pare_1;

alter table o_cached_bean_country drop constraint fk_o_cached_bean_country_o_ca_1;
drop index ix_o_cached_bean_country_o_ca_1;

alter table o_cached_bean_country drop constraint fk_o_cached_bean_country_o_co_2;
drop index ix_o_cached_bean_country_o_co_2;

alter table o_cached_bean_child drop constraint fk_o_cached_bean_child_cached_1;
drop index ix_o_cached_bean_child_cached_1;

alter table oengine drop constraint fk_oengine_car_id;

alter table ogear_box drop constraint fk_ogear_box_car_id;

alter table o_order drop constraint fk_o_order_kcustomer_id;
drop index ix_o_order_kcustomer_id;

alter table o_order_detail drop constraint fk_o_order_detail_order_id;
drop index ix_o_order_detail_order_id;

alter table o_order_detail drop constraint fk_o_order_detail_product_id;
drop index ix_o_order_detail_product_id;

alter table s_order_items drop constraint fk_s_order_items_order_uuid;
drop index ix_s_order_items_order_uuid;

alter table or_order_ship drop constraint fk_or_order_ship_order_id;
drop index ix_or_order_ship_order_id;

alter table oto_child drop constraint fk_oto_child_master_id;

alter table pfile drop constraint fk_pfile_file_content_id;

alter table pfile drop constraint fk_pfile_file_content2_id;

alter table paggview drop constraint fk_paggview_pview_id;

alter table pallet_location drop constraint fk_pallet_location_zone_sid;
drop index ix_pallet_location_zone_sid;

alter table parcel_location drop constraint fk_parcel_location_parcelid;

alter table rawinherit_parent_rawinherit_dat drop constraint fk_rawinherit_parent_rawinher_1;
drop index ix_rawinherit_parent_rawinher_1;

alter table rawinherit_parent_rawinherit_dat drop constraint fk_rawinherit_parent_rawinher_2;
drop index ix_rawinherit_parent_rawinher_2;

alter table c_participation drop constraint fk_c_participation_conversati_1;
drop index ix_c_participation_conversati_1;

alter table c_participation drop constraint fk_c_participation_user_id;
drop index ix_c_participation_user_id;

alter table persistent_file_content drop constraint fk_persistent_file_content_pe_1;

alter table person drop constraint fk_person_default_address_oid;
drop index ix_person_default_address_oid;

alter table phones drop constraint fk_phones_person_id;
drop index ix_phones_person_id;

alter table pp_to_ww drop constraint fk_pp_to_ww_pp;
drop index ix_pp_to_ww_pp_id;

alter table pp_to_ww drop constraint fk_pp_to_ww_wview;
drop index ix_pp_to_ww_ww_id;

alter table r_orders drop constraint fk_r_orders_rcustomer;
drop index ix_r_orders_company_customern_1;

alter table resourcefile drop constraint fk_resourcefile_parentresourc_1;
drop index ix_resourcefile_parentresourc_1;

alter table mt_role drop constraint fk_mt_role_tenant_id;
drop index ix_mt_role_tenant_id;

alter table mt_role_permission drop constraint fk_mt_role_permission_mt_role;
drop index ix_mt_role_permission_mt_role_1;

alter table mt_role_permission drop constraint fk_mt_role_permission_mt_perm_2;
drop index ix_mt_role_permission_mt_perm_2;

alter table f_second drop constraint fk_f_second_first;

alter table section drop constraint fk_section_article_id;
drop index ix_section_article_id;

alter table self_parent drop constraint fk_self_parent_parent_id;
drop index ix_self_parent_parent_id;

alter table self_ref_customer drop constraint fk_self_ref_customer_referred_1;
drop index ix_self_ref_customer_referred_1;

alter table self_ref_example drop constraint fk_self_ref_example_parent_id;
drop index ix_self_ref_example_parent_id;

alter table stockforecast drop constraint fk_stockforecast_inner_report_1;
drop index ix_stockforecast_inner_report_1;

alter table sub_section drop constraint fk_sub_section_section_id;
drop index ix_sub_section_section_id;

alter table t_detail_with_other_namexxxyy drop constraint fk_t_detail_with_other_namexx_1;
drop index ix_t_detail_with_other_namexx_1;

alter table ts_detail_two drop constraint fk_ts_detail_two_master_id;
drop index ix_ts_detail_two_master_id;

alter table twheel drop constraint fk_twheel_owner_plate_no;
drop index ix_twheel_owner_plate_no;

alter table tire drop constraint fk_tire_wheel;

alter table trip drop constraint fk_trip_vehicle_driver_id;
drop index ix_trip_vehicle_driver_id;

alter table trip drop constraint fk_trip_address_id;
drop index ix_trip_address_id;

alter table type drop constraint fk_type_sub_type_id;
drop index ix_type_sub_type_id;

alter table ut_detail drop constraint fk_ut_detail_utmaster_id;
drop index ix_ut_detail_utmaster_id;

alter table uutwo drop constraint fk_uutwo_master_id;
drop index ix_uutwo_master_id;

alter table c_user drop constraint fk_c_user_group_id;
drop index ix_c_user_group_id;

alter table oto_user drop constraint fk_oto_user_account_id;

alter table em_user_role drop constraint fk_em_user_role_user_id;
drop index ix_em_user_role_user_id;

alter table em_user_role drop constraint fk_em_user_role_role_id;
drop index ix_em_user_role_role_id;

alter table vehicle drop constraint fk_vehicle_truck_ref_id;
drop index ix_vehicle_truck_ref_id;

alter table vehicle drop constraint fk_vehicle_car_ref_id;
drop index ix_vehicle_car_ref_id;

alter table vehicle_driver drop constraint fk_vehicle_driver_vehicle_id;
drop index ix_vehicle_driver_vehicle_id;

alter table vehicle_driver drop constraint fk_vehicle_driver_address_id;
drop index ix_vehicle_driver_address_id;

alter table warehouses drop constraint fk_warehouses_officezoneid;
drop index ix_warehouses_officezoneid;

alter table warehousesshippingzones drop constraint fk_warehousesshippingzones_wa_1;
drop index ix_warehousesshippingzones_wa_1;

alter table warehousesshippingzones drop constraint fk_warehousesshippingzones_zo_2;
drop index ix_warehousesshippingzones_sh_2;

alter table sa_wheel drop constraint fk_sa_wheel_tire;
drop index ix_sa_wheel_tire;

alter table sa_wheel drop constraint fk_sa_wheel_car;
drop index ix_sa_wheel_car;

alter table with_zero drop constraint fk_with_zero_parent_id;
drop index ix_with_zero_parent_id;

drop table if exists asimple_bean;
drop sequence if exists asimple_bean_seq;

drop table if exists bar;
drop sequence if exists bar_seq;

drop table if exists oto_account;
drop sequence if exists oto_account_seq;

drop table if exists o_address;
drop sequence if exists o_address_seq;

drop table if exists address;
drop sequence if exists address_seq;

drop table if exists animals;
drop sequence if exists animals_seq;

drop table if exists animal_shelter;
drop sequence if exists animal_shelter_seq;

drop table if exists article;
drop sequence if exists article_seq;

drop table if exists attribute;
drop sequence if exists attribute_seq;

drop table if exists attribute_holder;
drop sequence if exists attribute_holder_seq;

drop table if exists audit_log;
drop sequence if exists audit_log_seq;

drop table if exists bbookmark;
drop sequence if exists bbookmark_seq;

drop table if exists bbookmark_user;
drop sequence if exists bbookmark_user_seq;

drop table if exists bsimple_with_gen;
drop sequence if exists bsimple_with_gen_seq;

drop table if exists bwith_qident;
drop sequence if exists bwith_qident_seq;

drop table if exists basic_joda_entity;
drop sequence if exists basic_joda_entity_seq;

drop table if exists bean_with_time_zone;
drop sequence if exists bean_with_time_zone_seq;

drop table if exists drel_booking;
drop sequence if exists drel_booking_seq;

drop table if exists ckey_assoc;
drop sequence if exists ckey_assoc_seq;

drop table if exists ckey_detail;
drop sequence if exists ckey_detail_seq;

drop table if exists ckey_parent;

drop table if exists calculation_result;
drop sequence if exists calculation_result_seq;

drop table if exists cao_bean;

drop table if exists sa_car;
drop sequence if exists sa_car_seq;

drop table if exists sp_car_car;
drop sequence if exists sp_car_car_seq;

drop table if exists sp_car_car_wheels;

drop table if exists car_accessory;
drop sequence if exists car_accessory_seq;

drop table if exists configuration;
drop sequence if exists configuration_seq;

drop table if exists configurations;
drop sequence if exists configurations_seq;

drop table if exists contact;
drop sequence if exists contact_seq;

drop table if exists contact_group;
drop sequence if exists contact_group_seq;

drop table if exists contact_note;
drop sequence if exists contact_note_seq;

drop table if exists c_conversation;
drop sequence if exists c_conversation_seq;

drop table if exists o_country;

drop table if exists o_customer;
drop sequence if exists o_customer_seq;

drop table if exists dexh_entity;
drop sequence if exists dexh_entity_seq;

drop table if exists dperson;
drop sequence if exists dperson_seq;

drop table if exists rawinherit_data;
drop sequence if exists rawinherit_data_seq;

drop table if exists e_basic;
drop sequence if exists e_basic_seq;

drop table if exists ebasic_clob;
drop sequence if exists ebasic_clob_seq;

drop table if exists ebasic_clob_fetch_eager;
drop sequence if exists ebasic_clob_fetch_eager_seq;

drop table if exists ebasic_clob_no_ver;
drop sequence if exists ebasic_clob_no_ver_seq;

drop table if exists e_basicenc;
drop sequence if exists e_basicenc_seq;

drop table if exists e_basicenc_bin;
drop sequence if exists e_basicenc_bin_seq;

drop table if exists e_basic_enum_id;

drop table if exists ebasic_json_map;
drop sequence if exists ebasic_json_map_seq;

drop table if exists ebasic_json_map_blob;
drop sequence if exists ebasic_json_map_blob_seq;

drop table if exists ebasic_json_map_clob;
drop sequence if exists ebasic_json_map_clob_seq;

drop table if exists ebasic_json_map_json_b;
drop sequence if exists ebasic_json_map_json_b_seq;

drop table if exists ebasic_json_map_varchar;
drop sequence if exists ebasic_json_map_varchar_seq;

drop table if exists ebasic_json_node;
drop sequence if exists ebasic_json_node_seq;

drop table if exists ebasic_json_node_blob;
drop sequence if exists ebasic_json_node_blob_seq;

drop table if exists ebasic_json_node_json_b;
drop sequence if exists ebasic_json_node_json_b_seq;

drop table if exists ebasic_json_node_varchar;
drop sequence if exists ebasic_json_node_varchar_seq;

drop table if exists e_basic_ndc;
drop sequence if exists e_basic_ndc_seq;

drop table if exists e_basicver;
drop sequence if exists e_basicver_seq;

drop table if exists e_basic_withlife;
drop sequence if exists e_basic_withlife_seq;

drop table if exists e_basicverucon;
drop sequence if exists e_basicverucon_seq;

drop table if exists eemb_inner;
drop sequence if exists eemb_inner_seq;

drop table if exists eemb_outer;
drop sequence if exists eemb_outer_seq;

drop table if exists egen_props;
drop sequence if exists egen_props_seq;

drop table if exists einvoice;
drop sequence if exists einvoice_seq;

drop table if exists e_main;
drop sequence if exists e_main_seq;

drop table if exists enull_collection;
drop sequence if exists enull_collection_seq;

drop table if exists enull_collection_detail;
drop sequence if exists enull_collection_detail_seq;

drop table if exists eopt_one_a;
drop sequence if exists eopt_one_a_seq;

drop table if exists eopt_one_b;
drop sequence if exists eopt_one_b_seq;

drop table if exists eopt_one_c;
drop sequence if exists eopt_one_c_seq;

drop table if exists eperson;
drop sequence if exists eperson_seq;

drop table if exists esimple;

drop table if exists esome_type;
drop sequence if exists esome_type_seq;

drop table if exists etrans_many;
drop sequence if exists etrans_many_seq;

drop table if exists evanilla_collection;
drop sequence if exists evanilla_collection_seq;

drop table if exists evanilla_collection_detail;
drop sequence if exists evanilla_collection_detail_seq;

drop table if exists ewho_props;
drop sequence if exists ewho_props_seq;

drop table if exists e_withinet;
drop sequence if exists e_withinet_seq;

drop table if exists td_child;
drop sequence if exists td_child_seq;

drop table if exists td_parent;
drop sequence if exists td_parent_seq;

drop table if exists feature_desc;
drop sequence if exists feature_desc_seq;

drop table if exists f_first;
drop sequence if exists f_first_seq;

drop table if exists foo;
drop sequence if exists foo_seq;

drop table if exists gen_key_identity;

drop table if exists gen_key_sequence;
drop sequence if exists seq;

drop table if exists gen_key_table;
drop sequence if exists gen_key_table_seq;

drop table if exists c_group;
drop sequence if exists c_group_seq;

drop table if exists imrelated;
drop sequence if exists imrelated_seq;

drop table if exists imroot;
drop sequence if exists imroot_seq;

drop table if exists ixresource;

drop table if exists info_company;
drop sequence if exists info_company_seq;

drop table if exists info_contact;
drop sequence if exists info_contact_seq;

drop table if exists info_customer;
drop sequence if exists info_customer_seq;

drop table if exists inner_report;
drop sequence if exists inner_report_seq;

drop table if exists drel_invoice;
drop sequence if exists drel_invoice_seq;

drop table if exists item;

drop table if exists level1;
drop sequence if exists level1_seq;

drop table if exists level1_level4;

drop table if exists level1_level2;

drop table if exists level2;
drop sequence if exists level2_seq;

drop table if exists level2_level3;

drop table if exists level3;
drop sequence if exists level3_seq;

drop table if exists level4;
drop sequence if exists level4_seq;

drop table if exists la_attr_value;
drop sequence if exists la_attr_value_seq;

drop table if exists la_attr_value_attribute;

drop table if exists mmedia;
drop sequence if exists mmedia_seq;

drop table if exists non_updateprop;
drop sequence if exists non_updateprop_seq;

drop table if exists mprinter;
drop sequence if exists mprinter_seq;

drop table if exists mprinter_state;
drop sequence if exists mprinter_state_seq;

drop table if exists mprofile;
drop sequence if exists mprofile_seq;

drop table if exists mprotected_construct_bean;
drop sequence if exists mprotected_construct_bean_seq;

drop table if exists mrole;
drop sequence if exists mrole_seq;

drop table if exists mrole_muser;

drop table if exists msome_other;
drop sequence if exists msome_other_seq;

drop table if exists muser;
drop sequence if exists muser_seq;

drop table if exists muser_type;
drop sequence if exists muser_type_seq;

drop table if exists map_super_actual;
drop sequence if exists map_super_actual_seq;

drop table if exists c_message;
drop sequence if exists c_message_seq;

drop table if exists mnoc_role;
drop sequence if exists mnoc_role_seq;

drop table if exists mnoc_user;
drop sequence if exists mnoc_user_seq;

drop table if exists mnoc_user_mnoc_role;

drop table if exists mp_role;
drop sequence if exists mp_role_seq;

drop table if exists mp_user;
drop sequence if exists mp_user_seq;

drop table if exists my_lob_size;
drop sequence if exists my_lob_size_seq;

drop table if exists my_lob_size_join_many;
drop sequence if exists my_lob_size_join_many_seq;

drop table if exists noidbean;

drop table if exists o_cached_bean;
drop sequence if exists o_cached_bean_seq;

drop table if exists o_cached_bean_country;

drop table if exists o_cached_bean_child;
drop sequence if exists o_cached_bean_child_seq;

drop table if exists ocar;
drop sequence if exists ocar_seq;

drop table if exists oengine;

drop table if exists ogear_box;

drop table if exists o_order;
drop sequence if exists o_order_seq;

drop table if exists o_order_detail;
drop sequence if exists o_order_detail_seq;

drop table if exists s_orders;

drop table if exists s_order_items;

drop table if exists or_order_ship;
drop sequence if exists or_order_ship_seq;

drop table if exists oto_child;
drop sequence if exists oto_child_seq;

drop table if exists oto_master;
drop sequence if exists oto_master_seq;

drop table if exists pfile;
drop sequence if exists pfile_seq;

drop table if exists pfile_content;
drop sequence if exists pfile_content_seq;

drop table if exists paggview;

drop table if exists pallet_location;
drop sequence if exists pallet_location_seq;

drop table if exists parcel;
drop sequence if exists parcel_seq;

drop table if exists parcel_location;
drop sequence if exists parcel_location_seq;

drop table if exists rawinherit_parent;
drop sequence if exists rawinherit_parent_seq;

drop table if exists rawinherit_parent_rawinherit_dat;

drop table if exists c_participation;
drop sequence if exists c_participation_seq;

drop table if exists mt_permission;

drop table if exists persistent_file;
drop sequence if exists persistent_file_seq;

drop table if exists persistent_file_content;
drop sequence if exists persistent_file_content_seq;

drop table if exists persons;
drop sequence if exists persons_seq;

drop table if exists person;
drop sequence if exists person_seq;

drop table if exists phones;
drop sequence if exists phones_seq;

drop table if exists o_product;
drop sequence if exists o_product_seq;

drop table if exists pp;

drop table if exists pp_to_ww;

drop table if exists rcustomer;

drop table if exists r_orders;

drop table if exists region;

drop table if exists resourcefile;

drop table if exists mt_role;

drop table if exists mt_role_permission;

drop table if exists em_role;
drop sequence if exists em_role_seq;

drop table if exists f_second;
drop sequence if exists f_second_seq;

drop table if exists section;
drop sequence if exists section_seq;

drop table if exists self_parent;
drop sequence if exists self_parent_seq;

drop table if exists self_ref_customer;
drop sequence if exists self_ref_customer_seq;

drop table if exists self_ref_example;
drop sequence if exists self_ref_example_seq;

drop table if exists some_enum_bean;
drop sequence if exists some_enum_bean_seq;

drop table if exists some_file_bean;
drop sequence if exists some_file_bean_seq;

drop table if exists some_new_types_bean;
drop sequence if exists some_new_types_bean_seq;

drop table if exists some_period_bean;
drop sequence if exists some_period_bean_seq;

drop table if exists stockforecast;
drop sequence if exists stockforecast_seq;

drop table if exists sub_section;
drop sequence if exists sub_section_seq;

drop table if exists sub_type;
drop sequence if exists sub_type_seq;

drop table if exists tbytes_only;
drop sequence if exists tbytes_only_seq;

drop table if exists tcar;

drop table if exists tint_root;
drop sequence if exists tint_root_seq;

drop table if exists tjoda_entity;
drop sequence if exists tjoda_entity_seq;

drop table if exists t_mapsuper1;
drop sequence if exists t_mapsuper1_seq;

drop table if exists t_oneb;
drop sequence if exists t_oneb_seq;

drop table if exists t_detail_with_other_namexxxyy;
drop sequence if exists t_atable_detail_seq;

drop table if exists ts_detail_two;
drop sequence if exists ts_detail_two_seq;

drop table if exists t_atable_thatisrelatively;
drop sequence if exists t_atable_master_seq;

drop table if exists ts_master_two;
drop sequence if exists ts_master_two_seq;

drop table if exists tuuid_entity;

drop table if exists twheel;
drop sequence if exists twheel_seq;

drop table if exists twith_pre_insert;
drop sequence if exists twith_pre_insert_seq;

drop table if exists mt_tenant;

drop table if exists sa_tire;
drop sequence if exists sa_tire_seq;

drop table if exists tire;
drop sequence if exists tire_seq;

drop table if exists trip;
drop sequence if exists trip_seq;

drop table if exists truck_ref;
drop sequence if exists truck_ref_seq;

drop table if exists type;

drop table if exists ut_detail;
drop sequence if exists ut_detail_seq;

drop table if exists ut_master;
drop sequence if exists ut_master_seq;

drop table if exists uuone;

drop table if exists uutwo;

drop table if exists em_user;
drop sequence if exists em_user_seq;

drop table if exists tx_user;
drop sequence if exists tx_user_seq;

drop table if exists c_user;
drop sequence if exists c_user_seq;

drop table if exists oto_user;
drop sequence if exists oto_user_seq;

drop table if exists em_user_role;

drop table if exists vehicle;
drop sequence if exists vehicle_seq;

drop table if exists vehicle_driver;
drop sequence if exists vehicle_driver_seq;

drop table if exists warehouses;
drop sequence if exists warehouses_seq;

drop table if exists warehousesshippingzones;

drop table if exists sa_wheel;
drop sequence if exists sa_wheel_seq;

drop table if exists sp_car_wheel;
drop sequence if exists sp_car_wheel_seq;

drop table if exists wheel;
drop sequence if exists wheel_seq;

drop table if exists with_zero;
drop sequence if exists with_zero_seq;

drop table if exists parent;
drop sequence if exists parent_seq;

drop table if exists wview;

drop table if exists zones;
drop sequence if exists zones_seq;

