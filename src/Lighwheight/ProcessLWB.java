package Lighwheight;

import Shared.Client;
import Shared.Ports;
import Shared.ReadCallback;
import Shared.Server;

import java.util.LinkedList;

public class ProcessLWB extends Thread {
    private boolean token = false;
    private Client ClientHW, ClientLW;
    private Server ServerLW;
    LamportClock c = new LamportClock();
    private static LinkedList<Integer> pendingQ = new LinkedList<>();
    private int myts = Integer.MAX_VALUE;
    public int id, PORT_LWserver, PORT_LWclient, numOkay;
    public ProcessLWB(int id, int PORT_LWserver, int PORT_LWclient) {
        this.id = id;
        this.PORT_LWserver = PORT_LWserver;
        this.PORT_LWclient = PORT_LWclient;
    }
    private final ReadCallback readCallbackHeavy = (String msg) -> {
        if (msg.contains("token")) {
            token = true;
        }
    };
    private final ReadCallback readCallbackLight = (String msg) -> {
        String[] cadenas = msg.split("\\.");

        // Quan rebem un MSG al Callback d'un altre LW:
        for (String cadena : cadenas) {
            //System.out.println("LW"+id+" "+cadena);

            if (cadena.contains("request")) {
                String[] valor = cadena.split("-");
                int id_req = Integer.parseInt(valor[1]);            // Guardem el ID de qui l'envia la request
                int value = Integer.parseInt(valor[2]);
                //System.out.println("LW"+id+" mine:"+cadena + "rebut:"+value);

                if (myts == Integer.MAX_VALUE || value < myts || (value == myts && id_req == 1)) {        // No volen CS
                    //System.out.println("1. LW"+id+" rep:"+cadena + "--> send OKAY");
                    ClientLW.write("okay.");
                } else {
                    //System.out.println("1. LW"+id+" rep:"+cadena + "--> pendingQ");
                    pendingQ.add(value);                                 // Encuem
                }

            } else if (cadena.contains("okay")) {        // Quan tenim ACK --> Release
                //System.out.println("2. LW"+id+" rep:"+cadena + "--> OKAY++");
                numOkay++;

            } else if (cadena.contains("release")) {
                //System.out.println("3. LW"+id+" rep:"+cadena + "--> pendingQ?");
                while (!pendingQ.isEmpty()) {
                    //System.out.println(pendingQ.size());
                    pendingQ.pop();
                    numOkay++;
                }
            }
        }
    };

    public void run() {
        // Conexió amb Heavywheight: CLIENT
        ClientHW = new Client(Ports.LIGHT_WHEIGHTB_TOKEN_RING_PORT, readCallbackHeavy);

        // Conexió entre Lightwheight: SERVER
        ServerLW = new Server(PORT_LWserver, readCallbackLight);

        // Conexió entre Lightwheight: CLIENT
        ClientLW = new Client(PORT_LWclient, readCallbackLight);

        while (true) {

            // Primerament esperem a que el HW tingui el toquen per a poder printar.
            while (!token) {
                System.out.print("");
            }

            c.tick();
            myts = c.getValue();

            //System.out.println("LW"+id+" Envio request:");
            ClientLW.write("request-" + id + "-" + myts + ".");

            //System.out.println("LW"+id+" Espero OK");
            while(numOkay != 1) {   // Esperem a tenir el OK
                System.out.print("");
            }
            numOkay = 0;
            //System.out.println("LW"+id+"Ja tinc OK == 1");

            for (int i = 0; i < 10; i++) {
                System.out.println("Soc el proces lightwheight B" + id);
                //ClientHW.write("Soc el proces lightwheight B" + id);

                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            //System.out.println("LW"+id+" Ja He fet prints, envio release");
            myts = Integer.MAX_VALUE;
            ClientLW.write("release"+".");

            token = false;
            ClientHW.write("token");
        }
    }
}
