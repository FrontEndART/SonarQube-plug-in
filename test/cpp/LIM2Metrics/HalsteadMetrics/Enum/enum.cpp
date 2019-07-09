int i = 0;


enum Enum {
    one,
    two,
};

/**
* Operators (3): void, enum, ;
* Operands (3): enum_test_1, one, two
*/
void enum_test_1() {
    enum { one, two };
}

/**
* Operators (7, 5): void, enum, ; x3, one, two
* Operands (4): enum_test_2, one, two, Enum
*/
void enum_test_2() {
    enum E { one, two };
    int i(one);
    two;
}

void enum_switch() {
    switch (i) {
    case one:
        return;
    case two:
        return;
    }
}

void enum_test_3() {
    enum class Fruits { Apple, Pear, Orange };
    enum class Colours { Blue, White, Orange };
    Fruits f = Fruits::Orange;
}

void enum_test_4() {
    enum class Foo : char { A, B, C };
    Foo f = Foo::A;
}

void enum_test_5() {
    enum { A, B = 8, C };
}

int main() {
    return 0;
}