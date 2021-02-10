package io.ebean.plugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class JunkMain {

  public static void main(String[] args) {

    new JunkMain().run();
  }

  long ac;
  long sc;

  Set<String> myset;
  List<String> myarray;

  private void run() {
    init();
    run0();
    run1();
    run0();
    run1();
    run0();
    run1();
    run0();
    run1();
    run0();
    run1();
    run0();
    run1();
  }
  private void init() {
    //"asdkasd","sdf","asdsdasd","dgdfg",
    String[] sa = new String[]{"sdfsf","sdfsdfsdf"};//,"sdfsdffs","sdfsdf","xcsdfsdf","b","c","d","e"};

    myset = new LinkedHashSet<>();
    myarray = new ArrayList<>();
    for (String val : sa) {
      myarray.add(val);
      myset.add(val);
    }
  }

  private void run0() {
    final long start = System.nanoTime();
    for (int i = 0; i < 100_000_000; i++) {
      arrayContains("sdfsdfsdf");
    }
    long exe = System.nanoTime() - start;
    System.out.println("array exe: "+exe+"  "+ac);
  }

  private void run1() {
    final long start = System.nanoTime();
    for (int i = 0; i < 10_000_000; i++) {
      setContains("sdfsdfsdf");
    }
    long exe = System.nanoTime() - start;
    System.out.println("set   exe: "+exe+"  "+sc);
  }

  public void arrayContains(String val) {
    if (myarray.contains(val)) {
      ac++;
    }
  }

  public void setContains(String val) {
    if (myset.contains(val)) {
      sc++;
    }
  }
}
