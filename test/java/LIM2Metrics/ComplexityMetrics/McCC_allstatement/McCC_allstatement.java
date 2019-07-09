package regtest;

class McCC_allstatement{

  //McCC = 2
  void if_else_foo(){
      if(true){}
      else{}
  }

  //McCC = 3
  void else_if_foo(){
      if(true){}
      else if(false){}
      else{}
  }

  //McCC = 2
  void for_foo(){
     for(int i = 0; i < 1; ++i){}
  }

  //McCC = 2
  void foreach_foo(){
      int array[] = {1, 2};
      for (int i : array) {}
  }

  //McCC = 2
  void while_foo(){
      while(true){}
  }

  //McCC = 2
  void do_while_foo(){
      do{}while(false);
  }

  //McCC = 3
  void case_label_foo(){
      int var1 = 0;
      switch(var1){
      case 1:
          break;
      case 2:
          break;
      default:
          ;
      }
  }

  //McCC = 2
  void handler_foo(){
      try{}
      catch(Exception e){}
      finally{}
  }

  //McCC = 2
  void conditional_foo(){
      int var2 = 0;
      var2 = (var2 == 0) ? 1 : 0;
  }

  //McCC = 1
  void empty_foo(){}

  public static void main(String[] args){}
}