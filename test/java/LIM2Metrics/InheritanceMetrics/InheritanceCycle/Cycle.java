class A extends B {}
class B extends A {}

interface I {}
interface J {}

class D extends E implements I, J {}
class E extends F {}
class F extends D implements I {}
class C extends D {}