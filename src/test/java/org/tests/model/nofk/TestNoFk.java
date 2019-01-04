package org.tests.model.nofk;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.SqlRow;
import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.persistence.EntityNotFoundException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestNoFk extends BaseTestCase {

  @Before
  public void setup() {
    // Reset t
    Ebean.find(EFileNoFk.class).delete();
    Ebean.find(EUserNoFk.class).delete();
    Ebean.find(EUserNoFkSoftDel.class).delete();

    Ebean.createSqlUpdate("delete from efile_no_fk_euser_no_fk").execute();
    Ebean.createSqlUpdate("delete from efile_no_fk_euser_no_fk_soft_del").execute();

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


    EFileNoFk bash = new EFileNoFk();
    bash.setFileName("bash");
    bash.setOwner(root);
    bash.setOwnerSoftDel(rootSoftDel);

    EUserNoFk user501 = new EUserNoFk();
    EUserNoFkSoftDel user501SoftDel = new EUserNoFkSoftDel();
    user501.setUserId(501);
    user501SoftDel.setUserId(501);
    bash.getEditors().add(root);
    bash.getEditors().add(nobody);
    bash.getEditors().add(user501);
    bash.getEditorsSoftDel().add(rootSoftDel);
    bash.getEditorsSoftDel().add(nobodySoftDel);
    bash.getEditorsSoftDel().add(user501SoftDel);

    Ebean.save(bash);

    // create relation to non existent user
    EFileNoFk cmd = new EFileNoFk();

    cmd.setFileName("java");
    EUserNoFk user500 = new EUserNoFk();
    user500.setUserId(500);
    user500.setUserName("not persisted");
    cmd.setOwner(user500);

    EUserNoFkSoftDel user500SoftDel = new EUserNoFkSoftDel();
    user500SoftDel.setUserId(500);
    user500SoftDel.setUserName("not persisted");
    cmd.setOwnerSoftDel(user500SoftDel);

    Ebean.save(cmd);


    assertThat(Ebean.find(EFileNoFk.class).findCount()).isEqualTo(2);
    assertThat(Ebean.find(EUserNoFk.class).findCount()).isEqualTo(2);
    assertThat(Ebean.find(EUserNoFkSoftDel.class).findCount()).isEqualTo(2);

    SqlRow row = Ebean.createSqlQuery("select count(*) as cnt from efile_no_fk_euser_no_fk").findOne();
    assertThat(row.getInteger("cnt")).isEqualTo(3);
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
    List<EFileNoFk> files = Ebean.find(EFileNoFk.class).findList();
    assertThat(files).hasSize(2);

    EFileNoFk file1 = files.get(0);
    EFileNoFk file2 = files.get(1);

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
    List<EFileNoFk> files = Ebean.find(EFileNoFk.class).fetch("owner").findList();
    assertThat(files).hasSize(2);

    EFileNoFk file1 = files.get(0);
    EFileNoFk file2 = files.get(1);

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
  public void testLazyLoadOwner() {
    List<EUserNoFk> owners = Ebean.find(EUserNoFk.class).findList();
    assertThat(owners).hasSize(2);

    EUserNoFk owner1 = owners.get(0);
    EUserNoFk owner2 = owners.get(1);

    assertThat(owner1.getUserId()).isEqualTo(1);
    assertThat(owner1.getUserName()).isEqualTo("root");
    assertThat(owner2.getUserId()).isEqualTo(2);
    assertThat(owner2.getUserName()).isEqualTo("nobody");

    assertThat(owner1.getFiles()).hasSize(1);
    assertThat(owner2.getFiles()).hasSize(0);

    assertThat(owner1.getFiles().get(0).getFileName()).isEqualTo("bash");

  }

  @Test
  public void testEagerLoadOwner() {
    List<EUserNoFk> owners = Ebean.find(EUserNoFk.class)
        .fetch("files")
        .findList();
    assertThat(owners).hasSize(2);

    EUserNoFk owner1 = owners.get(0);
    EUserNoFk owner2 = owners.get(1);

    assertThat(owner1.getUserId()).isEqualTo(1);
    assertThat(owner1.getUserName()).isEqualTo("root");
    assertThat(owner2.getUserId()).isEqualTo(2);
    assertThat(owner2.getUserName()).isEqualTo("nobody");

    assertThat(owner1.getFiles()).hasSize(1);
    assertThat(owner2.getFiles()).hasSize(0);

    assertThat(owner1.getFiles().get(0).getFileName()).isEqualTo("bash");

  }

  @Test
  public void testLazyLoadFileSoftDel() {
    List<EFileNoFk> files = Ebean.find(EFileNoFk.class).findList();
    assertThat(files).hasSize(2);

    EFileNoFk file1 = files.get(0);
    EFileNoFk file2 = files.get(1);

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
    List<EFileNoFk> files = Ebean.find(EFileNoFk.class).fetch("ownerSoftDel").findList();
    assertThat(files).hasSize(2);

    EFileNoFk file1 = files.get(0);
    EFileNoFk file2 = files.get(1);

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

  @Test
  public void testLazyLoadOwnerSoftDel() {
    List<EUserNoFkSoftDel> owners = Ebean.find(EUserNoFkSoftDel.class).findList();
    assertThat(owners).hasSize(2);

    EUserNoFkSoftDel owner1 = owners.get(0);
    EUserNoFkSoftDel owner2 = owners.get(1);

    assertThat(owner1.getUserId()).isEqualTo(1);
    assertThat(owner1.getUserName()).isEqualTo("root");
    assertThat(owner2.getUserId()).isEqualTo(2);
    assertThat(owner2.getUserName()).isEqualTo("nobody");

    assertThat(owner1.getFiles()).hasSize(1);
    assertThat(owner2.getFiles()).hasSize(0);

    assertThat(owner1.getFiles().get(0).getFileName()).isEqualTo("bash");

  }

  @Test
  public void testEagerLoadOwnerSoftDel() {
    List<EUserNoFkSoftDel> owners = Ebean.find(EUserNoFkSoftDel.class)
        .fetch("files")
        .findList();
    assertThat(owners).hasSize(2);

    EUserNoFkSoftDel owner1 = owners.get(0);
    EUserNoFkSoftDel owner2 = owners.get(1);

    assertThat(owner1.getUserId()).isEqualTo(1);
    assertThat(owner1.getUserName()).isEqualTo("root");
    assertThat(owner2.getUserId()).isEqualTo(2);
    assertThat(owner2.getUserName()).isEqualTo("nobody");

    assertThat(owner1.getFiles()).hasSize(1);
    assertThat(owner2.getFiles()).hasSize(0);

    assertThat(owner1.getFiles().get(0).getFileName()).isEqualTo("bash");

  }


  @Test
  @Ignore("this would be a bonus task :)")
  public void testLazyLoadUser500() {
    // user 500 does not exist in DB
    EUserNoFk owner = Ebean.find(EUserNoFk.class, 500);
    assertThat(owner).isNull();

    // but there are files that are owned by #500
    owner = Ebean.getReference(EUserNoFk.class, 500);
    assertThat(owner.getUserId()).isEqualTo(500);
    // this does not work yet, because the executed select contains a join:
    // select t0.user_id, t1.file_name, t1.owner_user_id
    //   from eowner_no_fk t0
    //   left join efile_no_fk t1 on t1.owner_user_id = t0.user_id
    //   where t0.user_id = ?   order by t0.user_id; --bind(500, )
    //
    // expected select (without join, as t0.user_id is already set and not neccessary to query):
    // select t1.file_name, t1.owner_user_id
    //   from efile_no_fk t1
    //   where t1.owner_user_id = ?; --bind(500, )
    //
    assertThat(owner.getFiles()).hasSize(1);
    assertThat(owner.getFiles().get(0).getFileName()).isEqualTo("java");

  }

  @Test
  @Ignore("Bonus Task 2")
  public void testM2mLazyLoadFile() {
    List<EFileNoFk> files = Ebean.find(EFileNoFk.class).findList();
    assertThat(files).hasSize(2);

    EFileNoFk file1 = files.get(0);
    EFileNoFk file2 = files.get(1);

    assertThat(file1.getFileName()).isEqualTo("bash");
    assertThat(file2.getFileName()).isEqualTo("java");


    // Currently, the test will fail here, because the generated SQL is:
    //
    // select int_.efile_no_fk_file_name, t0.user_id, t0.user_name from euser_no_fk t0
    //   left join efile_no_fk_euser_no_fk int_ on int_.euser_no_fk_user_id = t0.user_id
    //  where (int_.efile_no_fk_file_name) in (?, ?, ?, ?, ? ) ; --bind(Array[5]={bash,java,bash,bash,bash})
    //
    // So as there is no entry in 'euser_no_fk', we also cannot read the m2m table
    assertThat(file1.getEditors()).hasSize(3);
    assertThat(file1.getEditorsSoftDel()).hasSize(3);

    assertThat(file2.getEditors()).isEmpty();
    assertThat(file2.getEditorsSoftDel()).isEmpty();


    assertThat(file2.getEditors().get(2).getUserId()).isEqualTo(501);
    assertThatThrownBy(()->file2.getEditors().get(2).getUserName())
      .isInstanceOf(EntityNotFoundException.class)
      .hasMessageContaining("id:501 - Bean has been deleted");

    assertThat(file2.getEditorsSoftDel().get(2).getUserId()).isEqualTo(501);
    assertThat(file2.getEditorsSoftDel().get(2).isDeleted()).isTrue();
    assertThat(file2.getEditorsSoftDel().get(2).getUserName()).isNull();

  }
}
