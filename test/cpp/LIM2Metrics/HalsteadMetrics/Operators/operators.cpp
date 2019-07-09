class Class { public: int i; };
struct Struct { public: int i; };

/**
* Operators (13, 7): void, int x2, ++ (post) x2, -- (post) x2, +, -, ; x4
* Operands (9, 3): post_inc_dec_test_1, i x5, j x3
*/
void post_inc_dec_test_1(int i, int j) {
    i++;
    i--;
    i++ + j;
    i-- - j;
}

/**
* Operators (7, 6): void, Class, Struct, . x2, =, ;
* Operands (7, 5): member_selection_test_1, c x2, s, x2, i, i
*/
void member_selection_test_1(Class c, Struct s) {
    int variable;
    variable = 5;
    c.i = s.i;
    c.i = s.i;
}

/**
* Operators (3): void, sizeof, ;
* Operands (2): sizeof_test, 3
*/
void sizeof_test() {
    sizeof(3);
}

void assignment_test_1() {
    int i(2);
    int j = 3;
}

int main() {
    return 0;
}