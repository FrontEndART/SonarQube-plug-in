public class A extends B { //NM: 8 TNM: 8
        
    // the A subclass adds one field
    public int seatHeight;

    // the A subclass has one constructor
    public A(int startHeight,
                        int startCadence,
                        int startSpeed,
                        int startGear) {
        super(startCadence, startSpeed, startGear);
        seatHeight = startHeight;
    }   
        
    // the A subclass adds one method
    public void setHeight(int newValue) {
        seatHeight = newValue;
    }
	public static void main (String[] args) {}	
}