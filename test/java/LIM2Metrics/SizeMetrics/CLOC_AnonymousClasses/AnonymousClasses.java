public class AnonymousClasses {
  
    interface AnInterface {
        public void foo();
        public void bar(String str);
    }
  
    public void goo() {
        
        class BClass implements AnInterface {
            String name = "world";
            public void foo() {
                bar("world");
            }
            public void bar(String str) {
                name = str;
                System.out.println("Hello " + name);
            }
        }
      
        AnInterface anObj = new BClass();
        /* First anonymous class
		 * LOC: 10
		 * LLOC:3 (??)  TLLOC: 10
		 * CLOC: TCLOC:7
		 * CD:7+3/3 (???)  TCD: 7+3/3 (???)
		*/ 
        AnInterface anotherObj = new AnInterface() {
            String name = "something";
            public void foo() {
                bar("something");
            }
            public void bar(String str) {
                name = str;
                System.out.println("Say " + name);
            }
        };
		
        
        anObj.foo();
        anotherObj.bar("Fred");
    }

    public static void main(String... args) {
    	AnonymousClasses myApp =
            new AnonymousClasses();
        myApp.goo();
    }            
}