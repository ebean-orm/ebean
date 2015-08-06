IF OBJECT_ID('fk_bar_foo_id', 'F') IS NOT NULL alter table bar drop constraint fk_bar_foo_id;
drop index ix_bar_foo_id;

IF OBJECT_ID('fk_o_address_country_code', 'F') IS NOT NULL alter table o_address drop constraint fk_o_address_country_code;
drop index ix_o_address_country_code;

IF OBJECT_ID('fk_animals_shelter_id', 'F') IS NOT NULL alter table animals drop constraint fk_animals_shelter_id;
drop index ix_animals_shelter_id;

IF OBJECT_ID('fk_attribute_attribute_holder_1', 'F') IS NOT NULL alter table attribute drop constraint fk_attribute_attribute_holder_1;
drop index ix_attribute_attribute_holder_1;

IF OBJECT_ID('fk_bbookmark_user_id', 'F') IS NOT NULL alter table bbookmark drop constraint fk_bbookmark_user_id;
drop index ix_bbookmark_user_id;

drop index uq_drel_booking_agent_invoice;
drop index uq_drel_booking_client_invoice;
IF OBJECT_ID('fk_drel_booking_agent_invoice', 'F') IS NOT NULL alter table drel_booking drop constraint fk_drel_booking_agent_invoice;

IF OBJECT_ID('fk_drel_booking_client_invoice', 'F') IS NOT NULL alter table drel_booking drop constraint fk_drel_booking_client_invoice;

IF OBJECT_ID('fk_ckey_detail_ckey_parent', 'F') IS NOT NULL alter table ckey_detail drop constraint fk_ckey_detail_ckey_parent;
drop index ix_ckey_detail_one_key_two_key;

IF OBJECT_ID('fk_ckey_parent_assoc_id', 'F') IS NOT NULL alter table ckey_parent drop constraint fk_ckey_parent_assoc_id;
drop index ix_ckey_parent_assoc_id;

IF OBJECT_ID('fk_calculation_result_product_1', 'F') IS NOT NULL alter table calculation_result drop constraint fk_calculation_result_product_1;
drop index ix_calculation_result_product_1;

IF OBJECT_ID('fk_calculation_result_group_c_2', 'F') IS NOT NULL alter table calculation_result drop constraint fk_calculation_result_group_c_2;
drop index ix_calculation_result_group_c_2;

IF OBJECT_ID('fk_sp_car_car_wheels_sp_car_car', 'F') IS NOT NULL alter table sp_car_car_wheels drop constraint fk_sp_car_car_wheels_sp_car_car;
drop index ix_sp_car_car_wheels_car;

IF OBJECT_ID('fk_sp_car_car_wheels_sp_car_w_2', 'F') IS NOT NULL alter table sp_car_car_wheels drop constraint fk_sp_car_car_wheels_sp_car_w_2;
drop index ix_sp_car_car_wheels_wheel;

IF OBJECT_ID('fk_car_accessory_car_id', 'F') IS NOT NULL alter table car_accessory drop constraint fk_car_accessory_car_id;
drop index ix_car_accessory_car_id;

IF OBJECT_ID('fk_configuration_configuratio_1', 'F') IS NOT NULL alter table configuration drop constraint fk_configuration_configuratio_1;
drop index ix_configuration_configuratio_1;

IF OBJECT_ID('fk_contact_customer_id', 'F') IS NOT NULL alter table contact drop constraint fk_contact_customer_id;
drop index ix_contact_customer_id;

IF OBJECT_ID('fk_contact_group_id', 'F') IS NOT NULL alter table contact drop constraint fk_contact_group_id;
drop index ix_contact_group_id;

IF OBJECT_ID('fk_contact_note_contact_id', 'F') IS NOT NULL alter table contact_note drop constraint fk_contact_note_contact_id;
drop index ix_contact_note_contact_id;

IF OBJECT_ID('fk_c_conversation_group_id', 'F') IS NOT NULL alter table c_conversation drop constraint fk_c_conversation_group_id;
drop index ix_c_conversation_group_id;

IF OBJECT_ID('fk_o_customer_billing_address_1', 'F') IS NOT NULL alter table o_customer drop constraint fk_o_customer_billing_address_1;
drop index ix_o_customer_billing_address_1;

IF OBJECT_ID('fk_o_customer_shipping_addres_2', 'F') IS NOT NULL alter table o_customer drop constraint fk_o_customer_shipping_addres_2;
drop index ix_o_customer_shipping_addres_2;

IF OBJECT_ID('fk_eemb_inner_outer_id', 'F') IS NOT NULL alter table eemb_inner drop constraint fk_eemb_inner_outer_id;
drop index ix_eemb_inner_outer_id;

IF OBJECT_ID('fk_einvoice_person_id', 'F') IS NOT NULL alter table einvoice drop constraint fk_einvoice_person_id;
drop index ix_einvoice_person_id;

IF OBJECT_ID('fk_enull_collection_detail_en_1', 'F') IS NOT NULL alter table enull_collection_detail drop constraint fk_enull_collection_detail_en_1;
drop index ix_enull_collection_detail_en_1;

IF OBJECT_ID('fk_eopt_one_a_b_id', 'F') IS NOT NULL alter table eopt_one_a drop constraint fk_eopt_one_a_b_id;
drop index ix_eopt_one_a_b_id;

IF OBJECT_ID('fk_eopt_one_b_c_id', 'F') IS NOT NULL alter table eopt_one_b drop constraint fk_eopt_one_b_c_id;
drop index ix_eopt_one_b_c_id;

IF OBJECT_ID('fk_evanilla_collection_detail_1', 'F') IS NOT NULL alter table evanilla_collection_detail drop constraint fk_evanilla_collection_detail_1;
drop index ix_evanilla_collection_detail_1;

