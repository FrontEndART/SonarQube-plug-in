package regtest;

public class Factory extends Thread {
    private int number; // factory's number
    private Bin bin; // knows what bin to fill

    public Factory(Bin bin, int number) {
        this.bin = bin;
        this.number = number;
    }

    public void run() {
        // make 10 parts
        for (int i = 1; i <= 10; i++) {
            // put new part in bin
            bin.putContents("Part " + i);
            System.out.println("Factory #" + this.number + " made Part " + i);
            try {
                sleep(50); // recycle Factory equipment
            } catch (InterruptedException e) { 
                e.printStackTrace();
            }
        }
    }
}