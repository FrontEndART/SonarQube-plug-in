template < class type >
struct A {
    void f() {}
    type mX;
};
 
template < class type >
struct B : public A<type> {
    void g() { mY = ( this->mX ); this->f(); }
    type mY;
};

int main () {
    return 0;
}