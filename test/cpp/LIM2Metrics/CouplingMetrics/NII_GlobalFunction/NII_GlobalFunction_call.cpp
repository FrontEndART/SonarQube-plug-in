
void moo() {} // moo NII erteke: 1 NIIwi erteke: 1

int retOne() { return 1;}  // retOne NII erteke: 4 NIIwi erteke: 4


struct B {
    int k;
    B() : k(retOne()) { }
    void bar();
};
void B::bar() {
    moo();  
	moo();
}
int aGlobal = retOne();
int secondGlobal = retOne();

int main() {
    B b;
 	b.bar();
	b.k = retOne();
    return 0;
}
