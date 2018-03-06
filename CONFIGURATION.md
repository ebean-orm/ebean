Overview of ebean.properties file
=================================


### DbMigration options

You can set individual values for different platforms when generating migrations

`dbmigration.platform.<PLATFORM>.databaseSequenceBatchSize`
For DB's using sequences this is the number of sequence values prefetched.
 
`dbmigration.platform.<PLATFORM>.dbuuid`
Control, how UUID generation should work - it affects DDL which column type is generated. Possible values:
- BINARY enforces binary UUID 
- VARCHAR enforces varchar UUID
- BINARY_OPTIMIZED enforces binary-optimized UUID (makes sense only with Type1 ID)
- AUTO_BINARY use binary when platform does not support native UUID
- AUTO_BINARY_OPTIMIZED use binary-optimized when platform does not support native UUID
- AUTO_VARCHAR (default) use varchar when platform does not support UUID 

`dbmigration.platform.<PLATFORM>.uuidStoreAsBinary`
Same as setting dbuuid to BINARY

`dbmigration.platform.<PLATFORM>.geometrySRID`
The Geometry SRID value (default 4326).

`dbmigration.platform.<PLATFORM>.idType`
The ID type (IDENTITY, SEQUENCE, GENERATOR, EXTERNAL)

`dbmigration.platform.<PLATFORM>.mapping`
Adjust the mapping. For example `BOOLEAN=integer(32);BIT=tinyint(3)`


`ebean.migration.applyPrefix`
Set this to "V" to be compatible with FlywayDB.

`ebean.migration.applySuffix`
Defaults to '.sql'

`ebean.migration.dbusername`
DB user used to run the DB migration.

`ebean.migration.dbpassword`
DB password used to run the DB migration.

`ebean.migration.generate`
Set to true if the DB migration should be generated on server start.

`ebean.migration.generatePendingDrop`
The version of a pending drop that should be generated as the next migration.

`ebean.migration.includeGeneratedFileComment`
TODO

`ebean.migration.metaTable`
For running migration the DB table that holds migration execution status. Default 'db_migration'

`ebean.migration.migrationPath`
Resource path for the migration xml and sql.

`ebean.migration.modelPath`
Subdirectory the model xml files go into. Default 'model'

`ebean.migration.modelSuffix`
Suffix. Default '.model.xml' 

`ebean.migration.name`
Description text that can be appended to the version to become the ddl script file name
 
`ebean.migration.patchInsertOn`
migration versions that should be added to history without running.

`ebean.migration.patchResetChecksumOn`
migration versions that should have their checksum reset and not run.
Use this if you get a 'Checksum mismatch' error.

`ebean.migration.placeholders`
A comma and equals delimited placeholders that are substituted in SQL scripts when running migration (used by DB Migration runner only).

`ebean.migration.platform`
The database platform to generate migration DDL for.

`ebean.migration.run`
Flag set to true means to run any outstanding migrations on startup.

`ebean.migration.version`
The migration version name (typically FlywayDb compatible). Example: 1.1.1_2


### Ebean UUID options

`ebean.uuidVersion`
Controls, how the UUIDs are generated. Possible values
- VERSION4 (default) generate random V4 UUIDs,
- VERSION1 generate rfc4122 compliant Type 1 UUIDs (requires a state file)
- VERSION1RND generate fake Type 1 UUIDs

Note, that V1 UUIDs in conjunction with AUTO_BINARY_OPTIMIZED will give you the best index performance, but you MUST understand how this works to avoid collisions.

`ebean.uuidStateFile`
The state file that is Required to generate V1 UUIDs


### DocStoreConfig

`ebean.docstore.active`
True when the Document store integration is active/on.

`ebean.docstore.allowAllCertificates`
Set to true such that the client allows connections to invalid/self signed SSL certificates.

`ebean.docstore.bulkBatchSize`
The default batch size to use for the Bulk API calls.

`ebean.docstore.create`
When true the Document store should create any document indexes that don't already exist.

`ebean.docstore.dropCreate`
When true the Document store should drop and re-create document indexes.

`ebean.docstore.generateMapping`
Set to true means Ebean will generate mapping files on startup.

`ebean.docstore.mappingPath`
Resource path for the Document store mapping files.

`ebean.docstore.mappingSuffix`
Suffix used for mapping files.

`ebean.docstore.password`
 Password credential that be used for authentication to document store.
 
`ebean.docstore.pathToResources`
Location of resources that mapping files are generated into.

`ebean.docstore.persist`
The default mode used by indexes. `DEFAULT|IGNORE|QUEUE|UPDATE`

`ebean.docstore.url`
The URL of the Document store server. For example: http://localhost:9200.

`ebean.docstore.username`
Credential that be used for authentication to document store.

### Ebean Autotune

