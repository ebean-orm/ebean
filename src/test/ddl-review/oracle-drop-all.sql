alter table bar drop constraint fk_bar_foo_id;
drop index ix_bar_foo_id;

alter table addr drop constraint fk_addr_employee_id;
drop index ix_addr_employee_id;

alter table o_address drop constraint fk_o_address_country_code;
drop index ix_o_address_country_code;

alter table album drop constraint fk_album_cover_id;

alter table animal drop constraint fk_animal_shelter_id;
drop index ix_animal_shelter_id;

alter table attribute drop constraint fk_attrbt_ttrbt_hldr_d;
drop index ix_attrbt_ttrbt_hldr_d;

alter table bbookmark drop constraint fk_bbookmark_user_id;
drop index ix_bbookmark_user_id;

alter table bsite_user drop constraint fk_bsite_user_site_id;
drop index ix_bsite_user_site_id;

alter table bsite_user drop constraint fk_bsite_user_user_id;
drop index ix_bsite_user_user_id;

alter table bsite_user_b drop constraint fk_bsite_user_b_site;
drop index ix_bsite_user_b_site;

alter table bsite_user_b drop constraint fk_bsite_user_b_usr;
drop index ix_bsite_user_b_usr;

alter table bsite_user_c drop constraint fk_bsite_user_c_site_uid;
drop index ix_bsite_user_c_site_uid;

alter table bsite_user_c drop constraint fk_bsite_user_c_user_uid;
drop index ix_bsite_user_c_user_uid;

alter table drel_booking drop constraint fk_drel_booking_agent_invoice;

alter table drel_booking drop constraint fk_drl_bkng_clnt_nvc;

alter table ckey_detail drop constraint fk_ckey_detail_parent;
drop index ix_ckey_detail_parent;

alter table ckey_parent drop constraint fk_ckey_parent_assoc_id;
drop index ix_ckey_parent_assoc_id;

alter table calculation_result drop constraint fk_clcltn_rslt_prdct_cnfgrt_1;
drop index ix_clcltn_rslt_prdct_cnfgrt_1;

alter table calculation_result drop constraint fk_clcltn_rslt_grp_cnfgrtn_d;
drop index ix_clcltn_rslt_grp_cnfgrtn_d;

alter table sp_car_car_wheels drop constraint fk_sp_cr_cr_whls_sp_cr_cr;
drop index ix_sp_cr_cr_whls_sp_cr_cr;

alter table sp_car_car_wheels drop constraint fk_sp_cr_cr_whls_sp_cr_whl;
drop index ix_sp_cr_cr_whls_sp_cr_whl;

alter table car_accessory drop constraint fk_car_accessory_fuse_id;
drop index ix_car_accessory_fuse_id;

alter table car_accessory drop constraint fk_car_accessory_car_id;
drop index ix_car_accessory_car_id;

alter table category drop constraint fk_category_surveyobjectid;
drop index ix_category_surveyobjectid;

alter table child_person drop constraint fk_child_person_some_bean_id;
drop index ix_child_person_some_bean_id;

alter table child_person drop constraint fk_chld_prsn_prnt_dntfr;
drop index ix_chld_prsn_prnt_dntfr;

alter table configuration drop constraint fk_cnfgrtn_cnfgrtns_d;
drop index ix_cnfgrtn_cnfgrtns_d;

alter table contact drop constraint fk_contact_customer_id;
drop index ix_contact_customer_id;

alter table contact drop constraint fk_contact_group_id;
drop index ix_contact_group_id;

alter table contact_note drop constraint fk_contact_note_contact_id;
drop index ix_contact_note_contact_id;

alter table c_conversation drop constraint fk_c_conversation_group_id;
drop index ix_c_conversation_group_id;

alter table o_customer drop constraint fk_o_cstmr_bllng_ddrss_d;
drop index ix_o_cstmr_bllng_ddrss_d;

alter table o_customer drop constraint fk_o_cstmr_shppng_ddrss_d;
drop index ix_o_cstmr_shppng_ddrss_d;

alter table dcredit_drol drop constraint fk_dcredit_drol_dcredit;
drop index ix_dcredit_drol_dcredit;

alter table dcredit_drol drop constraint fk_dcredit_drol_drol;
drop index ix_dcredit_drol_drol;

alter table drot_drol drop constraint fk_drot_drol_drot;
drop index ix_drot_drol_drot;

alter table drot_drol drop constraint fk_drot_drol_drol;
drop index ix_drot_drol_drol;

alter table dfk_cascade drop constraint fk_dfk_cascade_one_id;
drop index ix_dfk_cascade_one_id;

alter table dfk_set_null drop constraint fk_dfk_set_null_one_id;
drop index ix_dfk_set_null_one_id;

alter table doc drop constraint fk_doc_id;

