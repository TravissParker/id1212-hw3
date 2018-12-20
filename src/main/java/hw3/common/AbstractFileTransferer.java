package hw3.common;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;

public abstract class AbstractFileTransferer {
    protected Action mode;
    protected String filename;
    protected final int PORT = 9000; // hardcoded during development.

    public abstract void setupConnectivity() throws IOException;

    public void writeToSocketChannel(String dirPath, String file, SocketChannel socketChannel) throws IOException {
        Path path = Paths.get(dirPath + file);
        FileChannel fileChannel = FileChannel.open(path);

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        while(fileChannel.read(buffer) > 0) {
            buffer.flip();
            socketChannel.write(buffer);
            buffer.clear();
        }
        fileChannel.close();
        socketChannel.close();
    }

    public void readFromSocketChannel(String dirPath, String file, SocketChannel socketChannel) throws IOException {
        Path path = Paths.get(dirPath + file);
        FileChannel fileChannel = FileChannel.open(path,
                EnumSet.of(StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.WRITE)
        );

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        while(socketChannel.read(buffer) > 0) {
            buffer.flip();
            fileChannel.write(buffer);
            buffer.clear();
        }
        fileChannel.close();
        socketChannel.close();
    }


    public void flowControll(String dir, SocketChannel channel) throws IOException {
        switch (mode) {
            case SEND:
                writeToSocketChannel(dir, filename, channel);
                break;
            case RECEIVE:
                readFromSocketChannel(dir, filename, channel);
                break;
            default:
                throw new IOException("No matching command");
        }
    }
}
