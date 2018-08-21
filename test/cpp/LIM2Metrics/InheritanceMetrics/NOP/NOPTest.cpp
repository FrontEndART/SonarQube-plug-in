class Super {};

class Parent1
{
protected:
    Parent1( const int& something ) : something( something ) {}
    int something;
};
struct Parent2 : public Super  // NOP : 1
{
	float stuffNumber;
};

class Child : public Parent1, Parent2  // NOP : 2
{
private:
    Child() : Parent1(10){} 
	class locCls : private Super {};  // NOP : 1
};

class Car {};
class PlayerCar : public Car {};  // NOP : 1
class PoliceCar;
class PoliceCar : public Car {};   // NOP: 1

class BaseClass {};
template <class TT>
class TemplateClass : public BaseClass {};  // NOP : 1
template <class TT>
class ChildClass : TemplateClass<TT> {};  // NOP : 1

int main () {
    return 0;
}

