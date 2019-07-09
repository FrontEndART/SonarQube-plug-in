/* 
 * comment
 */ 
class Simple{  // LOC erteke: 15  (((11 - 4) + 1) + ((21 - 15) + 1))
  // comment
  int foo(int a);   // LOC erteke: 7 ((21 - 15) + 1)
  
  void bar();
  
  void noo();
};

int multiple(int a, int b);  // LOC erteke: 5 ((27 - 23) + 1) 

int Simple::foo(int a) {
  int b = 5*a;
  int c = 2*a;

  // comment
  return b+c;
}

int multiple (int a, int b){

  // comment line
  return a+b;
}

int main() {
  return 0;
}