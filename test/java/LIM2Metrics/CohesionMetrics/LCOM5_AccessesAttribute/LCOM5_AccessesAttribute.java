package regtest;

class SuperBase{
  public int superBaseVar;
}

class Base extends SuperBase{
  public int baseVar1;
}

class ClassType{
  public int classTypeVar;
}

//LCOM5 = 5
class LCOM5_AccessesAttribute extends Base{
  public int localVar;
  public ClassType localObj;
    
  public void foo_localVar(){
    localVar++;
  }

  public void goo_localVar(){
    localVar++;
  }

  public void foo_baseVar(){
    baseVar1++;
  }

  public void goo_baseVar(){
    baseVar1++;
  }

  public void foo_superBaseVar(){
    superBaseVar++;
  }

  public void goo_superBaseVar(){
    superBaseVar++;
  }

  public void foo_objVarAccess(){
    localObj.classTypeVar++;
  }

  public void goo_objVarAccess(){
    localObj.classTypeVar++;
  }

  public static void main(String[] args){}
}