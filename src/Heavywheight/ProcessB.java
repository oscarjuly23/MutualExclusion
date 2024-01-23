package Heavywheight;

import Lighwheight.ProcessLWB;
import Shared.Client;
import Shared.Ports;
import Shared.ReadCallback;
import Shared.Server;

import java.util.concurrent.locks.ReentrantLock;

public class ProcessB extends Thread {
    private static Client ClientHW;
    private static Server ServerLW;
    private static boolean token = false;
    private static final ReentrantLock token_mtx = new ReentrantLock();
    private static int nToken;
    ProcessLWB ProcessLWB1, ProcessLWB2;

    public ProcessB() {

        //Conexió amb LightwheightsB: SERVER
        ServerLW = new Server(Ports.LIGHT_WHEIGHTB_TOKEN_RING_PORT, readCallbackLight);

        //Process B: heavyweight --> Invoquem: ProcessLWB1, ProcessLWB2  (Ricart and Agrawala)
        ProcessLWB1 = new ProcessLWB(1, Ports.LIGHT_WHEIGHTB1_TOKEN_RING_PORT, Ports.LIGHT_WHEIGHTB2_TOKEN_RING_PORT);     //Process B1: lightwheight
        ProcessLWB2 = new ProcessLWB(2, Ports.LIGHT_WHEIGHTB2_TOKEN_RING_PORT, Ports.LIGHT_WHEIGHTB1_TOKEN_RING_PORT);     //Process B2: lightwheight

        try {
            sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        ProcessLWB1.start();
        ProcessLWB2.start();
    }

    private static final ReadCallback readCallbackHeavy = (String msg) -> {
        if (msg.contains("token")) {
            token_mtx.lock();
            token = true;
            ServerLW.write("token");
            token_mtx.unlock();
        } else {
            System.out.println(msg);
        }
    };

    private static final ReadCallback readCallbackLight = (String msg) -> {
        if (msg.contains("token")) {
            nToken++;
            if (nToken == 2) {
                nToken = 0;
                token_mtx.lock();
                ClientHW.write("token");
                token = false;
                token_mtx.unlock();
            }
        } else {
            System.out.println(msg);
        }
    };

    public void run() {
        //Conexió entre Heavywheights: CLIENT
        ClientHW = new Client(Ports.HEAVY_WHEIGHT_TOKEN_RING_PORT, readCallbackHeavy);
    }

    public void endProcess() {
        try {
            ProcessLWB1.join();
            ProcessLWB2.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
