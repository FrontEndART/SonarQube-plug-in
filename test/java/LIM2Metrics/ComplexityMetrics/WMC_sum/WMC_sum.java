package regtest;

//WMC = 11
class WMC_sum{

  //McCC = 2
  void foo(){
      if(true){}
      else{}
  }

  //McCC = 3
  void goo(){
      if(true){
          while(true){}
      }
      else{}
  }
  
  //McCC = 4
  void hoo(){
      if(true){}
      else if(true){}
      else if(true){}
  }
  
  //McCC = 1
  void joo(){}

  //McCC = 1
  public static void main(String[] args){}
}