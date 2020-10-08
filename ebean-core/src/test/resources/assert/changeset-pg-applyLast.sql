alter table bar add constraint fk_bar_foo_id foreign key (foo_id) references foo (foo_id) on delete restrict on update restrict;
create index ix_bar_foo_id on bar (foo_id);

alter table o_address add constraint fk_o_address_country_code foreign key (country_code) references o_country (code) on delete restrict on update restrict;
create index ix_o_address_country_code on o_address (country_code);

alter table animals add constraint fk_animals_shelter_id foreign key (shelter_id) references animal_shelter (id) on delete restrict on update restrict;
create index ix_animals_shelter_id on animals (shelter_id);

alter table attribute add constraint fk_attribute_attribute_holder_id foreign key (attribute_holder_id) references attribute_holder (id) on delete restrict on update restrict;
create index ix_attribute_attribute_holder_id on attribute (attribute_holder_id);

alter table bbookmark add constraint fk_bbookmark_user_id foreign key (user_id) references bbookmark_user (id) on delete restrict on update restrict;
create index ix_bbookmark_user_id on bbookmark (user_id);

alter table drel_booking add constraint fk_drel_booking_agent_invoice foreign key (agent_invoice) references drel_invoice (id) on delete restrict on update restrict;
create index ix_drel_booking_agent_invoice on drel_booking (agent_invoice);

alter table drel_booking add constraint fk_drel_booking_client_invoice foreign key (client_invoice) references drel_invoice (id) on delete restrict on update restrict;
create index ix_drel_booking_client_invoice on drel_booking (client_invoice);

alter table ckey_detail add constraint fk_ckey_detail_ckey_parent foreign key (one_key,two_key) references ckey_parent (one_key,two_key) on delete restrict on update restrict;
create index ix_ckey_detail_one_key_two_key on ckey_detail (one_key,two_key);

alter table ckey_parent add constraint fk_ckey_parent_assoc_id foreign key (assoc_id) references ckey_assoc (id) on delete restrict on update restrict;
create index ix_ckey_parent_assoc_id on ckey_parent (assoc_id);

alter table calculation_result add constraint fk_calculation_result_product_configuration_id foreign key (product_configuration_ID) references configuration (ID) on delete restrict on update restrict;
create index ix_calculation_result_product_configuration_id on calculation_result (product_configuration_ID);

alter table calculation_result add constraint fk_calculation_result_group_configuration_id foreign key (group_configuration_ID) references configuration (ID) on delete restrict on update restrict;
create index ix_calculation_result_group_configuration_id on calculation_result (group_configuration_ID);

alter table sp_car_car_wheels add constraint fk_sp_car_car_wheels_sp_car_car foreign key (car) references sp_car_car (id) on delete restrict on update restrict;
create index ix_sp_car_car_wheels_car on sp_car_car_wheels (car);

alter table sp_car_car_wheels add constraint fk_sp_car_car_wheels_sp_car_wheel foreign key (wheel) references sp_car_wheel (id) on delete restrict on update restrict;
create index ix_sp_car_car_wheels_wheel on sp_car_car_wheels (wheel);

alter table car_accessory add constraint fk_car_accessory_car_id foreign key (car_id) references vehicle (id) on delete restrict on update restrict;
create index ix_car_accessory_car_id on car_accessory (car_id);

alter table configuration add constraint fk_configuration_configurations_id foreign key (configurations_ID) references configurations (ID) on delete restrict on update restrict;
create index ix_configuration_configurations_id on configuration (configurations_ID);

alter table contact add constraint fk_contact_customer_id foreign key (customer_id) references o_customer (id) on delete restrict on update restrict;
create index ix_contact_customer_id on contact (customer_id);

alter table contact add constraint fk_contact_group_id foreign key (group_id) references contact_group (id) on delete restrict on update restrict;
create index ix_contact_group_id on contact (group_id);

alter table contact_note add constraint fk_contact_note_contact_id foreign key (contact_id) references contact (id) on delete restrict on update restrict;
create index ix_contact_note_contact_id on contact_note (contact_id);

alter table c_conversation add constraint fk_c_conversation_group_id foreign key (group_id) references c_group (id) on delete restrict on update restrict;
create index ix_c_conversation_group_id on c_conversation (group_id);

alter table o_customer add constraint fk_o_customer_billing_address_id foreign key (billing_address_id) references o_address (id) on delete restrict on update restrict;
create index ix_o_customer_billing_address_id on o_customer (billing_address_id);

