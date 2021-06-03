package test;

public class Test {
    private int x = 1;

    //feature a
    public void foo() {
        x = x + 1;
    }

    //feature b
    public void bar() {
        x = x - 2;
    }


    //feature c
    public void baz() {
        int y = 1 / x; //bug here
    }

    //feature c
    public static void main(String[] args) {
        Test t1 = new Test();
        t1.foo();
        t1.baz();

        Test t2 = new Test();
        t2.bar();
        t2.baz();
    }
}
