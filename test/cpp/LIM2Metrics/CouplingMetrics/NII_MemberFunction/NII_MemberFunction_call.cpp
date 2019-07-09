int loo() { return 0; }
struct B {
    int k;
    B() : k(loo()) { } // B konstruktor NII erteke 3, NIIwi erteke 4
    void bar();  // bar NII erteke: 1 NIIwi erteke: 1
	static int retOne() { return 1;} //retOne NII erteke 2, NIIwi erteke 2
};

template <typename Type> 
Type max(Type tX, Type tY)
{
  B b;
  return (tX > tY) ? tX : tY;
}

void B::bar() {
    B* b_ptr = new B();
	b_ptr->retOne();
}
int aGlobal = B::retOne();


int main() {
	int a_var = 2, b_var = 2;
	max(a_var,b_var);
    B b;
	b.bar();
    return 0;
}
