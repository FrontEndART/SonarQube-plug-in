package regtest;

//LCOM5 = 1
class SuperBase{
  public void superBaseMethod(){}
}
//LCOM5 = 1
class Base extends SuperBase{
  public void baseMethod(){}
}

//LCOM5 = 5
class LCOM5_MethodCall extends Base{ 
  public void localMethod(){}
    
  public void foo_localMethod(){
    localMethod();
  }
  
  public void goo_localMethod(){
    localMethod();
  }
  
  public void foo_baseMethod(){
    baseMethod();
  }
  
  public void goo_baseMethod(){
    baseMethod();
  }
  
  public void foo_superBaseMethod(){
    superBaseMethod();
  }
  
  public void goo_superBaseMethod(){
    superBaseMethod();
  }
  
  public void foo_call(){
    goo_call();
  }
  
  public void goo_call(){
    foo_call();
  }

  public static void main(String[] args){}
}