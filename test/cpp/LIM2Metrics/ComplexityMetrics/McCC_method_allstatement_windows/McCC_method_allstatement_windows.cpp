class MyClass{

    //McCC = 2
    void if_else_foo(){
        if(true){}
        else{}
    }

    //McCC = 3
    void else_if_foo(){
        if(true){}
        else if(false){}
        else{}
    }

    //McCC = 2
    void for_foo(){
       for(int i = 0; i < 1; ++i){}
    }

    //McCC = 2
    void foreach_foo(){
        int array[2] = {1, 2};
        for each (int i in array) {}
    }

    //McCC = 2
    void while_foo(){
        while(false){}
    }

    //McCC = 2
    void do_while_foo(){
        do{}while(false);
    }

    //McCC = 3
    void case_label_foo(){
        int var1;
        switch(var1){
        case 1:
            break;
        case 2:
            break;
        default:
            ;
        }
    }

    //McCC = 2
    void handler_foo(){
        try{}
        catch(...){}
    }

    //McCC = 2
    void conditional_foo(){
        int var2 = 0;
        (var2 == 0 ? var2 = 1 : var2 = 0);
    }

    //McCC = 1
    void empty_foo(){}
	
	//McCC = 4
	void short_circuit_foo(){
		int a = 1, b = 2, c = 3;
		if ( (a && b) || c ) {}
	}

};

int main(){
    return 0;
}