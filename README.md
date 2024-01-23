 # MutualExclusion & Synchronize

Este repositorio contiene la implementación de una aplicación distribuida. La aplicación consta de dos procesos heavyweight, ProcessA y ProcessB, que invocan varios procesos lightweight (ProcessLWA1, ProcessLWA2, ProcessLWA3, ProcessLWB1, ProcessLWB2).  

La comunicación entre los procesos se realiza mediante sockets, ya que deben ejecutarse en la misma máquina pero como programas independientes. A continuación, se describen los aspectos clave de la implementación:

- ProcessA: Clase principal para el proceso heavyweight A, que invoca a los procesos lightweight LWA1, LWA2 y LWA3. Implementa la política de Lamport para la exclusión mutua entre los procesos lightweight invocados por ProcessA.
- ProcessB.java: Clase principal para el proceso heavyweight B, que invoca a los procesos lightweight LWB1 y LWB2. Implementa la política de Ricart & Agrawala para la exclusión mutua entre los procesos lightweight invocados por ProcessB.
- ProcessLWA1.java, ProcessLWA2.java, ProcessLWA3.java: Clases para los procesos lightweight invocados por ProcessA. Implementan la sincronización mediante el algoritmo de Lamport y comparten el recurso de la pantalla mediante exclusión mutua.
- ProcessLWB1.java, ProcessLWB2.java: Clases para los procesos lightweight invocados por ProcessB. Implementan la sincronización mediante el algoritmo de Ricart & Agrawala y comparten el recurso de la pantalla mediante exclusión mutua.

### Sincronización y Exclusión Mutua:
- Lamport Clock: Se utiliza el mecanismo de relojes de Lamport para asignar un orden lógico a los eventos y garantizar la sincronización entre los procesos lightweight invocados por ProcessA.

- Ricart & Agrawala: Se implementa el algoritmo de Ricart & Agrawala para la exclusión mutua entre los procesos lightweight invocados por ProcessB, evitando deadlocks y asegurando un acceso eficiente a los recursos compartidos.

##
- @author: Oscar Julián - Bernat Segura
- @date: Enero 2023
