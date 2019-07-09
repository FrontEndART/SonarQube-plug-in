namespace ALongNameToType {
#ifdef MACRO_1
	 struct ALongNameToType {
	 	static void Foo();   
 	};
	namespace c {
		namespace d {} 
	}
	#else
	struct ALongNameToType {
	static void Noo();   
	};
	namespace c {
		namespace e {} 
	}
	#endif
}
namespace a {
	namespace g { namespace h { namespace j{}}}
	namespace h {}

}

int main() {
  return 0;
}