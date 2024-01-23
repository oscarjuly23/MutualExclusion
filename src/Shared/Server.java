package Shared;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class Server {
    private final Selector sel;
    private final ArrayList<SocketChannel> sc_clients = new ArrayList<>();

    public Server(int port, ReadCallback callback) {
        try {
            ServerSocketChannel s = ServerSocketChannel.open();
            s.socket().bind(new InetSocketAddress(port));
            sel = Selector.open();
            s.configureBlocking(false);
            s.register(sel, SelectionKey.OP_ACCEPT);

            new Thread(new ServerThread(callback)).start();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void write(String token) {
        try {
            for (SocketChannel client : sc_clients) {
                byte[] bs = token.getBytes(StandardCharsets.UTF_8);
                ByteBuffer buf = ByteBuffer.wrap(bs);
                client.write(buf);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private class ServerThread extends Thread {
        private final ReadCallback readCallback;
        public ServerThread(ReadCallback callback) {
            this.readCallback = callback;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    int n = 0;
                    n = sel.select(100);
                    if (n == 0) {
                        continue;
                    }
                    Set<SelectionKey> keys = sel.selectedKeys();
                    Iterator<SelectionKey> it = keys.iterator();
                    while (it.hasNext()) {
                        SelectionKey key = it.next();
                        if (key.isAcceptable()) {
                            //System.out.println("! Connection accepted");
                            ServerSocketChannel ss = (ServerSocketChannel) key.channel();
                            SocketChannel sc = ss.accept();
                            sc.configureBlocking(false);
                            sc.register(sel, SelectionKey.OP_READ);
                            sc_clients.add(sc);
                            // System.out.println ("+ Connection from " + sc.getRemoteAddress());
                        } else if (key.isReadable()) {
                            SocketChannel sc = (SocketChannel) key.channel();
                            ByteBuffer buf = ByteBuffer.allocate(2048);
                            int len = sc.read(buf);

                            if (len <= 0) {
                                System.out.println("! Error Reading from " + sc.getRemoteAddress());
                                System.out.println("+ Closing Connection");
                                key.cancel();
                                sc.close();
                                break;
                            }
                            buf.flip();
                            byte[] msg = new byte[len];
                            buf.get(msg);
                            String desc = new String(msg).trim();
                            //System.out.println ("RECV ("+len+" bytes) >> "+ desc);
                            readCallback.read(desc);
                        }
                        it.remove();
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