alter table o_customer add constraint fk_o_customer_shipping_address_id foreign key (shipping_address_id) references o_address (id) on delete restrict on update restrict;
create index ix_o_customer_shipping_address_id on o_customer (shipping_address_id);

alter table eemb_inner add constraint fk_eemb_inner_outer_id foreign key (outer_id) references eemb_outer (id) on delete restrict on update restrict;
create index ix_eemb_inner_outer_id on eemb_inner (outer_id);

alter table einvoice add constraint fk_einvoice_person_id foreign key (person_id) references eperson (id) on delete restrict on update restrict;
create index ix_einvoice_person_id on einvoice (person_id);

alter table enull_collection_detail add constraint fk_enull_collection_detail_enull_collection_id foreign key (enull_collection_id) references enull_collection (id) on delete restrict on update restrict;
create index ix_enull_collection_detail_enull_collection_id on enull_collection_detail (enull_collection_id);

alter table eopt_one_a add constraint fk_eopt_one_a_b_id foreign key (b_id) references eopt_one_b (id) on delete restrict on update restrict;
create index ix_eopt_one_a_b_id on eopt_one_a (b_id);

alter table eopt_one_b add constraint fk_eopt_one_b_c_id foreign key (c_id) references eopt_one_c (id) on delete restrict on update restrict;
create index ix_eopt_one_b_c_id on eopt_one_b (c_id);

alter table evanilla_collection_detail add constraint fk_evanilla_collection_detail_evanilla_collection_id foreign key (evanilla_collection_id) references evanilla_collection (id) on delete restrict on update restrict;
create index ix_evanilla_collection_detail_evanilla_collection_id on evanilla_collection_detail (evanilla_collection_id);

alter table td_child add constraint fk_td_child_parent_id foreign key (parent_id) references td_parent (parent_id) on delete restrict on update restrict;
create index ix_td_child_parent_id on td_child (parent_id);

alter table imrelated add constraint fk_imrelated_owner_id foreign key (owner_id) references imroot (id) on delete restrict on update restrict;
create index ix_imrelated_owner_id on imrelated (owner_id);

alter table info_contact add constraint fk_info_contact_company_id foreign key (company_id) references info_company (id) on delete restrict on update restrict;
create index ix_info_contact_company_id on info_contact (company_id);

alter table info_customer add constraint fk_info_customer_company_id foreign key (company_id) references info_company (id) on delete restrict on update restrict;
create index ix_info_customer_company_id on info_customer (company_id);

alter table inner_report add constraint fk_inner_report_forecast_id foreign key (forecast_id) references stockforecast (id) on delete restrict on update restrict;
create index ix_inner_report_forecast_id on inner_report (forecast_id);

alter table drel_invoice add constraint fk_drel_invoice_booking foreign key (booking) references drel_booking (id) on delete restrict on update restrict;
create index ix_drel_invoice_booking on drel_invoice (booking);

alter table item add constraint fk_item_type foreign key (customer,type) references type (customer,type) on delete restrict on update restrict;
create index ix_item_customer_type on item (customer,type);

alter table item add constraint fk_item_region foreign key (customer,region) references region (customer,type) on delete restrict on update restrict;
create index ix_item_customer_region on item (customer,region);

alter table level1_level4 add constraint fk_level1_level4_level1 foreign key (level1_id) references level1 (id) on delete restrict on update restrict;
create index ix_level1_level4_level1_id on level1_level4 (level1_id);

alter table level1_level4 add constraint fk_level1_level4_level4 foreign key (level4_id) references level4 (id) on delete restrict on update restrict;
create index ix_level1_level4_level4_id on level1_level4 (level4_id);

alter table level1_level2 add constraint fk_level1_level2_level1 foreign key (level1_id) references level1 (id) on delete restrict on update restrict;
create index ix_level1_level2_level1_id on level1_level2 (level1_id);

alter table level1_level2 add constraint fk_level1_level2_level2 foreign key (level2_id) references level2 (id) on delete restrict on update restrict;
create index ix_level1_level2_level2_id on level1_level2 (level2_id);

alter table level2_level3 add constraint fk_level2_level3_level2 foreign key (level2_id) references level2 (id) on delete restrict on update restrict;
create index ix_level2_level3_level2_id on level2_level3 (level2_id);

alter table level2_level3 add constraint fk_level2_level3_level3 foreign key (level3_id) references level3 (id) on delete restrict on update restrict;
create index ix_level2_level3_level3_id on level2_level3 (level3_id);

