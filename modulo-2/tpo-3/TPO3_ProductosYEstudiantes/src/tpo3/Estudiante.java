package tpo3;

public class Estudiante {

    private String nombre;
    private int edad;
    private double calificacionPromedio;

    public Estudiante(String nombre, int edad, double calificacionPromedio) {
        this.nombre = nombre;
        this.edad = edad;
        this.calificacionPromedio = calificacionPromedio;
    }

    public String getNombre() {
        return nombre;
    }

    public int getEdad() {
        return edad;
    }

    public double getCalificacionPromedio() {
        return calificacionPromedio;
    }

    public double calcularCalificacionPromedio() {
        return calificacionPromedio;
    }
}
