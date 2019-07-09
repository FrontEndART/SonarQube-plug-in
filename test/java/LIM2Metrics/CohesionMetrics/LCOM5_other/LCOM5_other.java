package regtest;

class StaticAttrib{
  public static int atrib;
}

class ClassType{
  public int classVar;
}

class MethodClass{
  public void foo_other(){}
}

class VarClass{
  public int otherVar = 0;
}

class ClassTypeObj{
  public ClassType obj;
}

@interface MyAnnotation {
  int foo_Annotation();
  int goo_Annotation();
}

class AnonymClass<T>{}

//LCOM5 = 9
class LCOM5_other{

  public int lcom5Var;

  //LCOM5 = 2
  public AnonymClass anonym = new AnonymClass<Integer>(){
    void foo_Anonym(){
      lcom5Var++;
    }
    void goo_Anonym(){
      lcom5Var++;
    }
  };

  public void foo_call(){
    MethodClass cls = new MethodClass();
    cls.foo_other();
  }
  
  public void goo_call(){
    MethodClass cls = new MethodClass();
    cls.foo_other();
  }
  
  public void foo_other_access(){
    VarClass cls = new VarClass();
    cls.otherVar++;
  }
  
  public void goo_other_access(){
    VarClass cls = new VarClass();
    cls.otherVar++;
  }
  
  public void foo_static_access(){
    StaticAttrib.atrib++;
  }
  
  public void goo_static_access(){
    StaticAttrib.atrib++;
  }
  
  public void foo_other_obj_access(){
    ClassTypeObj clsobj = new ClassTypeObj();
    clsobj.obj.classVar++;
  }
  
  public void goo_other_obj_access(){
    ClassTypeObj clsobj = new ClassTypeObj();;
    clsobj.obj.classVar++;
  }

  public static void main(String[] args){}
}

class CommonByConstructor {

  public CommonByConstructor(int i) {
    f();
    g();
  }

  void g() {}
  void f() {}

}