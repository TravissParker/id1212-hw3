package hw3.client.view;

import hw3.client.constants.Command;
import hw3.client.net.ClientFileTransferer;
import hw3.common.Action;
import hw3.common.FileDTO;
import hw3.common.FileManager;
import hw3.common.Relayable;

import java.io.File;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Scanner;

import static hw3.common.FileManager.SERVER_NAME_IN_REGISTRY;

public class Interpreter implements Runnable {
    private static final String PROMPT = "> ";
    public static final String WORD_DELIMITER = " ";
    private final Scanner console = new Scanner(System.in);
    private SynchronizedStdOut printer = new SynchronizedStdOut();
    private FileManager server;
    private boolean running = false;
    private Relayable myRemoteObj;
    private int myServerID;
    private String myUsername = "GUEST";  //Used to identify the user when calling to server

    public Interpreter() throws RemoteException {
        myRemoteObj = new Relayer();
    }

    /**
     * Starts up a new interpreter thread.
     * */
    public void start() {
        if (running) {
            return;
        }
        running = true;
        new Thread(this).start();
    }

    /**
     * Reads and interprets user inputted commands.
     */
    @Override
    public void run() {
        while (running) {
            try {
                String input = readNextLine().toUpperCase().trim();
                Command cmd = Command.valueOf(parseCommand(input));

                switch (cmd) {
                    case CONNECT:
                        lookupServer("192.168.1.156"); // host address hardcoded during development
                        printer.println("Connection established");
                        break;
                    case REGISTER:
                        printer.println("Insert username: ");
                        String registerUsername = readNextLine().trim();
                        printer.println("Insert password: ");
                        String registerPassword = readNextLine().trim();
                        server.register(registerUsername, registerPassword);
                        break;
                    case LOGIN:
                        printer.println("Insert your username: ");
                        String loginUsername = readNextLine().trim();
                        printer.println("Insert your password: ");
                        String loginPassword = readNextLine().trim();
                        server.login(loginUsername, loginPassword, myRemoteObj);
                        myUsername = loginUsername;
                        break;
                    case LOGOUT:
                        server.logout(myUsername);
                        running = false;
                        break;
                    case UPLOAD:
                        if (myUsername.equals("GUEST")) {
                            System.out.println("You have not been assigned a username on the server, login and try again");
                            break;
                        }
                        printer.println("Filename of the file to upload: ");
                        String uploadFile = readNextLine().trim();
                        printer.println("Should file be readable (Y/any unused key) ");
                        String read = readNextLine().toUpperCase().trim();
                        printer.println("Should file be writable (Y/any unused key) ");
                        String write = readNextLine().toUpperCase().trim();
                        // Y/y will read as yes, else no
                        boolean readable = read.equals("Y");
                        boolean writable = write.equals("Y");
                        long size = new File("local/" + uploadFile).length();
                        FileDTO file = new FileDTO(uploadFile, size, myUsername, readable, writable);
                        server.upload(file);
                        new ClientFileTransferer(uploadFile, "local", Action.SEND);
                        break;
                    case DOWNLOAD:
                        printer.println("Filename of the file to download: ");
                        String downloadFile = readNextLine();
                        server.download(downloadFile);
                        new ClientFileTransferer(downloadFile, "local", Action.RECEIVE);
                        break;
                    case LIST:
                        displayList(server.list());
                        break;
                    case READ:
                        printer.println("Which file do you want to read: ");
                        String readFile = readNextLine().trim();
                        printer.println(server.read(myUsername, readFile));
                        break;
                    case MODIFY:
                        printer.println("Which file do you want to modify: ");
                        String modifyFile = readNextLine().trim();
                        printer.println("What attributes do you want to modify (blank = no modification)?");
                        printer.println("Name: ");
                        String newName = readNextLine().trim();
                        newName = newName.isEmpty() ? null : newName;
                        printer.println("Read: (Y/N)");
                        String newRead = readNextLine().trim().toUpperCase();
                        printer.println("Write: (Y/N)");
                        String newWrite = readNextLine().trim().toUpperCase();
                        boolean newReadable = newRead.equals("Y");
                        boolean newWritable = newWrite.equals("Y");
                        server.modify(myUsername, modifyFile, new FileDTO(newName, newReadable, newWritable));
                        break;
                    case DELETE:
                        printer.println("Which file do you want to delete: ");
                        String deleteFile = readNextLine().trim();
                        server.delete(myUsername, deleteFile);
                        break;
                    default:
                        printer.println("Default case");
                }
            } catch (RemoteException | NotBoundException | MalformedURLException e) {
                printer.println(e.getMessage());
            }
        }
    }

    private void lookupServer(String host) throws RemoteException, NotBoundException, MalformedURLException {
        server = (FileManager) Naming.lookup("//" + host + "/" + SERVER_NAME_IN_REGISTRY);
    }

    private String readNextLine() {
        System.out.print(myUsername + " " + PROMPT);
        return console.nextLine();
    }

    private String parseCommand(String input) {
        return input.split(WORD_DELIMITER)[0];
    }

    private void displayList(ArrayList list) {
        for (Object f : list) {
            printer.println(f);
        }
    }

    /*
    * Used to let server side objects send messages to a single client.
    * */
    private class Relayer extends UnicastRemoteObject implements Relayable {

        public Relayer() throws RemoteException{}

        public void relayMessage(String relay) {
            printer.println(relay);
        }

        @Override
        public void relayTicket(int ticket) {
            myServerID = ticket;
            System.out.println("You've got the ticket " + myServerID);
        }

        @Override
        public void relayUsername(String username) throws RemoteException {
            myUsername = username;
            System.out.println("You are signed in as " + username);
        }
    }
}
