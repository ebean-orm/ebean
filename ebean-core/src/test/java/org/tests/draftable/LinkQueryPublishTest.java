package org.tests.draftable;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.PagedList;
import io.ebean.Query;
import org.tests.model.draftable.Link;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class LinkQueryPublishTest {

  @Test
  public void testPublishViaQuery() {

    Link link1 = new Link("L1");
    link1.save();

    Link link2 = new Link("L2");
    link2.save();

    Link link3 = new Link("L3");
    link3.save();

    EbeanServer server = Ebean.getDefaultServer();

    List<Object> ids = new ArrayList<>();
    ids.add(link1.getId());
    ids.add(link2.getId());
    ids.add(link3.getId());

    PagedList<Link> pagedList =
      server.find(Link.class).asDraft()
        .where().idIn(ids)
        .setMaxRows(10)
        .findPagedList();

    assertThat(pagedList.getTotalCount()).isEqualTo(3);
    assertThat(pagedList.getList()).hasSize(3);


    Query<Link> pubQuery = server.find(Link.class)
      .where().idIn(ids)
      .order().asc("id");


    List<Link> pubList = server.publish(pubQuery);

    assertThat(pubList).hasSize(3);
    assertThat(pubList).extracting("id").contains(link1.getId(), link2.getId(), link3.getId());

  }


}
