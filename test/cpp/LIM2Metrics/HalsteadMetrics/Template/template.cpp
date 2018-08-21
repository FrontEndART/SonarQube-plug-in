template<class T>
class TemplateHelper {
};

template<int i, class T, template<class K> class L>
class Template {
};

class Class{};

/**
* Operators (4, 4): void, TemplateHelper, int, ;
* Operands (2, 2): types_template_1, th
*/
void types_template_1() {
    TemplateHelper<int> th;
}

/**
* Operators (6, 6): void, Template, Class, TemplateHelper, int, ;
* Operands (3, 3): types_template_2, 3, t
*/
void types_template_2() {
    Template<3, Class, TemplateHelper > t;
}

template<class T, typename K>
void templateFn(T t, K k) {
}

template<>
void templateFn<double, Class*>(double t, Class* k) {
}

void templateFnCall() {
    templateFn(3, 4);
}

int main() {

    return 0;
}