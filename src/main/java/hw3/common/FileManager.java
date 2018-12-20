package hw3.common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface FileManager extends Remote {

    public static final String SERVER_NAME_IN_REGISTRY = "FILE_EXPLORER";

    public void register(String username, String password) throws RemoteException;

    public void login(String username, String password, Relayable relayPoint) throws RemoteException;

    public void logout(String username) throws RemoteException;

    public void upload(FileDTO file) throws RemoteException;

    public void download(String file) throws RemoteException;

    public ArrayList list() throws RemoteException;

    public FileDTO read(String user, String file) throws RemoteException;

    public void delete(String user, String file) throws RemoteException;

    public FileDTO modify(String user, String orgFileName, FileDTO modification) throws RemoteException;
}
