/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package module1practice;

import java.util.Scanner;

/**
 * Exercise 3 - Body Mass Index Calculator.
 *
 * This program asks the user to enter their weight in kilograms
 * and their height in meters.
 * Then, it calculates and displays the Body Mass Index (BMI).
 *
 * Formula: BMI = weight / (height * height)
 *
 * @author Nahuel Gigena
 */
public class BodyMassIndex {

    /**
     * Main method. The execution of the program starts here.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {

        // Create a Scanner object to read data entered by the user.
        Scanner keyboard = new Scanner(System.in);

        // Ask the user to enter their weight in kilograms.
        System.out.print("Enter your weight in kilograms: ");
        double weight = keyboard.nextDouble();

        // Ask the user to enter their height in meters.
        System.out.print("Enter your height in meters: ");
        double height = keyboard.nextDouble();

        // Calculate the Body Mass Index (BMI).
        double bmi = weight / (height * height);

        // Display the result.
        System.out.println("Your Body Mass Index (BMI) is: " + bmi);

        // Close the Scanner because it is no longer needed.
        keyboard.close();
    }
}