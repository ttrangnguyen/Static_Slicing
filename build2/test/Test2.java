package test;

public class Test2 extends Test{
    private static int x = 1;
    private static int y = 1;
    @Override
    public void bar(int x) {
        System.out.println("Test2 &" + x);
    }

    public static void main(String[] args) {
        Test t1 = new Test();
        t1.foo();

        Test2 t2 = new Test2();
        t2.foo();
    }
}