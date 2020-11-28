package org.tests.model.basic;

  public class CustDto {

    final Integer id;

    final String name;

    int totalOrders;

    public CustDto(Integer id, String name) {
      this.id = id;
      this.name = name;
    }

    @Override
    public String toString() {
      return "id:" + id + " name:" + name + " totalOrders:" + totalOrders;
    }

    public Integer getId() {
      return id;
    }

    public String getName() {
      return name;
    }

    public int getTotalOrders() {
      return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
      this.totalOrders = totalOrders;
    }
  }