alter table doc_link drop constraint fk_doc_link_doc;
drop index ix_doc_link_doc;

alter table doc_link drop constraint fk_doc_link_link;
drop index ix_doc_link_link;

alter table document drop constraint fk_document_id;

alter table document drop constraint fk_document_organisation_id;
drop index ix_document_organisation_id;

alter table document_draft drop constraint fk_dcmnt_drft_rgnstn_d;
drop index ix_dcmnt_drft_rgnstn_d;

alter table document_media drop constraint fk_document_media_document_id;
drop index ix_document_media_document_id;

alter table document_media_draft drop constraint fk_dcmnt_md_drft_dcmnt_d;
drop index ix_dcmnt_md_drft_dcmnt_d;

alter table ebasic_json_map_detail drop constraint fk_ebsc_jsn_mp_dtl_wnr_d;
drop index ix_ebsc_jsn_mp_dtl_wnr_d;

alter table ebasic_no_sdchild drop constraint fk_ebasic_no_sdchild_owner_id;
drop index ix_ebasic_no_sdchild_owner_id;

alter table ebasic_sdchild drop constraint fk_ebasic_sdchild_owner_id;
drop index ix_ebasic_sdchild_owner_id;

alter table eemb_inner drop constraint fk_eemb_inner_outer_id;
drop index ix_eemb_inner_outer_id;

alter table einvoice drop constraint fk_einvoice_person_id;
drop index ix_einvoice_person_id;

alter table enull_collection_detail drop constraint fk_enll_cllctn_dtl_nll_cllc_1;
drop index ix_enll_cllctn_dtl_nll_cllc_1;

alter table eopt_one_a drop constraint fk_eopt_one_a_b_id;
drop index ix_eopt_one_a_b_id;

alter table eopt_one_b drop constraint fk_eopt_one_b_c_id;
drop index ix_eopt_one_b_c_id;

alter table esoft_del_book drop constraint fk_esoft_del_book_lend_by_id;
drop index ix_esoft_del_book_lend_by_id;

alter table esoft_del_book_esoft_del_user drop constraint fk_esft_dl_bk_sft_dl_sr_sft_1;
drop index ix_esft_dl_bk_sft_dl_sr_sft_1;

alter table esoft_del_book_esoft_del_user drop constraint fk_esft_dl_bk_sft_dl_sr_sft_2;
drop index ix_esft_dl_bk_sft_dl_sr_sft_2;

alter table esoft_del_down drop constraint fk_esft_dl_dwn_sft_dl_md_d;
drop index ix_esft_dl_dwn_sft_dl_md_d;

alter table esoft_del_mid drop constraint fk_esoft_del_mid_top_id;
drop index ix_esoft_del_mid_top_id;

alter table esoft_del_mid drop constraint fk_esoft_del_mid_up_id;
drop index ix_esoft_del_mid_up_id;

alter table esoft_del_role_esoft_del_user drop constraint fk_esft_dl_rl_sft_dl_sr_sft_1;
drop index ix_esft_dl_rl_sft_dl_sr_sft_1;

alter table esoft_del_role_esoft_del_user drop constraint fk_esft_dl_rl_sft_dl_sr_sft_2;
drop index ix_esft_dl_rl_sft_dl_sr_sft_2;

alter table esoft_del_user_esoft_del_role drop constraint fk_esft_dl_sr_sft_dl_rl_sft_1;
drop index ix_esft_dl_sr_sft_dl_rl_sft_1;

alter table esoft_del_user_esoft_del_role drop constraint fk_esft_dl_sr_sft_dl_rl_sft_2;
drop index ix_esft_dl_sr_sft_dl_rl_sft_2;

alter table rawinherit_uncle drop constraint fk_rawinherit_uncle_parent_id;
drop index ix_rawinherit_uncle_parent_id;

alter table evanilla_collection_detail drop constraint fk_evnll_cllctn_dtl_vnll_cl_1;
drop index ix_evnll_cllctn_dtl_vnll_cl_1;

alter table td_child drop constraint fk_td_child_parent_id;
drop index ix_td_child_parent_id;

alter table empl drop constraint fk_empl_default_address_id;
drop index ix_empl_default_address_id;

alter table grand_parent_person drop constraint fk_grnd_prnt_prsn_sm_bn_d;
drop index ix_grnd_prnt_prsn_sm_bn_d;

alter table survey_group drop constraint fk_srvy_grp_ctgrybjctd;
drop index ix_srvy_grp_ctgrybjctd;

alter table hx_link_doc drop constraint fk_hx_link_doc_hx_link;
drop index ix_hx_link_doc_hx_link;

alter table hx_link_doc drop constraint fk_hx_link_doc_he_doc;
drop index ix_hx_link_doc_he_doc;

