public class A extends B implements C {
   C c;
   public void bar() {
      c.bar();
   }
}