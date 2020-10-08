drop index ix_bar_foo_id;
alter table bar drop constraint fk_bar_foo_id;

drop index ix_o_address_country_code;
alter table o_address drop constraint fk_o_address_country_code;

drop index ix_animals_shelter_id;
alter table animals drop constraint fk_animals_shelter_id;

drop index ix_attribute_attribute_holder_id;
alter table attribute drop constraint fk_attribute_attribute_holder_id;

drop index ix_bbookmark_user_id;
alter table bbookmark drop constraint fk_bbookmark_user_id;

drop index ix_drel_booking_agent_invoice;
alter table drel_booking drop constraint fk_drel_booking_agent_invoice;

drop index ix_drel_booking_client_invoice;
alter table drel_booking drop constraint fk_drel_booking_client_invoice;

drop index ix_ckey_detail_one_key_two_key;
alter table ckey_detail drop constraint fk_ckey_detail_ckey_parent;

drop index ix_ckey_parent_assoc_id;
alter table ckey_parent drop constraint fk_ckey_parent_assoc_id;

drop index ix_calculation_result_product_configuration_id;
alter table calculation_result drop constraint fk_calculation_result_product_configuration_id;

drop index ix_calculation_result_group_configuration_id;
alter table calculation_result drop constraint fk_calculation_result_group_configuration_id;

drop index ix_sp_car_car_wheels_car;
alter table sp_car_car_wheels drop constraint fk_sp_car_car_wheels_sp_car_car;

drop index ix_sp_car_car_wheels_wheel;
alter table sp_car_car_wheels drop constraint fk_sp_car_car_wheels_sp_car_wheel;

drop index ix_car_accessory_car_id;
alter table car_accessory drop constraint fk_car_accessory_car_id;

drop index ix_configuration_configurations_id;
alter table configuration drop constraint fk_configuration_configurations_id;

drop index ix_contact_customer_id;
alter table contact drop constraint fk_contact_customer_id;

drop index ix_contact_group_id;
alter table contact drop constraint fk_contact_group_id;

drop index ix_contact_note_contact_id;
alter table contact_note drop constraint fk_contact_note_contact_id;

drop index ix_c_conversation_group_id;
alter table c_conversation drop constraint fk_c_conversation_group_id;

drop index ix_o_customer_billing_address_id;
alter table o_customer drop constraint fk_o_customer_billing_address_id;

drop index ix_o_customer_shipping_address_id;
alter table o_customer drop constraint fk_o_customer_shipping_address_id;

drop index ix_eemb_inner_outer_id;
alter table eemb_inner drop constraint fk_eemb_inner_outer_id;

drop index ix_einvoice_person_id;
alter table einvoice drop constraint fk_einvoice_person_id;

drop index ix_enull_collection_detail_enull_collection_id;
alter table enull_collection_detail drop constraint fk_enull_collection_detail_enull_collection_id;

drop index ix_eopt_one_a_b_id;
alter table eopt_one_a drop constraint fk_eopt_one_a_b_id;

drop index ix_eopt_one_b_c_id;
alter table eopt_one_b drop constraint fk_eopt_one_b_c_id;

drop index ix_evanilla_collection_detail_evanilla_collection_id;
alter table evanilla_collection_detail drop constraint fk_evanilla_collection_detail_evanilla_collection_id;

drop index ix_td_child_parent_id;
alter table td_child drop constraint fk_td_child_parent_id;

drop index ix_imrelated_owner_id;
alter table imrelated drop constraint fk_imrelated_owner_id;

drop index ix_info_contact_company_id;
alter table info_contact drop constraint fk_info_contact_company_id;

drop index ix_info_customer_company_id;
alter table info_customer drop constraint fk_info_customer_company_id;

drop index ix_inner_report_forecast_id;
alter table inner_report drop constraint fk_inner_report_forecast_id;

drop index ix_drel_invoice_booking;
alter table drel_invoice drop constraint fk_drel_invoice_booking;

drop index ix_item_customer_type;
alter table item drop constraint fk_item_type;

drop index ix_item_customer_region;
alter table item drop constraint fk_item_region;

drop index ix_level1_level4_level1_id;
alter table level1_level4 drop constraint fk_level1_level4_level1;

drop index ix_level1_level4_level4_id;
alter table level1_level4 drop constraint fk_level1_level4_level4;

drop index ix_level1_level2_level1_id;
alter table level1_level2 drop constraint fk_level1_level2_level1;

drop index ix_level1_level2_level2_id;
alter table level1_level2 drop constraint fk_level1_level2_level2;

drop index ix_level2_level3_level2_id;
alter table level2_level3 drop constraint fk_level2_level3_level2;

drop index ix_level2_level3_level3_id;
alter table level2_level3 drop constraint fk_level2_level3_level3;