alter table hi_link_doc drop constraint fk_hi_link_doc_hi_link;
drop index ix_hi_link_doc_hi_link;

alter table hi_link_doc drop constraint fk_hi_link_doc_hi_doc;
drop index ix_hi_link_doc_hi_doc;

alter table imrelated drop constraint fk_imrelated_owner_id;
drop index ix_imrelated_owner_id;

alter table info_contact drop constraint fk_info_contact_company_id;
drop index ix_info_contact_company_id;

alter table info_customer drop constraint fk_info_customer_company_id;

alter table inner_report drop constraint fk_inner_report_forecast_id;

alter table drel_invoice drop constraint fk_drel_invoice_booking;
drop index ix_drel_invoice_booking;

alter table item drop constraint fk_item_etype;
drop index ix_item_etype;

alter table item drop constraint fk_item_eregion;
drop index ix_item_eregion;

alter table trainer_monkey drop constraint fk_trainer_monkey_trainer;

alter table trainer_monkey drop constraint fk_trainer_monkey_monkey;

alter table troop_monkey drop constraint fk_troop_monkey_troop;

alter table troop_monkey drop constraint fk_troop_monkey_monkey;

alter table l2_cldf_reset_bean_child drop constraint fk_l2_cldf_rst_bn_chld_prnt_d;
drop index ix_l2_cldf_rst_bn_chld_prnt_d;

alter table level1_level4 drop constraint fk_level1_level4_level1;
drop index ix_level1_level4_level1;

alter table level1_level4 drop constraint fk_level1_level4_level4;
drop index ix_level1_level4_level4;

alter table level1_level2 drop constraint fk_level1_level2_level1;
drop index ix_level1_level2_level1;

alter table level1_level2 drop constraint fk_level1_level2_level2;
drop index ix_level1_level2_level2;

alter table level2_level3 drop constraint fk_level2_level3_level2;
drop index ix_level2_level3_level2;

alter table level2_level3 drop constraint fk_level2_level3_level3;
drop index ix_level2_level3_level3;

alter table link drop constraint fk_link_id;

alter table la_attr_value_attribute drop constraint fk_l_ttr_vl_ttrbt_l_ttr_vl;
drop index ix_l_ttr_vl_ttrbt_l_ttr_vl;

alter table la_attr_value_attribute drop constraint fk_l_ttr_vl_ttrbt_ttrbt;
drop index ix_l_ttr_vl_ttrbt_ttrbt;

alter table mprinter drop constraint fk_mprinter_current_state_id;
drop index ix_mprinter_current_state_id;

alter table mprinter drop constraint fk_mprinter_last_swap_cyan_id;

alter table mprinter drop constraint fk_mprntr_lst_swp_mgnt_d;

alter table mprinter drop constraint fk_mprntr_lst_swp_yllw_d;

alter table mprinter drop constraint fk_mprntr_lst_swp_blck_d;

alter table mprinter_state drop constraint fk_mprinter_state_printer_id;
drop index ix_mprinter_state_printer_id;

alter table mprofile drop constraint fk_mprofile_picture_id;
drop index ix_mprofile_picture_id;

alter table mrole_muser drop constraint fk_mrole_muser_mrole;
drop index ix_mrole_muser_mrole;

alter table mrole_muser drop constraint fk_mrole_muser_muser;
drop index ix_mrole_muser_muser;

alter table muser drop constraint fk_muser_user_type_id;
drop index ix_muser_user_type_id;

alter table mail_user_inbox drop constraint fk_mail_user_inbox_mail_user;
drop index ix_mail_user_inbox_mail_user;

alter table mail_user_inbox drop constraint fk_mail_user_inbox_mail_box;
drop index ix_mail_user_inbox_mail_box;

alter table mail_user_outbox drop constraint fk_mail_user_outbox_mail_user;
drop index ix_mail_user_outbox_mail_user;

alter table mail_user_outbox drop constraint fk_mail_user_outbox_mail_box;
drop index ix_mail_user_outbox_mail_box;

alter table c_message drop constraint fk_c_message_conversation_id;
drop index ix_c_message_conversation_id;

alter table c_message drop constraint fk_c_message_user_id;
drop index ix_c_message_user_id;

alter table mnoc_user_mnoc_role drop constraint fk_mnc_sr_mnc_rl_mnc_sr;
drop index ix_mnc_sr_mnc_rl_mnc_sr;

alter table mnoc_user_mnoc_role drop constraint fk_mnc_sr_mnc_rl_mnc_rl;
drop index ix_mnc_sr_mnc_rl_mnc_rl;

alter table mny_b drop constraint fk_mny_b_a_id;
drop index ix_mny_b_a_id;

alter table mny_b_mny_c drop constraint fk_mny_b_mny_c_mny_b;
drop index ix_mny_b_mny_c_mny_b;

