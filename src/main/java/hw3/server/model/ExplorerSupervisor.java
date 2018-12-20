package hw3.server.model;

import java.util.HashMap;

public class ExplorerSupervisor {
    private static final ExplorerSupervisor instance = new ExplorerSupervisor();
    private HashMap<String, Explorer> explorers = new HashMap<>();

    private ExplorerSupervisor(){
        // Should only exist one supervisor
    }

    public static ExplorerSupervisor getInstance() {
        return instance;
    }

    public void addExplorer(Explorer e) {
        explorers.put(e.getName(), e);
    }

    public void removeExplorer(String username) {
        Explorer e = explorers.get(username);
        e.notify("You are being signed out");
        explorers.remove(username);
    }

    public void notifyAccess(String ownerName, String accessingUser, String file, String action) {
        Explorer owner = explorers.get(ownerName);
        if (owner != null)
            owner.notify(accessingUser + " " + action + " your file: " + file);
    }

    public void notifyUser(String recipient, String msg) {
        Explorer owner = explorers.get(recipient);
        owner.notify(msg);
    }

    public void giveTicket(String username) {
        Explorer e = explorers.get(username);
        e.giveTicket();
    }

    public void giveUsername(String username) {
        explorers.get(username).giveName(username);
    }
}
