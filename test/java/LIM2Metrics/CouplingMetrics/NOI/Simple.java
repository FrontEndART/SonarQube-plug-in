

public class Simple  {// NOI:  3

	  public nestedClass  n = new nestedClass(); //NOI: 0
	  int total() {  // NOI: 0
		   int a_value = 3+3;
		   return a_value;
	  
	  }
	  void bar (int ... a) {  //NOI : 0
		  
	  }
	  
	  protected static class nestedClass {
		  void foo(){}  // NOI: 0
		  
		  
	  }
	  
  
  
  public static void main (String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException {  // NOI: 3
	  Simple test1 = new Simple();
	  //MyMethods test1 = new MyMethods();
	  test1.total();
	  int a=0, b=1, c=2;
	  test1.bar(a,b,c);
	//  test1.n.foo();
	  ((nestedClass)Class.forName("Simple.nestedClass").newInstance()).foo();
	  
	  
  }
}