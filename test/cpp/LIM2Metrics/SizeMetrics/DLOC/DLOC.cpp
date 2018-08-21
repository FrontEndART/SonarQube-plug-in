
//documentation comment, DLOC 1
void foo(){};

// class A DLOC 5, because the goo function has DLOC with 4 value, and A has 1
class A {
// just a simple comment 
public:
/*
 * the goo function 
 * documentation comment, DLOC 4
*/
 void goo(){}
 void (* pmf)(void);
// just another comment inside of class A
};

/*
 *
 *
 * documentation for B class, szamit a DLOC-hoz
 * DLOC : 8
 */
class B { 
 // konstruktor comment, szamit a DLOC-hoz
 public: B(){};
 // attribute comment, szamit a DLOC-hoz
 int a;
 // lokalis class komment, nem szamit a DLOC-hoz
 class C{};
};

int main() {
  A a;
  a.pmf= &foo;
  a.pmf();
  a.goo();
  return 0;
}