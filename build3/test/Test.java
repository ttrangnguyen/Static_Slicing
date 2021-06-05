package test;

public class Test {
    public int x = 1;

    public void foo(){
        //#ifdef A
            x  = 0;
        //#endif
    }

    public void bar(){
        int y = 1;
        //#ifdef B
            if (false){
                y = x;
            }
        //#endif
        int z = 1 / (1-y);
    }


    public static void main(String[] args) {
        Test t = new Test();
        t.foo();
        t.bar();
    }
}
