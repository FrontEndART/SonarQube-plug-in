
//NL = 9, NLE = 8
void foo(){
    try {
        if(true){}
        else if(true){}
        else{
            for(int i = 0; i < 1; ++i){
                int array[3] = {1, 2, 3};
                for each (int i in array){
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
    }
    catch (const int &i){}
    catch (...){}
}

int main(){
    return 0;
}