alter table la_attr_value_attribute add constraint fk_la_attr_value_attribute_la_attr_value foreign key (la_attr_value_id) references la_attr_value (id) on delete restrict on update restrict;
create index ix_la_attr_value_attribute_la_attr_value_id on la_attr_value_attribute (la_attr_value_id);

alter table la_attr_value_attribute add constraint fk_la_attr_value_attribute_attribute foreign key (attribute_id) references attribute (id) on delete restrict on update restrict;
create index ix_la_attr_value_attribute_attribute_id on la_attr_value_attribute (attribute_id);

alter table mprinter add constraint fk_mprinter_current_state_id foreign key (current_state_id) references mprinter_state (id) on delete restrict on update restrict;
create index ix_mprinter_current_state_id on mprinter (current_state_id);

alter table mprinter add constraint fk_mprinter_last_swap_cyan_id foreign key (last_swap_cyan_id) references mprinter_state (id) on delete restrict on update restrict;
create index ix_mprinter_last_swap_cyan_id on mprinter (last_swap_cyan_id);

alter table mprinter add constraint fk_mprinter_last_swap_magenta_id foreign key (last_swap_magenta_id) references mprinter_state (id) on delete restrict on update restrict;
create index ix_mprinter_last_swap_magenta_id on mprinter (last_swap_magenta_id);

alter table mprinter add constraint fk_mprinter_last_swap_yellow_id foreign key (last_swap_yellow_id) references mprinter_state (id) on delete restrict on update restrict;
create index ix_mprinter_last_swap_yellow_id on mprinter (last_swap_yellow_id);

alter table mprinter add constraint fk_mprinter_last_swap_black_id foreign key (last_swap_black_id) references mprinter_state (id) on delete restrict on update restrict;
create index ix_mprinter_last_swap_black_id on mprinter (last_swap_black_id);

alter table mprinter_state add constraint fk_mprinter_state_printer_id foreign key (printer_id) references mprinter (id) on delete restrict on update restrict;
create index ix_mprinter_state_printer_id on mprinter_state (printer_id);

alter table mprofile add constraint fk_mprofile_picture_id foreign key (picture_id) references mmedia (id) on delete restrict on update restrict;
create index ix_mprofile_picture_id on mprofile (picture_id);

alter table mrole_muser add constraint fk_mrole_muser_mrole foreign key (mrole_roleid) references mrole (roleid) on delete restrict on update restrict;
create index ix_mrole_muser_mrole_roleid on mrole_muser (mrole_roleid);

alter table mrole_muser add constraint fk_mrole_muser_muser foreign key (muser_userid) references muser (userid) on delete restrict on update restrict;
create index ix_mrole_muser_muser_userid on mrole_muser (muser_userid);

alter table muser add constraint fk_muser_user_type_id foreign key (user_type_id) references muser_type (id) on delete restrict on update restrict;
create index ix_muser_user_type_id on muser (user_type_id);

alter table c_message add constraint fk_c_message_conversation_id foreign key (conversation_id) references c_conversation (id) on delete restrict on update restrict;
create index ix_c_message_conversation_id on c_message (conversation_id);

alter table c_message add constraint fk_c_message_user_id foreign key (user_id) references c_user (id) on delete restrict on update restrict;
create index ix_c_message_user_id on c_message (user_id);

alter table mnoc_user_mnoc_role add constraint fk_mnoc_user_mnoc_role_mnoc_user foreign key (mnoc_user_user_id) references mnoc_user (user_id) on delete restrict on update restrict;
create index ix_mnoc_user_mnoc_role_mnoc_user_user_id on mnoc_user_mnoc_role (mnoc_user_user_id);

alter table mnoc_user_mnoc_role add constraint fk_mnoc_user_mnoc_role_mnoc_role foreign key (mnoc_role_role_id) references mnoc_role (role_id) on delete restrict on update restrict;
create index ix_mnoc_user_mnoc_role_mnoc_role_role_id on mnoc_user_mnoc_role (mnoc_role_role_id);

alter table mp_role add constraint fk_mp_role_mp_user_id foreign key (mp_user_id) references mp_user (id) on delete restrict on update restrict;
create index ix_mp_role_mp_user_id on mp_role (mp_user_id);

alter table my_lob_size_join_many add constraint fk_my_lob_size_join_many_parent_id foreign key (parent_id) references my_lob_size (id) on delete restrict on update restrict;
create index ix_my_lob_size_join_many_parent_id on my_lob_size_join_many (parent_id);

