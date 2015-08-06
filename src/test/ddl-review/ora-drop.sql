alter table bar drop constraint fk_bar_foo_id;
drop index ix_bar_foo_id;

alter table o_address drop constraint fk_o_address_country_code;
drop index ix_o_address_country_code;

alter table animals drop constraint fk_animals_shelter_id;
drop index ix_animals_shelter_id;

alter table attribute drop constraint fk_attribute_attribute_hold_1;
drop index ix_attribute_attribute_hold_1;

alter table bbookmark drop constraint fk_bbookmark_user_id;
drop index ix_bbookmark_user_id;

alter table drel_booking drop constraint fk_drel_booking_agent_invoice;

alter table drel_booking drop constraint fk_drel_booking_client_invo_2;

alter table ckey_detail drop constraint fk_ckey_detail_ckey_parent;
drop index ix_ckey_detail_one_key_two__1;

alter table ckey_parent drop constraint fk_ckey_parent_assoc_id;
drop index ix_ckey_parent_assoc_id;

alter table calculation_result drop constraint fk_calculation_result_produ_1;
drop index ix_calculation_result_produ_1;

alter table calculation_result drop constraint fk_calculation_result_group_2;
drop index ix_calculation_result_group_2;

alter table sp_car_car_wheels drop constraint fk_sp_car_car_wheels_sp_car_1;
drop index ix_sp_car_car_wheels_car;

alter table sp_car_car_wheels drop constraint fk_sp_car_car_wheels_sp_car_2;
drop index ix_sp_car_car_wheels_wheel;

alter table car_accessory drop constraint fk_car_accessory_car_id;
drop index ix_car_accessory_car_id;

alter table configuration drop constraint fk_configuration_configurat_1;
drop index ix_configuration_configurat_1;

alter table contact drop constraint fk_contact_customer_id;
drop index ix_contact_customer_id;

alter table contact drop constraint fk_contact_group_id;
drop index ix_contact_group_id;

alter table contact_note drop constraint fk_contact_note_contact_id;
drop index ix_contact_note_contact_id;

alter table c_conversation drop constraint fk_c_conversation_group_id;
drop index ix_c_conversation_group_id;

alter table o_customer drop constraint fk_o_customer_billing_addre_1;
drop index ix_o_customer_billing_addre_1;

alter table o_customer drop constraint fk_o_customer_shipping_addr_2;
drop index ix_o_customer_shipping_addr_2;

alter table eemb_inner drop constraint fk_eemb_inner_outer_id;
drop index ix_eemb_inner_outer_id;

alter table einvoice drop constraint fk_einvoice_person_id;
drop index ix_einvoice_person_id;

alter table enull_collection_detail drop constraint fk_enull_collection_detail__1;
drop index ix_enull_collection_detail__1;

alter table eopt_one_a drop constraint fk_eopt_one_a_b_id;
drop index ix_eopt_one_a_b_id;

alter table eopt_one_b drop constraint fk_eopt_one_b_c_id;
drop index ix_eopt_one_b_c_id;

alter table evanilla_collection_detail drop constraint fk_evanilla_collection_deta_1;
drop index ix_evanilla_collection_deta_1;

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

alter table la_attr_value_attribute drop constraint fk_la_attr_value_attribute__1;
drop index ix_la_attr_value_attribute__1;

alter table la_attr_value_attribute drop constraint fk_la_attr_value_attribute__2;
drop index ix_la_attr_value_attribute__2;

alter table mprinter drop constraint fk_mprinter_current_state_id;
drop index ix_mprinter_current_state_id;

alter table mprinter drop constraint fk_mprinter_last_swap_cyan_id;

alter table mprinter drop constraint fk_mprinter_last_swap_magen_3;

alter table mprinter drop constraint fk_mprinter_last_swap_yello_4;

alter table mprinter drop constraint fk_mprinter_last_swap_black_5;

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

alter table mnoc_user_mnoc_role drop constraint fk_mnoc_user_mnoc_role_mnoc_1;
drop index ix_mnoc_user_mnoc_role_mnoc_1;

