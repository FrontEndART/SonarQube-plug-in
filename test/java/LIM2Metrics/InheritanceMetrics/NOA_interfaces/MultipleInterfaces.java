package regtest;

public class MultipleInterfaces implements Foo, Bar
{
    private Foo foo;
    private Bar bar;


    public String getX() { return this.foo.getX(); }
    public String getY() { return this.bar.getY(); }
	
}