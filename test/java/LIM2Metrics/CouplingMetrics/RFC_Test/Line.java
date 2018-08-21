// The Line class definition
public class Line {
   // Private member variables
   Point begin, end;   // Declare begin and end as instances of Point
 
   // Constructors
   public Line(int x1, int y1, int x2, int y2) {
      begin = new Point(x1, y1);  // Construct Point instances
      end   = new Point(x2, y2);
   }
   public Line(Point begin, Point end) {
      this.begin = begin;  // Caller constructed Point instances
      this.end   = end;
   }
 
   // Public getter and setter for private variables
   public Point getBegin() {
      return begin;
   }
   public Point getEnd() {
      return end;
   }
   public void setBegin(Point begin) {
      this.begin = begin;
   }
   public void setEnd(Point end) {
      this.end = end;
   }
 
   public int getBeginX() {
      return begin.getX();
   }
   public void setBeginX(int x) {
      begin.setX(x);
   }
   public int getBeginY() {
      return begin.getY();
   }
   public void setBeginY(int y) {
      begin.setY(y);
   }
   public void setBeginXY(int x, int y) {
      begin.setX(x);
      begin.setY(y);
   }
   public int getEndX() {
      return end.getX();
   }
   public void setEndX(int x) {
      end.setX(x);
   }
   public int getEndY() {
      return end.getY();
   }
   public void setEndY(int y) {
      end.setY(y);
   }
   public void setEndXY(int x, int y) {
      end.setX(x);
      end.setY(y);
   }
 
   public String toString() {
      return "Line from " + begin + " to " + end;
   }
 
   public double getLength() {
      int xDiff = begin.getX() - end.getX();
      int yDiff = begin.getY() - end.getY();
      return Math.sqrt(xDiff*xDiff + yDiff*yDiff);
   }
}