int i;

struct Struct {
    int i;
};

class Class {
public:
    int i;
    virtual void foo() {}
};

class Child : public Class {
public:
    void foo() {}
    void goo() {}
};

void foo() {
    Class *classPtr = new Class();
}

void new_delete() {
    Class * c = new Class();
    Class * ca = new Class[5];
}

int main() {
    return 0;
}

class TestClass {
    int i;
public:
    explicit TestClass(int i) {
    }

    TestClass() {
    }

    ~TestClass() {
    }

    virtual void virtualTest() {
    }

    virtual void virtualTest2() {
    }

    virtual void pureVirtualTest() = 0;

    friend void friendTest() {
    }

    TestClass& operator+(const TestClass& t) {
        return *this;
    }

    const TestClass& operator+(const TestClass& t) const {
        return *this;
    }

    void setI(int i) {
        this->i = i;
    }

    void constMethodTest() const {
    }

    const Struct& constReturnTypeTest() {
    }

    void constDeclarationTest() {
        const char * str1 = "test";
        char const*  str2 = "test";
        char * const str3 = "test";
    }

    void constParemeterTest(const char*) {
    }


};

class TestChild : virtual TestClass {
    void virtualTest() override final {
        int override_final;
    }

    void virtualTest2() {
        int descendant;
    }

    virtual void finalTest() final {
    }

    virtual void pureVirtualTest() override final {
        int override, final;
    }

    void constTest() const {
    }

    void testDestructorCall() {
        TestChild c;
        c.~TestChild();
    }

    void tildeTest() {
        TestChild c;
        c.~TestChild();
        int i = 1, j = ~i, k = ~main();
    }


};


