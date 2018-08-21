using System;

namespace TestCase
{
    public class CGI<T>  where T : new()
    {
        private T field1;
    }
	
	public class GenericParameter
	{
		
	}
    
    class Program
    {
        public static void Main(string[] args)
        {
            var cgi = new CGI<GenericParameter>();
        }
    }
}