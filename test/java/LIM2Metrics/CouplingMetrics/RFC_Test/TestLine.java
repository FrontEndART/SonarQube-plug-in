// A test driver program for the Line class
public class TestLine {
   public static void main(String[] args) {
      Line l1 = new Line(0, 3, 4, 0);
      System.out.println(l1);   // toString()
      System.out.println(l1.getLength());
      l1.setBeginXY(1, 2);
      l1.setEndXY(3, 4);
      System.out.println(l1);
 
      Point p1 = new Point(3, 0);
      Point p2 = new Point(0, 4);
      Line l2 = new Line(p1, p2);
      System.out.println(l2);
      System.out.println(l2.getLength());
      l2.setBegin(new Point(5, 6));
      l2.setEnd(new Point(7, 8));
      System.out.println(l2);
   }
}