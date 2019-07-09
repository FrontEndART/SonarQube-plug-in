namespace nsA { // nsA NA erteke 5, TNA erteke 7
	class Ab {
	  int a;
	  void goo() {
		int b;
	  }
	};

	class Bb {
	  public :  
	  void goo(float a);
	  int b;
	  int z;
	};

	class Cb { 
	  /* … */
	};
	
	class X : public Ab, private Bb, public Cb {  // X osztaly NA erteke 3, TNA erteke 4
	  void moo(); 
	  int c;
	  class Hi {
		void bar(){}
		int d;
	  };
	};
	namespace nsB {  // NA erteke 2, TNA erteke 2
	  void bar() {}
	  class A {
	    int k;
		int j;
	  };
	
	}
	
}
int main() {
  return 0;
}
