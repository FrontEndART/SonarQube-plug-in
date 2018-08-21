namespace NS1 {
  struct structA {
    struct structB;
    struct structC{
       /*…*/
    };
    void foo() {
        struct structD{ /*… */};
      } 
  };
  namespace NS2 {
    struct structE { /*…*/ };
  }
}

int main() {
  return 0;
}