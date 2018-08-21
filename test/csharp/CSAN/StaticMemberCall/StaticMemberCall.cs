using System;

namespace TestCase
{
    class A
    {
        public static Q() { }
    }
    
    struct B
    {
        public static P() {}
    }
    
    class Program
    {
        public static void Main(string[] args)
        {
            A.Q();
            B.P();
        }
    }
}