alter table mnoc_user_mnoc_role drop constraint fk_mnoc_user_mnoc_role_mnoc_2;
drop index ix_mnoc_user_mnoc_role_mnoc_2;

alter table mp_role drop constraint fk_mp_role_mp_user_id;
drop index ix_mp_role_mp_user_id;

alter table my_lob_size_join_many drop constraint fk_my_lob_size_join_many_pa_1;
drop index ix_my_lob_size_join_many_pa_1;

alter table o_cached_bean_country drop constraint fk_o_cached_bean_country_o__1;
drop index ix_o_cached_bean_country_o__1;

alter table o_cached_bean_country drop constraint fk_o_cached_bean_country_o__2;
drop index ix_o_cached_bean_country_o__2;

alter table o_cached_bean_child drop constraint fk_o_cached_bean_child_cach_1;
drop index ix_o_cached_bean_child_cach_1;

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

alter table rawinherit_parent_rawinherit_d drop constraint fk_rawinherit_parent_rawinh_1;
drop index ix_rawinherit_parent_rawinh_1;

alter table rawinherit_parent_rawinherit_d drop constraint fk_rawinherit_parent_rawinh_2;
drop index ix_rawinherit_parent_rawinh_2;

alter table c_participation drop constraint fk_c_participation_conversa_1;
drop index ix_c_participation_conversa_1;

alter table c_participation drop constraint fk_c_participation_user_id;
drop index ix_c_participation_user_id;

alter table persistent_file_content drop constraint fk_persistent_file_content__1;

alter table person drop constraint fk_person_default_address_oid;
drop index ix_person_default_address_oid;

alter table phones drop constraint fk_phones_person_id;
drop index ix_phones_person_id;

alter table pp_to_ww drop constraint fk_pp_to_ww_pp;
drop index ix_pp_to_ww_pp_id;

alter table pp_to_ww drop constraint fk_pp_to_ww_wview;
drop index ix_pp_to_ww_ww_id;

alter table r_orders drop constraint fk_r_orders_rcustomer;
drop index ix_r_orders_company_custome_1;

alter table resourcefile drop constraint fk_resourcefile_parentresou_1;
drop index ix_resourcefile_parentresou_1;

alter table mt_role drop constraint fk_mt_role_tenant_id;
drop index ix_mt_role_tenant_id;

alter table mt_role_permission drop constraint fk_mt_role_permission_mt_role;
drop index ix_mt_role_permission_mt_ro_1;

alter table mt_role_permission drop constraint fk_mt_role_permission_mt_pe_2;
drop index ix_mt_role_permission_mt_pe_2;

alter table f_second drop constraint fk_f_second_first;

alter table section drop constraint fk_section_article_id;
drop index ix_section_article_id;

alter table self_parent drop constraint fk_self_parent_parent_id;
drop index ix_self_parent_parent_id;

alter table self_ref_customer drop constraint fk_self_ref_customer_referr_1;
drop index ix_self_ref_customer_referr_1;

alter table self_ref_example drop constraint fk_self_ref_example_parent_id;
drop index ix_self_ref_example_parent_id;

alter table stockforecast drop constraint fk_stockforecast_inner_repo_1;
drop index ix_stockforecast_inner_repo_1;

alter table sub_section drop constraint fk_sub_section_section_id;
drop index ix_sub_section_section_id;

alter table t_detail_with_other_namexxxyy drop constraint fk_t_detail_with_other_name_1;
drop index ix_t_detail_with_other_name_1;

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

alter table warehousesshippingzones drop constraint fk_warehousesshippingzones__1;
drop index ix_warehousesshippingzones__1;

alter table warehousesshippingzones drop constraint fk_warehousesshippingzones__2;
drop index ix_warehousesshippingzones__2;

alter table sa_wheel drop constraint fk_sa_wheel_tire;
drop index ix_sa_wheel_tire;

alter table sa_wheel drop constraint fk_sa_wheel_car;
drop index ix_sa_wheel_car;

alter table with_zero drop constraint fk_with_zero_parent_id;
drop index ix_with_zero_parent_id;

drop table asimple_bean cascade constraints purge;
drop sequence asimple_bean_seq;

