package registroexpress;

import java.util.Scanner;

/**
 * TPO 1 - RegistroExpress
 *
 * Console application developed in five stages: greeting and personal data,
 * arithmetic and logical operations, storage of three interests, and the
 * presentation of a final registration ticket.
 *
 * Each stage introduces basic Java concepts such as variables, console input,
 * conditionals, arrays, operators, and formatted output.
 *
 * @author Nahuel Gigena
 */
public class RegistroExpress {

    /**
     * Runs every stage of the RegistroExpress registration flow.
     *
     * @param args command-line arguments; they are not used by this program
     */
    public static void main(String[] args) {
        // Scanner reads all information entered by the user in the console.
        Scanner keyboard = new Scanner(System.in);

        // -------------------------------------------------
        // Stage 1: Greeting and welcome
        // -------------------------------------------------

        System.out.print("Enter a greeting: ");
        String greeting = keyboard.nextLine();

        System.out.print("Enter your full name: ");
        String fullName = keyboard.nextLine();

        System.out.println(
                greeting + " " + fullName + "! Welcome to RegistroExpress."
        );

        // -------------------------------------------------
        // Stage 2: Personal information
        // -------------------------------------------------

        System.out.print("Enter your age: ");
        int age = keyboard.nextInt();

        // Consume the line break left by nextInt before reading the city.
        keyboard.nextLine();

        System.out.print("Enter your city: ");
        String city = keyboard.nextLine();

        int nextYearAge = age + 1;
        boolean isAdult = age >= 18;

        // -------------------------------------------------
        // Stage 3: Arithmetic and logical operations
        // -------------------------------------------------

        System.out.print("Enter number a: ");
        int a = keyboard.nextInt();

        System.out.print("Enter number b: ");
        int b = keyboard.nextInt();

        int sum = a + b;
        int subtraction = a - b;
        int product = a * b;

        // Dividing by 2.0 produces a decimal average.
        double average = (a + b) / 2.0;

        boolean aGreaterThanB = a > b;
        boolean aEqualsB = a == b;
        boolean aDifferentFromB = a != b;

        // Both remainders must be zero for the two numbers to be even.
        boolean bothEven = (a % 2 == 0) && (b % 2 == 0);

        // Text values allow undefined results to be shown when b is zero.
        String quotient;
        String remainder;
        if (b != 0) {
            quotient = String.valueOf(a / b);
            remainder = String.valueOf(a % b);
        } else {
            quotient = "undefined";
            remainder = "undefined";
        }

        // Consume the line break before reading interests with nextLine.
        keyboard.nextLine();

        // -------------------------------------------------
        // Stage 4: Interests array
        // -------------------------------------------------

        // The assignment requires exactly three interests.
        String[] interests = new String[3];

        System.out.print("Enter your first interest: ");
        interests[0] = keyboard.nextLine();

        System.out.print("Enter your second interest: ");
        interests[1] = keyboard.nextLine();

        System.out.print("Enter your third interest: ");
        interests[2] = keyboard.nextLine();

        // -------------------------------------------------
        // Stage 5: Final registration ticket
        // -------------------------------------------------

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

        // Release the console input resource after completing every stage.
        keyboard.close();
    }
}
