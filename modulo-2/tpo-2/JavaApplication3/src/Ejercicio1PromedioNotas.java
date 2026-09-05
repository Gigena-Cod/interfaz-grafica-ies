import java.util.Scanner;

/*
 * Ejercicio 1 - Promedio de Notas
 *
 * El programa solicita la cantidad de estudiantes,
 * almacena sus notas en un arreglo y calcula el promedio.
 */
public class Ejercicio1PromedioNotas {

    public static void main(String[] args) {

        // Scanner permite ingresar datos por teclado.
        Scanner teclado = new Scanner(System.in);

        // Solicitar la cantidad de estudiantes.
        System.out.print("Ingrese la cantidad de estudiantes: ");
        int cantidadEstudiantes = teclado.nextInt();

        // Verificar que la cantidad ingresada sea válida.
        if (cantidadEstudiantes <= 0) {
            System.out.println("La cantidad de estudiantes debe ser mayor que cero.");
            teclado.close();
            return;
        }

        // Crear un arreglo para almacenar las notas.
        double[] notas = new double[cantidadEstudiantes];

        // Variable acumuladora para sumar todas las notas.
        double sumaNotas = 0;

        // Recorrer el arreglo para ingresar las notas.
        for (int i = 0; i < notas.length; i++) {

            System.out.print("Ingrese la nota del estudiante " + (i + 1) + ": ");

            // Guardar la nota en la posición correspondiente del arreglo.
            notas[i] = teclado.nextDouble();

            // Acumular la nota para luego calcular el promedio.
            sumaNotas += notas[i];
        }

        // Calcular el promedio utilizando la cantidad de elementos del arreglo.
        double promedio = sumaNotas / notas.length;

        // Mostrar el resultado.
        System.out.println("El promedio de las notas es: " + promedio);

        // Cerrar Scanner.
        teclado.close();
    }
}