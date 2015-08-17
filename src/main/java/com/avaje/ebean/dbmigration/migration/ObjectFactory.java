
package com.avaje.ebean.dbmigration.migration;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.avaje.ebean.dbmigration.migration package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.avaje.ebean.dbmigration.migration
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Rollback }
     * 
     */
    public Rollback createRollback() {
        return new Rollback();
    }

    /**
     * Create an instance of {@link AddColumn }
     * 
     */
    public AddColumn createAddColumn() {
        return new AddColumn();
    }

    /**
     * Create an instance of {@link Column }
     * 
     */
    public Column createColumn() {
        return new Column();
    }

    /**
     * Create an instance of {@link CreateTable }
     * 
     */
    public CreateTable createCreateTable() {
        return new CreateTable();
    }

    /**
     * Create an instance of {@link UniqueConstraint }
     * 
     */
    public UniqueConstraint createUniqueConstraint() {
        return new UniqueConstraint();
    }

    /**
     * Create an instance of {@link ForeignKey }
     * 
     */
    public ForeignKey createForeignKey() {
        return new ForeignKey();
    }

    /**
     * Create an instance of {@link Apply }
     * 
     */
    public Apply createApply() {
        return new Apply();
    }

    /**
     * Create an instance of {@link Configuration }
     * 
     */
    public Configuration createConfiguration() {
        return new Configuration();
    }

    /**
     * Create an instance of {@link DefaultTablespace }
     * 
     */
    public DefaultTablespace createDefaultTablespace() {
        return new DefaultTablespace();
    }

    /**
     * Create an instance of {@link RenameTable }
     * 
     */
    public RenameTable createRenameTable() {
        return new RenameTable();
    }

    /**
     * Create an instance of {@link DropHistoryTable }
     * 
     */
    public DropHistoryTable createDropHistoryTable() {
        return new DropHistoryTable();
    }

    /**
     * Create an instance of {@link AlterColumn }
     * 
     */
    public AlterColumn createAlterColumn() {
        return new AlterColumn();
    }

    /**
     * Create an instance of {@link DropColumn }
     * 
     */
    public DropColumn createDropColumn() {
        return new DropColumn();
    }

    /**
     * Create an instance of {@link CreateIndex }
     * 
     */
    public CreateIndex createCreateIndex() {
        return new CreateIndex();
    }

    /**
     * Create an instance of {@link ChangeSet }
     * 
     */
    public ChangeSet createChangeSet() {
        return new ChangeSet();
    }

    /**
     * Create an instance of {@link Sql }
     * 
     */
    public Sql createSql() {
        return new Sql();
    }

    /**
     * Create an instance of {@link DropTable }
     * 
     */
    public DropTable createDropTable() {
        return new DropTable();
    }

    /**
     * Create an instance of {@link AddHistoryTable }
     * 
     */
    public AddHistoryTable createAddHistoryTable() {
        return new AddHistoryTable();
    }

    /**
     * Create an instance of {@link RenameColumn }
     * 
     */
    public RenameColumn createRenameColumn() {
        return new RenameColumn();
    }

    /**
     * Create an instance of {@link DropIndex }
     * 
     */
    public DropIndex createDropIndex() {
        return new DropIndex();
    }

    /**
     * Create an instance of {@link AlterHistoryTable }
     * 
     */
    public AlterHistoryTable createAlterHistoryTable() {
        return new AlterHistoryTable();
    }

    /**
     * Create an instance of {@link Migration }
     * 
     */
    public Migration createMigration() {
        return new Migration();
    }

}
