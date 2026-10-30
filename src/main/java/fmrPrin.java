import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

/** Ventana principal: Triaje de Emergencias con Max-Heap. */
public class fmrPrin extends JFrame {

    // ----- datos -----
    private final MaxHeap heap;
    private int contadorLlegada = 0;   // para saber quién llegó primero
    private int atendidos = 0;
    private ArrayList<Paciente> cola = new ArrayList<>();  // lo que se muestra en la tabla

    // ----- componentes -----
    private final JTextField txtNombre = new JTextField();
    private final JTextField txtEdad = new JTextField();
    private final JTextField txtSintoma = new JTextField();
    private final JRadioButton[] radios = new JRadioButton[6];   // posiciones 1..5
    private final PanelArbol panelArbol;
    private final DefaultTableModel modeloTabla =
            new DefaultTableModel(new String[]{"#", "Paciente", "Prior.", "Síntoma"}, 0);
    private final JTable tabla = new JTable(modeloTabla);
    private final JTextArea txtLog = new JTextArea(7, 40);
    private final JLabel lblEstado = new JLabel();

    private static final String[] NOMBRES_PRIORIDAD = {"", "Consulta", "Leve", "Urgente", "Grave", "Crítico"};
    private static final String[] NOMBRES = {"Ana", "Luis", "Marta", "Pedro", "Lucía", "Carlos",
            "Eva", "Diego", "Valeria", "Juan", "Elena", "Sofía"};
    private static final String[][] SINTOMAS = {
            {},
            {"Receta médica", "Control general"},
            {"Esguince", "Dolor de garganta"},
            {"Dolor abdominal", "Fiebre alta"},
            {"Quemadura grave", "Fractura expuesta"},
            {"Hemorragia severa", "Paro respiratorio"}};

