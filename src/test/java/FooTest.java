/**
 * Created by rob on 11/02/16.
 */
public class FooTest {//

  interface Colour {
    int RED = 1;
  }

  static class SuperTest {
    int RED = 999;
  }

  static class Test extends SuperTest implements Colour {

    String RED = "RED";

    public static void main(String[] args) {
      new Test().printV();
    }

    void printV() {
      System.out.println(super.RED + " " + this.RED + " " + RED + " " + Colour.RED);
    }
  }

//  static class Test3  {
//    public static void main(String[] args) {
//      new Test().printV();
//    }
//
//    void printV2() {
//      System.out.println(Colour.order);
//    }
//
//  }
}
