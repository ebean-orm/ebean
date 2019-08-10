package org.tests.model.nofk;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Ebean;
import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityNotFoundException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

// test that simulates relations with @formula
public class Test2NoFk extends BaseTestCase {

  @Before
  public void setup() {
    // Reset t
    DB.truncate(EFile2NoFk.class, EUserNoFk.class, EUserNoFkSoftDel.class);
    assertThat(DB.find(EFile2NoFk.class).findCount()).isEqualTo(0);

    // There are two user accounts persisted in our database
    EUserNoFk root = new EUserNoFk();
    root.setUserId(1);
    root.setUserName("root");
    Ebean.save(root);

    EUserNoFk nobody = new EUserNoFk();
    nobody.setUserId(2);
    nobody.setUserName("nobody");
    Ebean.save(nobody);


    // Now build the same with the softDel flag.
    EUserNoFkSoftDel rootSoftDel = new EUserNoFkSoftDel();
    rootSoftDel.setUserId(1);
    rootSoftDel.setUserName("root");
    Ebean.save(rootSoftDel);

    EUserNoFkSoftDel nobodySoftDel = new EUserNoFkSoftDel();
    nobodySoftDel.setUserId(2);
    nobodySoftDel.setUserName("nobody");
    Ebean.save(nobodySoftDel);


    EFile2NoFk bash = new EFile2NoFk();
    bash.setFileName("bash");
    bash.setOwnerId(1);

    EUserNoFk user501 = new EUserNoFk();
    EUserNoFkSoftDel user501SoftDel = new EUserNoFkSoftDel();
    user501.setUserId(501);
    user501SoftDel.setUserId(501);

    Ebean.save(bash);

    // create relation to non existent user
    EFile2NoFk cmd = new EFile2NoFk();

    cmd.setFileName("java");
    cmd.setOwnerId(500);

    Ebean.save(cmd);

    assertThat(Ebean.find(EFile2NoFk.class).findCount()).isEqualTo(2);
    assertThat(Ebean.find(EUserNoFk.class).findCount()).isEqualTo(2);
    assertThat(Ebean.find(EUserNoFkSoftDel.class).findCount()).isEqualTo(2);

    // We should have this data in the database:
    //
    // Owner:   userId | userName    File: fileName | ownerId
    //          =======+=========          =========+========
    //               1 | root                  bash | 1       (= root)
    //               2 | nobody                java | 500     (= non existent user account)
    //
    // Editors: userId | fileName
    //          =======+=========
    //               1 | bash
    //               2 | bash
    //             501 | bash
  }

  @Test
  public void testLazyLoadFile() {
    List<EFile2NoFk> files = Ebean.find(EFile2NoFk.class).findList();
    assertThat(files).hasSize(2);

    EFile2NoFk file1 = files.get(0);
    EFile2NoFk file2 = files.get(1);

    assertThat(file1.getFileName()).isEqualTo("bash");
    assertThat(file2.getFileName()).isEqualTo("java");

    // File1 is owned by "root"
    EntityBeanIntercept ownerEbi = ((EntityBean)file1.getOwner())._ebean_getIntercept();
    assertThat(file1.getOwner().getUserId()).isEqualTo(1);
    assertTrue(ownerEbi.isReference());
    assertTrue(ownerEbi.isPartial());

    assertThat(file1.getOwner().getUserName()).isEqualTo("root"); // trigger lazy-load
    assertFalse(ownerEbi.isReference());
    assertFalse(ownerEbi.isPartial());  // and expect, that bean is fully loaded



    // File2 is owned by user #500, but user account does not exist
    ownerEbi = ((EntityBean)file2.getOwner())._ebean_getIntercept();

    assertThat(file2.getOwner().getUserId()).isEqualTo(500);
    assertThatThrownBy(()->file2.getOwner().getUserName())
      .isInstanceOf(EntityNotFoundException.class)
      .hasMessageContaining("id:500 - Bean has been deleted");

    assertTrue(ownerEbi.isReference());
    assertTrue(ownerEbi.isPartial());
    assertTrue(ownerEbi.isLazyLoadFailure());

  }

