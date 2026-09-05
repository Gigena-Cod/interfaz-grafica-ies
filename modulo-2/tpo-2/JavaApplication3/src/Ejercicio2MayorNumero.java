import java.util.Scanner;

/*
 * Ejercicio 2 - Buscando el Mayor Número
 *
 * El programa solicita una cantidad de números enteros,
 * los almacena en un arreglo y luego muestra el mayor.
 */
public class Ejercicio2MayorNumero {

    public static void main(String[] args) {

        // Scanner permite ingresar datos por teclado.
        Scanner teclado = new Scanner(System.in);

        // Solicitar cuántos números se van a ingresar.
        System.out.print("Ingrese la cantidad de números: ");
        int cantidadNumeros = teclado.nextInt();

        // Verificar que la cantidad sea válida.
        if (cantidadNumeros <= 0) {
            System.out.println("La cantidad debe ser mayor que cero.");
            teclado.close();
            return;
        }

        // Crear el arreglo para almacenar los números.
        int[] numeros = new int[cantidadNumeros];

        // Recorrer el arreglo para cargar los números.
        for (int i = 0; i < numeros.length; i++) {

            System.out.print("Ingrese el número " + (i + 1) + ": ");

            // Guardar el número en la posición correspondiente.
            numeros[i] = teclado.nextInt();
        }

        // Tomar el primer elemento como el mayor inicialmente.
        int mayor = numeros[0];

        // Recorrer el arreglo buscando un número mayor.
        for (int i = 1; i < numeros.length; i++) {

            if (numeros[i] > mayor) {
                mayor = numeros[i];
            }
        }

        // Mostrar el número mayor encontrado.
        System.out.println("El número mayor es: " + mayor);

        // Cerrar Scanner.
        teclado.close();
    }
}