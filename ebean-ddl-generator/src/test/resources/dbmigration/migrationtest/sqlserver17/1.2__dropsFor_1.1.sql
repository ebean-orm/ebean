-- Migrationscripts for ebean unittest
-- apply changes
EXEC usp_ebean_drop_column migtest_e_basic, old_boolean;

EXEC usp_ebean_drop_column migtest_e_basic, old_boolean2;

EXEC usp_ebean_drop_column migtest_e_basic, eref_id;

EXEC usp_ebean_drop_column migtest_e_history2, obsolete_string1;

EXEC usp_ebean_drop_column migtest_e_history2, obsolete_string2;

IF OBJECT_ID('migtest_e_ref', 'U') IS NOT NULL drop table migtest_e_ref;
IF OBJECT_ID('migtest_e_ref_seq', 'SO') IS NOT NULL drop sequence migtest_e_ref_seq;
