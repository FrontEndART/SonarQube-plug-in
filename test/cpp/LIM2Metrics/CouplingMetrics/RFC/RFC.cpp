
struct C { // C struct RFC es RFCwi erteke 2
public:
	int multiple(int a);
	void bar();
};

void loo() {}

struct A : public C{  // A struct RFC erteke 5, RFCwi erteke 5
  void foo(){
    /*...*/
  }
  static void goo();
  int a_Data;
  A() : a_Data(multiple(1)) {}
  template <class T> 
  void mf(T* t) {
	loo();
  } 
};

int C::multiple(int a) {
	return a*a;
}
int main() {
  return 0;
}
