// The Point class definition
public class Point {
   // Private member variables
   private int x, y;   // (x, y) co-ordinates
 
   // Constructors
   public Point(int x, int y) {
      this.x = x;
      this.y = y;
   }
   public Point() {    // default (no-arg) constructor
      x = 0;
      y = 0;
   }
 
   // Public getter and setter for private variables
   public int getX() { 
      return x; 
   }
   public void setX(int x) { 
      this.x = x; 
   }
   public int getY() { 
      return y; 
   }
   public void setY(int y) { 
      this.y = y; 
   }
 
   // toString() to describe itself
   public String toString() { 
      return "(" + x + "," + y + ")"; 
   }
}