
namespace {  // anonymous namespace NUN erteke 1, TNUN erteke 3
  union MyClass {
    public:
    MyClass (const float&) {	}
	
  };
  namespace NS3 {  // NS3 namespace NUN erteke 2, TNUN erteke 2
    union A{
	  union B{};
	};
  }
}

namespace NS2 {  // NS2 namespace NUN erteke 5, TNUN erteke 6

union MyClass {
public:
    MyClass (int) {}
	union E;
	union F{};
};
 union A {
	union B {
		void foo() {
			union C{};
			union H;
		}
	};
 };
 namespace NS4 {
    union J {};
 }
}

int main(){
  return 0;
}