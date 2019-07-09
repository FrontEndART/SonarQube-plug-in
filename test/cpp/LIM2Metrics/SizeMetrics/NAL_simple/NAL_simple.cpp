class A { // NAL: 2
	int a;
	int bar;
};

class B : public A {  // NAL: 3
	int b;
	int c;
    int a;
	void sum(int& sumNumber, int addedValue);
	int goo(int a_var) {
		a_var += 3;
		return a_var;
	}
	class C { // NAL: 2
		int a_memb;
		float b_memb;
		
	};
	int moo (int a) {
	    int c_var;
		c_var=a*a;
		return c_var;
	}
	
};

int main() {
  return 0;
}
