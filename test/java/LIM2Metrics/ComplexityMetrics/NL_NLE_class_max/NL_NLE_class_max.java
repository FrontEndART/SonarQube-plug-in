package regtest;

//NL = 3, NLE = 2
class NL_NLE_class_max{

  //NL = 1, NLE = 1
  void foo(){
      if(true){}
      else{}
  }

  //NL = 2, NLE = 2
  void goo(){
      if(true){
          while(true){}
      }
      else{}
  }
  
  //NL = 3, NLE = 1
  void hoo(){
      if(true){}
      else if(true){}
      else if(true){}
  }

  public static void main(String[] args){}
}