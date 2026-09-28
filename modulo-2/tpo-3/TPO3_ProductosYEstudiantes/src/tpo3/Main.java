package tpo3;

import java.awt.GridLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Main {

    public static void main(String[] args) {

        // =========================================================
        // PROBLEMA 1 - TIENDA DE PRODUCTOS
        // =========================================================

        Producto producto1 = new Producto("Teclado", 25000.00, 5);
        Producto producto2 = new Producto("Mouse", 15000.00, 8);
        Producto producto3 = new Producto("Monitor", 180000.00, 3);

        Producto[] productos = {producto1, producto2, producto3};

        System.out.println("=== TIENDA DE PRODUCTOS ===");

        for (int i = 0; i < productos.length; i++) {

            Producto producto = productos[i];

            System.out.println("Nombre: " + producto.getNombre());
            System.out.println("Precio: $" + producto.getPrecio());
            System.out.println("Cantidad en stock: " + producto.getCantidadEnStock());
            System.out.println("Valor total en stock: $" + producto.calcularValorTotal());
            System.out.println("--------------------------------");
        }

        mostrarProductosEnFormulario(productos);

        // =========================================================
        // PROBLEMA 2 - REGISTRO DE ESTUDIANTES
        // =========================================================

        Estudiante[] estudiantes = {
            new Estudiante("Ana", 20, 8.50),
            new Estudiante("Juan", 22, 7.25),
            new Estudiante("Lucia", 19, 9.00)
        };

        double sumaCalificaciones = 0;

        for (int i = 0; i < estudiantes.length; i++) {
            sumaCalificaciones += estudiantes[i].calcularCalificacionPromedio();
        }

        double promedioGeneral = sumaCalificaciones / estudiantes.length;

        System.out.println();
        System.out.println("=== REGISTRO DE ESTUDIANTES ===");
        System.out.println("Promedio general de calificaciones: " + promedioGeneral);

        // Se selecciona un estudiante en particular para mostrarlo en un formulario.
        Estudiante estudianteSeleccionado = estudiantes[0];

        mostrarEstudianteEnFormulario(estudianteSeleccionado);
    }

    public static void mostrarProductosEnFormulario(Producto[] productos) {

        JFrame ventana = new JFrame("Tienda de Productos");
        ventana.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(productos.length * 4, 1));

        for (int i = 0; i < productos.length; i++) {

            Producto producto = productos[i];

            panel.add(new JLabel("Nombre: " + producto.getNombre()));
            panel.add(new JLabel("Precio: $" + producto.getPrecio()));
            panel.add(new JLabel("Cantidad en stock: " + producto.getCantidadEnStock()));
            panel.add(new JLabel("Valor total: $" + producto.calcularValorTotal()));
        }

        ventana.add(panel);
        ventana.pack();
        ventana.setLocationRelativeTo(null);
        ventana.setVisible(true);
    }

    public static void mostrarEstudianteEnFormulario(Estudiante estudiante) {

        JFrame ventana = new JFrame("Información del Estudiante");
        ventana.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 1));

        panel.add(new JLabel("Nombre: " + estudiante.getNombre()));
        panel.add(new JLabel("Edad: " + estudiante.getEdad()));
        panel.add(new JLabel("Calificación promedio: "
                + estudiante.getCalificacionPromedio()));

        ventana.add(panel);
        ventana.pack();
        ventana.setLocationRelativeTo(null);
        ventana.setVisible(true);
    }
}