drop table bar cascade constraints purge;
drop sequence bar_seq;

drop table oto_account cascade constraints purge;
drop sequence oto_account_seq;

drop table o_address cascade constraints purge;
drop sequence o_address_seq;

drop table address cascade constraints purge;
drop sequence address_seq;

drop table animals cascade constraints purge;
drop sequence animals_seq;

drop table animal_shelter cascade constraints purge;
drop sequence animal_shelter_seq;

drop table article cascade constraints purge;
drop sequence article_seq;

drop table attribute cascade constraints purge;
drop sequence attribute_seq;

drop table attribute_holder cascade constraints purge;
drop sequence attribute_holder_seq;

drop table audit_log cascade constraints purge;
drop sequence audit_log_seq;

drop table bbookmark cascade constraints purge;
drop sequence bbookmark_seq;

drop table bbookmark_user cascade constraints purge;
drop sequence bbookmark_user_seq;

drop table bsimple_with_gen cascade constraints purge;
drop sequence bsimple_with_gen_seq;

drop table bwith_qident cascade constraints purge;
drop sequence bwith_qident_seq;

drop table basic_joda_entity cascade constraints purge;
drop sequence basic_joda_entity_seq;

drop table bean_with_time_zone cascade constraints purge;
drop sequence bean_with_time_zone_seq;

drop table drel_booking cascade constraints purge;
drop sequence drel_booking_seq;

drop table ckey_assoc cascade constraints purge;
drop sequence ckey_assoc_seq;

drop table ckey_detail cascade constraints purge;
drop sequence ckey_detail_seq;

drop table ckey_parent cascade constraints purge;

drop table calculation_result cascade constraints purge;
drop sequence calculation_result_seq;

drop table cao_bean cascade constraints purge;

drop table sa_car cascade constraints purge;
drop sequence sa_car_seq;

drop table sp_car_car cascade constraints purge;
drop sequence sp_car_car_seq;

drop table sp_car_car_wheels cascade constraints purge;

drop table car_accessory cascade constraints purge;
drop sequence car_accessory_seq;

drop table configuration cascade constraints purge;
drop sequence configuration_seq;

drop table configurations cascade constraints purge;
drop sequence configurations_seq;

drop table contact cascade constraints purge;
drop sequence contact_seq;

drop table contact_group cascade constraints purge;
drop sequence contact_group_seq;

drop table contact_note cascade constraints purge;
drop sequence contact_note_seq;

drop table c_conversation cascade constraints purge;
drop sequence c_conversation_seq;

drop table o_country cascade constraints purge;

drop table o_customer cascade constraints purge;
drop sequence o_customer_seq;

drop table dexh_entity cascade constraints purge;
drop sequence dexh_entity_seq;

drop table dperson cascade constraints purge;
drop sequence dperson_seq;

drop table rawinherit_data cascade constraints purge;
drop sequence rawinherit_data_seq;

drop table e_basic cascade constraints purge;
drop sequence e_basic_seq;

drop table ebasic_clob cascade constraints purge;
drop sequence ebasic_clob_seq;

drop table ebasic_clob_fetch_eager cascade constraints purge;
drop sequence ebasic_clob_fetch_eager_seq;

drop table ebasic_clob_no_ver cascade constraints purge;
drop sequence ebasic_clob_no_ver_seq;

drop table e_basicenc cascade constraints purge;
drop sequence e_basicenc_seq;

drop table e_basicenc_bin cascade constraints purge;
drop sequence e_basicenc_bin_seq;

drop table e_basic_enum_id cascade constraints purge;

drop table ebasic_json_map cascade constraints purge;
drop sequence ebasic_json_map_seq;

drop table ebasic_json_map_blob cascade constraints purge;
drop sequence ebasic_json_map_blob_seq;

drop table ebasic_json_map_clob cascade constraints purge;
drop sequence ebasic_json_map_clob_seq;

drop table ebasic_json_map_json_b cascade constraints purge;
drop sequence ebasic_json_map_json_b_seq;

drop table ebasic_json_map_varchar cascade constraints purge;
drop sequence ebasic_json_map_varchar_seq;

