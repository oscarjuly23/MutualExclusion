package Heavywheight;

import Lighwheight.ProcessLWA;
import Shared.Ports;
import Shared.ReadCallback;
import Shared.Server;

import java.util.concurrent.locks.ReentrantLock;

public class ProcessA extends Thread {
    private static Server ServerHW, ServerLW;
    private static boolean token = true;
    private static final ReentrantLock token_mtx = new ReentrantLock();
    private static int nToken;

    ProcessLWA ProcessLWA1, ProcessLWA2, ProcessLWA3;

    public ProcessA() {

        //Conexió amb LightwheightsA: SERVER
        ServerLW = new Server(Ports.LIGHT_WHEIGHTA_TOKEN_RING_PORT, readCallbackLight);

        //Process A: heavyweight --> Invoquem: ProcessLWA1, ProcessLWA2, ProcessLWA3  (Lamport)
        ProcessLWA1 = new ProcessLWA(0,1,2, Ports.LIGHT_WHEIGHTA1_TOKEN_RING_PORT, Ports.LIGHT_WHEIGHTA2_TOKEN_RING_PORT, Ports.LIGHT_WHEIGHTA3_TOKEN_RING_PORT);     //Process A1: lightwheight
        ProcessLWA2 = new ProcessLWA(1,0,2, Ports.LIGHT_WHEIGHTA2_TOKEN_RING_PORT, Ports.LIGHT_WHEIGHTA1_TOKEN_RING_PORT, Ports.LIGHT_WHEIGHTA3_TOKEN_RING_PORT);     //Process A2: lightwheight
        ProcessLWA3 = new ProcessLWA(2,0,1, Ports.LIGHT_WHEIGHTA3_TOKEN_RING_PORT, Ports.LIGHT_WHEIGHTA1_TOKEN_RING_PORT, Ports.LIGHT_WHEIGHTA2_TOKEN_RING_PORT);     //Process A3: lightwheight

        try {
            sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        ProcessLWA1.start();
        ProcessLWA2.start();
        ProcessLWA3.start();
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
            if (nToken == 3) {
                nToken = 0;
                token_mtx.lock();
                ServerHW.write("token");
                token = false;
                token_mtx.unlock();
            }
        } else {
            System.out.println(msg);
        }
    };

    public void run() {
        //Conexió entre Heavywheights: SERVER
        ServerHW = new Server(Ports.HEAVY_WHEIGHT_TOKEN_RING_PORT , readCallbackHeavy);

        if (token) {
            ServerLW.write("token");
        }
    }

    public void endProcess() {
        try {
            ProcessLWA1.join();
            ProcessLWA2.join();
            ProcessLWA3.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
