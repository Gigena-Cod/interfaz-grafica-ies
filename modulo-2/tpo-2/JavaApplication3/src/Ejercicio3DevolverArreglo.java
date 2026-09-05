/*
 * Ejercicio 3 - Devolver Arreglo
 *
 * El programa recibe dos vectores mediante un método,
 * copia todos sus valores en un tercer vector
 * y los ordena de menor a mayor.
 */
public class Ejercicio3DevolverArreglo {

    /*
     * El método recibe dos arreglos de números enteros
     * y devuelve un nuevo arreglo con todos sus valores
     * ordenados de menor a mayor.
     */
    public static int[] DevolverArreglo(int[] Vector1, int[] Vector2) {

        // Crear un nuevo vector con espacio suficiente
        // para guardar los elementos de los dos vectores.
        int[] VectorDevuelto =
                new int[Vector1.length + Vector2.length];

        // Posición utilizada para cargar el nuevo vector.
        int posicion = 0;

        // Copiar los elementos de Vector1.
        for (int i = 0; i < Vector1.length; i++) {

            VectorDevuelto[posicion] = Vector1[i];
            posicion++;
        }

        // Copiar los elementos de Vector2.
        for (int i = 0; i < Vector2.length; i++) {

            VectorDevuelto[posicion] = Vector2[i];
            posicion++;
        }

        // Ordenar los elementos de menor a mayor.
        for (int i = 0; i < VectorDevuelto.length - 1; i++) {

            for (int j = i + 1; j < VectorDevuelto.length; j++) {

                // Si el elemento de la derecha es menor,
                // se intercambian ambos valores.
                if (VectorDevuelto[j] < VectorDevuelto[i]) {

                    int auxiliar = VectorDevuelto[i];

                    VectorDevuelto[i] = VectorDevuelto[j];

                    VectorDevuelto[j] = auxiliar;
                }
            }
        }

        // Devolver el arreglo ya ordenado.
        return VectorDevuelto;
    }

    public static void main(String[] args) {

        // Crear los dos vectores indicados en el ejemplo.
        int[] Vector1 = {1, 4, 7};
        int[] Vector2 = {2, 3, 8};

        // Llamar al método y guardar el arreglo que devuelve.
        int[] VectorDevuelto = DevolverArreglo(Vector1, Vector2);

        // Mostrar el arreglo ordenado.
        System.out.print("VectorDevuelto = {");

        for (int i = 0; i < VectorDevuelto.length; i++) {

            System.out.print(VectorDevuelto[i]);

            // Agregar coma excepto después del último elemento.
            if (i < VectorDevuelto.length - 1) {
                System.out.print(", ");
            }
        }

        System.out.println("}");
    }
}