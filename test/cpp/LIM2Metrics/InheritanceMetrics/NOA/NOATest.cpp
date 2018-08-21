class Super {};  // NOC : 1

class Parent1
{
protected:
    Parent1( const int& something ) : something( something ) {}
    int something;
};
struct Parent2 : public Super  
{
	float stuffNumber;
};

class Child : public Parent1, Parent2  // NOA : 3
{
private:
    Child() : Parent1(10){} 
	class locCls : private Super {}; 
};

class Car {};  // NOC : 2
class PlayerCar : public Car {};  
class PoliceCar;
class PoliceCar : public Car {};   

template <class TT>
class SuperClass {};
template <class TT>
class BaseClass : public SuperClass< TT > {};  
template <class TT>
class TemplateClass : public BaseClass <TT> {};  // NOA :2
template <class TT>
class ChildClass : TemplateClass< TT> {}; // NOA : 3

int main () {
    return 0;
}