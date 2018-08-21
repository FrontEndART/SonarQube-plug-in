interface I0 {
  void foo();
}

interface I1 extends I0 {
  public void foo();
}

interface I2 {
  public void foo();
}

interface I3 {
  public void foo();
}

public class Test implements I1, I2, I3 {
  public void foo() {}
}