drop index ix_la_attr_value_attribute_la_attr_value_id;
alter table la_attr_value_attribute drop constraint fk_la_attr_value_attribute_la_attr_value;

drop index ix_la_attr_value_attribute_attribute_id;
alter table la_attr_value_attribute drop constraint fk_la_attr_value_attribute_attribute;

drop index ix_mprinter_current_state_id;
alter table mprinter drop constraint fk_mprinter_current_state_id;

drop index ix_mprinter_last_swap_cyan_id;
alter table mprinter drop constraint fk_mprinter_last_swap_cyan_id;

drop index ix_mprinter_last_swap_magenta_id;
alter table mprinter drop constraint fk_mprinter_last_swap_magenta_id;

drop index ix_mprinter_last_swap_yellow_id;
alter table mprinter drop constraint fk_mprinter_last_swap_yellow_id;

drop index ix_mprinter_last_swap_black_id;
alter table mprinter drop constraint fk_mprinter_last_swap_black_id;

drop index ix_mprinter_state_printer_id;
alter table mprinter_state drop constraint fk_mprinter_state_printer_id;

drop index ix_mprofile_picture_id;
alter table mprofile drop constraint fk_mprofile_picture_id;

drop index ix_mrole_muser_mrole_roleid;
alter table mrole_muser drop constraint fk_mrole_muser_mrole;

drop index ix_mrole_muser_muser_userid;
alter table mrole_muser drop constraint fk_mrole_muser_muser;

drop index ix_muser_user_type_id;
alter table muser drop constraint fk_muser_user_type_id;

drop index ix_c_message_conversation_id;
alter table c_message drop constraint fk_c_message_conversation_id;

drop index ix_c_message_user_id;
alter table c_message drop constraint fk_c_message_user_id;

drop index ix_mnoc_user_mnoc_role_mnoc_user_user_id;
alter table mnoc_user_mnoc_role drop constraint fk_mnoc_user_mnoc_role_mnoc_user;

drop index ix_mnoc_user_mnoc_role_mnoc_role_role_id;
alter table mnoc_user_mnoc_role drop constraint fk_mnoc_user_mnoc_role_mnoc_role;

drop index ix_mp_role_mp_user_id;
alter table mp_role drop constraint fk_mp_role_mp_user_id;

drop index ix_my_lob_size_join_many_parent_id;
alter table my_lob_size_join_many drop constraint fk_my_lob_size_join_many_parent_id;

drop index ix_o_cached_bean_country_o_cached_bean_id;
alter table o_cached_bean_country drop constraint fk_o_cached_bean_country_o_cached_bean;

drop index ix_o_cached_bean_country_o_country_code;
alter table o_cached_bean_country drop constraint fk_o_cached_bean_country_o_country;

drop index ix_o_cached_bean_child_cached_bean_id;
alter table o_cached_bean_child drop constraint fk_o_cached_bean_child_cached_bean_id;

drop index ix_oengine_car_id;
alter table oengine drop constraint fk_oengine_car_id;

drop index ix_ogear_box_car_id;
alter table ogear_box drop constraint fk_ogear_box_car_id;

drop index ix_o_order_kcustomer_id;
alter table o_order drop constraint fk_o_order_kcustomer_id;

drop index ix_o_order_detail_order_id;
alter table o_order_detail drop constraint fk_o_order_detail_order_id;

drop index ix_o_order_detail_product_id;
alter table o_order_detail drop constraint fk_o_order_detail_product_id;

drop index ix_s_order_items_order_uuid;
alter table s_order_items drop constraint fk_s_order_items_order_uuid;

drop index ix_or_order_ship_order_id;
alter table or_order_ship drop constraint fk_or_order_ship_order_id;

drop index ix_oto_child_master_id;
alter table oto_child drop constraint fk_oto_child_master_id;

drop index ix_pfile_file_content_id;
alter table pfile drop constraint fk_pfile_file_content_id;

drop index ix_pfile_file_content2_id;
alter table pfile drop constraint fk_pfile_file_content2_id;

drop index ix_paggview_pview_id;
alter table paggview drop constraint fk_paggview_pview_id;

drop index ix_pallet_location_zone_sid;
alter table pallet_location drop constraint fk_pallet_location_zone_sid;

drop index ix_parcel_location_parcelid;
alter table parcel_location drop constraint fk_parcel_location_parcelid;

drop index ix_rawinherit_parent_rawinherit_dat_rawinherit_parent_id;
alter table rawinherit_parent_rawinherit_dat drop constraint fk_rawinherit_parent_rawinherit_dat_rawinherit_parent;

drop index ix_rawinherit_parent_rawinherit_dat_rawinherit_data_id;
alter table rawinherit_parent_rawinherit_dat drop constraint fk_rawinherit_parent_rawinherit_dat_rawinherit_data;

drop index ix_c_participation_conversation_id;
alter table c_participation drop constraint fk_c_participation_conversation_id;

