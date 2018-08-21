//LCOM5 = 1
class SuperBase{
public:
    void superBaseMethod(){}
};
//LCOM5 = 1
class Base : public SuperBase{
public:
    void baseMethod(){}
};

//LCOM5 = 4
class MyClass : public Base{
    
    int localMethod(){}
    
    void foo_localMethod(){
        localMethod();
    }
    
    void goo_localMethod(){
        localMethod();
    }
    
    void foo_baseMethod(){
        baseMethod();
    }
    
    void goo_baseMethod(){
        baseMethod();
    }
    
    void foo_superBaseMethod(){
        superBaseMethod();
    }
    
    void goo_superBaseMethod(){
        superBaseMethod();
    }
    
    void foo_call(){
        goo_call();
    }
    
    void goo_call(){
        foo_call();
    }
};    

int main(){
    return 0;
}