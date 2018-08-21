class A { // NML :2
	void hoo() {};
	void bar() {};
};

class B : public A {  // NML: 5
	int b;
	void foo(int a) {
	

	}
	void sum(int& sumNumber, int addedValue);
	int goo(int a) {
		a += 3;
		return a;
	}
	class C { // NML : 2
		void too(){}
		void loo(){}
		
	};
	int moo (int a) {
		a+=4;
		return a;
	}
	void bar();
};
 void B::bar() { /*...*/}
int main() {
  return 0;
}
