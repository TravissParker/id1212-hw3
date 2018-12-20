package hw3.client.net;

import hw3.common.AbstractFileTransferer;
import hw3.common.Action;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class ClientFileTransferer extends AbstractFileTransferer implements Runnable {
    private SocketChannel socketChannel;
    private String localDir;

    public ClientFileTransferer(String filename, String dirPath, Action mode) {
        this.filename = filename;
        this.mode = mode;
        this.localDir = dirPath + "/";
        new Thread(this).start();
    }

    public void setupConnectivity() throws IOException {
        socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("localhost", PORT));
    }

    @Override
    public void run() {
        try {
            setupConnectivity();
            super.flowControll(localDir, socketChannel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}