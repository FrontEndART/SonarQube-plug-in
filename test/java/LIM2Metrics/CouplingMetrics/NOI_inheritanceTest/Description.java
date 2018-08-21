class Description {
  private String s;

  Description(String s) {
    this.s = s;
    //System.out.println("Creating Description " + s);
  }

  protected void dispose() {
    //System.out.println("finalizing Description " + s);
  }
}