/** Un paciente: solo guarda datos. */
public class Paciente {
    public final String nombre;
    public final int edad;
    public final String sintoma;
    public final int prioridad;   // 5 = Crítico ... 1 = Consulta
    public final int orden;       // número de llegada (1, 2, 3...) para desempatar

    public Paciente(String nombre, int edad, String sintoma, int prioridad, int orden) {
        this.nombre = nombre;
        this.edad = edad;
        this.sintoma = sintoma;
        this.prioridad = prioridad;
        this.orden = orden;
    }

    @Override
    public String toString() {
        return nombre + "(" + prioridad + ")";
    }
}
