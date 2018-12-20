package hw3.client.view;

public class SynchronizedStdOut {

    synchronized void print(String output) {
        System.out.print(output);
    }

    synchronized void println(String output) {
        System.out.println(output);
    }

    synchronized void println(Object output) {
        System.out.println(output);
    }
}
