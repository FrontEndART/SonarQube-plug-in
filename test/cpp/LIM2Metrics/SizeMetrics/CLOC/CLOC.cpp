
void foo(){ // foo cloc metric: 1
};

// a comment, A CLOC metric will be 7
class A {
// just a simple comment
public:
/*
 * the goo function 
 * documentation comment, CLOC 4,
*/
 void goo(){}
 void (* pmf)(void);
// just another comment inside of class A. 
};

// comment 
class B;

class B { 
  // CLOC : 2
 public: B(){
 };
};

int main() {
  A a;
  a.pmf= &foo;
  a.pmf();
  a.goo();
  return 0;
}