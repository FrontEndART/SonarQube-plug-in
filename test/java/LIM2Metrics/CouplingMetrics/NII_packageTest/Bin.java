package regtest;

public class Bin {
    private String contents; // holds 1 part
    
    // Factory puts parts in bin
    public void putContents(String part) {
        contents = part;  // put new part in bin
    }
    
    // Worker takes parts out of bin
    public String getContents() {
        return contents;    // return the part to Worker
    }
}

