namespace nsA {
	class K {
	  public:
	  template<class T>
	  void foo(T i){}
	};
}
namespace nsB {
	class Ab {
	  public : 
	  void goo(int a){ 
		nsA::K alma;
		alma.foo<int>(5);
	  }
	};

	class Bb {
	  public :  
	  void goo(float a);
	  int b;
	  int z;
	};

	class Cb { 
	public:
	  void noo( );
	  void goo(int a);
	  void too();
	};

	class X : public Ab, private Bb, public Cb { // X class NM erteke 6, TNM erteke 7
	  void moo();
	  void noo(); // override!
	  int c;
	  class Hi {
		void bar(){}
		int d;
	  };
	};
}
int main() {
  return 0;
}