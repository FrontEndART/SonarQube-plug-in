package regtest;

public class MultipleInterfaces implements Foo, Bar
{
    private Foo foo;
    private Bar bar;

    public MultipleInterfaces(Foo foo, Bar bar)
    {
        this.foo = foo;
        this.bar = bar;
    }

    public String getX() { return this.foo.getX(); }
    public String getY() { return this.bar.getY(); }
	
	public static void main(String [] args)
	{
		/*...*/
	} 
}