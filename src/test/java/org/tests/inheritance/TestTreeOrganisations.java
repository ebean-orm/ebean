package org.tests.inheritance;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestTreeOrganisations extends BaseTestCase {

  @Test
  public void test() {

    OrganizationTreeNode treeNode = new  OrganizationTreeNode();
    OrganizationNode node = new OrganizationUnit();
    
    treeNode.setOrganizationNode(node);
    Ebean.save(treeNode);

    treeNode = Ebean.find(OrganizationTreeNode.class, treeNode.getId());
    assertEquals(node, treeNode.getOrganizationNode());

  }
}
