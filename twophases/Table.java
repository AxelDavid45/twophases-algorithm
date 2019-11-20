package twophases;

import java.util.Vector;

public class Table {

    double[][] Matrix; //Contiene los datos para hacer iteraciones
    double[][] MatrixArtificial;
    double[][] Slacks;
    double[][] Artificial;
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
        for (Constraint x : constraints) {
            if (x.hasVArtificial() && x.hasVSlack()) {
                this.nArtificial++;
                this.nSlack++;
            } else if (x.hasVSlack()) {
                this.nSlack++;
            } else if (x.hasVArtificial()) {
                this.nArtificial++;
            }
            //Guardamos las soluciones de cada restriccion en el arreglo Solutions
            this.Solutions.addElement(x.getSolution());
        }
        
        //Creamos el tamaño de la matriz de holgura +1 para la funcion z o r
        this.Slacks = new double[constraints.length + 1][this.getnSlack()];
        //Agregamos los valores de las slacks a la matriz de forma escalonada, empezando en la fila 1
        int indexConstraints = 0; //Va deslizandose por los elementos del arreglo constraints
        int indexColMatrix = 0; // Va posicionando los valores de acuerdo a la restriccion que si tenga vSlack
        for (int i = 1; i < constraints.length +1; i++) {
            if (constraints[indexConstraints].hasVSlack()) { //Comprobamos si tiene variable slack -1 o 1
                this.Slacks[i][indexColMatrix] = constraints[indexConstraints].getvSlacks()[0];
                indexColMatrix++;
            }
            indexConstraints++;    
        }
        
        //Creamos la matriz de variables artificiales
        this.Artificial = new double[constraints.length + 1][this.getnArtificial()];
        //Agregamos los valores de las slacks a la matriz de forma escalonada, empezando en la fila 1
        indexConstraints = 0; //Va deslizandose por los elementos del arreglo constraints
        indexColMatrix = 0; // Va posicionando los valores de acuerdo a la restriccion que si tenga vSlack
        for (int i = 1; i < constraints.length +1; i++) {
            if (constraints[indexConstraints].hasVArtificial()) { //Comprobamos si tiene variable slack -1 o 1
                this.Artificial[i][indexColMatrix] = constraints[indexConstraints].getvArtificials()[0];
                indexColMatrix++;
            }
            indexConstraints++;    
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
    public void buildMatrixArtificials() {
        //Asignamos el tamano de la matriz tomando en cuenta la funcion artificial
          //Las filas sera el numero de restricciones + 1, donde 1 es la Objective
        this.setnRows(this.Constraints.length + 1);
        this.setnColumns(this.ZObjective.numberCoeficients());
        this.MatrixArtificial = new double[this.getnRows()][this.getnColumns()];
        int nElementsConstraints = 0, posConstraint = 0;
        for(int i = 1; i < this.getnRows(); i++) {
            for (int j = 0; j < this.getnColumns(); j++) {
                this.MatrixArtificial[i][j] = this.Constraints[nElementsConstraints].Coeficients[posConstraint];
                posConstraint++;
            }
            posConstraint = 0;
            nElementsConstraints++;
        }
        
    }
    
    public Vector searchNumOneInArtificials() {
        //Creamos el vector que contiene los indices de fila con 1
        Vector indexRows = new Vector(this.Artificial.length);
        //Recorremos la matriz para buscar
        for (int i = 1; i < this.Artificial.length; i++) {
            for (int j = 0; j < this.Artificial[0].length; j++) {
                //Comprobamos si esa fila contiene algun 1 y lo agregamos al vector
                if (this.Artificial[i][j] == 1) {
                    indexRows.add(i);
                }
                
            }
        }
        return indexRows;
    }
    
    public void buildMatrix() {
        //Asignamos el tamano de nuestra matriz usando la siguiente regla
        //Las columnas seran de acuerdo al numero de coeficientes que tenga la funcion Z + vArtifiales + VSlack + 1, 1 que sera la columna de soluciones
        //Las filas sera el numero de restricciones + 1, donde 1 es la Objective
        this.setnRows(this.Constraints.length + 1);
        this.setnColumns(this.ZObjective.numberCoeficients() + this.nArtificial + this.nSlack + 1);
        this.Matrix = new double[this.getnRows()][this.getnColumns()];
        //Como de principio Objective no cuenta con valores en las columnas de vSlack o VArtificial agregamos valor 0
        for (int i = 0; i < 1; i++) {
            for (int j = this.ZObjective.numberCoeficients(); j < nColumns; j++) {
                this.ZObjective.coeficients.add((double) 0);
            }
        }

        //Introducimos la fObjective Z por default
        for (int i = 0; i < 1; i++) {
            for (int j = 0; j < this.getnColumns(); j++) {
                this.Matrix[i][j] = this.ZObjective.coeficients.get(j);
            }
        }
        //Variable auxiliar que contiene el # de coeficientes de la restriccion
        int counterCoeficients = this.Constraints[0].getNumberCoeficients();
        int indexConstraint = 0, IndexConstraints = 0;
        //Llenamos la matriz con los datos que ya tenemos comenzando una fila abajo
        for (int i = 1; i <= this.getnRows() - 1; i++) {
            for (int j = 0; j < this.getnColumns(); j++) {
                //Comprueba que no rebasemos el limite de elementos de las constraints
                if (indexConstraint < counterCoeficients) {
                    this.Matrix[i][j] = this.Constraints[IndexConstraints].Coeficients[indexConstraint];
                } else {
                    //Comprobamos si hemos alcanzado la col de soluciones e insertamos la sol de cada constraint
                    this.Matrix[i][this.getnColumns() - 1] = this.Constraints[IndexConstraints].getSolution();
                }
                indexConstraint++; //Aumentamos el indice para ir cambiando de coeficiente
            }
            indexConstraint = 0; //Reseteamos el indice para la siguiente restriccion
            IndexConstraints++; //Aumentamos la posicion del indice para recorrer el array restricciones
        }

    }

    /*
        Metodo que remplaza la funcion objecito z o r dependiendo
     */
    public void replaceRObjective(Objective x) {
        for (int i = 0; i < 1; i ++) {
            for (int j = 0; j < this.Artificial[0].length; j++) {
                this.Artificial[i][j] = x.coeficients.get(j);
            }
        }
    }

    public void doSimplex(boolean type) {

    }

    public void phase1() {

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

    public void showResult() {
    }

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
