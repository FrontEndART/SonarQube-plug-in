package regtest;

class StaticInit {
	public static int init() {
		return 28;
	}
}

public class Child extends Base{   // RFC : 6

  public static final int CLASS_CONST = StaticInit.init();

  protected int a_Data;
  private void foo(){
    /*...*/
  }
  public void func(){
	  
	  goo();
  }
  public void bar() {
	  
  }
public static void main (String[] args) {}
}
