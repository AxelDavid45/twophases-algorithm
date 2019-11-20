package twophases;

import java.util.Vector;

public class Table {
    double[][] Matrix; //Contiene los datos para hacer iteraciones
    Vector Solutions; //Contiene las soluciones de cada fila el ultimo elemento es la sol de R o Z
    Constraint[] Constraints; //Contiene las restricciones
    int nArtificial = 0, nSlack = 0; //Guarda el numero de variables artificiales y holgura utilizadas
    Objective ZObjective = null, RObjective = null; //Contiene la funcion objetivo o la R
    int enteringColumn, leavingRow; //Contiene la posicion de la columna que entra y la fila que sale
    int nRows, nColumns; //Numero de filas y columnas que tendra la matriz
    
    public Table(Constraint[] constraints, Objective fObjective) {
        //Inicializamos el arreglo solutions de tamano constraints + 1 donde 1 es la Objective Z o R
        this.Solutions = new Vector(constraints.length + 1);
        //Recorremos el array y contabilizamos cuantas varibles de holgura o artificales tenemos
        for(Constraint x: constraints) {
            if (x.hasVArtificial() && x.hasVSlack()) {
                this.nArtificial++;
                this.nSlack++;
            } else if (x.hasVSlack()) 
                this.nSlack++;
            else if (x.hasVArtificial())
                this.nArtificial++;
            //Guardamos las soluciones de cada restriccion en el arreglo Solutions
            this.Solutions.addElement(x.getSolution());
        }
        //Agregamos la solucion inicial de Z que es 0 al final del vector
        this.Solutions.addElement(0);
        //Asignamos las constantes al arreglo de constantes
        this.Constraints = constraints;
        //Asignamos la funcion objetivo Z
        this.ZObjective = fObjective;
    }
    
    /*
        Build matrix nos servira para poder construir la matriz y despues a partir de los
        metodos phase1 y doSimplex modificarla para hacer el algoritmo dependiendo de si tiene 
        variables artificiales o no
    */
    public void buildMatrix() {
        //Asignamos el tamano de nuestra matriz usando la siguiente regla
        //Las columnas seran de acuerdo al numero de coeficientes que tenga la funcion Z + vArtifiales + VSlack + 1, 1 que sera la columna de soluciones
        //Las filas sera el numero de restricciones + 1, donde 1 es la Objective
        this.setnRows(this.Constraints.length + 1);
        this.setnColumns(this.ZObjective.numberCoeficients() + this.nArtificial + this.nSlack + 1);
        this.Matrix = new double[this.getnRows()][this.getnColumns()];
        //Como de principio Objective no cuenta con valores en las columnas de vSlack o VArtificial agregamos valor 0
        for (int i = 0; i < 1; i++)
            for (int j = this.ZObjective.numberCoeficients(); j < nColumns; j++)
                this.ZObjective.coeficients.add((double) 0);
            
        //Introducimos la fObjective Z por default
        for (int i = 0; i < 1; i++) {
            for (int j = 0; j < this.getnColumns(); j++) {
                this.Matrix[i][j] = this.ZObjective.coeficients.get(j); 
            }
        }
        //Variable auxiliar que contiene el # de coeficientes de la restriccion
        int counterCoeficients = this.Constraints[0].getNumberCoeficients();
        int indexConstraint = 0, IndexConstraints = 0;
        //Llenamos la matriz con los datos que ya tenemos comenzando una fila
        for (int i = 1; i <= this.getnRows() - 1; i++) {
            for (int j = 0; j < this.getnColumns(); j++) {
                if (indexConstraint < counterCoeficients)
                    this.Matrix[i][j] = this.Constraints[IndexConstraints].Coeficients[indexConstraint];    
                indexConstraint++; //Aumentamos el indice para ir cambiando de coeficiente
            }
            indexConstraint = 0; //Reseteamos el indice para la siguiente restriccion
            IndexConstraints++; //Aumentamos la posicion del indice para recorrer el array restricciones
        }
    }
    
    public void doSimplex(boolean type) {
        
    }
    
    public void phase1() {
        
    }
    
    public void replaceObjective(Objective x) {
        
    }
    
    public void updateTable() {
        
    }
    
    public void isTie() {
        
    }
    
    public void isThereSolution() {
        
    }

    public Vector getSolutions() {
        return Solutions;
    }

    public int getnArtificial() {
        return nArtificial;
    }

    public int getnSlack() {
        return nSlack;
    }

    public int getEnteringColumn() {
        return enteringColumn;
    }

    public int getLeavingRow() {
        return leavingRow;
    }
   
    public void showResult() {}

    public int getnRows() {
        return nRows;
    }

    public void setnRows(int nRows) {
        this.nRows = nRows;
    }

    public int getnColumns() {
        return nColumns;
    }

    public void setnColumns(int nColumns) {
        this.nColumns = nColumns;
    }
    
}
