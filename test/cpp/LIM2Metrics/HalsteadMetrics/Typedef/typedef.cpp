class Class {};

template<class T>
class TemplateHelper {
};

/**
* Operators (4): void, typedef, Class, ;
* Operands (2): typedefTest1, __class
*/
void typedefTest1() {
    typedef Class __class;
}

void typedeftest2() {
    typedef class A AClass;
}

/**
* Operators (7, 6): void, typedef, int, *, ; x2, intPtrType
* Operands (3): typedefTest3, intPtrType, iPtr
*/
void typedefTest3() {
    typedef int* intPtrType;
    intPtrType iPtr;
}

void typedefTest4() {
    typedef TemplateHelper<double*> NewType;
    NewType t;
}

int main() {
    return 0;
}