namespace nsA {  // nsA namespace NCL erteke 2, TNCL erteke 5
  void foo();
  class A{};
  class B;
  class C{};
  namespace nsB {  // nsB namespace NCL erteke 1, TNCL erteke 3
    class D{};
      int foo();
	
    namespace nsC {
      class E{};
      namespace nsD {
        class F{};
        int count = 0;
      }
    }
  }
}

int main(){
  return 0;
}
