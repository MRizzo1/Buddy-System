import java.io.*;
import java.util.*;

class Buddy {

    /**
     * Defines the boundaries of the reserved memory and the name associated with
     * it (if it is not free)
     */
    class BReserved {
        /**
         * Lower limit
         */
        int ll;
        /**
         * Upper limit
         */
        int ul;

        BReserved(int ll, int ul) {
            this.ll = ll;
            this.ul = ul;
        }
    }

    /**
     * Size of the memory
     */
    private int size;

    /**
     * LinkedList to keep saved free nodes, no matter the size
     */
    private LinkedList<LinkedList<BReserved>> free;

    /**
     * Hashmap to store the starting address and size of reserved segment
     */
    private HashMap<String, LinkedList<Integer>> reserved;

    /**
     * Indicates that the simulation has started
     */
    private Boolean simulation;

    Buddy(int size) {
        this.size = size;
        this.reserved = new HashMap<>();
        this.free = new LinkedList<LinkedList<BReserved>>();
        this.simulation = true;

        int power2 = (int) Math.ceil(Math.log(this.size) / Math.log(2)); // Number of all possible powers of two given
                                                                         // the size of the memory block

        for (int i = 0; i < power2 + 1; i++) {
            LinkedList<BReserved> row = new LinkedList<BReserved>();
            this.free.add(row);
        }

        this.free.get(power2).add(new BReserved(0, this.size - 1)); // Considering that initially the largest block is
        // free; that is to say, the whole block
    }

    void RESERVAR(String name, int size) {

        if (reserved.containsKey(name)) {
            System.out.println(
                    "Hubo un fallo al reservar memoria, ya fue reservado un valor con " + name + " como etiqueta.");
            return;
        }

        if (this.size < size) {
            System.out.println(
                    "El numero de bloques a reservar no puede ser mayor al numero de bloques en memoria.");
            return;
        }

        /**
         * Calculate which free list to search to get the smallest block large enough to
         * fit the request
         */

        int freeSearch = (int) Math.ceil(Math.log(size) / Math.log(2));
        Boolean largestBlockFound = false;

        BReserved temp = null;

        /**
         * If the searched block has been found, the block is not free anymore
         */
        if (free.get(freeSearch).size() > 0) {
            temp = (BReserved) free.get(freeSearch).remove(0);

            LinkedList<Integer> addressSize = new LinkedList<Integer>();
            addressSize.add(temp.ll);
            addressSize.add(temp.ul - temp.ll + 1);
            reserved.put(name, addressSize);

            System.out.println("Memoria desde " + temp.ll
                    + " hasta " + temp.ul + " reservada");
            return;
        }

        /**
         * If the searched block hasn't been found, a larger one is searched
         */
        int i = freeSearch + 1;
        while (i < free.size() && !largestBlockFound) {
            if (free.get(i).size() == 0) {
                i++;
                continue;
            }
            largestBlockFound = true;
        }

        /**
         * If the searched block hasn't been found at all
         * 
         */
        if (largestBlockFound == false) {
            System.out.println(
                    "Hubo un fallo al reservar memoria, no hay espacio para almacenar el numero de bloques solicitado");
            return;
        }

        temp = (BReserved) free.get(i).remove(0); // Remove the first block of the list in free

        for (int j = i - 1; j >= freeSearch; j--) {

            // Divide the block in two halves

            BReserved newBReserved = new BReserved(temp.ll, temp.ll
                    + (temp.ul - temp.ll) / 2);

            BReserved newBReserved2 = new BReserved(temp.ll
                    + (temp.ul - temp.ll + 1) / 2, temp.ul);

            // Add the two halves to the next list
            free.get(j).add(newBReserved);
            free.get(j).add(newBReserved2);

            // Remove a block
            temp = (BReserved) free.get(j).remove(0);
        }

        System.out.println("Memoria desde " + temp.ll
                + " hasta " + temp.ul + " reservada");

        LinkedList<Integer> addressSize = new LinkedList<Integer>();
        addressSize.add(temp.ll);
        addressSize.add(temp.ul - temp.ll + 1);
        reserved.put(name, addressSize);
    }

