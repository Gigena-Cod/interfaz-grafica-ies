package module1practice;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import java.util.Scanner;

/**
 * Exercise 1 - Addition and Subtraction Calculator.
 *
 * This program asks the user to enter two integer numbers.
 * Then, it calculates and displays their sum and subtraction.
 *
 * @author Nahuel Gigena
 */
public class Calculator {

    /**
     * Main method. The execution of the program starts here.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {

        // Create a Scanner object to read data entered by the user.
        Scanner keyboard = new Scanner(System.in);

        // Ask the user to enter the first integer number.
        System.out.print("Enter the first number: ");
        int firstNumber = keyboard.nextInt();

        // Ask the user to enter the second integer number.
        System.out.print("Enter the second number: ");
        int secondNumber = keyboard.nextInt();

        // Calculate the sum of both numbers.
        int sum = firstNumber + secondNumber;

        // Calculate the subtraction of both numbers.
        int subtraction = firstNumber - secondNumber;

        // Display the results.
        System.out.println("Sum: " + sum);
        System.out.println("Subtraction: " + subtraction);

        // Close the Scanner because it is no longer needed.
        keyboard.close();
    }
}