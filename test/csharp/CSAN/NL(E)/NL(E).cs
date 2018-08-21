using System;
using System.Linq;

namespace TestCase
{
    class Program
    {
        public void test1()
        {
            List<int> list = new List<int>();
            list.Where(t =>
            {
               if (t <= 1) return false;
               else if (t <= 3) return true;
               else if (t % 2 == 0 || t % 3 == 0) return false;
               int i = 5;
               while (i*i <= t) 
               {
                   if (t % i == 0 || t % (i + 2) == 0)
                       return false;
                   i += 6;
               }
               return true;
            });
        }
        
        public void test2()
        {
            new Thread(_ =>
            {
               while (true)
               {
                    if (true) 
                    {
                        
                    }
               }           
            });
        }
        
        public void test3()
        {
            if (true) {}
        }
        
        public void test4()
        {
            if (false){}
            else if (true) {}
        }
        
        public void test5()
        {
                if (false){}
                else {}
        }
        
        public void test6()
        {
            try
            {
                if (false){}
                else {}
            } catch() {}
        }
        
        public void test7(int param)
        {
            switch(param)
            {
                case 1:
                    if (true) {}
                    else {}
                    break;
                case 2:
                    break;
                default:
                    if (false){}
                    else if (true){
                        for (int i=0; i<param; i++){}
                    }
                    break;
            }
        }
        
        public void test8(){
                try {
                    for(int i = 0; i < 10; i++) {
                        if (i % 2 == 0) {}
                        else {}
                    }
                } catch() {}
        }
        
        public void test8(string[] args){
            foreach (string arg in args) {
                try {
                    for(int i = 0; i < int.Parese(arg); i++) {
                        if (i % 2 == 0) {
                            if (true) {}
                        }
                        else {
                            if (true) {
                                if (true) {}
                            }
                        }
                    }
                } catch() {}
            }
        }
    }
}