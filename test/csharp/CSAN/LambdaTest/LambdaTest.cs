using System;

namespace LambdaTest
{
    class Example
    {

        public void A()
        {
            Func<int, int> localMethod = x => 2*x;
        }
            

        public void B()
        {
            Func<int, int> localMethod = x => 2*x;
        }
    }
}