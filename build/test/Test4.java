package test;

public class Test4 {

    //feature a
    public int foo(int x) {
        x = x + 3;
        foo2(x);
        return x;
    }

    public void foo2(int x) {
        System.out.println(x);
    }


    //feature b
    public int bar(int x) {
        x = x - 4;
        return x;
    }

    public static void main(String[] args) {
        Test4 t = new Test4();
        int x = 1;
        x = t.foo(x); // x = 4
        x = t.bar(x); // x = 0
        int y = 1 / x; //bug here
    }

}
