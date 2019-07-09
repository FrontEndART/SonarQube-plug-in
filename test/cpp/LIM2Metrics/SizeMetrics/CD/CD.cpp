struct simpleOperations { 
  // multiple elements,  simpleOperation CD: 0.33=1/1+2 , multiple CD: 0.25="1/1+3"=CLOC/CLOC+LLOC
  int multiple(int a, int b) {
    return a*b;
  }  
};

/* 
 * Factorial struct 
 * set the value if the template param is N , CD value is 0.66=4/4+2
 */
template <int N>
struct Factorial {
	// recursive call, local class comments doesnt increase the CLOC, so doesnt increase the CD
    enum { value = N * Factorial<N - 1>::value };  
};

//  set the value if the template param is 0, CD: 1/2+1=0,33
template <>
struct Factorial<0> {
    enum { value = 1 };
};


int main() {
	// Factorial<4>::value == 24
	// Factorial<0>::value == 1
	const int x = Factorial<4>::value; // == 24
	const int y = Factorial<0>::value; // == 1
  return 0;
}