    public fmrPrin() {
        super("Triaje de Emergencias – Max-Heap");
        heap = new MaxHeap(this::escribirLog);
        panelArbol = new PanelArbol(heap);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));

        add(crearPanelRegistro(), BorderLayout.WEST);
        add(conTitulo(panelArbol, "Árbol del heap (se dibuja en vivo)"), BorderLayout.CENTER);
        add(crearPanelCola(), BorderLayout.EAST);
        add(crearPanelInferior(), BorderLayout.SOUTH);

        setSize(1200, 780);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
        actualizar();
    }

    // =====================================================================
    //  CONSTRUCCIÓN DE LA INTERFAZ
    // =====================================================================

    private JPanel conTitulo(JComponent c, String titulo) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), titulo, TitledBorder.LEFT, TitledBorder.TOP));
        p.add(c, BorderLayout.CENTER);
        return p;
    }

    private JPanel crearPanelRegistro() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        // formulario: Nombre / Edad / Síntoma
        JPanel form = new JPanel(new GridLayout(3, 2, 6, 6));
        form.add(new JLabel("Nombre"));   form.add(txtNombre);
        form.add(new JLabel("Edad"));     form.add(txtEdad);
        form.add(new JLabel("Síntoma"));  form.add(txtSintoma);
        form.setAlignmentX(LEFT_ALIGNMENT);
        p.add(form);
        p.add(Box.createVerticalStrut(8));

        // prioridad: 5 botones de opción de colores
        JPanel prio = new JPanel(new GridLayout(5, 1, 0, 3));
        ButtonGroup grupo = new ButtonGroup();
        for (int pr = 5; pr >= 1; pr--) {
            JRadioButton r = new JRadioButton(pr + " " + NOMBRES_PRIORIDAD[pr]);
            r.setBackground(PanelArbol.colorPrioridad(pr));
            r.setOpaque(true);
            r.setFont(r.getFont().deriveFont(Font.BOLD));
            grupo.add(r);
            prio.add(r);
            radios[pr] = r;
        }
        radios[3].setSelected(true);
        JPanel filaPrio = new JPanel(new BorderLayout(6, 0));
        filaPrio.add(new JLabel("Prioridad"), BorderLayout.WEST);
        filaPrio.add(prio, BorderLayout.CENTER);
        filaPrio.setAlignmentX(LEFT_ALIGNMENT);
        filaPrio.setMaximumSize(new Dimension(Integer.MAX_VALUE, 135));
        p.add(filaPrio);
        p.add(Box.createVerticalStrut(10));

        // botones
        p.add(boton("Registrar", e -> registrar()));
        p.add(Box.createVerticalStrut(8));
        p.add(new JSeparator());
        p.add(Box.createVerticalStrut(8));
        p.add(boton("ATENDER SIGUIENTE", e -> atenderSiguiente()));
        p.add(Box.createVerticalStrut(5));
        p.add(boton("Ver paciente en la raíz", e -> verRaiz()));
        p.add(Box.createVerticalStrut(5));
        p.add(boton("Generar 15 pacientes", e -> generar15()));
        p.add(Box.createVerticalStrut(5));
        p.add(boton("Comparar con cola FIFO", e -> compararFifo()));
        p.add(Box.createVerticalStrut(5));
        p.add(boton("Limpiar todo", e -> limpiarTodo()));
        p.add(Box.createVerticalGlue());

        JPanel envoltura = conTitulo(p, "Registrar paciente");
        p.setBorder(BorderFactory.createEmptyBorder(4, 8, 8, 8));
        envoltura.setPreferredSize(new Dimension(255, 0));
        return envoltura;
    }

    private JButton boton(String texto, java.awt.event.ActionListener accion) {
        JButton b = new JButton(texto);
        b.setAlignmentX(LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        b.addActionListener(accion);
        return b;
    }

    private JPanel crearPanelCola() {
        tabla.setRowHeight(22);
        tabla.setEnabled(false);               // solo para mirar
        tabla.setShowGrid(false);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(30);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(90);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(45);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(140);

        // pinta cada fila con el color de su prioridad
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                           boolean foc, int fila, int col) {
                super.getTableCellRendererComponent(t, v, false, false, fila, col);
                setBackground(PanelArbol.colorPrioridad(cola.get(fila).prioridad));
                setForeground(Color.BLACK);
                setHorizontalAlignment(col == 0 || col == 2 ? CENTER : LEFT);
                return this;
            }
        });

        JScrollPane sp = new JScrollPane(tabla);
        sp.getViewport().setBackground(Color.WHITE);
        JPanel p = conTitulo(sp, "Cola de atención (orden real)");
        p.setPreferredSize(new Dimension(340, 0));
        return p;
    }

    private JPanel crearPanelInferior() {
        txtLog.setEditable(false);
        txtLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JPanel log = conTitulo(new JScrollPane(txtLog), "Registro de pasos");

        JPanel sur = new JPanel(new BorderLayout(0, 4));
        sur.add(log, BorderLayout.CENTER);
        sur.add(lblEstado, BorderLayout.SOUTH);
        return sur;
    }

    // =====================================================================
    //  ACCIONES DE LOS BOTONES
    // =====================================================================

    private int prioridadElegida() {
        for (int pr = 1; pr <= 5; pr++) if (radios[pr].isSelected()) return pr;
        return 3;
    }

    private void registrar() {
        String nombre = txtNombre.getText().trim();
        String sintoma = txtSintoma.getText().trim();
        int edad;
        if (nombre.isEmpty() || sintoma.isEmpty()) {
            aviso("Escribe el nombre y el síntoma.");
            return;
        }
        try {
            edad = Integer.parseInt(txtEdad.getText().trim());
            if (edad < 0 || edad > 120) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            aviso("La edad debe ser un número entre 0 y 120.");
            return;
        }
        insertarPaciente(nombre, edad, sintoma, prioridadElegida());
        txtNombre.setText("");
        txtEdad.setText("");
        txtSintoma.setText("");
        txtNombre.requestFocus();
    }

    private void insertarPaciente(String nombre, int edad, String sintoma, int prioridad) {
        contadorLlegada++;
        heap.insertar(new Paciente(nombre, edad, sintoma, prioridad, contadorLlegada));
        actualizar();
    }

    private void atenderSiguiente() {
        Paciente p = heap.extraer();
        if (p == null) {
            aviso("No hay pacientes en espera.");
            return;
        }
        atendidos++;
        actualizar();
        JOptionPane.showMessageDialog(this,
                "Atendiendo a: " + p.nombre + "\nEdad: " + p.edad + "\nSíntoma: " + p.sintoma
                        + "\nPrioridad: " + p.prioridad + " " + NOMBRES_PRIORIDAD[p.prioridad],
                "Paciente atendido", JOptionPane.INFORMATION_MESSAGE);
    }

    private void verRaiz() {
        Paciente p = heap.verRaiz();
        if (p == null) {
            aviso("El heap está vacío.");
            return;
        }
        JOptionPane.showMessageDialog(this,
                "El siguiente en ser atendido es:\n\n" + p.nombre + " (" + p.edad + " años)\n"
                        + "Síntoma: " + p.sintoma + "\nPrioridad: " + p.prioridad + " "
                        + NOMBRES_PRIORIDAD[p.prioridad] + "\n\n(Ver la raíz no lo saca del heap)",
                "Paciente en la raíz", JOptionPane.INFORMATION_MESSAGE);
    }

    private void generar15() {
        Random r = new Random();
        for (int i = 0; i < 15; i++) {
            int pr = 1 + r.nextInt(5);
            String[] opciones = SINTOMAS[pr];
            insertarPaciente(NOMBRES[r.nextInt(NOMBRES.length)], 1 + r.nextInt(90),
                    opciones[r.nextInt(opciones.length)], pr);
        }
    }

    private void limpiarTodo() {
        heap.limpiar();
        contadorLlegada = 0;
        atendidos = 0;
        txtLog.setText("");
        actualizar();
    }

    /** Compara el orden del heap contra una cola FIFO (primero en llegar, primero en atenderse). */
    private void compararFifo() {
        if (heap.estaVacio()) {
            aviso("Registra o genera pacientes primero.");
            return;
        }
        ArrayList<Paciente> conHeap = heap.ordenReal();
        ArrayList<Paciente> fifo = new ArrayList<>(conHeap);
        fifo.sort((a, b) -> a.orden - b.orden);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-4s %-22s %-22s%n", "Pos", "CON HEAP", "COLA FIFO"));
        sb.append("-".repeat(50)).append("\n");
        for (int i = 0; i < conHeap.size(); i++) {
            sb.append(String.format("%-4d %-22s %-22s%n", i + 1, conHeap.get(i), fifo.get(i)));
        }
        sb.append("\nPosición promedio de los pacientes Crítico/Grave (4 y 5):\n");
        sb.append(String.format("  Con heap : %.1f%n", promedioGraves(conHeap)));
        sb.append(String.format("  Con FIFO : %.1f%n", promedioGraves(fifo)));
        sb.append("\n(Número menor = se atienden antes)");

        JTextArea area = new JTextArea(sb.toString());
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new Dimension(470, 420));
        JOptionPane.showMessageDialog(this, sp, "Heap vs FIFO", JOptionPane.PLAIN_MESSAGE);
    }

    private double promedioGraves(ArrayList<Paciente> lista) {
        int suma = 0, cuenta = 0;
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).prioridad >= 4) {
                suma += i + 1;
                cuenta++;
            }
        }
        return cuenta == 0 ? 0 : (double) suma / cuenta;
    }

    // =====================================================================
    //  ACTUALIZAR PANTALLA
    // =====================================================================

    private void actualizar() {
        cola = heap.ordenReal();
        modeloTabla.setRowCount(0);
        for (int i = 0; i < cola.size(); i++) {
            Paciente p = cola.get(i);
            modeloTabla.addRow(new Object[]{i + 1, p.nombre, p.prioridad, p.sintoma});
        }
        panelArbol.repaint();
        lblEstado.setText("  En espera: " + heap.tamano() + "    Atendidos: " + atendidos
                + "    Comparaciones totales: " + heap.getComparaciones());
    }

    private void escribirLog(String linea) {
        txtLog.append(linea + "\n");
        txtLog.setCaretPosition(txtLog.getDocument().getLength());
    }

    private void aviso(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Aviso", JOptionPane.WARNING_MESSAGE);
    }

    // =====================================================================
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignorar) { }
        SwingUtilities.invokeLater(() -> new fmrPrin().setVisible(true));
    }
}
