using System;

namespace GenericMethodParameter
{
    class Example
    {
        public static void D<T>(T param) { }

        public static void Main(string[] args)
        {
            D<int>(5);
        }
    }
}