// LCOM: 1
class A {
	int a;
	int b;

	public int getA() {
		return a;
	}

	public void setA(int a) {
		this.a = a;
	}

	public int getB() {
		return b;
	}

	public void setB(int b) {
		this.b = b;
	}
	
	public void foo() {
		int a = getA();
	}
	
	public void goo() {
		int a = getA();
	}
}

// Methods will not be getters/setters in lim
// --> LCOM: 2
class B {
	int a;
	int b;

	public int notGetA() {
		return a;
	}

	public void notSetA(int a) {
		this.a = a;
	}

	public int notGetB() {
		return b;
	}

	public void notSetB(int b) {
		this.b = b;
	}
}