IF OBJECT_ID('fk_td_child_parent_id', 'F') IS NOT NULL alter table td_child drop constraint fk_td_child_parent_id;
drop index ix_td_child_parent_id;

IF OBJECT_ID('fk_imrelated_owner_id', 'F') IS NOT NULL alter table imrelated drop constraint fk_imrelated_owner_id;
drop index ix_imrelated_owner_id;

IF OBJECT_ID('fk_info_contact_company_id', 'F') IS NOT NULL alter table info_contact drop constraint fk_info_contact_company_id;
drop index ix_info_contact_company_id;

drop index uq_info_customer_company_id;
IF OBJECT_ID('fk_info_customer_company_id', 'F') IS NOT NULL alter table info_customer drop constraint fk_info_customer_company_id;

drop index uq_inner_report_forecast_id;
IF OBJECT_ID('fk_inner_report_forecast_id', 'F') IS NOT NULL alter table inner_report drop constraint fk_inner_report_forecast_id;

IF OBJECT_ID('fk_drel_invoice_booking', 'F') IS NOT NULL alter table drel_invoice drop constraint fk_drel_invoice_booking;
drop index ix_drel_invoice_booking;

IF OBJECT_ID('fk_item_type', 'F') IS NOT NULL alter table item drop constraint fk_item_type;
drop index ix_item_customer_type;

IF OBJECT_ID('fk_item_region', 'F') IS NOT NULL alter table item drop constraint fk_item_region;
drop index ix_item_customer_region;

IF OBJECT_ID('fk_level1_level4_level1', 'F') IS NOT NULL alter table level1_level4 drop constraint fk_level1_level4_level1;
drop index ix_level1_level4_level1_id;

IF OBJECT_ID('fk_level1_level4_level4', 'F') IS NOT NULL alter table level1_level4 drop constraint fk_level1_level4_level4;
drop index ix_level1_level4_level4_id;

IF OBJECT_ID('fk_level1_level2_level1', 'F') IS NOT NULL alter table level1_level2 drop constraint fk_level1_level2_level1;
drop index ix_level1_level2_level1_id;

IF OBJECT_ID('fk_level1_level2_level2', 'F') IS NOT NULL alter table level1_level2 drop constraint fk_level1_level2_level2;
drop index ix_level1_level2_level2_id;

IF OBJECT_ID('fk_level2_level3_level2', 'F') IS NOT NULL alter table level2_level3 drop constraint fk_level2_level3_level2;
drop index ix_level2_level3_level2_id;

IF OBJECT_ID('fk_level2_level3_level3', 'F') IS NOT NULL alter table level2_level3 drop constraint fk_level2_level3_level3;
drop index ix_level2_level3_level3_id;

IF OBJECT_ID('fk_la_attr_value_attribute_la_1', 'F') IS NOT NULL alter table la_attr_value_attribute drop constraint fk_la_attr_value_attribute_la_1;
drop index ix_la_attr_value_attribute_la_1;

IF OBJECT_ID('fk_la_attr_value_attribute_at_2', 'F') IS NOT NULL alter table la_attr_value_attribute drop constraint fk_la_attr_value_attribute_at_2;
drop index ix_la_attr_value_attribute_at_2;

drop index uq_mprinter_last_swap_cyan_id;
drop index uq_mprinter_last_swap_magenta_2;
drop index uq_mprinter_last_swap_yellow_id;
drop index uq_mprinter_last_swap_black_id;
IF OBJECT_ID('fk_mprinter_current_state_id', 'F') IS NOT NULL alter table mprinter drop constraint fk_mprinter_current_state_id;
drop index ix_mprinter_current_state_id;

IF OBJECT_ID('fk_mprinter_last_swap_cyan_id', 'F') IS NOT NULL alter table mprinter drop constraint fk_mprinter_last_swap_cyan_id;

IF OBJECT_ID('fk_mprinter_last_swap_magenta_3', 'F') IS NOT NULL alter table mprinter drop constraint fk_mprinter_last_swap_magenta_3;

IF OBJECT_ID('fk_mprinter_last_swap_yellow_id', 'F') IS NOT NULL alter table mprinter drop constraint fk_mprinter_last_swap_yellow_id;

IF OBJECT_ID('fk_mprinter_last_swap_black_id', 'F') IS NOT NULL alter table mprinter drop constraint fk_mprinter_last_swap_black_id;

IF OBJECT_ID('fk_mprinter_state_printer_id', 'F') IS NOT NULL alter table mprinter_state drop constraint fk_mprinter_state_printer_id;
drop index ix_mprinter_state_printer_id;

IF OBJECT_ID('fk_mprofile_picture_id', 'F') IS NOT NULL alter table mprofile drop constraint fk_mprofile_picture_id;
drop index ix_mprofile_picture_id;

IF OBJECT_ID('fk_mrole_muser_mrole', 'F') IS NOT NULL alter table mrole_muser drop constraint fk_mrole_muser_mrole;
drop index ix_mrole_muser_mrole_roleid;

IF OBJECT_ID('fk_mrole_muser_muser', 'F') IS NOT NULL alter table mrole_muser drop constraint fk_mrole_muser_muser;
drop index ix_mrole_muser_muser_userid;

IF OBJECT_ID('fk_muser_user_type_id', 'F') IS NOT NULL alter table muser drop constraint fk_muser_user_type_id;
drop index ix_muser_user_type_id;

IF OBJECT_ID('fk_c_message_conversation_id', 'F') IS NOT NULL alter table c_message drop constraint fk_c_message_conversation_id;
drop index ix_c_message_conversation_id;

IF OBJECT_ID('fk_c_message_user_id', 'F') IS NOT NULL alter table c_message drop constraint fk_c_message_user_id;
drop index ix_c_message_user_id;