alter table mny_b_mny_c drop constraint fk_mny_b_mny_c_mny_c;
drop index ix_mny_b_mny_c_mny_c;

alter table subtopics drop constraint fk_subtopics_mny_topic_1;
drop index ix_subtopics_mny_topic_1;

alter table subtopics drop constraint fk_subtopics_mny_topic_2;
drop index ix_subtopics_mny_topic_2;

alter table mp_role drop constraint fk_mp_role_mp_user_id;
drop index ix_mp_role_mp_user_id;

alter table ms_many_a_many_b drop constraint fk_ms_many_a_many_b_ms_many_a;
drop index ix_ms_many_a_many_b_ms_many_a;

alter table ms_many_a_many_b drop constraint fk_ms_many_a_many_b_ms_many_b;
drop index ix_ms_many_a_many_b_ms_many_b;

alter table ms_many_b_many_a drop constraint fk_ms_many_b_many_a_ms_many_b;
drop index ix_ms_many_b_many_a_ms_many_b;

alter table ms_many_b_many_a drop constraint fk_ms_many_b_many_a_ms_many_a;
drop index ix_ms_many_b_many_a_ms_many_a;

alter table my_lob_size_join_many drop constraint fk_my_lb_sz_jn_mny_prnt_d;
drop index ix_my_lb_sz_jn_mny_prnt_d;

alter table o_cached_bean_country drop constraint fk_o_cchd_bn_cntry__cchd_bn;
drop index ix_o_cchd_bn_cntry__cchd_bn;

alter table o_cached_bean_country drop constraint fk_o_cchd_bn_cntry__cntry;
drop index ix_o_cchd_bn_cntry__cntry;

alter table o_cached_bean_child drop constraint fk_o_cchd_bn_chld_cchd_bn_d;
drop index ix_o_cchd_bn_chld_cchd_bn_d;

alter table oengine drop constraint fk_oengine_car_id;

alter table ogear_box drop constraint fk_ogear_box_car_id;

alter table oroad_show_msg drop constraint fk_oroad_show_msg_company_id;

alter table om_ordered_detail drop constraint fk_om_rdrd_dtl_mstr_d;
drop index ix_om_rdrd_dtl_mstr_d;

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

alter table oto_bchild drop constraint fk_oto_bchild_master_id;

alter table oto_child drop constraint fk_oto_child_master_id;

alter table oto_cust_address drop constraint fk_ot_cst_ddrss_cstmr_cd;

alter table oto_prime_extra drop constraint fk_oto_prime_extra_eid;

alter table oto_th_many drop constraint fk_oto_th_many_oto_th_top_id;
drop index ix_oto_th_many_oto_th_top_id;

alter table oto_th_one drop constraint fk_oto_th_one_many_id;

alter table oto_ubprime_extra drop constraint fk_oto_ubprime_extra_eid;

alter table oto_uprime_extra drop constraint fk_oto_uprime_extra_eid;

alter table pfile drop constraint fk_pfile_file_content_id;

alter table pfile drop constraint fk_pfile_file_content2_id;

alter table paggview drop constraint fk_paggview_pview_id;

alter table pallet_location drop constraint fk_pallet_location_zone_sid;
drop index ix_pallet_location_zone_sid;

alter table parcel_location drop constraint fk_parcel_location_parcelid;

alter table rawinherit_parent_rawinherit_d drop constraint fk_rwnhrt_prnt_rwnhrt_d_rwn_1;
drop index ix_rwnhrt_prnt_rwnhrt_d_rwn_1;

alter table rawinherit_parent_rawinherit_d drop constraint fk_rwnhrt_prnt_rwnhrt_d_rwn_2;
drop index ix_rwnhrt_prnt_rwnhrt_d_rwn_2;

alter table parent_person drop constraint fk_parent_person_some_bean_id;
drop index ix_parent_person_some_bean_id;

alter table parent_person drop constraint fk_prnt_prsn_prnt_dntfr;
drop index ix_prnt_prsn_prnt_dntfr;

alter table c_participation drop constraint fk_c_prtcptn_cnvrstn_d;
drop index ix_c_prtcptn_cnvrstn_d;

alter table c_participation drop constraint fk_c_participation_user_id;
drop index ix_c_participation_user_id;

alter table persistent_file_content drop constraint fk_prsstnt_fl_cntnt_prsstnt_1;

alter table person drop constraint fk_person_default_address_oid;
drop index ix_person_default_address_oid;

alter table phones drop constraint fk_phones_person_id;
drop index ix_phones_person_id;

alter table pp_to_ww drop constraint fk_pp_to_ww_pp;
drop index ix_pp_to_ww_pp;

alter table pp_to_ww drop constraint fk_pp_to_ww_wview;
drop index ix_pp_to_ww_wview;

