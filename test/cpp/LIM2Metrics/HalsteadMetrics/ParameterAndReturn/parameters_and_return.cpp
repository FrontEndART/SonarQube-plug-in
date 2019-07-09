int i = 0;
class Class {};
class Child : public Class {};
/**
* Operators (2, 2): void, int
* Operands (1, 1): parameter, i
*/
void parameter_builtin(int i) {
}

/**
* Operators (2, 2): void, Class
* Operands (1, 1): parameter, i
*/
void parameter_class(Class i) {
}

/**
* Operators (3): void, int, ...
* Operands (1): variadic_parameter
*/
void variadic_parameter(int a...) {
}

void valotileTest() {
    volatile int i;
}

/**
* Operators (2): static, void
* Operands (1): static_test
*/
static void static_test() {
}

void default_parameter_simple(int i = 3) {
}

void default_parameter_class(Class *c = new Class()) {
}

void default_parameter_Operators(Class *c = i ? new Child() : new Class()) {
}

int main() {
    return 0;
}