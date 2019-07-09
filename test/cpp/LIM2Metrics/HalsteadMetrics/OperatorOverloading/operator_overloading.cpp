class Operators {
public:
    void* operator new[](size_t size) throw() {
        return nullptr;
    }

        void* operator new (size_t size) throw(){
        return nullptr;
    }

        int operator+(const Operators& o) const {
        return 2;
    }

    Operators operator++() {
        return *this;
    }

    Operators operator++(int) {
        return *this;
    }

    /**
    * Operators (8, 6): void, ++ (pre), * x2, this x2, ++ (post) x2, ; x2
    * Operands (1): pluszPlusz
    */
    void pluszPlusz() {
        ++*this;
        (*this)++++;
        //this->operator++();
        //this->operator++(2);
    }

};


int main() {
    return 0;
}