IF OBJECT_ID('fk_mnoc_user_mnoc_role_mnoc_u_1', 'F') IS NOT NULL alter table mnoc_user_mnoc_role drop constraint fk_mnoc_user_mnoc_role_mnoc_u_1;
drop index ix_mnoc_user_mnoc_role_mnoc_u_1;

IF OBJECT_ID('fk_mnoc_user_mnoc_role_mnoc_r_2', 'F') IS NOT NULL alter table mnoc_user_mnoc_role drop constraint fk_mnoc_user_mnoc_role_mnoc_r_2;
drop index ix_mnoc_user_mnoc_role_mnoc_r_2;

IF OBJECT_ID('fk_mp_role_mp_user_id', 'F') IS NOT NULL alter table mp_role drop constraint fk_mp_role_mp_user_id;
drop index ix_mp_role_mp_user_id;

IF OBJECT_ID('fk_my_lob_size_join_many_pare_1', 'F') IS NOT NULL alter table my_lob_size_join_many drop constraint fk_my_lob_size_join_many_pare_1;
drop index ix_my_lob_size_join_many_pare_1;

IF OBJECT_ID('fk_o_cached_bean_country_o_ca_1', 'F') IS NOT NULL alter table o_cached_bean_country drop constraint fk_o_cached_bean_country_o_ca_1;
drop index ix_o_cached_bean_country_o_ca_1;

IF OBJECT_ID('fk_o_cached_bean_country_o_co_2', 'F') IS NOT NULL alter table o_cached_bean_country drop constraint fk_o_cached_bean_country_o_co_2;
drop index ix_o_cached_bean_country_o_co_2;

IF OBJECT_ID('fk_o_cached_bean_child_cached_1', 'F') IS NOT NULL alter table o_cached_bean_child drop constraint fk_o_cached_bean_child_cached_1;
drop index ix_o_cached_bean_child_cached_1;

drop index uq_oengine_car_id;
IF OBJECT_ID('fk_oengine_car_id', 'F') IS NOT NULL alter table oengine drop constraint fk_oengine_car_id;

drop index uq_ogear_box_car_id;
IF OBJECT_ID('fk_ogear_box_car_id', 'F') IS NOT NULL alter table ogear_box drop constraint fk_ogear_box_car_id;

IF OBJECT_ID('fk_o_order_kcustomer_id', 'F') IS NOT NULL alter table o_order drop constraint fk_o_order_kcustomer_id;
drop index ix_o_order_kcustomer_id;

IF OBJECT_ID('fk_o_order_detail_order_id', 'F') IS NOT NULL alter table o_order_detail drop constraint fk_o_order_detail_order_id;
drop index ix_o_order_detail_order_id;

IF OBJECT_ID('fk_o_order_detail_product_id', 'F') IS NOT NULL alter table o_order_detail drop constraint fk_o_order_detail_product_id;
drop index ix_o_order_detail_product_id;

IF OBJECT_ID('fk_s_order_items_order_uuid', 'F') IS NOT NULL alter table s_order_items drop constraint fk_s_order_items_order_uuid;
drop index ix_s_order_items_order_uuid;

IF OBJECT_ID('fk_or_order_ship_order_id', 'F') IS NOT NULL alter table or_order_ship drop constraint fk_or_order_ship_order_id;
drop index ix_or_order_ship_order_id;

drop index uq_oto_child_master_id;
IF OBJECT_ID('fk_oto_child_master_id', 'F') IS NOT NULL alter table oto_child drop constraint fk_oto_child_master_id;

drop index uq_pfile_file_content_id;
drop index uq_pfile_file_content2_id;
IF OBJECT_ID('fk_pfile_file_content_id', 'F') IS NOT NULL alter table pfile drop constraint fk_pfile_file_content_id;

IF OBJECT_ID('fk_pfile_file_content2_id', 'F') IS NOT NULL alter table pfile drop constraint fk_pfile_file_content2_id;

drop index uq_paggview_pview_id;
IF OBJECT_ID('fk_paggview_pview_id', 'F') IS NOT NULL alter table paggview drop constraint fk_paggview_pview_id;

IF OBJECT_ID('fk_pallet_location_zone_sid', 'F') IS NOT NULL alter table pallet_location drop constraint fk_pallet_location_zone_sid;
drop index ix_pallet_location_zone_sid;

drop index uq_parcel_location_parcelid;
IF OBJECT_ID('fk_parcel_location_parcelid', 'F') IS NOT NULL alter table parcel_location drop constraint fk_parcel_location_parcelid;

IF OBJECT_ID('fk_rawinherit_parent_rawinher_1', 'F') IS NOT NULL alter table rawinherit_parent_rawinherit_dat drop constraint fk_rawinherit_parent_rawinher_1;
drop index ix_rawinherit_parent_rawinher_1;

IF OBJECT_ID('fk_rawinherit_parent_rawinher_2', 'F') IS NOT NULL alter table rawinherit_parent_rawinherit_dat drop constraint fk_rawinherit_parent_rawinher_2;
drop index ix_rawinherit_parent_rawinher_2;

IF OBJECT_ID('fk_c_participation_conversati_1', 'F') IS NOT NULL alter table c_participation drop constraint fk_c_participation_conversati_1;
drop index ix_c_participation_conversati_1;

IF OBJECT_ID('fk_c_participation_user_id', 'F') IS NOT NULL alter table c_participation drop constraint fk_c_participation_user_id;
drop index ix_c_participation_user_id;

drop index uq_persistent_file_content_pe_1;
IF OBJECT_ID('fk_persistent_file_content_pe_1', 'F') IS NOT NULL alter table persistent_file_content drop constraint fk_persistent_file_content_pe_1;

IF OBJECT_ID('fk_person_default_address_oid', 'F') IS NOT NULL alter table person drop constraint fk_person_default_address_oid;
drop index ix_person_default_address_oid;

