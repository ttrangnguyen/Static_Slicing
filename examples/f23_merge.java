package test;

public class Test {

    public int foo__F2(int n) {
        System.out.println("Feature F2");
        return 1;
    }

    public int foo(int n) {
        System.out.println("Feature F3");
        int x = foo__F2(n);
        if (x != 0) {
            return;
        }
        int a = 1 / 0; // EXCEPTION
    }
}
