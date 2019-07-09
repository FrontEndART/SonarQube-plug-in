using System;

namespace MGITest
{
    public class B
    {
        public void C()
        {
        }
    }

    class Program
    {
        private static void A<T>()
            where T : new()
        {
            T v = new T();
        }
        
        public static void Main(string[] args)
        {
            A<B>();
        }
    }
}