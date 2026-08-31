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

        System.out.println("Greeting: " + greeting);
        System.out.println("Name: " + fullName);
        System.out.println("Age: " + age);
        System.out.println("City: " + city);
        System.out.println("Next year: " + nextYearAge);
        System.out.println("Adult: " + isAdult);

        keyboard.close();
    }
}
