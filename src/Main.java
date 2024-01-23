import Heavywheight.ProcessA;
import Heavywheight.ProcessB;

import static java.lang.Thread.sleep;

public class Main {
    public static void main(String[] args) {

        // Invoquem els dos processos Heavywheights
        ProcessA processA = new ProcessA(); // Process A: heavyweight
        ProcessB processB = new ProcessB(); // Process B: heavyweight

        try {
            sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        processA.start();
        processB.start();

        // Bloquea el subproceso de llamada hasta que finaliza el subproceso
        try {
            processA.endProcess();
            processB.endProcess();

            processA.join();
            processB.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