alter table o_cached_bean_country add constraint fk_o_cached_bean_country_o_cached_bean foreign key (o_cached_bean_id) references o_cached_bean (id) on delete restrict on update restrict;
create index ix_o_cached_bean_country_o_cached_bean_id on o_cached_bean_country (o_cached_bean_id);

alter table o_cached_bean_country add constraint fk_o_cached_bean_country_o_country foreign key (o_country_code) references o_country (code) on delete restrict on update restrict;
create index ix_o_cached_bean_country_o_country_code on o_cached_bean_country (o_country_code);

alter table o_cached_bean_child add constraint fk_o_cached_bean_child_cached_bean_id foreign key (cached_bean_id) references o_cached_bean (id) on delete restrict on update restrict;
create index ix_o_cached_bean_child_cached_bean_id on o_cached_bean_child (cached_bean_id);

alter table oengine add constraint fk_oengine_car_id foreign key (car_id) references ocar (id) on delete restrict on update restrict;
create index ix_oengine_car_id on oengine (car_id);

alter table ogear_box add constraint fk_ogear_box_car_id foreign key (car_id) references ocar (id) on delete restrict on update restrict;
create index ix_ogear_box_car_id on ogear_box (car_id);

alter table o_order add constraint fk_o_order_kcustomer_id foreign key (kcustomer_id) references o_customer (id) on delete restrict on update restrict;
create index ix_o_order_kcustomer_id on o_order (kcustomer_id);

alter table o_order_detail add constraint fk_o_order_detail_order_id foreign key (order_id) references o_order (id) on delete restrict on update restrict;
create index ix_o_order_detail_order_id on o_order_detail (order_id);

alter table o_order_detail add constraint fk_o_order_detail_product_id foreign key (product_id) references o_product (id) on delete restrict on update restrict;
create index ix_o_order_detail_product_id on o_order_detail (product_id);

alter table s_order_items add constraint fk_s_order_items_order_uuid foreign key (order_uuid) references s_orders (uuid) on delete restrict on update restrict;
create index ix_s_order_items_order_uuid on s_order_items (order_uuid);

alter table or_order_ship add constraint fk_or_order_ship_order_id foreign key (order_id) references o_order (id) on delete restrict on update restrict;
create index ix_or_order_ship_order_id on or_order_ship (order_id);

alter table oto_child add constraint fk_oto_child_master_id foreign key (master_id) references oto_master (id) on delete restrict on update restrict;
create index ix_oto_child_master_id on oto_child (master_id);

alter table pfile add constraint fk_pfile_file_content_id foreign key (file_content_id) references pfile_content (id) on delete restrict on update restrict;
create index ix_pfile_file_content_id on pfile (file_content_id);

alter table pfile add constraint fk_pfile_file_content2_id foreign key (file_content2_id) references pfile_content (id) on delete restrict on update restrict;
create index ix_pfile_file_content2_id on pfile (file_content2_id);

alter table paggview add constraint fk_paggview_pview_id foreign key (pview_id) references pp (id) on delete restrict on update restrict;
create index ix_paggview_pview_id on paggview (pview_id);

alter table pallet_location add constraint fk_pallet_location_zone_sid foreign key (ZONE_SID) references zones (ID) on delete restrict on update restrict;
create index ix_pallet_location_zone_sid on pallet_location (ZONE_SID);

alter table parcel_location add constraint fk_parcel_location_parcelid foreign key (parcelId) references parcel (parcelId) on delete restrict on update restrict;
create index ix_parcel_location_parcelid on parcel_location (parcelId);

alter table rawinherit_parent_rawinherit_dat add constraint fk_rawinherit_parent_rawinherit_dat_rawinherit_parent foreign key (rawinherit_parent_id) references rawinherit_parent (id) on delete restrict on update restrict;
create index ix_rawinherit_parent_rawinherit_dat_rawinherit_parent_id on rawinherit_parent_rawinherit_dat (rawinherit_parent_id);

alter table rawinherit_parent_rawinherit_dat add constraint fk_rawinherit_parent_rawinherit_dat_rawinherit_data foreign key (rawinherit_data_id) references rawinherit_data (id) on delete restrict on update restrict;
create index ix_rawinherit_parent_rawinherit_dat_rawinherit_data_id on rawinherit_parent_rawinherit_dat (rawinherit_data_id);

alter table c_participation add constraint fk_c_participation_conversation_id foreign key (conversation_id) references c_conversation (id) on delete restrict on update restrict;
create index ix_c_participation_conversation_id on c_participation (conversation_id);

