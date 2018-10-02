int i = 0;
class Class {
public:
    int i;
    virtual void foo() {}
    virtual void goo() {}
};

class Child : public Class {
public:
    virtual void foo() {}
    virtual void goo() {}
};

struct Struct {};

/**
* Operators (6,5):
* Operands (3,3):
*/
void cast_test_1() {
    char c = (char)i;
}

/**
* Operators (12,9):
* Operands (4,3):  -> (4,2) k�ne legyen, de a paramban l�v� c-t m�snak veszi
*/
void cast_test_2(Class* c) {
    c->foo();
    ((Child*)c)->goo();
}

/**
* Operators (9,8):
* Operands (3,3): -> (3,2) k�ne hogy legyen -> param �s ut�na c k�l�nb�z�
*/
void cast_test_3(Class* c) {
    dynamic_cast<Child*>(c)->goo();
}

/**
* Operators (7,6):
* Operands (3,3): -> (3,2) k�ne ua, mint fent
*/
void cast_test_4(Class* c) {
    reinterpret_cast<Struct*>(c);
}

/**
* Operators (10,8):
* Operands (5,5): -> (5,4) k�ne
*/
void cast_test_5(const Class* cc) {
    const_cast<Class*>(cc)->i = 3;
}

/**
* Operators (6,5):
* Operands (4,4): -> (3,3) k�ne -> mi�rt van k�tszer 3-as literal?
*/
void cast_test_6() {
    int i = static_cast<int>(3.14);
}


/**
* Operators (3,3):
* Operands (2,2):
*/
int main() {
    return 0;
}