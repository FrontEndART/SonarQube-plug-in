
class StaticAttrib{
public:
    static int atrib;
};

class ClassType{
public:
    int classVar;
};

void foo_global(){}
unsigned globalVar = 0;
int StaticAttrib::atrib;
ClassType obj;

//LCOM5 = 8
class MyClass{
public:
    void foo_call(){
        foo_global();
    }
    
    void goo_call(){
        foo_global();
    }
    
    void foo_global_access(){
        globalVar;
    }
    
    void goo_global_access(){
        globalVar;
    }
    
    void foo_static_access(){
        StaticAttrib::atrib;
    }
    
    void goo_static_access(){
        StaticAttrib::atrib;
    }
    
    void foo_global_obj_access(){
        obj.classVar;
    }
    
    void goo_global_obj_access(){
        obj.classVar;
    }
};

int main(){
    return 0;
}