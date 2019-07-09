public class MainClass {
  static int m(Integer v) {
    return v ; // auto-unbox to int
  } 

  public static void main(String args[]) {
    Integer iOb = m(100);

    System.out.println(iOb);
  }
}