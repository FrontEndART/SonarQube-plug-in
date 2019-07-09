class Class {
public: int i;
};

void pointer_to_member() {
    int Class::*pointer;
}

void pointer_to_member_2() {
    int Class::*pointer = &Class::i;
    Class c;
    c.*pointer = 2;
}

void null() {
    void *p = nullptr;
}

int main() {
    return 0;
}