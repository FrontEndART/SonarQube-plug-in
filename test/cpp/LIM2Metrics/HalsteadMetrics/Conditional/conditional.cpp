int i = 0;

/**
* Operators (3, 3): void, if, ;
* Operands (2, 2): if_test_1, 1
*/
void if_test_1() {
    if (1)
        ;
}

/**
* Operators (4, 4): void, bool, if, ;
* Operands (3, 2): if_test_2, b x2
*/
void if_test_2(bool b) {
    if (b)
        ;
}

/**
* Operators (4): void, bool, if, {}
* Operands (3, 3): if_test_3, b x2  ->(3,2) 
*/
void if_test_3(bool b) {
    if (b) {
    }
}

/**
* Operators (6, 6): void, bool, if, {}, else, ;
* Operands (3, 3): if_test_4, b x2
*/
void if_test_4(bool b) {
    if (b) {
    }
    else
        ;
}

/**
* Operators (10, 6): void, if x2, {} x3, else x2, true, false;
* Operands (1, 1): if_test_5
*/
void if_test_5() {
    if (false) {
    }
    else if (true) {
    }
    else {
    }
}

/**
* Operators (3,3): switch, void, ';'
* Operands (2,2): i, label_swtich_test_1
*/
void label_swtich_test_1() {
    switch (i)
        ;
}

/**
* Operators (5,5): switch, case, return, ';', void
* Operands (3,3): label_swtich_test_2, i, 1
*/
void label_swtich_test_2() {
    switch (i)
    case 1:
        return;
}

/**
* Operators (6,5): void, switch x2, case, return, ';'
* Operands (4,3): label_swtich_test_3, i x2, 1
*/
void label_swtich_test_3() {
    switch (i)
        switch (i)
    case 1:
        return;
}

/**
* Operators (11,9): void, switch, case x2, --, ++, return, if, ';' x2, {}
* Operands (6,4): label_swtich_test_4, i x3, 1, 0
*/
void label_swtich_test_4() {
    switch (i)
    case 1:
        if (--i) {
            return;
    case 0:
        i++;
        }
}

/**
* Operators (9,8): void, switch, case, return, default, ++, ';' x2, {}
* Operands (4,3): label_swtich_test_5, i x2, 1
*/
void label_swtich_test_5() {
    switch (i) {
    case 1:
        return;
    default:
        i++;
    }
}

int main() {
    return 0;
}