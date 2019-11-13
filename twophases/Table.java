package twophases;


public class Table {
    double[][] Matrix; //Contiene los datos para hacer iteraciones
    double[] Solutions; //Contiene las soluciones de cada fila
    double[][] Constraints; //Contiene las restricciones
    Objective FObjective; //Contiene la funcion objetivo
    int enteringColumn, leavingRow; //Contiene la posicion de la columna que entra y la fila que sale
}
