class Base{
public:
    int baseVar;

};
template<class T>
class BaseTemplate{
public:
    T baseTemplasteVar;

};
//LCOM5 = 1
template<class T>
class MyClass{
    int var1;
    void foo_MyClass(){
        var1;
    }
    void goo_MyClass(){
        var1;
    }
};
//LCOM5 = 1
template<class T>
class MyClass2 : public Base{
    void foo_MyClass2(){
        baseVar;
    }
    void goo_MyClass2(){
        baseVar;
    }
};
//LCOM5 = 1
template<class T>
class MyClass3 : public BaseTemplate<T>{
    void foo_MyClass3(){
        this->baseTemplasteVar;
    }
    void goo_MyClass3(){
        this->baseTemplasteVar;
    }
};
//LCOM5 = 2
template<class T>
class MyClass4 : public BaseTemplate<int>{
    void foo_MyClas4(){
        this->baseTemplasteVar;
    }
    void goo_MyClas4(){
        this->baseTemplasteVar;
    }
};
//LCOM5 = 1
class MyClass5{
    int var2;
    template<class T>
    void foo_MyClass5(){
        var2;
    }
    void goo_MyClass5(){
        var2;
    }
};

//LCOM5 = 1
template<class T>
class MyClass6{
public:
    T var3;
    template<class L>
    void foo_MyClass6(){
        var3;
    }
    void goo_MyClass6(){
        var3;
    }
};
//LCOM5 = 1
template<>
class MyClass<short>{
    short var4;
    void foo_MyClass(){
        var4;
    }
    void goo_MyClass(){
        var4;
    }
};

int main(){
    return 0;
}