alter table c_participation add constraint fk_c_participation_user_id foreign key (user_id) references c_user (id) on delete restrict on update restrict;
create index ix_c_participation_user_id on c_participation (user_id);

alter table persistent_file_content add constraint fk_persistent_file_content_persistent_file_id foreign key (persistent_file_id) references persistent_file (id) on delete restrict on update restrict;
create index ix_persistent_file_content_persistent_file_id on persistent_file_content (persistent_file_id);

alter table person add constraint fk_person_default_address_oid foreign key (default_address_oid) references address (oid) on delete restrict on update restrict;
create index ix_person_default_address_oid on person (default_address_oid);

alter table PHONES add constraint fk_phones_person_id foreign key (PERSON_ID) references PERSONS (ID) on delete restrict on update restrict;
create index ix_phones_person_id on PHONES (PERSON_ID);

alter table pp_to_ww add constraint fk_pp_to_ww_pp foreign key (pp_id) references pp (id) on delete restrict on update restrict;
create index ix_pp_to_ww_pp_id on pp_to_ww (pp_id);

alter table pp_to_ww add constraint fk_pp_to_ww_wview foreign key (ww_id) references wview (id) on delete restrict on update restrict;
create index ix_pp_to_ww_ww_id on pp_to_ww (ww_id);

alter table r_orders add constraint fk_r_orders_rcustomer foreign key (company,customerName) references rcustomer (company,name) on delete restrict on update restrict;
create index ix_r_orders_company_customerName on r_orders (company,customerName);

alter table ResourceFile add constraint fk_resourcefile_parentresourcefileid foreign key (parentResourceFileId) references ResourceFile (id) on delete restrict on update restrict;
create index ix_resourcefile_parentresourcefileid on ResourceFile (parentResourceFileId);

alter table mt_role add constraint fk_mt_role_tenant_id foreign key (tenant_id) references mt_tenant (id) on delete restrict on update restrict;
create index ix_mt_role_tenant_id on mt_role (tenant_id);

alter table mt_role_permission add constraint fk_mt_role_permission_mt_role foreign key (mt_role_id) references mt_role (id) on delete restrict on update restrict;
create index ix_mt_role_permission_mt_role_id on mt_role_permission (mt_role_id);

alter table mt_role_permission add constraint fk_mt_role_permission_mt_permission foreign key (mt_permission_id) references mt_permission (id) on delete restrict on update restrict;
create index ix_mt_role_permission_mt_permission_id on mt_role_permission (mt_permission_id);

alter table f_second add constraint fk_f_second_first foreign key (first) references f_first (id) on delete restrict on update restrict;
create index ix_f_second_first on f_second (first);

alter table section add constraint fk_section_article_id foreign key (article_id) references article (id) on delete restrict on update restrict;
create index ix_section_article_id on section (article_id);

alter table self_parent add constraint fk_self_parent_parent_id foreign key (parent_id) references self_parent (id) on delete restrict on update restrict;
create index ix_self_parent_parent_id on self_parent (parent_id);

alter table self_ref_customer add constraint fk_self_ref_customer_referred_by_id foreign key (referred_by_id) references self_ref_customer (id) on delete restrict on update restrict;
create index ix_self_ref_customer_referred_by_id on self_ref_customer (referred_by_id);

alter table self_ref_example add constraint fk_self_ref_example_parent_id foreign key (parent_id) references self_ref_example (id) on delete restrict on update restrict;
create index ix_self_ref_example_parent_id on self_ref_example (parent_id);

alter table stockforecast add constraint fk_stockforecast_inner_report_id foreign key (inner_report_id) references inner_report (id) on delete restrict on update restrict;
create index ix_stockforecast_inner_report_id on stockforecast (inner_report_id);

alter table sub_section add constraint fk_sub_section_section_id foreign key (section_id) references section (id) on delete restrict on update restrict;
create index ix_sub_section_section_id on sub_section (section_id);

alter table t_detail_with_other_namexxxyy add constraint fk_t_detail_with_other_namexxxyy_master_id foreign key (master_id) references t_atable_thatisrelatively (id) on delete restrict on update restrict;
create index ix_t_detail_with_other_namexxxyy_master_id on t_detail_with_other_namexxxyy (master_id);

alter table ts_detail_two add constraint fk_ts_detail_two_master_id foreign key (master_id) references ts_master_two (id) on delete restrict on update restrict;
create index ix_ts_detail_two_master_id on ts_detail_two (master_id);