IF OBJECT_ID('fk_phones_person_id', 'F') IS NOT NULL alter table phones drop constraint fk_phones_person_id;
drop index ix_phones_person_id;

IF OBJECT_ID('fk_pp_to_ww_pp', 'F') IS NOT NULL alter table pp_to_ww drop constraint fk_pp_to_ww_pp;
drop index ix_pp_to_ww_pp_id;

IF OBJECT_ID('fk_pp_to_ww_wview', 'F') IS NOT NULL alter table pp_to_ww drop constraint fk_pp_to_ww_wview;
drop index ix_pp_to_ww_ww_id;

IF OBJECT_ID('fk_r_orders_rcustomer', 'F') IS NOT NULL alter table r_orders drop constraint fk_r_orders_rcustomer;
drop index ix_r_orders_company_customern_1;

IF OBJECT_ID('fk_resourcefile_parentresourc_1', 'F') IS NOT NULL alter table resourcefile drop constraint fk_resourcefile_parentresourc_1;
drop index ix_resourcefile_parentresourc_1;

IF OBJECT_ID('fk_mt_role_tenant_id', 'F') IS NOT NULL alter table mt_role drop constraint fk_mt_role_tenant_id;
drop index ix_mt_role_tenant_id;

IF OBJECT_ID('fk_mt_role_permission_mt_role', 'F') IS NOT NULL alter table mt_role_permission drop constraint fk_mt_role_permission_mt_role;
drop index ix_mt_role_permission_mt_role_1;

IF OBJECT_ID('fk_mt_role_permission_mt_perm_2', 'F') IS NOT NULL alter table mt_role_permission drop constraint fk_mt_role_permission_mt_perm_2;
drop index ix_mt_role_permission_mt_perm_2;

drop index uq_f_second_first;
IF OBJECT_ID('fk_f_second_first', 'F') IS NOT NULL alter table f_second drop constraint fk_f_second_first;

IF OBJECT_ID('fk_section_article_id', 'F') IS NOT NULL alter table section drop constraint fk_section_article_id;
drop index ix_section_article_id;

IF OBJECT_ID('fk_self_parent_parent_id', 'F') IS NOT NULL alter table self_parent drop constraint fk_self_parent_parent_id;
drop index ix_self_parent_parent_id;

IF OBJECT_ID('fk_self_ref_customer_referred_1', 'F') IS NOT NULL alter table self_ref_customer drop constraint fk_self_ref_customer_referred_1;
drop index ix_self_ref_customer_referred_1;

IF OBJECT_ID('fk_self_ref_example_parent_id', 'F') IS NOT NULL alter table self_ref_example drop constraint fk_self_ref_example_parent_id;
drop index ix_self_ref_example_parent_id;

IF OBJECT_ID('fk_stockforecast_inner_report_1', 'F') IS NOT NULL alter table stockforecast drop constraint fk_stockforecast_inner_report_1;
drop index ix_stockforecast_inner_report_1;

IF OBJECT_ID('fk_sub_section_section_id', 'F') IS NOT NULL alter table sub_section drop constraint fk_sub_section_section_id;
drop index ix_sub_section_section_id;

IF OBJECT_ID('fk_t_detail_with_other_namexx_1', 'F') IS NOT NULL alter table t_detail_with_other_namexxxyy drop constraint fk_t_detail_with_other_namexx_1;
drop index ix_t_detail_with_other_namexx_1;

IF OBJECT_ID('fk_ts_detail_two_master_id', 'F') IS NOT NULL alter table ts_detail_two drop constraint fk_ts_detail_two_master_id;
drop index ix_ts_detail_two_master_id;

IF OBJECT_ID('fk_twheel_owner_plate_no', 'F') IS NOT NULL alter table twheel drop constraint fk_twheel_owner_plate_no;
drop index ix_twheel_owner_plate_no;

drop index uq_tire_wheel;
IF OBJECT_ID('fk_tire_wheel', 'F') IS NOT NULL alter table tire drop constraint fk_tire_wheel;

IF OBJECT_ID('fk_trip_vehicle_driver_id', 'F') IS NOT NULL alter table trip drop constraint fk_trip_vehicle_driver_id;
drop index ix_trip_vehicle_driver_id;

IF OBJECT_ID('fk_trip_address_id', 'F') IS NOT NULL alter table trip drop constraint fk_trip_address_id;
drop index ix_trip_address_id;

IF OBJECT_ID('fk_type_sub_type_id', 'F') IS NOT NULL alter table type drop constraint fk_type_sub_type_id;
drop index ix_type_sub_type_id;

IF OBJECT_ID('fk_ut_detail_utmaster_id', 'F') IS NOT NULL alter table ut_detail drop constraint fk_ut_detail_utmaster_id;
drop index ix_ut_detail_utmaster_id;

IF OBJECT_ID('fk_uutwo_master_id', 'F') IS NOT NULL alter table uutwo drop constraint fk_uutwo_master_id;
drop index ix_uutwo_master_id;

IF OBJECT_ID('fk_c_user_group_id', 'F') IS NOT NULL alter table c_user drop constraint fk_c_user_group_id;
drop index ix_c_user_group_id;

drop index uq_oto_user_account_id;
IF OBJECT_ID('fk_oto_user_account_id', 'F') IS NOT NULL alter table oto_user drop constraint fk_oto_user_account_id;

IF OBJECT_ID('fk_em_user_role_user_id', 'F') IS NOT NULL alter table em_user_role drop constraint fk_em_user_role_user_id;
drop index ix_em_user_role_user_id;

IF OBJECT_ID('fk_em_user_role_role_id', 'F') IS NOT NULL alter table em_user_role drop constraint fk_em_user_role_role_id;
drop index ix_em_user_role_role_id;

IF OBJECT_ID('fk_vehicle_truck_ref_id', 'F') IS NOT NULL alter table vehicle drop constraint fk_vehicle_truck_ref_id;
drop index ix_vehicle_truck_ref_id;

