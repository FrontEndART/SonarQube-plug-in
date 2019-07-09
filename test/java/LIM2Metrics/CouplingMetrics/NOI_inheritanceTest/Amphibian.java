class Amphibian extends Animal {
  private Characteristic p = new Characteristic("can live in water");

  private Description t = new Description("Both water and land");

  Amphibian() {
    //System.out.println("Amphibian()");
  }

  protected void dispose() {
    //System.out.println("Amphibian dispose");
    t.dispose();
    p.dispose();
    super.dispose();
  }
}