package twophases;

import java.util.Scanner;

public class Implementation {

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);

        //Ingresamos el numero de variables del problema
        System.out.println("Ingresa el numero de variables a utilizar: ");
        int numberVariables = in.nextInt();
        //Ingresamos el numero de restricciones
        System.out.println("Ingrese el numero de restricciones: ");
        int numberConstraints = in.nextInt();

        //Pedimos valores de la funcion objetivo
        System.out.println("-------Ingresa los valores de la funcion objetivo-------");
        double[] z = new double[numberVariables];
        int answer = 0;
        do {
            if (answer == 2) {
                System.out.println("Introduce los datos nuevamente: ");
            }
            //Llenamos los valores
            for (int i = 0; i < numberVariables; i++) {
                System.out.print("X" + (i + 1) + ":");
                z[i] = in.nextDouble();
            }

            System.out.println("¿Ingresaste los valores correctos?");
            System.out.println("SI = 1 || NO = 2");
            answer = in.nextInt();
        } while (answer == 2);

        Constraint[] constraints = new Constraint[numberConstraints];
        double[] coeficients = new double[numberVariables];
        double value, solution;
        String operator = "";
        //Creamos restricciones
        do {
            System.out.println("Ingresa los valores para las restricciones");
            if (answer == 2) {
                System.out.println("Introduce los datos nuevamente: ");
            }
            for (int i = 0; i < numberConstraints; i++) {
                for (int j = 0; j < coeficients.length; j++) {
                    System.out.print("X" + (j + 1) + ": ");
                    coeficients[j] = in.nextDouble();
                }
                System.out.println("Selecciona el tipo de desigualdad o igualdad: >= Numero #1 ||<= #2 || = #3");
                int option = in.nextInt();
                switch (option) {
                    case 1:
                        operator = ">";
                        break;
                    case 2:
                        operator = "<";
                        break;
                    case 3:
                        operator = "=";
                        break;
                }
                System.out.print("Ingresa el valor del termino independiente de la restriccion: ");
                solution = in.nextDouble();
                //Creamos la restriccion
                constraints[i] = new Constraint(coeficients, operator, solution);
                System.out.println("----------------------------------------");
            }
            System.out.println("¿Ingresaste los valores correctos?");
            System.out.println("SI = 1 || NO = 2");
            answer = in.nextInt();
        } while (answer == 2);

        for (int i = 0; i < numberVariables; i++) {
            System.out.println(z[i]);
        }

        for (int i = 0; i < constraints.length; i++) {
            System.out.println(constraints[i].Coeficients[i]);
        }

    }
}
