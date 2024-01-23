package Lighwheight;

import Shared.Client;
import Shared.Ports;
import Shared.ReadCallback;
import Shared.Server;

import java.util.Arrays;
import java.util.Collections;

public class ProcessLWA extends Thread {
    private boolean token = false;
    private Client ClientHW, ClientLW1, ClientLW2;
    private Server ServerLW;
    LamportClock logicClock = new LamportClock();
    private Integer[] clockShared = {Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE};
    public int id, id_LW1, id_LW2, PORT_LWserver, PORT_LWclient1, PORT_LWclient2, ack;
    public ProcessLWA(int id,int id_LW1, int id_LW2, int PORT_LWserver, int PORT_LWclient1, int PORT_LWclient2) {
        this.id = id;
        this.id_LW1 = id_LW1;
        this.id_LW2 = id_LW2;
        this.PORT_LWserver = PORT_LWserver;
        this.PORT_LWclient1 = PORT_LWclient1;
        this.PORT_LWclient2 = PORT_LWclient2;
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
            if (cadena.contains("request")) {
                String[] valor = cadena.split("-");
                int id = Integer.parseInt(valor[1]);            // Guardem el ID de qui l'envia la request
                clockShared[id] = Integer.parseInt(valor[2]);   // Actualitzem en la seva posició del array amb el nou valor de clock
                //System.out.println("REQUEST: " + Arrays.toString(clockShared));

                if (id == id_LW1) {                             // Responem ACK
                    ClientLW1.write("acknowledge.");
                } else if (id == id_LW2) {
                    ClientLW2.write("acknowledge.");
                }

            } else if (cadena.contains("acknowledge")) {        // Quan tenim ACK --> Release
                ack++;

            } else if (cadena.contains("release")) {
                String[] valor = cadena.split("-");
                int id = Integer.parseInt(valor[1]);            // Guardem el ID de qui l'envia
                clockShared[id] = Integer.MAX_VALUE;            // 'L'eliminem' de l'array
                //System.out.println("RELEASE: " + Arrays.toString(clockShared));
            }
        }
    };

    public void run() {
        // Conexió amb Heavywheight: CLIENT
        ClientHW = new Client(Ports.LIGHT_WHEIGHTA_TOKEN_RING_PORT, readCallbackHeavy);

        // Conexió entre Lightwheight: SERVER
        ServerLW = new Server(PORT_LWserver, readCallbackLight);

        // Conexió entre Lightwheight: CLIENT
        ClientLW1 = new Client(PORT_LWclient1, readCallbackLight);
        ClientLW2 = new Client(PORT_LWclient2, readCallbackLight);

        while (true) {

            // Primerament esperem a que el HW tingui el toquen per a poder printar.
            while (!token) {
                System.out.print("");
            }

            // Cada procés incrementa el rellotge lógic
            logicClock.sendAction();
            clockShared[id] = logicClock.getValue();

            // Abans d'accedir a la pantalla compartida, enviem a cada proces el nostre logicClock ( REQUEST ).
            ClientLW1.write("request-" + id + "-" + logicClock.getValue()+".");
            ClientLW2.write("request-" + id + "-" + logicClock.getValue()+".");

            while(ack != 2) {   // Esperem a tenir els dos ACK
                System.out.print("");
            }
            ack = 0;

            while (!isValid()) {     // Esperem a tenir el clock més petit (amb releases)
                System.out.print("");
            }

            for (int i = 0; i < 10; i++) {

                // Si som el més petit, printem per pantalla i actualitzem el clockLocal
                int printID = id+1;
                System.out.println("Soc el proces lightwheight A" + printID);
                //ClientHW.write("Soc el proces lightwheight A" + printID);

                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            // Quan ja hem escrit, eliminem de la cua:
            clockShared[id] = Integer.MAX_VALUE;
            ClientLW1.write("release-" + id);
            ClientLW2.write("release-" + id);

            token = false;
            ClientHW.write("token");
        }
    }

    private boolean isValid() {
        int min = Collections.min(Arrays.asList(clockShared));
        if (logicClock.getValue() < min) {
            return true;
        } else if (logicClock.getValue() == min) {      // Si el Clock es igual al mímin, prioritzem per ID
            for (int i = 0; i < 3; i++) {
                if (clockShared[i] == min) {
                    return id == i;
                }
            }
        }
        return false;
    }
}

