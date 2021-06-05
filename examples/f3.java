package test;

public class Test {

    public int foo(int n) {
        System.out.println("Feature F3");
        int x = __original__(n);
        if (x != 0) {
            return;
        }
        int a = 1 / 0; // EXCEPTION
    }
}