  @Test
  public void testEagerLoadFile() {
    List<EFile2NoFk> files = Ebean.find(EFile2NoFk.class).fetch("owner").findList();
    assertThat(files).hasSize(2);

    EFile2NoFk file1 = files.get(0);
    EFile2NoFk file2 = files.get(1);

    assertThat(file1.getFileName()).isEqualTo("bash");
    assertThat(file2.getFileName()).isEqualTo("java");

    // File1 is owned by "root"
    EntityBeanIntercept ownerEbi = ((EntityBean)file1.getOwner())._ebean_getIntercept();
    assertFalse(ownerEbi.isReference());
    assertFalse(ownerEbi.isPartial());

    assertThat(file1.getOwner().getUserId()).isEqualTo(1);
    assertThat(file1.getOwner().getUserName()).isEqualTo("root");

    // File2 is owned by user #500, but user account does not exist
    ownerEbi = ((EntityBean)file2.getOwner())._ebean_getIntercept();

    assertThat(file2.getOwner().getUserId()).isEqualTo(500);
    assertThatThrownBy(()->file2.getOwner().getUserName())
      .isInstanceOf(EntityNotFoundException.class)
      .hasMessageContaining("id:500 - Bean has been deleted");

    assertTrue(ownerEbi.isReference());
    assertTrue(ownerEbi.isPartial());
    assertTrue(ownerEbi.isLazyLoadFailure());

  }

  @Test
  public void testLazyLoadFileSoftDel() {
    List<EFile2NoFk> files = Ebean.find(EFile2NoFk.class).findList();
    assertThat(files).hasSize(2);

    EFile2NoFk file1 = files.get(0);
    EFile2NoFk file2 = files.get(1);

    assertThat(file1.getFileName()).isEqualTo("bash");
    assertThat(file2.getFileName()).isEqualTo("java");

    // File1 is owned by "root"
    EntityBeanIntercept ownerEbi = ((EntityBean)file1.getOwnerSoftDel())._ebean_getIntercept();
    assertThat(file1.getOwnerSoftDel().getUserId()).isEqualTo(1);
    assertTrue(ownerEbi.isReference());
    assertTrue(ownerEbi.isPartial());


    assertThat(file1.getOwnerSoftDel().isDeleted()).isEqualTo(false); // trigger lazy-load
    assertFalse(ownerEbi.isReference());
    assertFalse(ownerEbi.isPartial());  // and expect, that bean is fully loaded

    assertThat(file1.getOwnerSoftDel().getUserName()).isEqualTo("root");


    // File2 is owned by user #500, but user account does not exist
    ownerEbi = ((EntityBean)file2.getOwnerSoftDel())._ebean_getIntercept();

    assertThat(file2.getOwnerSoftDel().getUserId()).isEqualTo(500);
    assertThat(file2.getOwnerSoftDel().isDeleted()).isEqualTo(true);
    assertThat(file2.getOwnerSoftDel().getUserName()).isNull();

    assertFalse(ownerEbi.isReference());
    assertFalse(ownerEbi.isPartial());

  }

  @Test
  public void testEagerLoadFileSoftDel() {
    List<EFile2NoFk> files = Ebean.find(EFile2NoFk.class).fetch("ownerSoftDel").findList();
    assertThat(files).hasSize(2);

    EFile2NoFk file1 = files.get(0);
    EFile2NoFk file2 = files.get(1);

    assertThat(file1.getFileName()).isEqualTo("bash");
    assertThat(file2.getFileName()).isEqualTo("java");

    // File1 is owned by "root"
    EntityBeanIntercept ownerEbi = ((EntityBean)file1.getOwnerSoftDel())._ebean_getIntercept();
    assertFalse(ownerEbi.isReference());
    assertFalse(ownerEbi.isPartial());

    assertThat(file1.getOwnerSoftDel().getUserId()).isEqualTo(1);
    assertThat(file1.getOwnerSoftDel().isDeleted()).isEqualTo(false);
    assertThat(file1.getOwnerSoftDel().getUserName()).isEqualTo("root");

    // File2 is owned by user #500, but user account does not exist
    ownerEbi = ((EntityBean)file2.getOwnerSoftDel())._ebean_getIntercept();
//    assertFalse(ownerEbi.isReference());
//    assertFalse(ownerEbi.isPartial());

    assertThat(file2.getOwnerSoftDel().getUserId()).isEqualTo(500);
    assertThat(file2.getOwnerSoftDel().isDeleted()).isEqualTo(true);
    assertThat(file2.getOwnerSoftDel().getUserName()).isNull();


  }

}
