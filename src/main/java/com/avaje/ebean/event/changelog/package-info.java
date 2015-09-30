/**
 * Provides a built in change log mechanism and can audit changes.
 * <p>
 * Although you can build something similar using existing event adapters such as <code>BeanPersistController</code>
 * this is built for purpose to reduce the effort and provide optimal performance for auditing or logging changes.
 * </p>
 * <p>
 * By default you can annotate beans with <code>@ChangeLog</code> and associated change events are logged by default
 * in a JSON form with appropriate auditing attributes such as who made the changes and ip address of the user etc
 * via implementation of <code>ChangeLogPrepare</code>
 * </p>
 */
package com.avaje.ebean.event.changelog;