alter table question drop constraint fk_question_groupobjectid;
drop index ix_question_groupobjectid;

alter table r_orders drop constraint fk_r_orders_customer;
drop index ix_r_orders_customer;

alter table resourcefile drop constraint fk_rsrcfl_prntrsrcfld;
drop index ix_rsrcfl_prntrsrcfld;

alter table mt_role drop constraint fk_mt_role_tenant_id;
drop index ix_mt_role_tenant_id;

alter table mt_role_permission drop constraint fk_mt_role_permission_mt_role;
drop index ix_mt_role_permission_mt_role;

alter table mt_role_permission drop constraint fk_mt_rl_prmssn_mt_prmssn;
drop index ix_mt_rl_prmssn_mt_prmssn;

alter table f_second drop constraint fk_f_second_first;

alter table section drop constraint fk_section_article_id;
drop index ix_section_article_id;

alter table self_parent drop constraint fk_self_parent_parent_id;
drop index ix_self_parent_parent_id;

alter table self_ref_customer drop constraint fk_slf_rf_cstmr_rfrrd_by_d;
drop index ix_slf_rf_cstmr_rfrrd_by_d;

alter table self_ref_example drop constraint fk_self_ref_example_parent_id;
drop index ix_self_ref_example_parent_id;

alter table stockforecast drop constraint fk_stckfrcst_nnr_rprt_d;
drop index ix_stckfrcst_nnr_rprt_d;

alter table sub_section drop constraint fk_sub_section_section_id;
drop index ix_sub_section_section_id;

alter table tevent_many drop constraint fk_tevent_many_event_id;
drop index ix_tevent_many_event_id;

alter table tevent_one drop constraint fk_tevent_one_event_id;

alter table t_detail_with_other_namexxxyy drop constraint fk_t_dtl_wth_thr_nmxxxyy_ms_1;
drop index ix_t_dtl_wth_thr_nmxxxyy_ms_1;

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

alter table vehicle drop constraint fk_vehicle_car_ref_id;
drop index ix_vehicle_car_ref_id;

alter table vehicle drop constraint fk_vehicle_truck_ref_id;
drop index ix_vehicle_truck_ref_id;

alter table vehicle_driver drop constraint fk_vehicle_driver_vehicle_id;
drop index ix_vehicle_driver_vehicle_id;

alter table vehicle_driver drop constraint fk_vehicle_driver_address_id;
drop index ix_vehicle_driver_address_id;

alter table warehouses drop constraint fk_warehouses_officezoneid;
drop index ix_warehouses_officezoneid;

alter table warehousesshippingzones drop constraint fk_wrhssshppngzns_wrhss;
drop index ix_wrhssshppngzns_wrhss;

alter table warehousesshippingzones drop constraint fk_wrhssshppngzns_zns;
drop index ix_wrhssshppngzns_zns;

alter table sa_wheel drop constraint fk_sa_wheel_tire;
drop index ix_sa_wheel_tire;

alter table sa_wheel drop constraint fk_sa_wheel_car;
drop index ix_sa_wheel_car;

alter table g_who_props_otm drop constraint fk_g_wh_prps_tm_wh_crtd_d;
drop index ix_g_wh_prps_tm_wh_crtd_d;

alter table g_who_props_otm drop constraint fk_g_wh_prps_tm_wh_mdfd_d;
drop index ix_g_wh_prps_tm_wh_mdfd_d;

alter table with_zero drop constraint fk_with_zero_parent_id;
drop index ix_with_zero_parent_id;

drop table asimple_bean cascade constraints purge;
drop sequence asimple_bean_seq;

drop table bar cascade constraints purge;
drop sequence bar_seq;

drop table oto_account cascade constraints purge;
drop sequence oto_account_seq;

drop table addr cascade constraints purge;
drop sequence addr_seq;

drop table address cascade constraints purge;
drop sequence address_seq;

drop table o_address cascade constraints purge;
drop sequence o_address_seq;

drop table album cascade constraints purge;
drop sequence album_seq;

drop table animal cascade constraints purge;
drop sequence animal_seq;

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

drop table bsite cascade constraints purge;

drop table bsite_user cascade constraints purge;

drop table bsite_user_b cascade constraints purge;

drop table bsite_user_c cascade constraints purge;

drop table buser cascade constraints purge;

drop table bwith_qident cascade constraints purge;
drop sequence bwith_qident_seq;

drop table basic_joda_entity cascade constraints purge;
drop sequence basic_joda_entity_seq;

drop table bean_with_time_zone cascade constraints purge;
drop sequence bean_with_time_zone_seq;

drop table drel_booking cascade constraints purge;
drop sequence drel_booking_seq;

drop table cinh_root cascade constraints purge;
drop sequence cinh_root_seq;

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

drop table car_fuse cascade constraints purge;
drop sequence car_fuse_seq;

