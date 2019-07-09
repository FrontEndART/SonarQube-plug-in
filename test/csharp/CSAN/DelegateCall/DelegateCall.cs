using System;

namespace TestCase
{
    delegate int A(string a);

    class Program
    {
        static void B(A a)
        {
            int ab = a("1");
        }


        public static void Main(string[] args)
        {
            B(K);
        }
        
        static int K(string a)
        {
            return int.Parse(a);
        }
    }
}