drop index ix_c_participation_user_id;
alter table c_participation drop constraint fk_c_participation_user_id;

drop index ix_persistent_file_content_persistent_file_id;
alter table persistent_file_content drop constraint fk_persistent_file_content_persistent_file_id;

drop index ix_person_default_address_oid;
alter table person drop constraint fk_person_default_address_oid;

drop index ix_phones_person_id;
alter table PHONES drop constraint fk_phones_person_id;

drop index ix_pp_to_ww_pp_id;
alter table pp_to_ww drop constraint fk_pp_to_ww_pp;

drop index ix_pp_to_ww_ww_id;
alter table pp_to_ww drop constraint fk_pp_to_ww_wview;

drop index ix_r_orders_company_customerName;
alter table r_orders drop constraint fk_r_orders_rcustomer;

drop index ix_resourcefile_parentresourcefileid;
alter table ResourceFile drop constraint fk_resourcefile_parentresourcefileid;

drop index ix_mt_role_tenant_id;
alter table mt_role drop constraint fk_mt_role_tenant_id;

drop index ix_mt_role_permission_mt_role_id;
alter table mt_role_permission drop constraint fk_mt_role_permission_mt_role;

drop index ix_mt_role_permission_mt_permission_id;
alter table mt_role_permission drop constraint fk_mt_role_permission_mt_permission;

drop index ix_f_second_first;
alter table f_second drop constraint fk_f_second_first;

drop index ix_section_article_id;
alter table section drop constraint fk_section_article_id;

drop index ix_self_parent_parent_id;
alter table self_parent drop constraint fk_self_parent_parent_id;

drop index ix_self_ref_customer_referred_by_id;
alter table self_ref_customer drop constraint fk_self_ref_customer_referred_by_id;

drop index ix_self_ref_example_parent_id;
alter table self_ref_example drop constraint fk_self_ref_example_parent_id;

drop index ix_stockforecast_inner_report_id;
alter table stockforecast drop constraint fk_stockforecast_inner_report_id;

drop index ix_sub_section_section_id;
alter table sub_section drop constraint fk_sub_section_section_id;

drop index ix_t_detail_with_other_namexxxyy_master_id;
alter table t_detail_with_other_namexxxyy drop constraint fk_t_detail_with_other_namexxxyy_master_id;

drop index ix_ts_detail_two_master_id;
alter table ts_detail_two drop constraint fk_ts_detail_two_master_id;

drop index ix_twheel_owner_plate_no;
alter table twheel drop constraint fk_twheel_owner_plate_no;

drop index ix_tire_wheel;
alter table tire drop constraint fk_tire_wheel;

drop index ix_trip_vehicle_driver_id;
alter table trip drop constraint fk_trip_vehicle_driver_id;

drop index ix_trip_address_id;
alter table trip drop constraint fk_trip_address_id;

drop index ix_type_sub_type_id;
alter table type drop constraint fk_type_sub_type_id;

drop index ix_ut_detail_utmaster_id;
alter table ut_detail drop constraint fk_ut_detail_utmaster_id;

drop index ix_uutwo_master_id;
alter table uutwo drop constraint fk_uutwo_master_id;

drop index ix_c_user_group_id;
alter table c_user drop constraint fk_c_user_group_id;

drop index ix_oto_user_account_id;
alter table oto_user drop constraint fk_oto_user_account_id;

drop index ix_em_user_role_user_id;
alter table em_user_role drop constraint fk_em_user_role_user_id;

drop index ix_em_user_role_role_id;
alter table em_user_role drop constraint fk_em_user_role_role_id;

drop index ix_vehicle_truck_ref_id;
alter table vehicle drop constraint fk_vehicle_truck_ref_id;

drop index ix_vehicle_car_ref_id;
alter table vehicle drop constraint fk_vehicle_car_ref_id;

drop index ix_vehicle_driver_vehicle_id;
alter table vehicle_driver drop constraint fk_vehicle_driver_vehicle_id;

drop index ix_vehicle_driver_address_id;
alter table vehicle_driver drop constraint fk_vehicle_driver_address_id;

drop index ix_warehouses_officezoneid;
alter table warehouses drop constraint fk_warehouses_officezoneid;

drop index ix_warehousesshippingzones_warehouseid;
alter table WarehousesShippingZones drop constraint fk_warehousesshippingzones_warehouses;

drop index ix_warehousesshippingzones_shippingzoneid;
alter table WarehousesShippingZones drop constraint fk_warehousesshippingzones_zones;

drop index ix_sa_wheel_tire;
alter table sa_wheel drop constraint fk_sa_wheel_tire;

drop index ix_sa_wheel_car;
alter table sa_wheel drop constraint fk_sa_wheel_car;

drop index ix_with_zero_parent_id;
alter table with_zero drop constraint fk_with_zero_parent_id;

