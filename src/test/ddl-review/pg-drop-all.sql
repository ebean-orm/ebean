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

drop table if exists asimple_bean cascade;

drop table if exists bar cascade;

drop table if exists oto_account cascade;

drop table if exists o_address cascade;

drop table if exists address cascade;

drop table if exists animals cascade;

drop table if exists animal_shelter cascade;

drop table if exists article cascade;

drop table if exists attribute cascade;

drop table if exists attribute_holder cascade;

drop table if exists audit_log cascade;

drop table if exists bbookmark cascade;

drop table if exists bbookmark_user cascade;

drop table if exists bsimple_with_gen cascade;

drop table if exists bwith_qident cascade;

drop table if exists basic_joda_entity cascade;

drop table if exists bean_with_time_zone cascade;

drop table if exists drel_booking cascade;
drop sequence if exists drel_booking_seq;

drop table if exists ckey_assoc cascade;

drop table if exists ckey_detail cascade;

drop table if exists ckey_parent cascade;

drop table if exists calculation_result cascade;

drop table if exists cao_bean cascade;

drop table if exists sa_car cascade;
drop sequence if exists sa_car_seq;

drop table if exists sp_car_car cascade;
drop sequence if exists sp_car_car_seq;

drop table if exists sp_car_car_wheels cascade;

drop table if exists car_accessory cascade;

drop table if exists configuration cascade;

drop table if exists configurations cascade;

drop table if exists contact cascade;

drop table if exists contact_group cascade;

drop table if exists contact_note cascade;

drop table if exists c_conversation cascade;

drop table if exists o_country cascade;

drop table if exists o_customer cascade;

drop table if exists dexh_entity cascade;

drop table if exists dperson cascade;

drop table if exists rawinherit_data cascade;

drop table if exists e_basic cascade;

drop table if exists ebasic_clob cascade;

drop table if exists ebasic_clob_fetch_eager cascade;

drop table if exists ebasic_clob_no_ver cascade;

drop table if exists e_basicenc cascade;

drop table if exists e_basicenc_bin cascade;

drop table if exists e_basic_enum_id cascade;

drop table if exists ebasic_json_map cascade;

drop table if exists ebasic_json_map_blob cascade;

drop table if exists ebasic_json_map_clob cascade;

drop table if exists ebasic_json_map_json_b cascade;

drop table if exists ebasic_json_map_varchar cascade;

drop table if exists ebasic_json_node cascade;

drop table if exists ebasic_json_node_blob cascade;

drop table if exists ebasic_json_node_json_b cascade;

drop table if exists ebasic_json_node_varchar cascade;

drop table if exists e_basic_ndc cascade;

drop table if exists e_basicver cascade;

drop table if exists e_basic_withlife cascade;

drop table if exists e_basicverucon cascade;

drop table if exists eemb_inner cascade;

drop table if exists eemb_outer cascade;

drop table if exists egen_props cascade;

drop table if exists einvoice cascade;

drop table if exists e_main cascade;

drop table if exists enull_collection cascade;

drop table if exists enull_collection_detail cascade;

drop table if exists eopt_one_a cascade;

drop table if exists eopt_one_b cascade;

drop table if exists eopt_one_c cascade;

drop table if exists eperson cascade;

drop table if exists esimple cascade;

drop table if exists esome_type cascade;

drop table if exists etrans_many cascade;

drop table if exists evanilla_collection cascade;

drop table if exists evanilla_collection_detail cascade;

drop table if exists ewho_props cascade;

drop table if exists e_withinet cascade;

drop table if exists td_child cascade;

drop table if exists td_parent cascade;

drop table if exists feature_desc cascade;

drop table if exists f_first cascade;

drop table if exists foo cascade;

drop table if exists gen_key_identity cascade;

drop table if exists gen_key_sequence cascade;
drop sequence if exists seq;

drop table if exists gen_key_table cascade;

drop table if exists c_group cascade;

drop table if exists imrelated cascade;

drop table if exists imroot cascade;

drop table if exists ixresource cascade;

drop table if exists info_company cascade;

drop table if exists info_contact cascade;

drop table if exists info_customer cascade;

drop table if exists inner_report cascade;

drop table if exists drel_invoice cascade;
drop sequence if exists drel_invoice_seq;

drop table if exists item cascade;

drop table if exists level1 cascade;

drop table if exists level1_level4 cascade;

drop table if exists level1_level2 cascade;

drop table if exists level2 cascade;

drop table if exists level2_level3 cascade;

drop table if exists level3 cascade;

drop table if exists level4 cascade;

drop table if exists la_attr_value cascade;

drop table if exists la_attr_value_attribute cascade;

drop table if exists mmedia cascade;

drop table if exists non_updateprop cascade;

