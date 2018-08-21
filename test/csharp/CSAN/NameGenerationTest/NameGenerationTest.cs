using System;
using System.Linq;

namespace Test
{
    delegate int method(string a);
    
    class NameGenerationTest
    {       
        public static void Main(string[] args)
        {
            method asd2 = delegate (string b)
            {
                var anon = new { I = new int[] { 1, 2, 3, 4 }.Where((x) => x == 2), B = "random string" };
                if (!string.IsNullOrEmpty(b))
                {
                    for (int i = 0; i < 15; i++)
                    {
                        b += b;
                    }
                }
                else
                {
                    b = "[Empty String]";
                }
 
                return b.Select(t => (int)t).Sum();
            };
        }
    }
}