package org.tests.inheritance;

import io.ebean.BaseTestCase;
import io.ebean.DB;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestTreeOrganisations extends BaseTestCase {

  @Test
  public void test() {

    OrganizationTreeNode treeNode = new  OrganizationTreeNode();
    treeNode.setName("tree");

    OrganizationUnit node = new OrganizationUnit();
    node.setTitle("node");

    treeNode.setOrganizationNode(node);
    DB.save(treeNode);

    treeNode = DB.find(OrganizationTreeNode.class, treeNode.getId());
    assertEquals(node, treeNode.getOrganizationNode());

  }
}