drop table category cascade constraints purge;
drop sequence category_seq;

drop table child_person cascade constraints purge;
drop sequence child_person_seq;

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

drop table cover cascade constraints purge;
drop sequence cover_seq;

drop table o_customer cascade constraints purge;
drop sequence o_customer_seq;

drop table dcredit cascade constraints purge;
drop sequence dcredit_seq;

drop table dcredit_drol cascade constraints purge;

drop table dexh_entity cascade constraints purge;
drop sequence dexh_entity_seq;

drop table dperson cascade constraints purge;
drop sequence dperson_seq;

drop table drol cascade constraints purge;
drop sequence drol_seq;

drop table drot cascade constraints purge;
drop sequence drot_seq;

drop table drot_drol cascade constraints purge;

drop table rawinherit_data cascade constraints purge;
drop sequence rawinherit_data_seq;

drop table dfk_cascade cascade constraints purge;
drop sequence dfk_cascade_seq;

drop table dfk_cascade_one cascade constraints purge;
drop sequence dfk_cascade_one_seq;

drop table dfk_none cascade constraints purge;
drop sequence dfk_none_seq;

drop table dfk_none_via_join cascade constraints purge;
drop sequence dfk_none_via_join_seq;

drop table dfk_one cascade constraints purge;
drop sequence dfk_one_seq;

drop table dfk_set_null cascade constraints purge;
drop sequence dfk_set_null_seq;

drop table doc cascade constraints purge;
drop sequence doc_seq;

drop table doc_link cascade constraints purge;

drop table doc_link_draft cascade constraints purge;

drop table doc_draft cascade constraints purge;
drop sequence doc_draft_seq;

drop table document cascade constraints purge;
drop sequence document_seq;

drop table document_draft cascade constraints purge;
drop sequence document_draft_seq;

drop table document_media cascade constraints purge;
drop sequence document_media_seq;

drop table document_media_draft cascade constraints purge;
drop sequence document_media_draft_seq;

drop table earray_bean cascade constraints purge;
drop sequence earray_bean_seq;

drop table earray_set_bean cascade constraints purge;
drop sequence earray_set_bean_seq;

drop table e_basic cascade constraints purge;
drop sequence e_basic_seq;

drop table ebasic_change_log cascade constraints purge;
drop sequence ebasic_change_log_seq;

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

drop table e_basic_eni cascade constraints purge;
drop sequence e_basic_eni_seq;

drop table ebasic_hstore cascade constraints purge;
drop sequence ebasic_hstore_seq;

drop table ebasic_json_list cascade constraints purge;
drop sequence ebasic_json_list_seq;

drop table ebasic_json_map cascade constraints purge;
drop sequence ebasic_json_map_seq;

drop table ebasic_json_map_blob cascade constraints purge;
drop sequence ebasic_json_map_blob_seq;

drop table ebasic_json_map_clob cascade constraints purge;
drop sequence ebasic_json_map_clob_seq;

drop table ebasic_json_map_detail cascade constraints purge;
drop sequence ebasic_json_map_detail_seq;

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

drop table ebasic_json_unmapped cascade constraints purge;
drop sequence ebasic_json_unmapped_seq;

drop table e_basic_ndc cascade constraints purge;
drop sequence e_basic_ndc_seq;

drop table ebasic_no_sdchild cascade constraints purge;
drop sequence ebasic_no_sdchild_seq;

drop table ebasic_sdchild cascade constraints purge;
drop sequence ebasic_sdchild_seq;

drop table ebasic_soft_delete cascade constraints purge;
drop sequence ebasic_soft_delete_seq;

drop table e_basicver cascade constraints purge;
drop sequence e_basicver_seq;

drop table e_basic_withlife cascade constraints purge;
drop sequence e_basic_withlife_seq;

drop table e_basicverucon cascade constraints purge;
drop sequence e_basicverucon_seq;

drop table e_col_ab cascade constraints purge;
drop sequence e_col_ab_seq;

drop table ecustom_id cascade constraints purge;

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

drop table e_person_online cascade constraints purge;
drop sequence e_person_online_seq;

drop table esimple cascade constraints purge;

drop table esoft_del_book cascade constraints purge;
drop sequence esoft_del_book_seq;

drop table esoft_del_book_esoft_del_user cascade constraints purge;

drop table esoft_del_down cascade constraints purge;
drop sequence esoft_del_down_seq;

drop table esoft_del_mid cascade constraints purge;
drop sequence esoft_del_mid_seq;

drop table esoft_del_role cascade constraints purge;
drop sequence esoft_del_role_seq;

drop table esoft_del_role_esoft_del_user cascade constraints purge;

drop table esoft_del_top cascade constraints purge;
drop sequence esoft_del_top_seq;

