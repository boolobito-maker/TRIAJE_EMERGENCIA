import javax.swing.*;
import java.awt.*;

/** Dibuja el heap como un árbol binario. Cada vez que cambia el heap se llama repaint(). */
public class PanelArbol extends JPanel {
    private final MaxHeap heap;

    public PanelArbol(MaxHeap heap) {
        this.heap = heap;
        setBackground(Color.WHITE);
    }

    /** Color según prioridad (se usa también en la tabla). */
    public static Color colorPrioridad(int p) {
        switch (p) {
            case 5:  return new Color(0xE5, 0x39, 0x35); // rojo
            case 4:  return new Color(0xFB, 0x8C, 0x00); // naranja
            case 3:  return new Color(0xFD, 0xD8, 0x35); // amarillo
            case 2:  return new Color(0x66, 0xBB, 0x6A); // verde
            default: return new Color(0x42, 0xA5, 0xF5); // azul
        }
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        int n = heap.tamano();
        if (n == 0) return;

        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // cuántos niveles tiene el árbol
        int niveles = 1;
        while ((1 << niveles) - 1 < n) niveles++;

        int radio = Math.max(10, Math.min(26, getWidth() / ((1 << (niveles - 1)) * 2 + 1)));
        int altoNivel = Math.min(105, (getHeight() - 2 * radio - 20) / Math.max(1, niveles - 1 + 1));

        // 1) primero las líneas (así quedan detrás de los círculos)
        g.setColor(Color.GRAY);
        g.setStroke(new BasicStroke(2f));
        for (int i = 1; i < n; i++) {
            Point hijo = posicion(i, altoNivel, radio);
            Point padre = posicion((i - 1) / 2, altoNivel, radio);
            g.drawLine(padre.x, padre.y, hijo.x, hijo.y);
        }

        // 2) luego los nodos
        g.setStroke(new BasicStroke(1f));
        Font normal = new Font("SansSerif", Font.BOLD, Math.max(9, radio / 2));
        Font chica = new Font("SansSerif", Font.PLAIN, Math.max(8, radio / 3));
        for (int i = 0; i < n; i++) {
            Paciente p = heap.get(i);
            Point c = posicion(i, altoNivel, radio);

            g.setColor(colorPrioridad(p.prioridad));
            g.fillOval(c.x - radio, c.y - radio, radio * 2, radio * 2);
            g.setColor(new Color(60, 60, 60));
            g.drawOval(c.x - radio, c.y - radio, radio * 2, radio * 2);

            String nombre = p.nombre.length() > 5 ? p.nombre.substring(0, 5) : p.nombre;
            g.setColor(Color.BLACK);
            g.setFont(normal);
            centrar(g, nombre, c.x, c.y - 2);
            g.setFont(chica);
            centrar(g, "[" + i + "]", c.x, c.y + radio / 2 + 2);
        }
    }

    /** Posición (x,y) del centro del nodo i. */
    private Point posicion(int i, int altoNivel, int radio) {
        int nivel = 0;
        while ((1 << (nivel + 1)) - 1 <= i) nivel++;
        int primeroDelNivel = (1 << nivel) - 1;
        int indiceEnNivel = i - primeroDelNivel;
        int nodosEnNivel = 1 << nivel;

        int x = (int) ((indiceEnNivel + 0.5) * getWidth() / nodosEnNivel);
        int y = radio + 15 + nivel * altoNivel;
        return new Point(x, y);
    }

    private void centrar(Graphics2D g, String texto, int x, int y) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(texto, x - fm.stringWidth(texto) / 2, y + fm.getAscent() / 2 - 1);
    }
}
