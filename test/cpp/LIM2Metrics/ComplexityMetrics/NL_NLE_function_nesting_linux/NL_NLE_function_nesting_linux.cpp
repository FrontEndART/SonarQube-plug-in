
//NL = 8, NLE = 7
void foo(){
    try {
        if(true){}
        else if(true){}
        else{
            for(int i = 0; i < 1; ++i){
                while(false){
                    do{
                        int i = 1;
                        switch(i){
                        case 0:
                            break;
                        case 1:
                            break;
                        default:
                            false ? 1 : 0;
                        }
                    }
                    while(false);
                }
            }
        }
    }
    catch (const int &i){}
    catch (...){}
}

int main(){
    return 0;
}