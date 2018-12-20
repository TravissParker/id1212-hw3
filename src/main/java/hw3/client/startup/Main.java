package hw3.client.startup;

import hw3.client.view.Interpreter;

import java.rmi.RemoteException;

public class Main {
    public static void main(String[] args) {
        try {
            new Interpreter().start();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
