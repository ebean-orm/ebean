package misc.migration.v1_1;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import io.ebean.annotation.DbComment;
import io.ebean.annotation.History;
import io.ebean.annotation.Index;

@Table(name = "`table`")
@Entity
@History // FIXME: remove later
public class ETable {

  @Column(name = "`index`")
  @DbComment("this is an other comment")
  @Id
  private String index;

  @Column(name = "`from`")
  @Index
  private String from;

  @Column(name = "`to`")
  @Index(unique = true)
  private String to;

  @Column(name = "`varchar`")
  @Index(unique = true)
  private String varchar;

  @Column(name = "`select`")
  @Index(unique = true)
  private String select;

  @ManyToOne
  @JoinColumn(name = "`foreign`")
  ETable foreign;

  @OneToMany(mappedBy = "foreign")
  List<ETable> foreigns;
}