IF OBJECT_ID('fk_vehicle_car_ref_id', 'F') IS NOT NULL alter table vehicle drop constraint fk_vehicle_car_ref_id;
drop index ix_vehicle_car_ref_id;

IF OBJECT_ID('fk_vehicle_driver_vehicle_id', 'F') IS NOT NULL alter table vehicle_driver drop constraint fk_vehicle_driver_vehicle_id;
drop index ix_vehicle_driver_vehicle_id;

IF OBJECT_ID('fk_vehicle_driver_address_id', 'F') IS NOT NULL alter table vehicle_driver drop constraint fk_vehicle_driver_address_id;
drop index ix_vehicle_driver_address_id;

IF OBJECT_ID('fk_warehouses_officezoneid', 'F') IS NOT NULL alter table warehouses drop constraint fk_warehouses_officezoneid;
drop index ix_warehouses_officezoneid;

IF OBJECT_ID('fk_warehousesshippingzones_wa_1', 'F') IS NOT NULL alter table warehousesshippingzones drop constraint fk_warehousesshippingzones_wa_1;
drop index ix_warehousesshippingzones_wa_1;

IF OBJECT_ID('fk_warehousesshippingzones_zo_2', 'F') IS NOT NULL alter table warehousesshippingzones drop constraint fk_warehousesshippingzones_zo_2;
drop index ix_warehousesshippingzones_sh_2;

IF OBJECT_ID('fk_sa_wheel_tire', 'F') IS NOT NULL alter table sa_wheel drop constraint fk_sa_wheel_tire;
drop index ix_sa_wheel_tire;

IF OBJECT_ID('fk_sa_wheel_car', 'F') IS NOT NULL alter table sa_wheel drop constraint fk_sa_wheel_car;
drop index ix_sa_wheel_car;

IF OBJECT_ID('fk_with_zero_parent_id', 'F') IS NOT NULL alter table with_zero drop constraint fk_with_zero_parent_id;
drop index ix_with_zero_parent_id;

IF OBJECT_ID('asimple_bean', 'U') IS NOT NULL drop table asimple_bean;

IF OBJECT_ID('bar', 'U') IS NOT NULL drop table bar;

IF OBJECT_ID('oto_account', 'U') IS NOT NULL drop table oto_account;

IF OBJECT_ID('o_address', 'U') IS NOT NULL drop table o_address;

IF OBJECT_ID('address', 'U') IS NOT NULL drop table address;

IF OBJECT_ID('animals', 'U') IS NOT NULL drop table animals;

IF OBJECT_ID('animal_shelter', 'U') IS NOT NULL drop table animal_shelter;

IF OBJECT_ID('article', 'U') IS NOT NULL drop table article;

IF OBJECT_ID('attribute', 'U') IS NOT NULL drop table attribute;

IF OBJECT_ID('attribute_holder', 'U') IS NOT NULL drop table attribute_holder;

IF OBJECT_ID('audit_log', 'U') IS NOT NULL drop table audit_log;

IF OBJECT_ID('bbookmark', 'U') IS NOT NULL drop table bbookmark;

IF OBJECT_ID('bbookmark_user', 'U') IS NOT NULL drop table bbookmark_user;

IF OBJECT_ID('bsimple_with_gen', 'U') IS NOT NULL drop table bsimple_with_gen;

IF OBJECT_ID('bwith_qident', 'U') IS NOT NULL drop table bwith_qident;

IF OBJECT_ID('basic_joda_entity', 'U') IS NOT NULL drop table basic_joda_entity;

IF OBJECT_ID('bean_with_time_zone', 'U') IS NOT NULL drop table bean_with_time_zone;

IF OBJECT_ID('drel_booking', 'U') IS NOT NULL drop table drel_booking;

IF OBJECT_ID('ckey_assoc', 'U') IS NOT NULL drop table ckey_assoc;

IF OBJECT_ID('ckey_detail', 'U') IS NOT NULL drop table ckey_detail;

IF OBJECT_ID('ckey_parent', 'U') IS NOT NULL drop table ckey_parent;

IF OBJECT_ID('calculation_result', 'U') IS NOT NULL drop table calculation_result;

IF OBJECT_ID('cao_bean', 'U') IS NOT NULL drop table cao_bean;

IF OBJECT_ID('sa_car', 'U') IS NOT NULL drop table sa_car;

IF OBJECT_ID('sp_car_car', 'U') IS NOT NULL drop table sp_car_car;

IF OBJECT_ID('sp_car_car_wheels', 'U') IS NOT NULL drop table sp_car_car_wheels;

IF OBJECT_ID('car_accessory', 'U') IS NOT NULL drop table car_accessory;

IF OBJECT_ID('configuration', 'U') IS NOT NULL drop table configuration;

IF OBJECT_ID('configurations', 'U') IS NOT NULL drop table configurations;

IF OBJECT_ID('contact', 'U') IS NOT NULL drop table contact;

IF OBJECT_ID('contact_group', 'U') IS NOT NULL drop table contact_group;

IF OBJECT_ID('contact_note', 'U') IS NOT NULL drop table contact_note;

IF OBJECT_ID('c_conversation', 'U') IS NOT NULL drop table c_conversation;

IF OBJECT_ID('o_country', 'U') IS NOT NULL drop table o_country;

IF OBJECT_ID('o_customer', 'U') IS NOT NULL drop table o_customer;

IF OBJECT_ID('dexh_entity', 'U') IS NOT NULL drop table dexh_entity;

IF OBJECT_ID('dperson', 'U') IS NOT NULL drop table dperson;

IF OBJECT_ID('rawinherit_data', 'U') IS NOT NULL drop table rawinherit_data;

IF OBJECT_ID('e_basic', 'U') IS NOT NULL drop table e_basic;

