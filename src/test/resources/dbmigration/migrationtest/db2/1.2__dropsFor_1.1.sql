-- apply changes
-- Migrationscript for db2;
-- identity type: IDENTITY;
-- generated at Tue Sep 12 12:06:06 CEST 2017;
-- generator null/null null;

alter table migtest_e_basic drop column old_boolean;

alter table migtest_e_basic drop column old_boolean2;

alter table migtest_e_basic drop column eref_id;

drop table migtest_e_ref;
drop sequence migtest_e_ref_seq;
