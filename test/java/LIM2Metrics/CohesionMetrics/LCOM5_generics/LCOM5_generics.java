package regtest;

class Base{
  public int baseVar;
}

class BaseTemplate<T>{
  public T baseTemplasteVar;
}

//LCOM5 = 1
class MyClass<T>{
  public int var1;
  public void foo_MyClass(){
    var1++;
  }
  public void goo_MyClass(){
    var1++;
  }
}

//LCOM5 = 1
class MyClass2<T> extends Base{
  public void foo_MyClass2(){
    baseVar++;
  }
  public void goo_MyClass2(){
    baseVar++;
  }
}

//LCOM5 = 1
class MyClass3<T> extends BaseTemplate<T>{
  public void foo_MyClass3(){
    T var = this.baseTemplasteVar;
  }
  public void goo_MyClass3(){
    T var = this.baseTemplasteVar;
  }
}

//LCOM5 = 1
class MyClass4<T> extends BaseTemplate<Integer>{
  public void foo_MyClas4(){
    baseTemplasteVar++;
  }
  public void goo_MyClas4(){
    baseTemplasteVar++;
  }
}

//LCOM5 = 1
class MyClass5{
  public int var2;
  public <T> void foo_MyClass5(){
    var2++;
  }
  public void goo_MyClass5(){
    var2++;
  }
}

//LCOM5 = 1
class MyClass6<T>{
  public T var3;
  public <L> void foo_MyClass6(){
    T var = var3;
  }
  public void goo_MyClass6(){
    T var = var3;
  }
}

//LCOM5 = 1
class MyClass7<T extends Base>{
  public short var5;
  public void foo_MyClass7(){
      var5++;
  }
  public void goo_MyClass7(){
      var5++;
  }
}

//LCOM5 = 1
enum Enum {
  VAR;
  private int attrib;
  
  public void foo(){
    attrib++;
  }
  
  public void goo(){
    attrib++;
  }

}

abstract class AnonymClass<T>{}

//LCOM5 = 1
class LCOM5_generics extends Base{
  
  //LCOM5 = 1
  AnonymClass anonym = new AnonymClass<Integer>(){
    public int anonymVar;
    void foo_Anonym(){
      anonymVar++;
    }
    void goo_Anonym(){
      anonymVar++;
    }
  };
  
  public static void main(String[] args){}
}