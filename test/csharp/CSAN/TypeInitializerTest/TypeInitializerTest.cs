using System;

namespace TypeInitializerTest
{
    enum Brand
    {
        Audi,
        BMW,
        Ford
    }
    
    public class Car
    {
        public Brand Type { get; set; }
        public int Price { get; set; } 
    }
    
    class Example
    {


        public static void Main(string[] args)
        {
            Car car1 = new Car()
            {
                Type = Brand.BMW,
                Price = 7900000
            };
            
            Car car2 = new Car()
            {
                Type = Brand.Audi,
                Price = 8250000
            };
        }
    }
}