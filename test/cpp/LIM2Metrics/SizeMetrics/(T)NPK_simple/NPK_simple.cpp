namespace ALongNameToType {  // NPK erteke 1, TNPK erteke 4

  struct ALongNameToType {
    static void Foo();   
  };
  namespace nsC {  // nsc NPK erteke 2, TNPK erteke 3
    namespace nsD{
      namespace nsF { /*…*/ }
    } 
    namespace nsE{ /*…*/  }
  }
}	

int main () {
  return 0;
}