package test;

public class Test {

    public void bar() {
        System.out.println("Feature F1");
    }

    public int foo__F1(int n) {
        System.out.println("Feature F1");
        return 0;
    }

    public int foo(int n) {
        System.out.println("Feature F3");
        int x = foo__F1(n);
        if (x != 0) {
            return;
        }
        int a = 1 / 0; // EXCEPTION
    }
}
