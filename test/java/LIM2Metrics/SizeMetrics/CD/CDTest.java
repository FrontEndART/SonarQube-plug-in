package regtest;
/*
*   Documentation comment
*/
public class CDTest 
{
	String longString = "some long text";
	
	// comment for goo
    public 
    static
    void goo
    (
    ) 
    {
    }
    
    public static void foo() {
    	// comment line
    	// comment line
    }
    
    public class AClass {
		// comment for size attribute
       private 
       final 
       int 
       size
       = 
       20
       ;
	   /*
	   *	comment lines
	   */
	   private int height
	   =
	   10
	   ;
    }
    
    public class Box
    <T> //< comment
    {
        // T stands for "Type"
        private T t;

        public void set(T t) { 
        	this.t = t
        	; 
        }
        public T get() { return t; }
    }
    
    public static void main(String [] args) {}
}