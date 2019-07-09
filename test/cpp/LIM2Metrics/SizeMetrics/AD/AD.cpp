namespace nsA { // AD = 1
//comment for A , AD: 1
struct A {
 public:
   // comment for multiple
  int multiple(int a, int b) {
    return a*b;
  }  
  //comment for bar
  void bar(){};
 private:
  int foo();

};
// comment for B
class B;
// comment for B too, AD:1
class B {   
public :
  //comment1
  void too(){};
  //comment2
  void loo(){};
};
// namespace nsB AD metrika erteke: 0,33 ; a C osztalye pedig szinten 0,33
namespace nsB { 
  class C {
   public :
   // comment for moo
     void moo(){};
	 void goo() {};
  };

}

}
int main(){
  return 0;
}