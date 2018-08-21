namespace Namespace {
  namespace InnerNamespace {
    class ClassInTwoNamespaces {
    };
  }
  
  class ClassInNamespace {
  };
}
  
/**
 TODO
* Operators (11, 8): void, typedef x2, Class, class, using, namespace, myNamespace, ; x3
* Operands (4, 4): typedef_test, __class, A, AClass
*/
namespace myNamespace {}
void usingTest1() {
  using namespace myNamespace;
}

void usingNamespace() {
  using namespace Namespace;
  ClassInNamespace c;
}

void usingClass() {
  using Namespace::ClassInNamespace;
}

void usingClass2() {
  using CIN = Namespace::ClassInNamespace;
}

int main(){
  return 0;
}