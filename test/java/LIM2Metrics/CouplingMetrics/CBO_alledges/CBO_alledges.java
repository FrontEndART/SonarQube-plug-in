package regtest;

class Base<T>{}

class CallsClass{
  public static void callMethod(){}
}

class AccessesAttributeClass{
  public static int attribute;
}

class HasTypeClass{}

class AttribInitClass{
  public static Integer attribInit(){
    return 0;
  }
}

class InstantiatesClass{}

class ReturnsClass{}

class ThrowsClass extends Exception{}

class CanThrowClass extends Exception{}

class UsesClass{}

class ParameterClass{}

class HasArgumentsClass{}

class MyTemplateClass<T>{}

class ClassUsesClass{}

class ConstraintClass{}

class TypeArgumentConstraintClass<T>{}

//CBO = 16
class MyClass extends Base<ClassUsesClass>{
  int selfVar;
  Integer typeClsAttrib;
  HasTypeClass attrib;
  static int attribCalls;
  TypeArgumentConstraintClass<? extends ConstraintClass> typeargVar;
  
  MyClass(){
    typeClsAttrib = AttribInitClass.attribInit();
  }
  
  void selfReference(){
    selfVar++;
  }
  
  ReturnsClass foo(){
    CallsClass.callMethod();
    AccessesAttributeClass.attribute++;
    InstantiatesClass instAttrib;
    
    Object obj = new UsesClass();
    
    return new ReturnsClass();
  }
  
  void canThrowMethod() throws CanThrowClass {
  }
  
  void throwMethod(){
    try{
      throw new ThrowsClass();
    }
    catch(ThrowsClass e){}
  }
  
  void ParameterMethod(ParameterClass param){}
  
  void methodGeneric(MyTemplateClass<HasArgumentsClass> protoParam){}

  public static void main(String[] args){}
}