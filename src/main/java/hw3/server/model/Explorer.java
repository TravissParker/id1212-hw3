package hw3.server.model;

import hw3.common.Relayable;

import java.rmi.RemoteException;

public class Explorer {
    private String name; //name is unique
    private Relayable relayPoint;
    private int serverId;

    public Explorer(String name, Relayable relayPoint) {
        this.name = name;
        this.relayPoint = relayPoint;
    }

    public void notify(String message) {
        try {
            relayPoint.relayMessage(message);
        } catch (RemoteException e) {
            e.printStackTrace();
            System.out.println("NOTIFY TRIED TO SEND MESSAGE TO REMOTE BUT IT FAILED");
        }
    }

    public void giveTicket() {
        try {
            relayPoint.relayTicket(serverId);
        } catch (RemoteException e) {
            e.printStackTrace();
            System.out.println("TRIED TO GIVE TICKET BUT IT FAILED");
        }
    }

    public String getName() {
        return name;
    }

    public void giveName(String username) {
        try {
            relayPoint.relayUsername(username);
        } catch (RemoteException e) {
            e.printStackTrace();
            System.out.println("TRIED TO GIVE NAME BUT IT FAILED");
        }
    }
}
