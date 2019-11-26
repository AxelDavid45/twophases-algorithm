package twophases;

import java.util.Scanner;

public class Implementation {

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int answer = 0;
        do {

            //Ingresamos el numero de variables del problema
            System.out.println("Ingresa el numero de variables a utilizar: ");
            int numberVariables = in.nextInt();
            //Ingresamos el numero de restricciones
            System.out.println("Ingrese el numero de restricciones: ");
            int numberConstraints = in.nextInt();

            //Pedimos valores de la funcion objetivo
            System.out.println("-------Ingresa los valores de la funcion objetivo-------");
            double[] z = new double[numberVariables];

            int type = 0;
            do {
                if (answer == 2) {
                    System.out.println("Introduce los datos nuevamente: ");
                }
                //Tipo de optimizacion

                //Llenamos los valores
                for (int i = 0; i < numberVariables; i++) {
                    System.out.print("X" + (i + 1) + ":");
                    z[i] = in.nextDouble();
                }
                System.out.println("Ingresa tipo de optimizacion: Minimizar = 1 || Maximizar = 2");
                type = in.nextInt();
                System.out.println("¿Ingresaste los valores correctos?");
                System.out.println("SI = 1 || NO = 2");
                answer = in.nextInt();
            } while (answer == 2);

            //Creamos la funcion objetivo
            ZObjective ZFunction = new ZObjective(z, type);

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
                    do {
                        if(operator.equals("x"))
                            System.out.println("Incorrecto, introduce el dato nuevamente");
                        System.out.println("Selecciona el tipo de desigualdad o igualdad: >=  #1 ||<= #2 || = #3");
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
                            default:
                                operator = "x";
                                break;
                        }
                    } while (operator.equals("x"));
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

            //Creamos el objeto tabla para pasar a standard
            Table table = new Table(constraints, ZFunction);

            //Construimos la matriz inicial
            table.buildMatrixArtificials();
            //A partir de esto, determinamos si nuestro problema tiene variables artificiales, si tiene variables artificiales comenzamos la fase 1
            if (table.getnArtificial() > 0) {
                System.out.println("Resolviendo por 2 fases");

                //Creamos funcion artificial
                RObjective rFunction = new RObjective(table.getnArtificial());
                //Remplazamos Z por R en tabloide
                table.replaceRObjective(rFunction);
                //Imprimimos las matrices
                System.out.println("------Tablas antes de comenzar fase 1--------");
                Implementation.printMatrixArtificial(table);
                Implementation.printSlacks(table);
                Implementation.printArtificials(table);
                Implementation.printSolutions(table);
                //Comenzamos fase 1
                System.out.println("----------Comezando fase 1-----------");
                table.phase1();
                System.out.println("----Tablas finales fase 1----------");
                Implementation.printMatrixArtificial(table);
                Implementation.printSlacks(table);
                Implementation.printArtificials(table);
                Implementation.printSolutions(table);
                //Comenzamos simplex
                System.out.println("----Comenzamos fase 2 (simplex)-----");
                table.doSimplex();
                System.out.println("----Tablas finales fase 2----------");
                Implementation.printMatrixArtificial(table);
                Implementation.printSlacks(table);
                Implementation.printArtificials(table);
                Implementation.printSolutions(table);

//                Implementation.printFinalSolutions(table);
            } else {
                System.out.println("INGRESA UN PROBLEMA CON VARIABLES ARTIFICIALES");
            }

            System.out.println("¿Deseas repetir el programa? SI = 1 || NO = 2");
        } while (answer == 1);
        System.exit(0);

    }

    public static void printSlacks(Table t) {
        System.out.println("---");
        System.out.println("Slack");
        for (int i = 0; i < t.Slacks.length; i++) {
            for (int j = 0; j < t.Slacks[0].length; j++) {
                System.out.print(t.Slacks[i][j] + " ");
            }
            System.out.println("");
        }
    }

    public static void printArtificials(Table t) {
        System.out.println("---");
        System.out.println("Artificiales");
        for (int i = 0; i < t.Artificial.length; i++) {
            for (int j = 0; j < t.Artificial[0].length; j++) {
                System.out.print(t.Artificial[i][j] + " ");
            }
            System.out.println("");
        }
    }

    public static void printMatrixArtificial(Table t) {
        System.out.println("----------------");
        System.out.println("Coeficienes artificial");
        for (int i = 0; i < t.getnRows(); i++) {
            for (int j = 0; j < t.getnColumns(); j++) {
                System.out.print("| " + t.MatrixArtificial[i][j] + "    ");
            }
            System.out.println("");
        }
    }

    public static void printSolutions(Table t) {
        System.out.println("---");
        System.out.println("Soluciones");
        for (int i = 0; i < t.Solutions.size(); i++) {
            System.out.println(t.Solutions.get(i));
        }
    }

//    public static void printFinalSolutions(Table t) {
//        System.out.println("Soluciones finales");
//        for (int i = 0; i < 1; i++) {
//            for (int j = 0; j < t.finalSolutionsWRow[0].length; j++) {
//                System.out.println("X" + (j + 1) + ": " + t.finalSolutionsWRow[i][j]);
//            }
//        }
//        System.out.println("Sol Z = " + t.Solutions.lastElement());
//    }
}
