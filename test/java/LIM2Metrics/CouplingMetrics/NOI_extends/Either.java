public class Either<A, B> {  // NOI:3
  private Either() { }// NOI:0
	
  public A left() { return null; }// NOI:0
  public B right() { return null; }// NOI:0
	
  public final static class Left<A, B> extends Either<A, B> { // NOI:1 
    private A a;
    public Left(A a) { this.a = a; }
    public A left() { return a; }
  }

  public static class Right<A, B> extends Either<A, B> {  // NOI:1
    private B b;
    public Right(B b) { this.b = b; }
    public B right() { return b; }
  }
	
  public <C> C either(Map<A, C> f, Map<B, C> g) {  // NOI:3
    if(this instanceof Left)
      return f.apply(left());
    return g.apply(right());
  }
  
  public interface Map<A, B> { //NOI:0
    B apply(A a);
  }
  
  public static void main(String [] args) {}//NOI:0

}
