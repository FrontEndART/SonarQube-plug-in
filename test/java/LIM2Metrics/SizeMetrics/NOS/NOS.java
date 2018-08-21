package regtest;


class NOS{  // NOS : 21

  void foo(){ // NOS: 19
      if(true){}
      else if(false){}
      else{}
      int array[] = {0, 1};
      for (int i : array) {}
      for(int i = 0; i < 1; ++i){}      
      int counter = 0;
      while(counter != 2){ counter++;}
      int var1 = 0;
      switch(var1){
      case 1:
          break;
      case 2:
          break;
      default:
          ;
      }
	  ;
      do{ counter--; }while( counter != 0);
      label:
    	  if(false){ break label;}
    	 
  }

  void bar(){  // NOS: 2
      try{}
      catch(Exception e){}
      finally{}

      int var2 = 0;   
      var2 = (var2 == 0) ? 1 : 0;  
  }


  public static void main(String[] args){
	  
  }
	  
}