class Class {};

/**
* Operators (8, 6): void, int, class, A, *, ; x3
* Operands (4, 4): types1, i, A, a
*/
void types1() {
    int i;
    class A;
    A* a;
}

/**
* Operators (10, 6): void, Class x3, ; x3, *, &, =
* Operands (5, 4): types2, c1 x2, c2, c3
*/
void types2() {
    Class c1;
    Class *c2;
    Class &c3 = c1;
}

/**
* Operators ():
* Operands ():
*/
void types3() {
    bool b;
    char c;
    int i;
    long l;
    unsigned long ul;
    signed long sl;
    unsigned long long ull;
}

/**
* Operators ():
* Operands ():
*/
void types_signed_unsigned() {
    int i;
    unsigned u;
    unsigned int ui;
    signed int si;
}

/**
* :)
*
*/
void type_auto() {
#if __cplusplus < 201103L
    //auto int i = 3;
#else
    int i = 3;
    auto j = i;
#endif
}

/**
* Operators (4): void, register, int, ;
* Operands (2): type_register, i
*/
void type_register() {
    register int i;
}

int main() {
    return 0;
}