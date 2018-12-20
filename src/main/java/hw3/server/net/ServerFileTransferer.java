package hw3.server.net;

import hw3.common.AbstractFileTransferer;
import hw3.common.Action;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class ServerFileTransferer extends AbstractFileTransferer implements Runnable {
    private ServerSocketChannel serverSocketChannel;

    public ServerFileTransferer(String receivingFilename, Action mode) {
        this.filename = receivingFilename;
        this.mode = mode;
        new Thread(this).start();
    }

    @Override
    public void setupConnectivity() throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        ServerSocket serverSocket = serverSocketChannel.socket();
        serverSocket.setReuseAddress(true);
        serverSocket.bind(new InetSocketAddress(PORT));
    }


    @Override
    public void run() {
            try {
                setupConnectivity();
                SocketChannel clientSocketChannel = serverSocketChannel.accept();
                String CATALOG_PATH = "catalog/";
                super.flowControll(CATALOG_PATH, clientSocketChannel);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    serverSocketChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
    }
}
