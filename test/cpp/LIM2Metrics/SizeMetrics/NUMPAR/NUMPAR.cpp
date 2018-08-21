
class A {
public:
	void foo(int a, int b) ;

};
	
void A::foo(int a, int b) {
	int c = 0;
	for (int i=0;i<=a;i++) {
		switch (b) {
			case 1 : 
			  c = 1;
			  break;
		    case 2 :
			  c = 10;
			  break;
			case 3 : 
			  c = 110;
			  break;
			default :
			  c = -1;
		}
	
	}

}

int main() {
   return 0;
}


