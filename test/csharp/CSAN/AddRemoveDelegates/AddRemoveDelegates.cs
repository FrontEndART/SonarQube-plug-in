using System;

namespace AddRemoveDelegates
{
    public class Account
    {
        private decimal balance;
        // we have to explicitly declare a MulticastDelegate
        private EventHandler<EventArgs> onOverdrawn;

        // here, we control the adding and removing of listeners
        public event EventHandler<EventArgs> Overdrawn
        {
            add
            {
                onOverdrawn =
                    (EventHandler<EventArgs>)Delegate.Combine(onOverdrawn, value);
            }
            remove
            {
                onOverdrawn =
                    (EventHandler<EventArgs>)Delegate.Remove(onOverdrawn, value);
            }
        }

        public decimal Balance
        {
            get { return balance; }
            set { balance = value; }
        }

        public void UpdateBalance(decimal amount)
        {
            balance += amount;

            if (balance < 0)
            {
                // we also have to invoke the delegate through the field, now
                if (onOverdrawn != null) onOverdrawn(this, EventArgs.Empty);
            }
        }
    }
    
    class Example
    {

        public static void account_Overdrawn( object sender, EventArgs e )
        {
            Console.WriteLine(“Account is overdrawn”);
        }
        

        public static void Main(string[] args)
        {
            Account account = new Account();
            // hookup the event listener
            account.Overdrawn += account_Overdrawn;
            account.UpdateBalance(-10);
            account.Overdrawn -= account_Overdrawn;
        }
    }
}