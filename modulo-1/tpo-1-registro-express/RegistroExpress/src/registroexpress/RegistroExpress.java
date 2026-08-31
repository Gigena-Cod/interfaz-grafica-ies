package registroexpress;

import java.util.Scanner;

/**
 * TPO 1 - RegistroExpress
 *
 * @author Nahuel Gigena
 */
public class RegistroExpress {

    public static void main(String[] args) {
        Scanner keyboard = new Scanner(System.in);

        System.out.print("Enter a greeting: ");
        String greeting = keyboard.nextLine();

        System.out.print("Enter your full name: ");
        String fullName = keyboard.nextLine();

        System.out.println(
                greeting + " " + fullName + "! Welcome to RegistroExpress."
        );

        System.out.print("Enter your age: ");
        int age = keyboard.nextInt();
        keyboard.nextLine();

        System.out.print("Enter your city: ");
        String city = keyboard.nextLine();

        int nextYearAge = age + 1;
        boolean isAdult = age >= 18;

        System.out.print("Enter number a: ");
        int a = keyboard.nextInt();

        System.out.print("Enter number b: ");
        int b = keyboard.nextInt();

        int sum = a + b;
        int subtraction = a - b;
        int product = a * b;
        double average = (a + b) / 2.0;

        boolean aGreaterThanB = a > b;
        boolean aEqualsB = a == b;
        boolean aDifferentFromB = a != b;
        boolean bothEven = (a % 2 == 0) && (b % 2 == 0);

        String quotient;
        String remainder;
        if (b != 0) {
            quotient = String.valueOf(a / b);
            remainder = String.valueOf(a % b);
        } else {
            quotient = "undefined";
            remainder = "undefined";
        }

        keyboard.nextLine();

        String[] interests = new String[3];

        System.out.print("Enter your first interest: ");
        interests[0] = keyboard.nextLine();

        System.out.print("Enter your second interest: ");
        interests[1] = keyboard.nextLine();

        System.out.print("Enter your third interest: ");
        interests[2] = keyboard.nextLine();

        System.out.println();
        System.out.println("===== RegistroExpress =====");
        System.out.println("Greeting: " + greeting);
        System.out.println(
                "User: " + fullName
                + " (" + age
                + " -> next year: " + nextYearAge
                + ") - " + city
        );
        System.out.println(
                "Numbers a=" + a
                + ", b=" + b
                + " -> sum=" + sum
                + ", subtraction=" + subtraction
                + ", product=" + product
                + ", quotient=" + quotient
                + ", remainder=" + remainder
                + ", average=" + average
        );
        System.out.println(
                "Logical -> a>b=" + aGreaterThanB
                + ", a==b=" + aEqualsB
                + ", a!=b=" + aDifferentFromB
                + ", bothEven=" + bothEven
        );
        System.out.println(
                "Interests: "
                + interests[0] + " | "
                + interests[1] + " | "
                + interests[2]
        );
        System.out.println("Adult: " + isAdult);
        System.out.println("===========================");

        keyboard.close();
    }
}
