class Base{};

class AttributeCallsClass{
public:
    static int initAttribCall(){
        return int();
    }
};

class CallsClass{
public:
    static void callMethod(){}
};

class AccessesAttributeClass{
public:
    static int attribute;
};

class HasTypeClass{};

class TypeClass{
public:
    int initAttrib;
};

class AttribInitClass{
public:
    static int attribInit(){
        return int();
    }
};

class InstantiatesClass{};

class ReturnsClass{};

class ThrowsClass{};

class CanThrowClass{};

class UsesClass{};

class ParameterClass{};

class HasArgumentsClass{};

template<class T>
class MyTemplateClass{};

template<>
class MyTemplateClass<int>{};

class ClassUsesClass{};

//CBO = 17
class MyClass : public Base{
    int selfVar;
    int typeClsAttrib;
    HasTypeClass attrib;
    static int attribCalls;
    typedef ClassUsesClass classUses;
    
    MyClass() : selfVar(), typeClsAttrib(AttribInitClass::attribInit()), attrib()
    {}
    
    void selfReference(){
        selfVar;
    }
    ReturnsClass foo(){
        CallsClass::callMethod();
        AccessesAttributeClass::attribute;
        InstantiatesClass instAttrib;
        
        TypeClass typeClsAttrib = {AttribInitClass::attribInit()};
        
        UsesClass();
        
        return ReturnsClass();
    }
    
    void throwMethod() throw(CanThrowClass) {
        throw ThrowsClass();
    }
    
    void ParameterMethod(ParameterClass param){}
    
    void methodGeneric(MyTemplateClass<HasArgumentsClass> ProtoParam, MyTemplateClass<int> specParam){}


};

int MyClass::attribCalls = AttributeCallsClass::initAttribCall();

int main(){
    return 0;
}