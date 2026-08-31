/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package module1practice;

import java.util.Scanner;

/**
 * Exercise 2 - Temperature Converter.
 *
 * This program asks the user to enter a temperature in Celsius.
 * Then, it converts the temperature to Fahrenheit and displays the result.
 *
 * Formula: F = (C * 9 / 5) + 32
 *
 * @author Nahuel Gigena
 */
public class TemperatureConverter {

    /**
     * Main method. The execution of the program starts here.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {

        // Create a Scanner object to read data entered by the user.
        Scanner keyboard = new Scanner(System.in);

        // Ask the user to enter the temperature in Celsius.
        System.out.print("Enter the temperature in Celsius: ");
        double celsius = keyboard.nextDouble();

        // Convert the temperature from Celsius to Fahrenheit.
        double fahrenheit = (celsius * 9.0 / 5.0) + 32;

        // Display the result.
        System.out.println("Temperature in Fahrenheit: " + fahrenheit);

        // Close the Scanner because it is no longer needed.
        keyboard.close();
    }
}