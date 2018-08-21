
public class Simple { // NII: 2 (fromArraToCollection, main)
    private int k;
    private Simple(){} // NII: 0 (az inicializalas nem szamolodik)
    public void bar() {} //NII : 1 (fromArrayToCollection
	protected int retOne(){ return 1;} //NII: 1 main

	public <T> void fromArrayToCollection(T[] a) { //NII: 1 (main)
	    for (T o : a) {
	       /* ... */
	    	bar();
	    }
	}
	
	public void recursion() {
		recursion();
	}
	
	public static void main(String [] args) { //NII: 0
		Simple simple = new Simple();
		String[] sa = new String[100];
		simple.fromArrayToCollection(sa);
		simple.retOne();
	}

}
