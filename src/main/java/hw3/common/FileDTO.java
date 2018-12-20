package hw3.common;

import java.io.Serializable;

public class FileDTO implements Serializable {
    private int pid;
    private String name;
    private Long size;
    private String owner;
    private Boolean read;
    private Boolean write;

    public FileDTO(String name, long size , String owner, Boolean read, Boolean write) {
        this.name = name;
        this.size = size;
        this.owner = owner;
        this.read = read;
        this.write = write;
    }

    public FileDTO(String name, Boolean read, Boolean write) {
        this.name = name;
        this.read = read;
        this.write = write;
    }

    // Constructor used when generating FileDTO from files in DB
    public FileDTO(String name, long size , String owner, Boolean read, Boolean write, int pid) {
        this.name = name;
        this.size = size;
        this.owner = owner;
        this.read = read;
        this.write = write;
        this.pid = pid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public boolean isWrite() {
        return write;
    }

    public void setWrite(boolean write) {
        this.write = write;
    }

    public String toString() {
        return "File: " + name + "\tBytes: " + size + "\tOwner: " + owner + "\tRead: " + read + "\tWrite"  + write;
    }

    public int getPid() {
        return pid;
    }
}
