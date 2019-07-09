class Characteristic {
  private String s;

  Characteristic(String s) {
    this.s = s;
    //System.out.println("Creating Characteristic " + s);
  }

  protected void dispose() {
    //System.out.println("finalizing Characteristic " + s);
  }
}