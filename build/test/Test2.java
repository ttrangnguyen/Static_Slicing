package test;

public  class  Test2 {


    //__feature_mapping__ [Base] [5:9]
//	public void call_st1() {
//        int t = 0;
////        int m = t; // m = 0
//        call_first(t);
//    }
//
//
//
//    //__feature_mapping__ [Base] [11:13]
//	public void call_st2(int num) {
//        call_first(num);
//    }



    //__feature_mapping__ [Base] [15:19]
	public void call_first(int num) {
        int x = num + 1;
        int i = 2;
        int y = i;
        int z = 1/0;
        if (i == 2){
            y = 3;
            return;
        }
        call_something();
        call_second(x, y);
    }



    //__feature_mapping__ [Base] [21:26]
	public void call_second(int x, int y) {
        System.out.println(x);
        System.out.println(y);
        int z = x + y;
        System.out.println(z);
    }

    //__feature_mapping__ [Base] [28:30]
    public void call_something(){
        System.out.println(1);
        call_something2();
    }

    //__feature_mapping__ [Base] [31:33]
    public void call_something2(){
        int z = 5;
    }


}
