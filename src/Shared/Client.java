package Shared;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class Client {
    SocketChannel client;

    public Client(int port, ReadCallback readCallback) {
        try {
            client = SocketChannel.open(new InetSocketAddress("localhost", port));
            new Thread(new ClientThread(readCallback)).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void write(String msg) {
        try {
            byte[] bs = msg.getBytes(StandardCharsets.UTF_8);
            ByteBuffer buf = ByteBuffer.wrap(bs);
            client.write(buf);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private class ClientThread extends Thread {
        private final ReadCallback readCallback;
        public ClientThread(ReadCallback callback) {
            this.readCallback = callback;
        }
        @Override
        public void run() {
            while (true) {
                try {
                    String response = null;
                    ByteBuffer inBuf = ByteBuffer.allocate(2048);
                    if (client.read(inBuf) > 0) {
                        response = new String(inBuf.array(), StandardCharsets.UTF_8).trim();
                        //System.out.println("Client: " + response);
                    }
                    this.readCallback.read(response);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
