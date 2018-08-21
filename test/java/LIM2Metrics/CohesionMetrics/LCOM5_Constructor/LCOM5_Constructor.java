// LLOC: 0
class A {
	int a;
	int b;

	public A() {
		this.a = 0;
	}

	public A( int b ) {
		this.b = b;
	}
}

// Methods will not be constructors in lim
// --> LLOC: 2
class B {
	int a;
	int b;

	public int notB() {
		this.a = 0;
	}

	public void notB( int b ) {
		this.b = b;
	}
}