drop table ebasic_json_node cascade constraints purge;
drop sequence ebasic_json_node_seq;

drop table ebasic_json_node_blob cascade constraints purge;
drop sequence ebasic_json_node_blob_seq;

drop table ebasic_json_node_json_b cascade constraints purge;
drop sequence ebasic_json_node_json_b_seq;

drop table ebasic_json_node_varchar cascade constraints purge;
drop sequence ebasic_json_node_varchar_seq;

drop table e_basic_ndc cascade constraints purge;
drop sequence e_basic_ndc_seq;

drop table e_basicver cascade constraints purge;
drop sequence e_basicver_seq;

drop table e_basic_withlife cascade constraints purge;
drop sequence e_basic_withlife_seq;

drop table e_basicverucon cascade constraints purge;
drop sequence e_basicverucon_seq;

drop table eemb_inner cascade constraints purge;
drop sequence eemb_inner_seq;

drop table eemb_outer cascade constraints purge;
drop sequence eemb_outer_seq;

drop table egen_props cascade constraints purge;
drop sequence egen_props_seq;

drop table einvoice cascade constraints purge;
drop sequence einvoice_seq;

drop table e_main cascade constraints purge;
drop sequence e_main_seq;

drop table enull_collection cascade constraints purge;
drop sequence enull_collection_seq;

drop table enull_collection_detail cascade constraints purge;
drop sequence enull_collection_detail_seq;

drop table eopt_one_a cascade constraints purge;
drop sequence eopt_one_a_seq;

drop table eopt_one_b cascade constraints purge;
drop sequence eopt_one_b_seq;

drop table eopt_one_c cascade constraints purge;
drop sequence eopt_one_c_seq;

drop table eperson cascade constraints purge;
drop sequence eperson_seq;

drop table esimple cascade constraints purge;
drop sequence esimple_seq;

drop table esome_type cascade constraints purge;
drop sequence esome_type_seq;

drop table etrans_many cascade constraints purge;
drop sequence etrans_many_seq;

drop table evanilla_collection cascade constraints purge;
drop sequence evanilla_collection_seq;

drop table evanilla_collection_detail cascade constraints purge;
drop sequence evanilla_collection_detail_seq;

drop table ewho_props cascade constraints purge;
drop sequence ewho_props_seq;

drop table e_withinet cascade constraints purge;
drop sequence e_withinet_seq;

drop table td_child cascade constraints purge;
drop sequence td_child_seq;

drop table td_parent cascade constraints purge;
drop sequence td_parent_seq;

drop table feature_desc cascade constraints purge;
drop sequence feature_desc_seq;

drop table f_first cascade constraints purge;
drop sequence f_first_seq;

drop table foo cascade constraints purge;
drop sequence foo_seq;

drop table gen_key_identity cascade constraints purge;
drop sequence gen_key_identity_seq;

drop table gen_key_sequence cascade constraints purge;
drop sequence seq;

drop table gen_key_table cascade constraints purge;
drop sequence gen_key_table_seq;

drop table c_group cascade constraints purge;
drop sequence c_group_seq;

drop table imrelated cascade constraints purge;
drop sequence imrelated_seq;

drop table imroot cascade constraints purge;
drop sequence imroot_seq;

drop table ixresource cascade constraints purge;

drop table info_company cascade constraints purge;
drop sequence info_company_seq;

drop table info_contact cascade constraints purge;
drop sequence info_contact_seq;

drop table info_customer cascade constraints purge;
drop sequence info_customer_seq;

drop table inner_report cascade constraints purge;
drop sequence inner_report_seq;

drop table drel_invoice cascade constraints purge;
drop sequence drel_invoice_seq;

drop table item cascade constraints purge;

drop table level1 cascade constraints purge;
drop sequence level1_seq;

drop table level1_level4 cascade constraints purge;

drop table level1_level2 cascade constraints purge;

drop table level2 cascade constraints purge;
drop sequence level2_seq;

drop table level2_level3 cascade constraints purge;

drop table level3 cascade constraints purge;
drop sequence level3_seq;

drop table level4 cascade constraints purge;
drop sequence level4_seq;

