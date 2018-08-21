namespace ns_A {
	void foo();
	class A{};
	class B;
	class C{};
	namespace ns_B {
		class D{};
		int foo();
		#ifdef MACRO_1
		class H;
		namespace ns_C {
			class E{};
			namespace ns_D {
				class F{};
				int count = 0;
			}
		}
		
		#else 
		namespace ns_C {
			class E{};
			int count = 0;
		
		}
		#endif
	}

}

int main () {
  return 0;
}