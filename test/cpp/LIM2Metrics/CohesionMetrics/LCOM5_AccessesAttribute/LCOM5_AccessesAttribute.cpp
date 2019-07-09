class SuperBase{
public:
    int superBaseVar;
};
    
class Base : public SuperBase{
public:
    int baseVar1;
};

class ClassType{
public:
    int classTypeVar;
};

//LCOM5 = 4
class MyClass : public Base{
    int localVar;
    ClassType localObj;
    
    void foo_localVar(){
        localVar;
    }
    
    void goo_localVar(){
        localVar;
    }
    
    void foo_baseVar(){
        baseVar1;
    }
    
    void goo_baseVar(){
        baseVar1;
    }
    
    void foo_superBaseVar(){
        superBaseVar;
    }
    
    void goo_superBaseVar(){
        superBaseVar;
    }
    
    void foo_objVarAccess(){
        localObj.classTypeVar;
    }
    
    void goo_objVarAccess(){
        localObj.classTypeVar;
    }

};    

int main(){
    return 0;
}