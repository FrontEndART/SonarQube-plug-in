package regtest;

public class AClass implements Foo // DIT:3
{
    private Foo foo;
    private Bar bar;


    public String getX() { return this.foo.getX(); }
    public String getY() { return this.bar.getY(); }
	
}