//NL = 1, NLE = 1
void if_else_foo(){
    if(true){}
    else{}
}

//NL = 2, NLE = 1
void else_if_foo(){
    if(true){}
    else if(false){}
    else{}
}

//NL = 1, NLE = 1
void for_foo(){
   for(int i = 0; i < 1; ++i){}
}

//NL = 1, NLE = 1
void while_foo(){
    while(false){}
}

//NL = 1, NLE = 1
void do_while_foo(){
    do{}while(false);
}

//NL = 1, NLE = 1
void switch_foo(){
    int var1;
    switch(var1){
    case 1:
        break;
    default:
        ;
    }
}

//NL = 1, NLE = 1
void try_foo(){
    try{}
    catch(...){}
}

//NL = 1, NLE = 1
void conditional_foo(){
    int var2 = 0;
    (var2 == 0 ? var2 = 1 : var2 = 0);
}

int main(){
    return 0;
}