package regtest;

public class ProductionLine {
    public static void main(String[] args) {
        Bin bin = new Bin();  // create a bin
        Factory f1 = new Factory(bin, 1); // & factory
        Worker w1 = new Worker(bin, 1); // create worker
        System.out.println("nFactory thread is " + 
                            f1.getName());
        System.out.println("Worker thread is " + 
                            w1.getName() + "n");
        
        System.out.println("Starting production 
                            line...n");

        f1.start();  // start factory thread
        w1.start();  // start worker thread
    }
}