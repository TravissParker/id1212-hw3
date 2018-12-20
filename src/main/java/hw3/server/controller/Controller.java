package hw3.server.controller;

import hw3.common.Action;
import hw3.common.FileDTO;
import hw3.common.FileManager;
import hw3.common.Relayable;
import hw3.server.integration.DBDAO;
import hw3.server.integration.DBException;
import hw3.server.integration.UserFriendlyDBException;
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
        } catch (UserFriendlyDBException e) {
            throw new RemoteException("The registration failed, try again.");
        }
    }

    @Override
    public synchronized void login(String username, String password, Relayable relayPoint) throws RemoteException {
        if (db.findUserPassword(username, password)) {
            supervisor.addExplorer(new Explorer(username, relayPoint));
            supervisor.notifyUser(username, "Welcome " + username + " you are signed in!");
        } else {
            throw new RemoteException("Credentials were not found, try again, or register a new user.");
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
        } catch (UserFriendlyDBException e) {
            throw new RemoteException("That user name is already taken.");
        } catch (DBException e) {
            throw new RemoteException("There was a problem when trying to upload the file.");
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
            throw new RemoteException("There was a problem getting a list of all files.");
        }
    }

    @Override
    public synchronized FileDTO read(String user, String fileName) throws RemoteException {
        try {
            FileDTO file = db.getFileByName(fileName);
            if (!file.isRead()) {
                throw new RemoteException("The file is not readable, and you are not the owner: " + fileName);
            }
            if (!isOwner(user, file)) {
                supervisor.notifyAccess(file.getOwner(), user, fileName, "read");
            }
            return file;
        } catch (DBException e) {
            throw new RemoteException("There was a problem getting the file.");
        } catch (UserFriendlyDBException e) {
            throw new RemoteException("A file with that name doesn't exist: " + fileName);
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
                throw new RemoteException("You don't have the necessary privileges");
            }
        } catch (DBException e) {
            throw new RemoteException("There was a problem deleting the file.");
        } catch (UserFriendlyDBException e) {
            throw new RemoteException("A file with that name doesn't exist: " + fileName);
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
                throw new RemoteException("You don't have the necessary privileges");
            }
        } catch (DBException e) {
            throw new RemoteException("There was a problem modifying the file.");
        } catch (UserFriendlyDBException e) {
            throw new RemoteException("A file with that name doesn't exist: " + orgFileName);
        }
        return null;
    }

    private boolean isOwner(String user, FileDTO file) {
        return user.equals(file.getOwner());
    }
}

