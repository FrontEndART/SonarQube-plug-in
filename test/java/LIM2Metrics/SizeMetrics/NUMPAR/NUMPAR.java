
public class NUMPAR {
    public void draw(String s) {  // NUMPAR:1
        /*...*/
    }
    public void draw(int i) {  // NUMPAR:1
        /*...*/
    }
    public void draw(double f) {  // NUMPAR:1
        /*...*/
    }
    public void draw(int i, double f) {  // NUMPAR:2
        /*...*/
    }
	public int bar () {  // NUMPAR:0
		return 0;
	}
	public void foo(int ... a) {} // NUMPAR: 0
	public static void main(String [] args)
	{
		/*...*/
	}
}