IF OBJECT_ID('ebasic_clob', 'U') IS NOT NULL drop table ebasic_clob;

IF OBJECT_ID('ebasic_clob_fetch_eager', 'U') IS NOT NULL drop table ebasic_clob_fetch_eager;

IF OBJECT_ID('ebasic_clob_no_ver', 'U') IS NOT NULL drop table ebasic_clob_no_ver;

IF OBJECT_ID('e_basicenc', 'U') IS NOT NULL drop table e_basicenc;

IF OBJECT_ID('e_basicenc_bin', 'U') IS NOT NULL drop table e_basicenc_bin;

IF OBJECT_ID('e_basic_enum_id', 'U') IS NOT NULL drop table e_basic_enum_id;

IF OBJECT_ID('ebasic_json_map', 'U') IS NOT NULL drop table ebasic_json_map;

IF OBJECT_ID('ebasic_json_map_blob', 'U') IS NOT NULL drop table ebasic_json_map_blob;

IF OBJECT_ID('ebasic_json_map_clob', 'U') IS NOT NULL drop table ebasic_json_map_clob;

IF OBJECT_ID('ebasic_json_map_json_b', 'U') IS NOT NULL drop table ebasic_json_map_json_b;

IF OBJECT_ID('ebasic_json_map_varchar', 'U') IS NOT NULL drop table ebasic_json_map_varchar;

IF OBJECT_ID('ebasic_json_node', 'U') IS NOT NULL drop table ebasic_json_node;

IF OBJECT_ID('ebasic_json_node_blob', 'U') IS NOT NULL drop table ebasic_json_node_blob;

IF OBJECT_ID('ebasic_json_node_json_b', 'U') IS NOT NULL drop table ebasic_json_node_json_b;

IF OBJECT_ID('ebasic_json_node_varchar', 'U') IS NOT NULL drop table ebasic_json_node_varchar;

IF OBJECT_ID('e_basic_ndc', 'U') IS NOT NULL drop table e_basic_ndc;

IF OBJECT_ID('e_basicver', 'U') IS NOT NULL drop table e_basicver;

IF OBJECT_ID('e_basic_withlife', 'U') IS NOT NULL drop table e_basic_withlife;

IF OBJECT_ID('e_basicverucon', 'U') IS NOT NULL drop table e_basicverucon;

IF OBJECT_ID('eemb_inner', 'U') IS NOT NULL drop table eemb_inner;

IF OBJECT_ID('eemb_outer', 'U') IS NOT NULL drop table eemb_outer;

IF OBJECT_ID('egen_props', 'U') IS NOT NULL drop table egen_props;

IF OBJECT_ID('einvoice', 'U') IS NOT NULL drop table einvoice;

IF OBJECT_ID('e_main', 'U') IS NOT NULL drop table e_main;

IF OBJECT_ID('enull_collection', 'U') IS NOT NULL drop table enull_collection;

IF OBJECT_ID('enull_collection_detail', 'U') IS NOT NULL drop table enull_collection_detail;

IF OBJECT_ID('eopt_one_a', 'U') IS NOT NULL drop table eopt_one_a;

IF OBJECT_ID('eopt_one_b', 'U') IS NOT NULL drop table eopt_one_b;

IF OBJECT_ID('eopt_one_c', 'U') IS NOT NULL drop table eopt_one_c;

IF OBJECT_ID('eperson', 'U') IS NOT NULL drop table eperson;

IF OBJECT_ID('esimple', 'U') IS NOT NULL drop table esimple;

IF OBJECT_ID('esome_type', 'U') IS NOT NULL drop table esome_type;

IF OBJECT_ID('etrans_many', 'U') IS NOT NULL drop table etrans_many;

IF OBJECT_ID('evanilla_collection', 'U') IS NOT NULL drop table evanilla_collection;

IF OBJECT_ID('evanilla_collection_detail', 'U') IS NOT NULL drop table evanilla_collection_detail;

IF OBJECT_ID('ewho_props', 'U') IS NOT NULL drop table ewho_props;

IF OBJECT_ID('e_withinet', 'U') IS NOT NULL drop table e_withinet;

IF OBJECT_ID('td_child', 'U') IS NOT NULL drop table td_child;

IF OBJECT_ID('td_parent', 'U') IS NOT NULL drop table td_parent;

IF OBJECT_ID('feature_desc', 'U') IS NOT NULL drop table feature_desc;

IF OBJECT_ID('f_first', 'U') IS NOT NULL drop table f_first;

IF OBJECT_ID('foo', 'U') IS NOT NULL drop table foo;

IF OBJECT_ID('gen_key_identity', 'U') IS NOT NULL drop table gen_key_identity;

IF OBJECT_ID('gen_key_sequence', 'U') IS NOT NULL drop table gen_key_sequence;

IF OBJECT_ID('gen_key_table', 'U') IS NOT NULL drop table gen_key_table;

IF OBJECT_ID('c_group', 'U') IS NOT NULL drop table c_group;

IF OBJECT_ID('imrelated', 'U') IS NOT NULL drop table imrelated;

IF OBJECT_ID('imroot', 'U') IS NOT NULL drop table imroot;

IF OBJECT_ID('ixresource', 'U') IS NOT NULL drop table ixresource;

IF OBJECT_ID('info_company', 'U') IS NOT NULL drop table info_company;

IF OBJECT_ID('info_contact', 'U') IS NOT NULL drop table info_contact;

IF OBJECT_ID('info_customer', 'U') IS NOT NULL drop table info_customer;

IF OBJECT_ID('inner_report', 'U') IS NOT NULL drop table inner_report;

IF OBJECT_ID('drel_invoice', 'U') IS NOT NULL drop table drel_invoice;

IF OBJECT_ID('item', 'U') IS NOT NULL drop table item;