drop table esoft_del_up cascade constraints purge;
drop sequence esoft_del_up_seq;

drop table esoft_del_user cascade constraints purge;
drop sequence esoft_del_user_seq;

drop table esoft_del_user_esoft_del_role cascade constraints purge;

drop table esome_convert_type cascade constraints purge;
drop sequence esome_convert_type_seq;

drop table esome_type cascade constraints purge;
drop sequence esome_type_seq;

drop table etrans_many cascade constraints purge;
drop sequence etrans_many_seq;

drop table rawinherit_uncle cascade constraints purge;
drop sequence rawinherit_uncle_seq;

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

drop table empl cascade constraints purge;
drop sequence empl_seq;

drop table feature_desc cascade constraints purge;
drop sequence feature_desc_seq;

drop table f_first cascade constraints purge;
drop sequence f_first_seq;

drop table foo cascade constraints purge;
drop sequence foo_seq;

drop table gen_key_identity cascade constraints purge;

drop table gen_key_sequence cascade constraints purge;
drop sequence SEQ;

drop table gen_key_table cascade constraints purge;
drop sequence gen_key_table_seq;

drop table grand_parent_person cascade constraints purge;
drop sequence grand_parent_person_seq;

drop table survey_group cascade constraints purge;
drop sequence survey_group_seq;

drop table c_group cascade constraints purge;
drop sequence c_group_seq;

drop table he_doc cascade constraints purge;
drop sequence he_doc_seq;

drop table hx_link cascade constraints purge;
drop sequence hx_link_seq;

drop table hx_link_doc cascade constraints purge;

drop table hi_doc cascade constraints purge;
drop sequence hi_doc_seq;

drop table hi_link cascade constraints purge;
drop sequence hi_link_seq;

drop table hi_link_doc cascade constraints purge;

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

drop table monkey cascade constraints purge;
drop sequence monkey_seq;

drop table trainer cascade constraints purge;
drop sequence trainer_seq;

drop table trainer_monkey cascade constraints purge;

drop table troop cascade constraints purge;
drop sequence troop_seq;

drop table troop_monkey cascade constraints purge;

drop table l2_cldf_reset_bean cascade constraints purge;
drop sequence l2_cldf_reset_bean_seq;

drop table l2_cldf_reset_bean_child cascade constraints purge;
drop sequence l2_cldf_reset_bean_child_seq;

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

drop table link cascade constraints purge;
drop sequence link_seq;

drop table link_draft cascade constraints purge;
drop sequence link_draft_seq;

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

drop table mail_box cascade constraints purge;
drop sequence mail_box_seq;

drop table mail_user cascade constraints purge;
drop sequence mail_user_seq;

drop table mail_user_inbox cascade constraints purge;

drop table mail_user_outbox cascade constraints purge;

drop table map_super_actual cascade constraints purge;
drop sequence map_super_actual_seq;

drop table c_message cascade constraints purge;
drop sequence c_message_seq;

drop table mnoc_role cascade constraints purge;
drop sequence mnoc_role_seq;

drop table mnoc_user cascade constraints purge;
drop sequence mnoc_user_seq;

drop table mnoc_user_mnoc_role cascade constraints purge;

drop table mny_a cascade constraints purge;
drop sequence mny_a_seq;

drop table mny_b cascade constraints purge;
drop sequence mny_b_seq;

drop table mny_b_mny_c cascade constraints purge;

drop table mny_c cascade constraints purge;
drop sequence mny_c_seq;

drop table mny_topic cascade constraints purge;
drop sequence mny_topic_seq;

drop table subtopics cascade constraints purge;

drop table mp_role cascade constraints purge;
drop sequence mp_role_seq;

drop table mp_user cascade constraints purge;
drop sequence mp_user_seq;

drop table ms_many_a cascade constraints purge;
drop sequence ms_many_a_seq;

drop table ms_many_a_many_b cascade constraints purge;

drop table ms_many_b cascade constraints purge;
drop sequence ms_many_b_seq;

drop table ms_many_b_many_a cascade constraints purge;

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

drop table o_cached_natkey cascade constraints purge;
drop sequence o_cached_natkey_seq;

drop table o_cached_natkey3 cascade constraints purge;
drop sequence o_cached_natkey3_seq;

drop table ocar cascade constraints purge;
drop sequence ocar_seq;

drop table ocompany cascade constraints purge;
drop sequence ocompany_seq;

drop table oengine cascade constraints purge;

drop table ogear_box cascade constraints purge;

drop table oroad_show_msg cascade constraints purge;
drop sequence oroad_show_msg_seq;

drop table om_ordered_detail cascade constraints purge;
drop sequence om_ordered_detail_seq;

drop table om_ordered_master cascade constraints purge;
drop sequence om_ordered_master_seq;

