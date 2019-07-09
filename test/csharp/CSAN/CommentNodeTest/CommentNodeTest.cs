using System;


namespace Test
{
    /// <summary>
    /// test
    /// </summary>
    class Class
    {
        /// <summary>
        /// test
        /// </summary>
        /// <param name="args"></param>
        public static void Main(string[] args)
        {
        }

        /**
         * test 
         **/
        public static void Test() {
          //it is not a comment node just a simple comment which can increase the comment lines value
        }

        //test
        public static void A() { }

        /* test */
        public static void B() { }

        /// <summary>
        /// test
        /// </summary>
        public int MyProperty { get; set; }
    }
}