`ebean.autoTune.mode`
The autoTune mode `DEFAULT_OFF|DEFAULT_ON|DEFAULT_ONIFEMPTY`

`ebean.autoTune.profiling`
TODO

`ebean.autoTune.profilingBase`
TODO

`ebean.autoTune.profilingFile`
TODO

`ebean.autoTune.profilingRate`
TODO

`ebean.autoTune.profilingUpdateFrequency`
TODO

`ebean.autoTune.queryTuning`
TODO

`ebean.autoTune.queryTuningAddVersion`
TODO

`ebean.autoTune.queryTuningFile`
TODO

`ebean.autoTune.skipGarbageCollectionOnShutdown`
TODO

`ebean.autoTune.skipProfileReportingOnShutdown`
TODO


### Ebean options
`ebean.allQuotedIdentifiers`
Quote all identifiers

`ebean.asOfSysPeriod`
Column used to support history and 'As of' queries. This column is a timestamp range or equivalent.

`ebean.asOfViewSuffix`
Suffix appended to the base table to derive the view that contains the union of the base table and the history table in order to support asOf queries.

`ebean.autoCommitMode`
Set to true if the DataSource uses autoCommit. Indicates that Ebean should use autoCommit friendly Transactions and TransactionManager.

`ebean.autoReadOnlyDataSource`
When true create a read only DataSource using readOnlyDataSourceConfig defaulting values from dataSourceConfig

`ebean.autostart`
Should the server start all

`ebean.backgroundExecutorSchedulePoolSize`
TODO

`ebean.backgroundExecutorShutdownSecs`
TODO

and some more todos:
`ebean.batch.mode`
`ebean.batch.size`
`ebean.changeLogAsync`
`ebean.changeLogIncludeInserts`
`ebean.classes`
`ebean.collectQueryOrigins`
`ebean.collectQueryStatsByNode`
`ebean.currentUserProvider`
`ebean.dataSourceJndiName`
`ebean.dataTimeZone`
`ebean.databaseBooleanFalse`
`ebean.databaseBooleanTrue`
`ebean.databasePlatform`
`ebean.databasePlatformName`
`ebean.databaseSequenceBatchSize`
ebean.datasource.h2.adminpassword
ebean.datasource.h2.adminusername
`ebean.dbEncrypt`
`ebean.dbOffline`
ebean.ddl.createOnly
ebean.ddl.generate
ebean.ddl.header
ebean.ddl.initSql
ebean.ddl.run
ebean.ddl.seedSql
ebean.defaultDeleteMissingChildren
ebean.defaultOrderById
ebean.disableClasspathSearch
ebean.disableL2Cache
ebean.docStoreOnly
ebean.docstore.active
ebean.docstore.allowAllCertificates
ebean.docstore.bulkBatchSize
ebean.docstore.create
ebean.docstore.dropCreate
ebean.docstore.generateMapping
ebean.docstore.mappingPath
ebean.docstore.mappingSuffix
ebean.docstore.password
ebean.docstore.pathToResources
ebean.docstore.persist
ebean.docstore.url
ebean.docstore.username
ebean.encryptDeployManager
ebean.encryptKeyManager
ebean.encryptor
ebean.explicitTransactionBeginMode
ebean.expressionEqualsWithNullAsNoop
ebean.expressionNativeIlike
ebean.geometrySRID
ebean.historyTableSuffix
ebean.jdbcFetchSizeFindEach
ebean.jdbcFetchSizeFindList
ebean.jodaLocalTimeMode
ebean.jsonDateTime
ebean.jsonInclude
ebean.lazyLoadBatchSize
ebean.localTimeWithNanos
ebean.namingConvention.schema
ebean.namingConvention.sequenceFormat
ebean.namingConvention.useForeignKeyPrefix
ebean.namingconvention
ebean.notifyL2CacheInForeground
ebean.packages
ebean.persistBatch
ebean.persistBatchOnCascade
ebean.persistBatchSize
ebean.persistBatching
ebean.persistenceContextScope
ebean.profiling
ebean.profiling.directory
ebean.profiling.includeProfileIds
ebean.profiling.minimumMicros
ebean.profiling.profilesPerFile
ebean.profiling.verbose
ebean.queryBatchSize
ebean.queryPlanTTLSeconds
ebean.search.packages
ebean.serverCachePlugin
ebean.skipCacheAfterWrite
ebean.slowQueryMillis
ebean.tenant.catalogProvider
ebean.tenant.currentTenantProvider
ebean.tenant.mode
ebean.tenant.partitionColumn
ebean.tenant.schemaProvider
ebean.updateAllPropertiesInBatch
ebean.updateChangesOnly
ebean.updatesDeleteMissingChildren
ebean.useJavaxValidationNotNull
ebean.useJtaTransactionManager