drop table o_order cascade constraints purge;
drop sequence o_order_seq;

drop table o_order_detail cascade constraints purge;
drop sequence o_order_detail_seq;

drop table s_orders cascade constraints purge;

drop table s_order_items cascade constraints purge;

drop table or_order_ship cascade constraints purge;
drop sequence or_order_ship_seq;

drop table organisation cascade constraints purge;
drop sequence organisation_seq;

drop table oto_bchild cascade constraints purge;
drop sequence oto_bchild_seq;

drop table oto_bmaster cascade constraints purge;
drop sequence oto_bmaster_seq;

drop table oto_child cascade constraints purge;
drop sequence oto_child_seq;

drop table oto_cust cascade constraints purge;
drop sequence oto_cust_seq;

drop table oto_cust_address cascade constraints purge;
drop sequence oto_cust_address_seq;

drop table oto_master cascade constraints purge;
drop sequence oto_master_seq;

drop table oto_prime cascade constraints purge;
drop sequence oto_prime_seq;

drop table oto_prime_extra cascade constraints purge;

drop table oto_th_many cascade constraints purge;
drop sequence oto_th_many_seq;

drop table oto_th_one cascade constraints purge;
drop sequence oto_th_one_seq;

drop table oto_th_top cascade constraints purge;
drop sequence oto_th_top_seq;

drop table oto_ubprime cascade constraints purge;

drop table oto_ubprime_extra cascade constraints purge;

drop table oto_uprime cascade constraints purge;

drop table oto_uprime_extra cascade constraints purge;

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

drop table parent_person cascade constraints purge;
drop sequence parent_person_seq;

drop table c_participation cascade constraints purge;
drop sequence c_participation_seq;

drop table mt_permission cascade constraints purge;

drop table persistent_file cascade constraints purge;
drop sequence persistent_file_seq;

drop table persistent_file_content cascade constraints purge;
drop sequence persistent_file_content_seq;

drop table persons cascade constraints purge;
drop sequence PERSONS_seq;

drop table person cascade constraints purge;
drop sequence person_seq;

drop table phones cascade constraints purge;
drop sequence PHONES_seq;

drop table primary_revision cascade constraints purge;

drop table o_product cascade constraints purge;
drop sequence o_product_seq;

drop table pp cascade constraints purge;

drop table pp_to_ww cascade constraints purge;

drop table question cascade constraints purge;
drop sequence question_seq;

drop table rcustomer cascade constraints purge;

drop table r_orders cascade constraints purge;

drop table region cascade constraints purge;

drop table resourcefile cascade constraints purge;

drop table em_role cascade constraints purge;
drop sequence em_role_seq;

drop table mt_role cascade constraints purge;

drop table mt_role_permission cascade constraints purge;

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

drop table survey cascade constraints purge;
drop sequence survey_seq;

drop table tbytes_only cascade constraints purge;
drop sequence tbytes_only_seq;

drop table tcar cascade constraints purge;

drop table tevent cascade constraints purge;
drop sequence tevent_seq;

drop table tevent_many cascade constraints purge;
drop sequence tevent_many_seq;

drop table tevent_one cascade constraints purge;
drop sequence tevent_one_seq;

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

drop table test_annotation_base_entity cascade constraints purge;

drop table tire cascade constraints purge;
drop sequence tire_seq;

drop table sa_tire cascade constraints purge;
drop sequence sa_tire_seq;

drop table trip cascade constraints purge;
drop sequence trip_seq;

drop table truck_ref cascade constraints purge;
drop sequence truck_ref_seq;

drop table type cascade constraints purge;

drop table tz_bean cascade constraints purge;
drop sequence tz_bean_seq;

drop table ut_detail cascade constraints purge;
drop sequence ut_detail_seq;

drop table ut_master cascade constraints purge;
drop sequence ut_master_seq;

drop table uuone cascade constraints purge;

drop table uutwo cascade constraints purge;

drop table c_user cascade constraints purge;
drop sequence c_user_seq;

drop table tx_user cascade constraints purge;
drop sequence tx_user_seq;

drop table g_user cascade constraints purge;
drop sequence g_user_seq;

drop table em_user cascade constraints purge;
drop sequence em_user_seq;

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

drop table wheel cascade constraints purge;
drop sequence wheel_seq;

drop table sp_car_wheel cascade constraints purge;
drop sequence sp_car_wheel_seq;

drop table g_who_props_otm cascade constraints purge;
drop sequence g_who_props_otm_seq;

drop table with_zero cascade constraints purge;
drop sequence with_zero_seq;

drop table parent cascade constraints purge;
drop sequence parent_seq;

drop table wview cascade constraints purge;

drop table zones cascade constraints purge;
drop sequence zones_seq;

drop index ix_cntct_lst_nm_frst_nm;
drop index ix_e_basic_name;
