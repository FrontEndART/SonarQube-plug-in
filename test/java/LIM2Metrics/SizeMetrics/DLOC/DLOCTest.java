// doc comment
public class DLOCTest {
// just a simple comment 
	public
	/*
	 * the goo function 
	 * documentation comment, DLOC 4
	*/
	 void goo(){}

	/*
	*
	*
	* documentation for B class, szamit a DLOC-hoz
	* DLOC : 8
	*/
    protected class B { 
	  // konstruktor comment, szamit a DLOC-hoz
	  protected B(){};
	  // attribute comment, szamit a DLOC-hoz
	  int a;
	  // lokalis class komment, nem szamit a DLOC-hoz
	  class C{};
	}
// just another comment inside of class A

    public static void main(String [] args) {}
}