alter table twheel add constraint fk_twheel_owner_plate_no foreign key (owner_plate_no) references tcar (plate_no) on delete restrict on update restrict;
create index ix_twheel_owner_plate_no on twheel (owner_plate_no);

alter table tire add constraint fk_tire_wheel foreign key (wheel) references wheel (id) on delete restrict on update restrict;
create index ix_tire_wheel on tire (wheel);

alter table trip add constraint fk_trip_vehicle_driver_id foreign key (vehicle_driver_id) references vehicle_driver (id) on delete restrict on update restrict;
create index ix_trip_vehicle_driver_id on trip (vehicle_driver_id);

alter table trip add constraint fk_trip_address_id foreign key (address_id) references o_address (id) on delete restrict on update restrict;
create index ix_trip_address_id on trip (address_id);

alter table type add constraint fk_type_sub_type_id foreign key (sub_type_id) references sub_type (sub_type_id) on delete restrict on update restrict;
create index ix_type_sub_type_id on type (sub_type_id);

alter table ut_detail add constraint fk_ut_detail_utmaster_id foreign key (utmaster_id) references ut_master (id) on delete restrict on update restrict;
create index ix_ut_detail_utmaster_id on ut_detail (utmaster_id);

alter table uutwo add constraint fk_uutwo_master_id foreign key (master_id) references uuone (id) on delete restrict on update restrict;
create index ix_uutwo_master_id on uutwo (master_id);

alter table c_user add constraint fk_c_user_group_id foreign key (group_id) references c_group (id) on delete restrict on update restrict;
create index ix_c_user_group_id on c_user (group_id);

alter table oto_user add constraint fk_oto_user_account_id foreign key (account_id) references oto_account (id) on delete restrict on update restrict;
create index ix_oto_user_account_id on oto_user (account_id);

alter table em_user_role add constraint fk_em_user_role_user_id foreign key (user_id) references em_user (id) on delete restrict on update restrict;
create index ix_em_user_role_user_id on em_user_role (user_id);

alter table em_user_role add constraint fk_em_user_role_role_id foreign key (role_id) references em_role (id) on delete restrict on update restrict;
create index ix_em_user_role_role_id on em_user_role (role_id);

alter table vehicle add constraint fk_vehicle_truck_ref_id foreign key (truck_ref_id) references truck_ref (id) on delete restrict on update restrict;
create index ix_vehicle_truck_ref_id on vehicle (truck_ref_id);

alter table vehicle add constraint fk_vehicle_car_ref_id foreign key (car_ref_id) references truck_ref (id) on delete restrict on update restrict;
create index ix_vehicle_car_ref_id on vehicle (car_ref_id);

alter table vehicle_driver add constraint fk_vehicle_driver_vehicle_id foreign key (vehicle_id) references vehicle (id) on delete restrict on update restrict;
create index ix_vehicle_driver_vehicle_id on vehicle_driver (vehicle_id);

alter table vehicle_driver add constraint fk_vehicle_driver_address_id foreign key (address_id) references o_address (id) on delete restrict on update restrict;
create index ix_vehicle_driver_address_id on vehicle_driver (address_id);

alter table warehouses add constraint fk_warehouses_officezoneid foreign key (officeZoneId) references zones (ID) on delete restrict on update restrict;
create index ix_warehouses_officezoneid on warehouses (officeZoneId);

alter table WarehousesShippingZones add constraint fk_warehousesshippingzones_warehouses foreign key (warehouseId) references warehouses (ID) on delete restrict on update restrict;
create index ix_warehousesshippingzones_warehouseid on WarehousesShippingZones (warehouseId);

alter table WarehousesShippingZones add constraint fk_warehousesshippingzones_zones foreign key (shippingZoneId) references zones (ID) on delete restrict on update restrict;
create index ix_warehousesshippingzones_shippingzoneid on WarehousesShippingZones (shippingZoneId);

alter table sa_wheel add constraint fk_sa_wheel_tire foreign key (tire) references sa_tire (id) on delete restrict on update restrict;
create index ix_sa_wheel_tire on sa_wheel (tire);

alter table sa_wheel add constraint fk_sa_wheel_car foreign key (car) references sa_car (id) on delete restrict on update restrict;
create index ix_sa_wheel_car on sa_wheel (car);

alter table with_zero add constraint fk_with_zero_parent_id foreign key (parent_id) references parent (id) on delete restrict on update restrict;
create index ix_with_zero_parent_id on with_zero (parent_id);

