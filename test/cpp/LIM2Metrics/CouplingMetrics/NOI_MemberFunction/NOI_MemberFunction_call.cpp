int loo() { return 0; }
struct B {  // NOI erteke 3, NOIwi erteke 3
  template <typename Type> 
  Type max(Type tX, Type tY){};
  int k;
  B() : k(loo()) { } // B konstruktor NOI erteke 1, NOIwi erteke 1
  void bar();  // bar NII erteke: 1 NIIwi erteke: 1
  static int retOne() { return 1;} //retOne NOI erteke 0, NOIwi erteke 0
};

template <typename Type> 
Type max(Type tX, Type tY)
{
  B b;
  return (tX > tY) ? tX : tY;
}

void B::bar() {  // NOI erteke 2, NOIwi erteke 2
  B* b_ptr = new B();
  b_ptr->retOne();
}
int aGlobal = B::retOne();


int main() {   // NOI erteke 3, NOIwi erteke 4
  int a_var = 2, b_var = 2;
  float a_float = 2.0 , b_float = 2.0;
  max(a_var,b_var);
  max(a_float,b_float);
  B b;
  b.bar();
  return 0;
}
