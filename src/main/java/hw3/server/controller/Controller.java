package hw3.server.controller;

import hw3.common.Action;
import hw3.common.FileDTO;
import hw3.common.FileManager;
import hw3.common.Relayable;
import hw3.server.integration.DBDAO;
import hw3.server.integration.DBException;
import hw3.server.model.Explorer;
import hw3.server.model.ExplorerSupervisor;
import hw3.server.net.ServerFileTransferer;

import java.lang.reflect.Field;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class Controller extends UnicastRemoteObject implements FileManager {
    private DBDAO db = new DBDAO();
    private ExplorerSupervisor supervisor = ExplorerSupervisor.getInstance();

    public Controller() throws RemoteException {
    }

    @Override
    public synchronized void register(String username, String password) throws RemoteException {
        try {
            db.registerUser(username, password);
        } catch (DBException e) {
            System.out.println(e.getMessage());
            throw new RemoteException();
        }
    }

    @Override
    public synchronized void login(String username, String password, Relayable relayPoint) throws RemoteException {
        if (db.findUserPassword(username, password)) {
            supervisor.addExplorer(new Explorer(username, relayPoint));
            supervisor.notifyUser(username, "Welcome " + username + " you are signed in!");
        } else {
            throw new RemoteException();
        }
    }


    @Override
    public synchronized void logout(String username) throws RemoteException {
        supervisor.removeExplorer(username);
    }

    @Override
    public synchronized void upload(FileDTO file) throws RemoteException {
        // First make sure that there is no such file
        try {
            db.insertFile(file);
        } catch (DBException e) {
            System.out.println(e.getMessage());
            throw new RemoteException();
        }
        //we must assume that transfer went alright.
        new ServerFileTransferer(file.getName(), Action.RECEIVE);
    }

    @Override
    public synchronized void download(String file) throws RemoteException {
        new ServerFileTransferer(file, Action.SEND);
    }

    @Override
    public synchronized ArrayList list() throws RemoteException {
        try {
            return db.getAllFiles();
        } catch (DBException e) {
            System.out.println(e.getMessage());
            throw new RemoteException();
        }
    }

    @Override
    public synchronized FileDTO read(String user, String fileName) throws RemoteException {
        try {
            FileDTO file = db.getFileByName(fileName);
            if (!file.isRead()) {
                throw new RemoteException();
            }
            if (!isOwner(user, file)) {
                supervisor.notifyAccess(file.getOwner(), user, fileName, "read");
            }
            return file;
        } catch (DBException e) {
            System.out.println(e.getMessage());
            throw new RemoteException();
        }
    }

    @Override
    public synchronized void delete(String user, String fileName) throws RemoteException {
        FileDTO file = null;
        try {
            file = db.getFileByName(fileName);
            if (isOwner(user, file)) {
                db.deleteFileByName(fileName);
            } else {
                supervisor.notifyAccess(file.getOwner(), user, fileName, "tried to delete");
                throw new RemoteException();
            }
        } catch (DBException e) {
            System.out.println(e.getMessage());
            throw new RemoteException();
        }

    }

    @Override
    public synchronized FileDTO modify(String user, String orgFileName, FileDTO mods) throws RemoteException {
        FileDTO current;
        try {
            current = db.getFileByName(orgFileName);
            if (isOwner(user, current) || current.isWrite()) {
                Field[] modFields = mods.getClass().getDeclaredFields();
                int fileID = current.getPid();
                for (Field f : modFields) {
                    if (f != null) {
                        switch (f.getName()) {
                            case "name":
                                db.updateName(mods.getName(), fileID);
                                break;
                            case "read":
                                db.updateRead(mods.isRead(), fileID);
                                break;
                            case "write":
                                db.updateWrite(mods.isWrite(), fileID);
                                break;
                            default:
                                //no-operation
                        }
                    }
                }
                supervisor.notifyUser(current.getOwner(), user + " modified your file: " + orgFileName);
            } else {
                supervisor.notifyAccess(mods.getOwner(), user, orgFileName, "tried to modify");
                throw new RemoteException();
            }
        } catch (DBException e) {
            System.out.println(e.getMessage());
            throw new RemoteException();
        }
        return null;
    }

    private boolean isOwner(String user, FileDTO file) {
        return user.equals(file.getOwner());
    }
}

