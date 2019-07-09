package regtest;

public class AClass extends Foo
{
    private Foo foo;
    private Bar bar;

    public AClass(Foo foo, Bar bar)
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