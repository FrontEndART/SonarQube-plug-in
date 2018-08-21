#define MAJD_TOROLNI

#ifdef __linux
#ifndef MAJD_TOROLNI
MAJD_TOROLNI
void attribute_test_1() __attribute__((optimize("-O3")));
#endif
#endif
/**
* Operators (1): void
* Operands (1): attribute_test_1
*/
void attribute_test_1() {
}


#ifndef MAJD_TOROLNI
MAJD_TOROLNI
/**
* Operators (1): void
* Operands (1): attribute_test_1
*/
void attribute_test_2[[something]]() {
    [[likely(true)]] if (i)
        ;
}
#endif

int main() {
    return 0;
}