drop table la_attr_value cascade constraints purge;
drop sequence la_attr_value_seq;

drop table la_attr_value_attribute cascade constraints purge;

drop table mmedia cascade constraints purge;
drop sequence mmedia_seq;

drop table non_updateprop cascade constraints purge;
drop sequence non_updateprop_seq;

drop table mprinter cascade constraints purge;
drop sequence mprinter_seq;

drop table mprinter_state cascade constraints purge;
drop sequence mprinter_state_seq;

drop table mprofile cascade constraints purge;
drop sequence mprofile_seq;

drop table mprotected_construct_bean cascade constraints purge;
drop sequence mprotected_construct_bean_seq;

drop table mrole cascade constraints purge;
drop sequence mrole_seq;

drop table mrole_muser cascade constraints purge;

drop table msome_other cascade constraints purge;
drop sequence msome_other_seq;

drop table muser cascade constraints purge;
drop sequence muser_seq;

drop table muser_type cascade constraints purge;
drop sequence muser_type_seq;

drop table map_super_actual cascade constraints purge;
drop sequence map_super_actual_seq;

drop table c_message cascade constraints purge;
drop sequence c_message_seq;

drop table mnoc_role cascade constraints purge;
drop sequence mnoc_role_seq;

drop table mnoc_user cascade constraints purge;
drop sequence mnoc_user_seq;

drop table mnoc_user_mnoc_role cascade constraints purge;

drop table mp_role cascade constraints purge;
drop sequence mp_role_seq;

drop table mp_user cascade constraints purge;
drop sequence mp_user_seq;

drop table my_lob_size cascade constraints purge;
drop sequence my_lob_size_seq;

drop table my_lob_size_join_many cascade constraints purge;
drop sequence my_lob_size_join_many_seq;

drop table noidbean cascade constraints purge;

drop table o_cached_bean cascade constraints purge;
drop sequence o_cached_bean_seq;

drop table o_cached_bean_country cascade constraints purge;

drop table o_cached_bean_child cascade constraints purge;
drop sequence o_cached_bean_child_seq;

drop table ocar cascade constraints purge;
drop sequence ocar_seq;

drop table oengine cascade constraints purge;

drop table ogear_box cascade constraints purge;

drop table o_order cascade constraints purge;
drop sequence o_order_seq;

drop table o_order_detail cascade constraints purge;
drop sequence o_order_detail_seq;

drop table s_orders cascade constraints purge;

drop table s_order_items cascade constraints purge;

drop table or_order_ship cascade constraints purge;
drop sequence or_order_ship_seq;

drop table oto_child cascade constraints purge;
drop sequence oto_child_seq;

drop table oto_master cascade constraints purge;
drop sequence oto_master_seq;

drop table pfile cascade constraints purge;
drop sequence pfile_seq;

drop table pfile_content cascade constraints purge;
drop sequence pfile_content_seq;

drop table paggview cascade constraints purge;

drop table pallet_location cascade constraints purge;
drop sequence pallet_location_seq;

drop table parcel cascade constraints purge;
drop sequence parcel_seq;

drop table parcel_location cascade constraints purge;
drop sequence parcel_location_seq;

drop table rawinherit_parent cascade constraints purge;
drop sequence rawinherit_parent_seq;

drop table rawinherit_parent_rawinherit_d cascade constraints purge;

drop table c_participation cascade constraints purge;
drop sequence c_participation_seq;

drop table mt_permission cascade constraints purge;

drop table persistent_file cascade constraints purge;
drop sequence persistent_file_seq;

drop table persistent_file_content cascade constraints purge;
drop sequence persistent_file_content_seq;

drop table persons cascade constraints purge;
drop sequence persons_seq;

drop table person cascade constraints purge;
drop sequence person_seq;

drop table phones cascade constraints purge;
drop sequence phones_seq;

drop table o_product cascade constraints purge;
drop sequence o_product_seq;

drop table pp cascade constraints purge;

drop table pp_to_ww cascade constraints purge;

drop table rcustomer cascade constraints purge;

drop table r_orders cascade constraints purge;

drop table region cascade constraints purge;