IF OBJECT_ID('level1', 'U') IS NOT NULL drop table level1;

IF OBJECT_ID('level1_level4', 'U') IS NOT NULL drop table level1_level4;

IF OBJECT_ID('level1_level2', 'U') IS NOT NULL drop table level1_level2;

IF OBJECT_ID('level2', 'U') IS NOT NULL drop table level2;

IF OBJECT_ID('level2_level3', 'U') IS NOT NULL drop table level2_level3;

IF OBJECT_ID('level3', 'U') IS NOT NULL drop table level3;

IF OBJECT_ID('level4', 'U') IS NOT NULL drop table level4;

IF OBJECT_ID('la_attr_value', 'U') IS NOT NULL drop table la_attr_value;

IF OBJECT_ID('la_attr_value_attribute', 'U') IS NOT NULL drop table la_attr_value_attribute;

IF OBJECT_ID('mmedia', 'U') IS NOT NULL drop table mmedia;

IF OBJECT_ID('non_updateprop', 'U') IS NOT NULL drop table non_updateprop;

IF OBJECT_ID('mprinter', 'U') IS NOT NULL drop table mprinter;

IF OBJECT_ID('mprinter_state', 'U') IS NOT NULL drop table mprinter_state;

IF OBJECT_ID('mprofile', 'U') IS NOT NULL drop table mprofile;

IF OBJECT_ID('mprotected_construct_bean', 'U') IS NOT NULL drop table mprotected_construct_bean;

IF OBJECT_ID('mrole', 'U') IS NOT NULL drop table mrole;

IF OBJECT_ID('mrole_muser', 'U') IS NOT NULL drop table mrole_muser;

IF OBJECT_ID('msome_other', 'U') IS NOT NULL drop table msome_other;

IF OBJECT_ID('muser', 'U') IS NOT NULL drop table muser;

IF OBJECT_ID('muser_type', 'U') IS NOT NULL drop table muser_type;

IF OBJECT_ID('map_super_actual', 'U') IS NOT NULL drop table map_super_actual;

IF OBJECT_ID('c_message', 'U') IS NOT NULL drop table c_message;

IF OBJECT_ID('mnoc_role', 'U') IS NOT NULL drop table mnoc_role;

IF OBJECT_ID('mnoc_user', 'U') IS NOT NULL drop table mnoc_user;

IF OBJECT_ID('mnoc_user_mnoc_role', 'U') IS NOT NULL drop table mnoc_user_mnoc_role;

IF OBJECT_ID('mp_role', 'U') IS NOT NULL drop table mp_role;

IF OBJECT_ID('mp_user', 'U') IS NOT NULL drop table mp_user;

IF OBJECT_ID('my_lob_size', 'U') IS NOT NULL drop table my_lob_size;

IF OBJECT_ID('my_lob_size_join_many', 'U') IS NOT NULL drop table my_lob_size_join_many;

IF OBJECT_ID('noidbean', 'U') IS NOT NULL drop table noidbean;

IF OBJECT_ID('o_cached_bean', 'U') IS NOT NULL drop table o_cached_bean;

IF OBJECT_ID('o_cached_bean_country', 'U') IS NOT NULL drop table o_cached_bean_country;

IF OBJECT_ID('o_cached_bean_child', 'U') IS NOT NULL drop table o_cached_bean_child;

IF OBJECT_ID('ocar', 'U') IS NOT NULL drop table ocar;

IF OBJECT_ID('oengine', 'U') IS NOT NULL drop table oengine;

IF OBJECT_ID('ogear_box', 'U') IS NOT NULL drop table ogear_box;

IF OBJECT_ID('o_order', 'U') IS NOT NULL drop table o_order;

IF OBJECT_ID('o_order_detail', 'U') IS NOT NULL drop table o_order_detail;

IF OBJECT_ID('s_orders', 'U') IS NOT NULL drop table s_orders;

IF OBJECT_ID('s_order_items', 'U') IS NOT NULL drop table s_order_items;

IF OBJECT_ID('or_order_ship', 'U') IS NOT NULL drop table or_order_ship;

IF OBJECT_ID('oto_child', 'U') IS NOT NULL drop table oto_child;

IF OBJECT_ID('oto_master', 'U') IS NOT NULL drop table oto_master;

IF OBJECT_ID('pfile', 'U') IS NOT NULL drop table pfile;

IF OBJECT_ID('pfile_content', 'U') IS NOT NULL drop table pfile_content;

IF OBJECT_ID('paggview', 'U') IS NOT NULL drop table paggview;

IF OBJECT_ID('pallet_location', 'U') IS NOT NULL drop table pallet_location;

IF OBJECT_ID('parcel', 'U') IS NOT NULL drop table parcel;

IF OBJECT_ID('parcel_location', 'U') IS NOT NULL drop table parcel_location;

IF OBJECT_ID('rawinherit_parent', 'U') IS NOT NULL drop table rawinherit_parent;

IF OBJECT_ID('rawinherit_parent_rawinherit_dat', 'U') IS NOT NULL drop table rawinherit_parent_rawinherit_dat;

IF OBJECT_ID('c_participation', 'U') IS NOT NULL drop table c_participation;

IF OBJECT_ID('mt_permission', 'U') IS NOT NULL drop table mt_permission;

IF OBJECT_ID('persistent_file', 'U') IS NOT NULL drop table persistent_file;

IF OBJECT_ID('persistent_file_content', 'U') IS NOT NULL drop table persistent_file_content;

IF OBJECT_ID('persons', 'U') IS NOT NULL drop table persons;

IF OBJECT_ID('person', 'U') IS NOT NULL drop table person;

IF OBJECT_ID('phones', 'U') IS NOT NULL drop table phones;

IF OBJECT_ID('o_product', 'U') IS NOT NULL drop table o_product;

