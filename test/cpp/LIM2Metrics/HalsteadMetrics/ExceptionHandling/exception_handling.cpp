class Class {};
struct Struct {};
/**
* Operators (9, 7): void, try, catch x3, Class, &, int, ...
* Operands (2): try_fn, c
*/
void try_fn() try {
}
catch (Class& c) {
}
catch (int) {
}
catch (...) {
}

void nothrow_test() throw() {
}

void throw_test() throw(Struct) {
}

int main() {
    return 0;
}