drop table resourcefile cascade constraints purge;

drop table mt_role cascade constraints purge;

drop table mt_role_permission cascade constraints purge;

drop table em_role cascade constraints purge;
drop sequence em_role_seq;

drop table f_second cascade constraints purge;
drop sequence f_second_seq;

drop table section cascade constraints purge;
drop sequence section_seq;

drop table self_parent cascade constraints purge;
drop sequence self_parent_seq;

drop table self_ref_customer cascade constraints purge;
drop sequence self_ref_customer_seq;

drop table self_ref_example cascade constraints purge;
drop sequence self_ref_example_seq;

drop table some_enum_bean cascade constraints purge;
drop sequence some_enum_bean_seq;

drop table some_file_bean cascade constraints purge;
drop sequence some_file_bean_seq;

drop table some_new_types_bean cascade constraints purge;
drop sequence some_new_types_bean_seq;

drop table some_period_bean cascade constraints purge;
drop sequence some_period_bean_seq;

drop table stockforecast cascade constraints purge;
drop sequence stockforecast_seq;

drop table sub_section cascade constraints purge;
drop sequence sub_section_seq;

drop table sub_type cascade constraints purge;
drop sequence sub_type_seq;

drop table tbytes_only cascade constraints purge;
drop sequence tbytes_only_seq;

drop table tcar cascade constraints purge;

drop table tint_root cascade constraints purge;
drop sequence tint_root_seq;

drop table tjoda_entity cascade constraints purge;
drop sequence tjoda_entity_seq;

drop table t_mapsuper1 cascade constraints purge;
drop sequence t_mapsuper1_seq;

drop table t_oneb cascade constraints purge;
drop sequence t_oneb_seq;

drop table t_detail_with_other_namexxxyy cascade constraints purge;
drop sequence t_atable_detail_seq;

drop table ts_detail_two cascade constraints purge;
drop sequence ts_detail_two_seq;

drop table t_atable_thatisrelatively cascade constraints purge;
drop sequence t_atable_master_seq;

drop table ts_master_two cascade constraints purge;
drop sequence ts_master_two_seq;

drop table tuuid_entity cascade constraints purge;

drop table twheel cascade constraints purge;
drop sequence twheel_seq;

drop table twith_pre_insert cascade constraints purge;
drop sequence twith_pre_insert_seq;

drop table mt_tenant cascade constraints purge;

drop table sa_tire cascade constraints purge;
drop sequence sa_tire_seq;

drop table tire cascade constraints purge;
drop sequence tire_seq;

drop table trip cascade constraints purge;
drop sequence trip_seq;

drop table truck_ref cascade constraints purge;
drop sequence truck_ref_seq;

drop table type cascade constraints purge;

drop table ut_detail cascade constraints purge;
drop sequence ut_detail_seq;

drop table ut_master cascade constraints purge;
drop sequence ut_master_seq;

drop table uuone cascade constraints purge;

drop table uutwo cascade constraints purge;

drop table em_user cascade constraints purge;
drop sequence em_user_seq;

drop table tx_user cascade constraints purge;
drop sequence tx_user_seq;

drop table c_user cascade constraints purge;
drop sequence c_user_seq;

drop table oto_user cascade constraints purge;
drop sequence oto_user_seq;

drop table em_user_role cascade constraints purge;

drop table vehicle cascade constraints purge;
drop sequence vehicle_seq;

drop table vehicle_driver cascade constraints purge;
drop sequence vehicle_driver_seq;

drop table warehouses cascade constraints purge;
drop sequence warehouses_seq;

drop table warehousesshippingzones cascade constraints purge;

drop table sa_wheel cascade constraints purge;
drop sequence sa_wheel_seq;

drop table sp_car_wheel cascade constraints purge;
drop sequence sp_car_wheel_seq;

drop table wheel cascade constraints purge;
drop sequence wheel_seq;

drop table with_zero cascade constraints purge;
drop sequence with_zero_seq;

drop table parent cascade constraints purge;
drop sequence parent_seq;

drop table wview cascade constraints purge;

drop table zones cascade constraints purge;
drop sequence zones_seq;

