package regtest;

class NL_NLE_nesting{

  //NL = 9, NLE = 8
  void foo(){
      try {
          if(true){}
          else if(true){}
          else{
              for(int i = 0; i < 1; ++i){
                  int array[] = {1, 2, 3};
                  for(int j : array){
                      while(true){
                          do{
                              int k = 1;
                              switch(k){
                              case 0:
                                  break;
                              case 1:
                                  break;
                              default:
                                  k = false ? 1 : 0;
                              }
                          }
                          while(false);
                      }
                  }
              }
          }
      }
      catch (Exception e){}
      finally{}
  }

  public static void main(String[] args){}
}