package hw3.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Relayable extends Remote {

    public void relayMessage(String relay) throws RemoteException;

    public void relayTicket(int ticket) throws RemoteException;

    public void relayUsername(String username) throws RemoteException;

}

