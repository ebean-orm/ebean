-- Migrationscripts for ebean unittest
-- apply changes
-- alter table migtest_e_history2 set (system_versioning = off (history_table=dbo.migtest_e_history2_history));
-- history migration goes here
-- alter table migtest_e_history2 set (system_versioning = on (history_table=dbo.migtest_e_history2_history));
-- apply alter tables
EXEC usp_ebean_drop_column migtest_e_basic, description_file;
EXEC usp_ebean_drop_column migtest_e_basic, old_boolean;
EXEC usp_ebean_drop_column migtest_e_basic, old_boolean2;
EXEC usp_ebean_drop_column migtest_e_basic, eref_id;
EXEC usp_ebean_drop_column migtest_e_history2, obsolete_string1;
EXEC usp_ebean_drop_column migtest_e_history2, obsolete_string2;
-- apply post alter
IF OBJECT_ID('drop_main', 'U') IS NOT NULL drop table drop_main;
IF OBJECT_ID('drop_main_seq', 'SO') IS NOT NULL drop sequence drop_main_seq;
IF OBJECT_ID('drop_main_drop_ref_many', 'U') IS NOT NULL drop table drop_main_drop_ref_many;
IF OBJECT_ID('drop_ref_many', 'U') IS NOT NULL drop table drop_ref_many;
IF OBJECT_ID('drop_ref_many_seq', 'SO') IS NOT NULL drop sequence drop_ref_many_seq;
IF OBJECT_ID('drop_ref_one', 'U') IS NOT NULL drop table drop_ref_one;
IF OBJECT_ID('drop_ref_one_seq', 'SO') IS NOT NULL drop sequence drop_ref_one_seq;
IF OBJECT_ID('"migtest_QuOtEd"', 'U') IS NOT NULL drop table "migtest_QuOtEd";
IF OBJECT_ID('migtest_e_ref', 'U') IS NOT NULL drop table migtest_e_ref;
IF OBJECT_ID('migtest_e_ref_seq', 'SO') IS NOT NULL drop sequence migtest_e_ref_seq;
