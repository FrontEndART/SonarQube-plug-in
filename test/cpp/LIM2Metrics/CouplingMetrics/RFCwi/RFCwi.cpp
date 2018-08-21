
void moo() {}

template <typename Type>
void addToFirst(Type& tX, Type& tY) {
	tX += tY;
}

struct C {  // C struct RFC es RFCwi 1 ( max prototype)
public:
	template <typename Type> 
	Type max(Type tX, Type tY)
	{
	  moo();
	  addToFirst(tX, tY);
	  int a = 1;
	  int b = 2;
	  addToFirst<int>(a, b);
	  return (tX > tY) ? tX : tY;
	}
};
struct A : public C{  // A class RFC erteke:5 ( foo, max prototype, loo, goo, mf)  RFCwi erteke: 7 (foo, 3 max peldany, loo, goo, mf) 
  void foo(int a, int b){
    int g = 0, i = 1; 
	float c = 1.0f, d = 2.0f; 
	max(c,d); 
	loo();
	max(i,g); 
	double h = 0.0, j = 3.1;
	max(h,j);
  }
  static void goo();
  void loo(){
	moo();
  }
  
  int a_Data;
  
  template <class T> void mf(T* t) {
	loo();
  } 
};

int main() {
  return 0;
}
