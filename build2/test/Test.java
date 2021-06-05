package test;

public class Test {
    public void foo() {
        int z = 1;
        bar(z);
    }

    public void bar(int x) {
        System.out.println("Test1 &" + x);
    }
}