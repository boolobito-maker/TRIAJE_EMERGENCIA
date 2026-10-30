import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * Max-Heap de pacientes guardado en un ArrayList.
 * Para la posición i:  padre = (i-1)/2   hijo izq = 2*i+1   hijo der = 2*i+2
 * La raíz (posición 0) es siempre el paciente más grave.
 */
public class MaxHeap {
    private final ArrayList<Paciente> datos = new ArrayList<>();
    private final Consumer<String> log;   // para escribir en el "Registro de pasos"
    private int comparaciones = 0;

    public MaxHeap(Consumer<String> log) {
        this.log = log;
    }

    // ---------- consultas ----------
    public int tamano()              { return datos.size(); }
    public boolean estaVacio()       { return datos.isEmpty(); }
    public Paciente get(int i)       { return datos.get(i); }
    public Paciente verRaiz()        { return datos.isEmpty() ? null : datos.get(0); }
    public int getComparaciones()    { return comparaciones; }

    public void limpiar() {
        datos.clear();
        comparaciones = 0;
    }

    /** ¿a debe atenderse antes que b? Mayor prioridad gana; si empatan, el que llegó primero. */
    private boolean tienePreferencia(Paciente a, Paciente b) {
        comparaciones++;
        if (a.prioridad != b.prioridad) return a.prioridad > b.prioridad;
        return a.orden < b.orden;
    }

    private void intercambiar(int i, int j) {
        Paciente tmp = datos.get(i);
        datos.set(i, datos.get(j));
        datos.set(j, tmp);
    }

    // ---------- INSERTAR: se pone al final y SUBE ----------
    public void insertar(Paciente p) {
        datos.add(p);
        int i = datos.size() - 1;
        log.accept("Insertar " + p + ": se coloca al final (posición " + i + ").");

        while (i > 0) {
            int padre = (i - 1) / 2;
            if (tienePreferencia(datos.get(i), datos.get(padre))) {
                log.accept("  " + datos.get(i) + " es más grave que su padre " + datos.get(padre) + " -> SUBE");
                intercambiar(i, padre);
                i = padre;
            } else {
                log.accept("  " + datos.get(i) + " no supera a su padre " + datos.get(padre) + " -> se queda.");
                break;
            }
        }
    }

    // ---------- EXTRAER: se saca la raíz, el último ocupa su lugar y BAJA ----------
    public Paciente extraer() {
        if (datos.isEmpty()) return null;

        Paciente raiz = datos.get(0);
        Paciente ultimo = datos.remove(datos.size() - 1);
        log.accept("Atender " + raiz + ": se saca la raíz.");

        if (datos.isEmpty()) return raiz;

        datos.set(0, ultimo);
        log.accept("  " + ultimo + " (el último) pasa a la raíz y empieza a bajar.");

        int i = 0;
        while (true) {
            int izq = 2 * i + 1;
            int der = 2 * i + 2;
            int mejor = i;
            if (izq < datos.size() && tienePreferencia(datos.get(izq), datos.get(mejor))) mejor = izq;
            if (der < datos.size() && tienePreferencia(datos.get(der), datos.get(mejor))) mejor = der;

            if (mejor == i) {
                log.accept("  " + datos.get(i) + " ya es más grave que sus hijos -> se queda.");
                break;
            }
            log.accept("  " + datos.get(i) + " baja: se cambia con su hijo " + datos.get(mejor));
            intercambiar(i, mejor);
            i = mejor;
        }
        return raiz;
    }

    /** Copia ordenada de mayor a menor gravedad (la "cola real"), sin tocar el heap. */
    public ArrayList<Paciente> ordenReal() {
        ArrayList<Paciente> copia = new ArrayList<>(datos);
        copia.sort((a, b) -> a.prioridad != b.prioridad
                ? b.prioridad - a.prioridad
                : a.orden - b.orden);
        return copia;
    }
}
