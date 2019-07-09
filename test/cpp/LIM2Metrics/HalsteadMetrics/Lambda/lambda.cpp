void lambda_empty() {
  auto l = [](){};
}

void lambda_1() {
  auto l = [](){
    int lambda_variable;
  };
}

void lambda_2() {
  int i;
  auto l = [i](){
    int lambda_variable = i;
  };
}

int main(){
  return 0;
}