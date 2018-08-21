/**
* Operators (3): void, for(;;), ;
* Operands (1): for_test_1
*/
void for_test_1() {
    for (;;)
        ;
}

/**
* Operators (3): void, for(;;), {}
* Operands (1): for_test_2
*/
void for_test_2() {
    for (;;) {
    }
}

/**
* Operators (7, 7): void, for(;;), int, =, <, ++, {}
* Operands (6, 4): for_test_3, i x3, 0, 5
*/
void for_test_3() {
    for (int i = 0; i < 5; i++) {
    }
}

/**
* Operators (9, 8): void, int x2, [], {} , ;, for(:), &, {}
* Operands (8, 7): for_test_4, t x2, 1, 2, 3, 4, i
*/
int t[] = { 1, 2, 3, 4 };
void for_test_4() {
#if 201103L <= __cplusplus
    for (int &i : t) {
    }
#endif
}

/**
* Operators (5, 4): void, do, while, ; x2
* Operands (3, 3): do_test_1, 1, 0
*/
void do_test_1() {
    do
        1;
    while (0);
}

void do_test_2() {
    do {
        1;
    } while (0);
}

/**
* Operators (3, 3): void, while, ;
* Operands (3, 3): while_test_1, 1, 0
*/
void while_test_1() {
    while (0)
        1;
}

void while_test_2() {
    while (0) {
        1;
    }
}

/**
* Operators ():
* Operands ():
*/
void break_continue_test_1() {
    for (;;) {
        break;
        break;
        continue;
        continue;
    }
}

/**
* Operators (5): void, :, goto, first, ;
* Operands (2): label_goto_test_1, first
*/
void label_goto_test_1() {
first:
    goto first;
}

/**
* Operators ():
* Operands ():
*/
void label_goto_test_2() {
first:
    goto first;
    goto first;
}

/**
* Operators ():
* Operands ():
*/
void label_goto_test_3() {
first:
    goto first;
second:
    goto second;
}

int main() {
    return 0;
}