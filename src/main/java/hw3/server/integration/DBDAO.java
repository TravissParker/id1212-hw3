package hw3.server.integration;

import hw3.common.FileDTO;

import java.sql.*;
import java.util.ArrayList;

public class DBDAO {
    private static final String DB_NAME = "dbhw3";
    private static final String USERS_TABLE = "users_table";
    private static final String FILES_TABLE = "files_table";
    private static final String USERS_USERNAME_COLUMN = "username";
    private static final String USERS_PASSWORD_COLUMN = "password";
    private static final String FILES_NAME_COLUMN = "name";
    private static final String FILES_OWNER_COLUMN = "owner";
    private static final String FILES_SIZE_COLUMN = "size";
    private static final String FILES_READ_COLUMN = "read";
    private static final String FILES_WRITE_COLUMN = "write";
    private final int fileNameIndex = 1;
    private final int ownerIndex = 2;
    private final int sizeIndex = 3;
    private final int readIndex = 4;
    private final int writeIndex = 5;
    private final int pidIndex = 6;
    private PreparedStatement findUserStmt;
    private PreparedStatement registerUserStmt;
    private PreparedStatement insertFileStmt;
    private PreparedStatement findFileByNameStmt;
    private PreparedStatement getAllFilesStmt;
    private PreparedStatement deleteFileByNameStmt;
    private PreparedStatement getFileByNameStmt;
    private PreparedStatement updateNameStmt;
    private PreparedStatement updateReadStmt;
    private PreparedStatement updateWriteStmt;
    private final String GENERIC_ERROR_MSG = "An exception was caught in the database.";

    public DBDAO() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Where is your MySQL JDBC Driver?");
            e.printStackTrace();
            return;
        }

        System.out.println("MySQL JDBC Driver Registered!");
        Connection connection = null;

        try {
            connection = DriverManager
                    .getConnection("jdbc:mysql://localhost:3306/dbhw3","root", "password");

        } catch (SQLException e) {
            System.out.println("Connection Failed! Check output console");
            e.printStackTrace();
            return;
        }

        if (connection != null) {
            System.out.println("You are connected to the database now!");
            try {
                prepareStatements(connection);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Failed to make connection!");
        }
    }

    private void prepareStatements(Connection connection) throws SQLException {
        findUserStmt = connection.prepareStatement("SELECT * FROM "+ DB_NAME + "." + USERS_TABLE + " WHERE " + USERS_USERNAME_COLUMN + " = ? AND " + USERS_PASSWORD_COLUMN + " = ?");
        registerUserStmt = connection.prepareStatement("INSERT INTO " + USERS_TABLE + " (" + USERS_USERNAME_COLUMN + ", " + USERS_PASSWORD_COLUMN + ") VALUE (?, ?)");
        insertFileStmt = connection.prepareStatement("INSERT INTO dbhw3.files_table (`name`, `owner`, `size`, `read`, `write`) VALUES (?, ?, ?, ?, ?);");
        findFileByNameStmt = connection.prepareStatement("SELECT " + FILES_NAME_COLUMN + " FROM " + DB_NAME + "." + FILES_TABLE + " WHERE name = ?");
        getAllFilesStmt = connection.prepareStatement("SELECT * FROM dbhw3.files_table");
        deleteFileByNameStmt = connection.prepareStatement("DELETE FROM `dbhw3`.`files_table` WHERE (`name` = ?);");
        getFileByNameStmt = connection.prepareStatement("SELECT * FROM dbhw3.files_table WHERE name = ?");
        updateNameStmt = connection.prepareStatement("UPDATE `dbhw3`.`files_table` SET `name` = ? WHERE (`pid` = ?)");
        updateReadStmt = connection.prepareStatement("UPDATE `dbhw3`.`files_table` SET `read` = ? WHERE (`pid` = ?)");
        updateWriteStmt = connection.prepareStatement("UPDATE `dbhw3`.`files_table` SET `write` = ? WHERE (`pid` = ?)");
    }

    public boolean findUserPassword(String name, String password) {
        try {
            findUserStmt.setString(1, name);
            findUserStmt.setString(2, password);
            ResultSet result = findUserStmt.executeQuery();
            if (result.next()) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void registerUser(String username, String password) throws DBException {
        try {
            registerUserStmt.setString(1, username);
            registerUserStmt.setString(2, password);
            registerUserStmt.executeUpdate();
        } catch (SQLException e) {
            throw new DBException("The registration of the user failed.", e);
        }

    }

    public boolean doesFileExist(String name)throws SQLException {
        findFileByNameStmt.setString(1, name);
        ResultSet result = findFileByNameStmt.executeQuery();
        if (result.next()) {
                return true;
            } else {
                return false;
            }
    }

    public void insertFile(FileDTO file) throws DBException {
        try {
             if (doesFileExist(file.getName())) {
                 throw new DBException("A file with the name: " + file.getName() + " already exists.");
            }
            insertFileStmt.setString(1, file.getName());
            insertFileStmt.setString(2, file.getOwner());
            insertFileStmt.setLong(3, file.getSize());
            insertFileStmt.setBoolean(4, file.isRead());
            insertFileStmt.setBoolean(5, file.isWrite());
            insertFileStmt.executeUpdate();
        } catch (SQLException e) {
            throw new DBException(GENERIC_ERROR_MSG, e);
        }
    }

    public FileDTO getFileByName(String file) throws DBException {
        FileDTO fileMetaData = null;
        try {
            if (doesFileExist(file)) {
                getFileByNameStmt.setString(1, file);
                ResultSet rs = getFileByNameStmt.executeQuery();
                rs.next();
                fileMetaData = new FileDTO(
                        rs.getString(fileNameIndex),
                        rs.getLong(sizeIndex),
                        rs.getString(ownerIndex),
                        rs.getBoolean(readIndex),
                        rs.getBoolean(writeIndex),
                        rs.getInt(pidIndex));
            } else {
                throw new DBException("The file name doesn't exits: " + file);
            }
        } catch (SQLException e) {
            throw new DBException(GENERIC_ERROR_MSG, e);
        }
        return fileMetaData;
    }

    public ArrayList getAllFiles() throws DBException {
        ArrayList<FileDTO> list = new ArrayList<>();
        try {
            ResultSet rs = getAllFilesStmt.executeQuery();
            FileDTO fileMetaData;
            while (rs.next()) {
                fileMetaData = new FileDTO(
                        rs.getString(fileNameIndex),
                        rs.getLong(sizeIndex),
                        rs.getString(ownerIndex),
                        rs.getBoolean(readIndex),
                        rs.getBoolean(writeIndex));
                list.add(fileMetaData);
            }
        } catch (SQLException e) {
            throw new DBException(GENERIC_ERROR_MSG, e);
        }
        return list;
    }

    public void deleteFileByName(String fileName) {
        try {
            deleteFileByNameStmt.setString(1, fileName);
            deleteFileByNameStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateName(String name, int pid) {
        try {
            updateNameStmt.setString(1, name);
            updateNameStmt.setInt(2, pid);
            updateNameStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateRead(boolean read, int pid) {
        updatePermission(read, pid, updateReadStmt);
    }

    public void updateWrite(boolean write, int pid) {
        updatePermission(write, pid, updateWriteStmt);
    }

    private void updatePermission(boolean write, int pid, PreparedStatement updateWriteStmt) {
        try {
            updateWriteStmt.setBoolean(1, write);
            updateWriteStmt.setInt(2, pid);
            updateWriteStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
