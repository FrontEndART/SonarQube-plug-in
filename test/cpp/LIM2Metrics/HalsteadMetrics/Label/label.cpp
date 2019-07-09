/**
* Operators (4): void, :, return, ;
* Operands (2): label_test_1, first (label)
*/
void label_test_1() {
first:
    return;
}


/**
* Operators ():
* Operands ():
*/
void label_address_test_1() {
#ifdef __linux
    label :
          void *__label = &&label;
          goto *__label;
      label2:
          int diff = (char*) && label2 - (char*) && label;
#endif
}

int main() {
    return 0;
}