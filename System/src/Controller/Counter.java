package Controller;

public class Counter implements AutoCloseable {

    static int sum;
    static {
        sum = 0;
    }

    public void add() {
        sum++;
    }

    @Override
    public void close() {
        System.out.println("Выход");
    }
}