IF OBJECT_ID('pp', 'U') IS NOT NULL drop table pp;

IF OBJECT_ID('pp_to_ww', 'U') IS NOT NULL drop table pp_to_ww;

IF OBJECT_ID('rcustomer', 'U') IS NOT NULL drop table rcustomer;

IF OBJECT_ID('r_orders', 'U') IS NOT NULL drop table r_orders;

IF OBJECT_ID('region', 'U') IS NOT NULL drop table region;

IF OBJECT_ID('resourcefile', 'U') IS NOT NULL drop table resourcefile;

IF OBJECT_ID('mt_role', 'U') IS NOT NULL drop table mt_role;

IF OBJECT_ID('mt_role_permission', 'U') IS NOT NULL drop table mt_role_permission;

IF OBJECT_ID('em_role', 'U') IS NOT NULL drop table em_role;

IF OBJECT_ID('f_second', 'U') IS NOT NULL drop table f_second;

IF OBJECT_ID('section', 'U') IS NOT NULL drop table section;

IF OBJECT_ID('self_parent', 'U') IS NOT NULL drop table self_parent;

IF OBJECT_ID('self_ref_customer', 'U') IS NOT NULL drop table self_ref_customer;

IF OBJECT_ID('self_ref_example', 'U') IS NOT NULL drop table self_ref_example;

IF OBJECT_ID('some_enum_bean', 'U') IS NOT NULL drop table some_enum_bean;

IF OBJECT_ID('some_file_bean', 'U') IS NOT NULL drop table some_file_bean;

IF OBJECT_ID('some_new_types_bean', 'U') IS NOT NULL drop table some_new_types_bean;

IF OBJECT_ID('some_period_bean', 'U') IS NOT NULL drop table some_period_bean;

IF OBJECT_ID('stockforecast', 'U') IS NOT NULL drop table stockforecast;

IF OBJECT_ID('sub_section', 'U') IS NOT NULL drop table sub_section;

IF OBJECT_ID('sub_type', 'U') IS NOT NULL drop table sub_type;

IF OBJECT_ID('tbytes_only', 'U') IS NOT NULL drop table tbytes_only;

IF OBJECT_ID('tcar', 'U') IS NOT NULL drop table tcar;

IF OBJECT_ID('tint_root', 'U') IS NOT NULL drop table tint_root;

IF OBJECT_ID('tjoda_entity', 'U') IS NOT NULL drop table tjoda_entity;

IF OBJECT_ID('t_mapsuper1', 'U') IS NOT NULL drop table t_mapsuper1;

IF OBJECT_ID('t_oneb', 'U') IS NOT NULL drop table t_oneb;

IF OBJECT_ID('t_detail_with_other_namexxxyy', 'U') IS NOT NULL drop table t_detail_with_other_namexxxyy;

IF OBJECT_ID('ts_detail_two', 'U') IS NOT NULL drop table ts_detail_two;

IF OBJECT_ID('t_atable_thatisrelatively', 'U') IS NOT NULL drop table t_atable_thatisrelatively;

IF OBJECT_ID('ts_master_two', 'U') IS NOT NULL drop table ts_master_two;

IF OBJECT_ID('tuuid_entity', 'U') IS NOT NULL drop table tuuid_entity;

IF OBJECT_ID('twheel', 'U') IS NOT NULL drop table twheel;

IF OBJECT_ID('twith_pre_insert', 'U') IS NOT NULL drop table twith_pre_insert;

IF OBJECT_ID('mt_tenant', 'U') IS NOT NULL drop table mt_tenant;

IF OBJECT_ID('sa_tire', 'U') IS NOT NULL drop table sa_tire;

IF OBJECT_ID('tire', 'U') IS NOT NULL drop table tire;

IF OBJECT_ID('trip', 'U') IS NOT NULL drop table trip;

IF OBJECT_ID('truck_ref', 'U') IS NOT NULL drop table truck_ref;

IF OBJECT_ID('type', 'U') IS NOT NULL drop table type;

IF OBJECT_ID('ut_detail', 'U') IS NOT NULL drop table ut_detail;

IF OBJECT_ID('ut_master', 'U') IS NOT NULL drop table ut_master;

IF OBJECT_ID('uuone', 'U') IS NOT NULL drop table uuone;

IF OBJECT_ID('uutwo', 'U') IS NOT NULL drop table uutwo;

IF OBJECT_ID('em_user', 'U') IS NOT NULL drop table em_user;

IF OBJECT_ID('tx_user', 'U') IS NOT NULL drop table tx_user;

IF OBJECT_ID('c_user', 'U') IS NOT NULL drop table c_user;

IF OBJECT_ID('oto_user', 'U') IS NOT NULL drop table oto_user;

IF OBJECT_ID('em_user_role', 'U') IS NOT NULL drop table em_user_role;

IF OBJECT_ID('vehicle', 'U') IS NOT NULL drop table vehicle;

IF OBJECT_ID('vehicle_driver', 'U') IS NOT NULL drop table vehicle_driver;

IF OBJECT_ID('warehouses', 'U') IS NOT NULL drop table warehouses;

IF OBJECT_ID('warehousesshippingzones', 'U') IS NOT NULL drop table warehousesshippingzones;

IF OBJECT_ID('sa_wheel', 'U') IS NOT NULL drop table sa_wheel;

IF OBJECT_ID('sp_car_wheel', 'U') IS NOT NULL drop table sp_car_wheel;

IF OBJECT_ID('wheel', 'U') IS NOT NULL drop table wheel;

IF OBJECT_ID('with_zero', 'U') IS NOT NULL drop table with_zero;

IF OBJECT_ID('parent', 'U') IS NOT NULL drop table parent;

IF OBJECT_ID('wview', 'U') IS NOT NULL drop table wview;

IF OBJECT_ID('zones', 'U') IS NOT NULL drop table zones;