    void LIBERAR(String name) {

        if (!reserved.containsKey(name)) {
            System.out.println("No se pudo liberar la memoria, no ha sido reservado un valor con esa etiqueta");
            return;
        }

        int s = reserved.get(name).get(0);
        int power2 = (int) Math.ceil(Math.log(reserved.get(name).get(1)) / Math.log(2));
        int buddyNumber, buddyAddress;

        free.get(power2).add(new BReserved(s, s + (int) Math.pow(2, power2) - 1)); // Add block to the free list

        System.out.println("Bloque en memoria de " + s + " hasta "
                + (s + (int) Math.pow(2, power2) - 1) + " ha sido liberado");

        buddyNumber = s / reserved.get(name).get(1); // Calculating buddy number.

        if (buddyNumber % 2 != 0) {
            buddyAddress = s - (int) Math.pow(2, power2); // Calculating buddy address if buddyNumber is even.
        } else {
            buddyAddress = s + (int) Math.pow(2, power2); // Calculating buddy address if buddyNumber is odd.
        }

        for (int i = 0; i < free.get(power2).size(); i++) {

            // The buddy is also free
            if (free.get(power2).get(i).ll == buddyAddress) {
                if (buddyNumber % 2 == 0) {
                    free.get(power2 + 1).add(new BReserved(s, s
                            + 2 * ((int) Math.pow(2, power2)) - 1));
                } else {
                    free.get(power2 + 1).add(new BReserved(buddyAddress,
                            buddyAddress + 2 * ((int) Math.pow(2, power2))
                                    - 1));
                }

                // Remove the individual segments
                free.get(power2).remove(i);
                free.get(power2).remove(free.get(power2).size() - 1);
                break;
            }
        }

        reserved.remove(name);
    }

    void MOSTRAR() {

        BReserved temp;
        System.out.println("\nBLOQUES LIBRES: [direccion inicio, direccion final]");

        System.out.print("[ ");
        for (int i = 0; i < free.size(); i++) {

            if (free.get(i).size() > 0) {
                temp = (BReserved) free.get(i).get(0);
                System.out.print("[ " + temp.ll + ", " + temp.ul + "] ");
            } else {
                System.out.print("[ ] ");
            }
        }
        System.out.println("]\n");

        System.out.println("BLOQUES RESERVADOS: [nombre, direccion inicio, direccion final]");

        System.out.print("[ ");
        reserved.entrySet().forEach(entry -> {
            System.out.print("[ " + entry.getKey() + ", " + entry.getValue().get(0) + ", "
                    + (entry.getValue().get(0) + entry.getValue().get(1) - 1) + "] ");
        });
        System.out.println("]\n");
    }

    void SALIR() {
        simulation = false;
    }

    public static void main(String args[]) throws IOException {
        int initialMemory = 0, nblocks = 0;
        String name = "", action = "";
        LinkedList<String> names = new LinkedList<String>();

        if (args.length != 1) {
            System.err.println("Uso: java Buddy <numeroBloques>");
            System.out.println("Saliendo del programa.");
            return;
        }

        try {
            initialMemory = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("Introduzca un número");
            return;
        }

        Buddy buddy = new Buddy(initialMemory);
        try (Scanner sc = new Scanner(System.in)) {
            while (buddy.simulation) {
                System.out.println("Inserte una acción a ejecutar (RESERVAR, LIBERAR, MOSTRAR, SALIR)");
                action = sc.nextLine();
                if (action.equals("RESERVAR")) {
                    System.out.println("Inserte el número de bloques a usar para reservar");
                    nblocks = sc.nextInt();
                    if (nblocks <= 0) {
                        System.out.println("Introduzca el número de bloques valido.");
                        continue;
                    }

                    System.out.println("Inserte un nombre");
                    sc.nextLine();
                    name = sc.nextLine();
                    if (name.equals("\n")) {
                        System.out.println("Nombre vacío, inserte uno valido.");
                        continue;
                    }
                    buddy.RESERVAR(name, nblocks);
                } else if (action.equals("LIBERAR")) {
                    System.out.println("Inserte un nombre");
                    name = sc.nextLine();
                    if (name.equals("\n")) {
                        System.out.println("Nombre vacío, inserte uno válido");
                        continue;
                    }
                    buddy.LIBERAR(name);
                } else if (action.equals("MOSTRAR")) {
                    buddy.MOSTRAR();
                } else if (action.equals("SALIR")) {
                    System.out.println("Saliendo del programa.");
                    buddy.SALIR();
                } else {
                    System.out
                            .println("Acción no valida. Las acciones validas son: (RESERVAR, LIBERAR, MOSTRAR, SALIR)");
                }
            }
        }
    }
}
