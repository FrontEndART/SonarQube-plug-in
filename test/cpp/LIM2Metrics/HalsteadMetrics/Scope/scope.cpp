int i = 0;

class Class {
public: int i;
};

void variableInDifferentScope1() {
    i = 3;
    int i;
    Class c;
    c.i = i = ::i;
}

void variableInDifferentScope2() {
    {
        int j;
    }
    {
        int j;
    }
}

int main() {
    return 0;
}