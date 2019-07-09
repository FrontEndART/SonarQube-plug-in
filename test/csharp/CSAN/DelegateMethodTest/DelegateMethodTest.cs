using System;

namespace DelegateMethodTest
{
    class Program
    {
        public static void Main(string[] args)
        {
            Func<int, int> method = delegate(int x)
            {
                if (x < 1024)
                    x *= 13;
                else
                    x /= 11;
                
                while (x >= 10) x--;
                while (x <= 100) ++x;
                
                x = (1 + x * x) / 11;
                while (x >= 10) x -= 10;
                while (x <= 100) x += 5;
                
                return x;
            }
        }
        
    }
}