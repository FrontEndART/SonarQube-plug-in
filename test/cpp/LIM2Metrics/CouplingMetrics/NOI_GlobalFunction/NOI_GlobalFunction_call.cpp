
void moo() {} // moo NOI erteke: 0 NOIwi erteke: 0

int retOne() { moo(); return 1;}  // retOne Noi erteke: 1 NOIwi erteke: 1


struct B {  // B struct NOI erteke 2 
    int k;
    B() : k(retOne()) { }  // B constructor NOI erteke 1, NOIwi erteke 3
    void bar();  // bar NOI erteke 1
};
void B::bar() {
    moo();  
	moo();
}
int aGlobal = retOne();
int secondGlobal = retOne();

int main() {  // main NOI erteke 3, NOIwi erteke 3
    B b;
 	b.bar();
	b.k = retOne();
    return 0;
}
