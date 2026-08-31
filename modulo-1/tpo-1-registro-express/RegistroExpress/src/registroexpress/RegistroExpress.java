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

        System.out.println("Greeting: " + greeting);
        System.out.println("Name: " + fullName);
        System.out.println("Age: " + age);
        System.out.println("City: " + city);
        System.out.println("Next year: " + nextYearAge);
        System.out.println("Adult: " + isAdult);
        System.out.println("sum=" + sum);
        System.out.println("subtraction=" + subtraction);
        System.out.println("product=" + product);
        System.out.println("quotient=" + quotient);
        System.out.println("remainder=" + remainder);
        System.out.println("average=" + average);
        System.out.println("a>b=" + aGreaterThanB);
        System.out.println("a==b=" + aEqualsB);
        System.out.println("a!=b=" + aDifferentFromB);
        System.out.println("bothEven=" + bothEven);

        keyboard.close();
    }
}