drop table if exists mprinter cascade;

drop table if exists mprinter_state cascade;

drop table if exists mprofile cascade;

drop table if exists mprotected_construct_bean cascade;

drop table if exists mrole cascade;

drop table if exists mrole_muser cascade;

drop table if exists msome_other cascade;

drop table if exists muser cascade;

drop table if exists muser_type cascade;

drop table if exists map_super_actual cascade;

drop table if exists c_message cascade;

drop table if exists mnoc_role cascade;

drop table if exists mnoc_user cascade;

drop table if exists mnoc_user_mnoc_role cascade;

drop table if exists mp_role cascade;

drop table if exists mp_user cascade;

drop table if exists my_lob_size cascade;

drop table if exists my_lob_size_join_many cascade;

drop table if exists noidbean cascade;

drop table if exists o_cached_bean cascade;

drop table if exists o_cached_bean_country cascade;

drop table if exists o_cached_bean_child cascade;

drop table if exists ocar cascade;

drop table if exists oengine cascade;

drop table if exists ogear_box cascade;

drop table if exists o_order cascade;

drop table if exists o_order_detail cascade;

drop table if exists s_orders cascade;

drop table if exists s_order_items cascade;

drop table if exists or_order_ship cascade;

drop table if exists oto_child cascade;

drop table if exists oto_master cascade;

drop table if exists pfile cascade;

drop table if exists pfile_content cascade;

drop table if exists paggview cascade;

drop table if exists pallet_location cascade;

drop table if exists parcel cascade;

drop table if exists parcel_location cascade;

drop table if exists rawinherit_parent cascade;

drop table if exists rawinherit_parent_rawinherit_dat cascade;

drop table if exists c_participation cascade;

drop table if exists mt_permission cascade;

drop table if exists persistent_file cascade;

drop table if exists persistent_file_content cascade;

drop table if exists persons cascade;

drop table if exists person cascade;

drop table if exists phones cascade;

drop table if exists o_product cascade;

drop table if exists pp cascade;

drop table if exists pp_to_ww cascade;

drop table if exists rcustomer cascade;

drop table if exists r_orders cascade;

drop table if exists region cascade;

drop table if exists resourcefile cascade;

drop table if exists mt_role cascade;

drop table if exists mt_role_permission cascade;

drop table if exists em_role cascade;

drop table if exists f_second cascade;

drop table if exists section cascade;

drop table if exists self_parent cascade;

drop table if exists self_ref_customer cascade;

drop table if exists self_ref_example cascade;

drop table if exists some_enum_bean cascade;

drop table if exists some_file_bean cascade;

drop table if exists some_new_types_bean cascade;

drop table if exists some_period_bean cascade;

drop table if exists stockforecast cascade;

drop table if exists sub_section cascade;

drop table if exists sub_type cascade;

drop table if exists tbytes_only cascade;

drop table if exists tcar cascade;

drop table if exists tint_root cascade;

drop table if exists tjoda_entity cascade;

drop table if exists t_mapsuper1 cascade;

drop table if exists t_oneb cascade;

drop table if exists t_detail_with_other_namexxxyy cascade;
drop sequence if exists t_atable_detail_seq;

drop table if exists ts_detail_two cascade;

drop table if exists t_atable_thatisrelatively cascade;
drop sequence if exists t_atable_master_seq;

drop table if exists ts_master_two cascade;

drop table if exists tuuid_entity cascade;

drop table if exists twheel cascade;

drop table if exists twith_pre_insert cascade;

drop table if exists mt_tenant cascade;

drop table if exists sa_tire cascade;
drop sequence if exists sa_tire_seq;

drop table if exists tire cascade;
drop sequence if exists tire_seq;

drop table if exists trip cascade;

drop table if exists truck_ref cascade;

drop table if exists type cascade;

drop table if exists ut_detail cascade;

drop table if exists ut_master cascade;

drop table if exists uuone cascade;

drop table if exists uutwo cascade;

drop table if exists em_user cascade;

drop table if exists tx_user cascade;

drop table if exists c_user cascade;

drop table if exists oto_user cascade;

drop table if exists em_user_role cascade;

drop table if exists vehicle cascade;

drop table if exists vehicle_driver cascade;

drop table if exists warehouses cascade;

drop table if exists warehousesshippingzones cascade;

drop table if exists sa_wheel cascade;
drop sequence if exists sa_wheel_seq;

drop table if exists sp_car_wheel cascade;
drop sequence if exists sp_car_wheel_seq;

drop table if exists wheel cascade;
drop sequence if exists wheel_seq;

drop table if exists with_zero cascade;

drop table if exists parent cascade;

drop table if exists wview cascade;

drop table if exists zones cascade;

