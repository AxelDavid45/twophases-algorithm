package twophases;

import java.util.Vector;

public class Table {

    double[][] MatrixArtificial;//Matriz de coeficientes de las varibles del problema
    double[][] Slacks;
    double[][] Artificial;
    //Final solutions con informacion de fila
    double[] finalSolutionsWRow;
    Vector Solutions; //Contiene las soluciones de cada fila el ultimo elemento es la sol de R o Z
    Constraint[] Constraints; //Contiene las restricciones
    int nArtificial = 0, nSlack = 0; //Guarda el numero de variables artificiales y holgura utilizadas
    Objective ZObjective = null, RObjective = null; //Contiene la funcion objetivo o la R
    int enteringColumn = 0, leavingRow = 0; //Contiene la posicion de la columna que entra y la fila que sale
    int nRows, nColumns; //Numero de filas y columnas que tendra la matriz
    //Contienen valores temporales para poder hacer operaciones de suma o resta de filas
    double[] tmpCoeficients;
    double[] tmpSlacks;
    double[] tmpArtificials;
    double tmpSolution;
    double[] pivotColumnPhase1;

    public Table(Constraint[] constraints, Objective fObjective) {
        //Inicializamos el arreglo solutions de tamano constraints + 1 donde 1 es la Objective Z o R
        this.Solutions = new Vector(constraints.length + 1);
        //Creamos la matriz de soluciones con informacion de la fila
        this.finalSolutionsWRow = new double[fObjective.coeficients.size()];
        this.pivotColumnPhase1 = new double[fObjective.coeficients.size()];
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
        for (int i = 1; i < constraints.length + 1; i++) {
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
        for (int i = 1; i < constraints.length + 1; i++) {
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
        for (int i = 1; i < this.getnRows(); i++) {
            for (int j = 0; j < this.getnColumns(); j++) {
                this.MatrixArtificial[i][j] = this.Constraints[nElementsConstraints].Coeficients[posConstraint];
                posConstraint++;
            }
            posConstraint = 0;
            nElementsConstraints++;
        }

    }

    /*
        Metodo que busca las filas que contengan variables artificiales en la matriz de 
        artificiales
     */
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

    public boolean isStoppablePhase1() {
        boolean stoppable = false;
        for (int i = 0; i < 1; i++) {
            for (int j = 0; j < this.Artificial[0].length; j++) {
                if (this.Artificial[i][j] == -1) {
                    stoppable = true;
                }
            }
        }
        return stoppable;
    }

    public boolean hasInitialSolution() {
        return Double.valueOf(this.Solutions.lastElement().toString()) == 0 && this.isStoppablePhase1();
    }

    //Comienza a realizar la fase 1 del metodo dos fases
    public void phase1() {
        //Creamos el tamano de los arreglos temporales
        this.tmpCoeficients = new double[this.MatrixArtificial[0].length];
        this.tmpSlacks = new double[this.Slacks[0].length];
        this.tmpArtificials = new double[this.Artificial[0].length];

        //Buscamos las filas que contienen variables artificiales, usamos el metodo creado para esto
        Vector rowsArtficial = this.searchNumOneInArtificials();
        int rowR = 0;
        //Comenzamos a iterar de acuerdo al numero de elementos que contenga nuestro vector
        for (int i = 0; i < rowsArtficial.size(); i++) {
            //Almacenamos la fila temporalmente para hacer la iteracion
            int rowTmp = (int) rowsArtficial.get(i);
            //Actualiza la fila con la operacion solicitada en la matriz de coeficientes
            this.mkOperatCoeficients(rowR, rowTmp, "+");
            //Actualiza la fila con la operacion solicitada en la matriz de slacks
            this.mkOperatSlacks(rowR, rowTmp, "+");
            //Actualiza la fila con la operacion solicitada en la matriz de artificiales
            this.mkOperatArtificial(rowR, rowTmp, "+");
            //Actualiza la fila con la operacion solicitada en la matriz de soluciones
            this.mkOperatSolutionsObjective(this.Solutions.size(), rowTmp, "+");
        }
        do {
            //Encontramos nuestra variable de salida y de entrada
            this.setEnteringColumn(1); //Encontrar variable para minimizar de entrada
            this.setLeavingRow(); //Encontrar fila de salida

            //Seleccionamos el valor en la matriz de coeficientes que coincida con la posicon [enteringcolumn, leavingRow] y este sera la fila pivote y comprobar si este es igual a 1, si no multiplicar toda la fila por el inverso multiplicativo
            if (this.MatrixArtificial[this.getLeavingRow() - 1][this.getEnteringColumn()] != 1.0) {
                //Hacemos la fila pivote, multiplicando por el inverso multiplicativo
                this.mkRowPivot(false);
            }
            //Una vez realizada la fila pivote asignamos la solucion

            //Una vez realizada la fila pivote, debemos de comprobar que los demas valores que se encuentren en la misma columna que nuestro elemento pivote(valor 1) sea igual a cero, si no debemos comenzar a realizar las sumas o restas correspondientes a cada fila
            //Guardamos en arreglos pequeños los elementos que pertenecen a la fila pivote
            //Comenzamos a llenar los arreglos temporales
            //Llenamos los arreglos temporales con la primera iteracion
            this.fillTmpsArrays(false);

            //Recorremos la columna buscando si es 0 o no, comenzamos desde la fila 0
            for (int i = 0; i < this.MatrixArtificial.length; i++) {
                if ((this.MatrixArtificial[i][this.getEnteringColumn()] != 0 && this.MatrixArtificial[i][this.getEnteringColumn()] > 0) && i != this.getLeavingRow()) {
                    //Procedemos a guardar el valor temporal para multiplicarlo
                    double tmpValue = (double) this.MatrixArtificial[i][this.getEnteringColumn()] * -1;

                    this.multiplyTmpCoeficients(tmpValue);
                    this.multiplyTmpSlacks(tmpValue);
                    this.multiplyTmpArtificials(tmpValue);
                    this.multiplyTmpSolution(tmpValue);
                    //Realizamos la suma de las filas en coeficientes
                    this.mkOperatTmpCoeficients(i, "+");
//                //Realizamos la suma de las filas en slacks
                    this.mkOperatTmpSlacks(i, "+");
//                //Realizamos la suma de las filas en artificiales
                    this.mkOperatTmpArtificials(i, "+");
                    //Realizamos la suma de las soluciones tmp y las que estan en el arreglo de soluciones
                    if (i == 0) {
                        this.mkOperatTmpSolution(this.Solutions.size() - 1, "+");
                    } else {
                        this.mkOperatTmpSolution(i - 1, "+");
                    }

                }

                //Volvemos a llenar los arreglos para otra iteracion
                this.fillTmpsArrays(false);
            }

        } while (!this.isStoppablePhase1());

        //Detectamos cuales fueron las columnas como pivote
    }

    public void doSimplex() {
//Creamos el tamano de los arreglos temporales
        this.tmpCoeficients = new double[this.MatrixArtificial[0].length];
        this.tmpSlacks = new double[this.Slacks[0].length];
        this.tmpArtificials = new double[this.Artificial[0].length];

        //Remplaza la funcion R por Z
        this.replaceZObjective();
        do {
            //Encontramos nuestra variable de salida y de entrada
            this.setEnteringColumn(this.ZObjective.typeOptimization);
            this.setLeavingRow(); //Encontrar fila de salida

            //Seleccionamos el valor en la matriz de coeficientes que coincida con la posicon [enteringcolumn, leavingRow] y este sera la fila pivote y comprobar si este es igual a 1, si no multiplicar toda la fila por el inverso multiplicativo
            if (this.MatrixArtificial[this.getLeavingRow()][this.getEnteringColumn()] != 1.0) {
                //Hacemos la fila pivote, multiplicando por el inverso multiplicativo
                this.mkRowPivot(true);
            }
            //Decimos que esta fila ya es pivote

            //Una vez realizada la fila pivote, debemos de comprobar que los demas valores que se encuentren en la misma columna que nuestro elemento pivote(valor 1) sea igual a cero, si no debemos comenzar a realizar las sumas o restas correspondientes a cada fila
            //Guardamos en arreglos pequeños los elementos que pertenecen a la fila pivote
            //Comenzamos a llenar los arreglos temporales
            //Llenamos los arreglos temporales con la primera iteracion
            this.fillTmpsArrays(true);

            //Recorremos la columna buscando si es 0 o no, comenzamos desde la fila 0
            for (int i = 0; i < this.MatrixArtificial.length; i++) {
                if ((this.MatrixArtificial[i][this.getEnteringColumn()] != 0 && this.MatrixArtificial[i][this.getEnteringColumn()] < 0 || this.MatrixArtificial[i][this.getEnteringColumn()] > 0) && i != this.getLeavingRow()) {
                    //Procedemos a guardar el valor temporal para multiplicarlo
                    double tmpValue = (double) this.MatrixArtificial[i][this.getEnteringColumn()] * -1;
                    //Multiplicamos el valor por los arreglos temporales
                    this.multiplyTmpCoeficients(tmpValue);
                    this.multiplyTmpSlacks(tmpValue);
                    this.multiplyTmpArtificials(tmpValue);
                    this.multiplyTmpSolution(tmpValue);
                    //Realizamos la suma de las filas en coeficientes
                    this.mkOperatTmpCoeficients(i, "+");
//                //Realizamos la suma de las filas en slacks
                    this.mkOperatTmpSlacks(i, "+");
//                //Realizamos la suma de las filas en artificiales
                    this.mkOperatTmpArtificials(i, "+");
                    //Realizamos la suma de las soluciones tmp y las que estan en el arreglo de soluciones
                    if (i == 0) {
                        this.mkOperatTmpSolution(this.Solutions.size() - 1, "+");
                    } else {
                        this.mkOperatTmpSolution(i - 1, "+");
                    }

                }

                //Volvemos a llenar los arreglos para otra iteracion
                this.fillTmpsArrays(true);
            }

        } while (!this.isStoppableSimplex(this.ZObjective.typeOptimization));

    }

    public void identifyRowZero() {
        for (int i = 1; i < this.MatrixArtificial.length; i++) {
            for (int j = 0; j < this.MatrixArtificial[0].length; j++) {
                if (this.MatrixArtificial[i][j] == 1) {
                    this.finalSolutionsWRow[j] = (double) this.Solutions.get(i -1);
                }
            }
        }
    }

    //Funcion que va a decirnos que variables entraron al proceso
    public void indentifyZeroinZ() {
        for (int i = 0; i < 1; i++) {
            for (int j = 0; j < this.MatrixArtificial[0].length; j++) {
                if (this.MatrixArtificial[i][j] != 0) {
                    this.finalSolutionsWRow[j] = 0;
                } else {
                    this.finalSolutionsWRow[j] = (double) this.Solutions.get(j);
                }
            }
        }
    }

    public boolean isStoppableSimplex(int type) {
        boolean stoppable = false;
        if (type == 1) //Minimizar
        {
            double suma = 0;
            //se detiene cuando ya no hay ningun elemento negativo para maximizar en la fila objetivo
            for (int i = 0; i < 1; i++) {
                for (int j = 0; j < this.MatrixArtificial[0].length; j++) {
                    suma += this.MatrixArtificial[i][j];
                }
            }
            if (suma >= 0) {
                stoppable = true;
            }
        }
        //Maximizar
        if (type == 2) {
            double suma = 0;
            //se detiene cuando ya no hay ningun elemento negativo para maximizar en la fila objetivo
            for (int i = 0; i < 1; i++) {
                for (int j = 0; j < this.MatrixArtificial[0].length; j++) {
                    suma += this.MatrixArtificial[i][j];
                }
            }
            if (suma >= 0) {
                stoppable = true;
            }
        }
        return stoppable;
    }

    private boolean detectPivotRow(int row) {
        //La suma de la fila debe ser igual a 1
        double addition = 0;
        for (int i = row; i < 1; i++) {
            for (int j = 0; j < this.MatrixArtificial[0].length; j++) {
                addition += this.MatrixArtificial[i][j];
            }
        }

        return addition == 1;
    }

    private boolean detectPivotColumn(int column) {
        //La suma de la fila debe ser igual a 1
        double addition = 0;
        for (int i = 0; i < this.MatrixArtificial.length; i++) {
            for (int j = column; j < column + 1; j++) {
                addition += this.MatrixArtificial[i][j];
            }
        }

        return addition == 1;
    }

    public void fillTmpsArrays(boolean simplex) {
        if (!simplex) {
            //Llenamos el arreglo tmp con valores de la matriz de coeficientes
            for (int i = this.getLeavingRow(); i < this.getLeavingRow() + 1; i++) {
                for (int j = 0; j < this.MatrixArtificial[0].length; j++) {
                    this.tmpCoeficients[j] = (double) this.MatrixArtificial[i][j];
                }
            }

            //Llenamos el arreglo tmp con valores de la matriz de slacks
            for (int i = this.getLeavingRow(); i < this.getLeavingRow() + 1; i++) {
                for (int j = 0; j < this.Slacks[0].length; j++) {
                    this.tmpSlacks[j] = (double) this.Slacks[i][j];
                }
            }

            //Llenamos el arreglo tmp con valores de la matriz artificiales
            for (int i = this.getLeavingRow(); i < this.getLeavingRow() + 1; i++) {
                for (int j = 0; j < this.Artificial[0].length; j++) {
                    this.tmpArtificials[j] = (double) this.Artificial[i][j];
                }
            }

            //Guardamos el valor de tmpSolution
            this.tmpSolution = (double) this.Solutions.get(this.getLeavingRow() - 1);
        } else {
            //Llenamos el arreglo tmp con valores de la matriz de coeficientes
            for (int i = this.getLeavingRow(); i < this.getLeavingRow() + 1; i++) {
                for (int j = 0; j < this.MatrixArtificial[0].length; j++) {
                    this.tmpCoeficients[j] = (double) this.MatrixArtificial[i][j];
                }
            }

            //Llenamos el arreglo tmp con valores de la matriz de slacks
            for (int i = this.getLeavingRow(); i < this.getLeavingRow() + 1; i++) {
                for (int j = 0; j < this.Slacks[0].length; j++) {
                    this.tmpSlacks[j] = (double) this.Slacks[i][j];
                }
            }

            //Llenamos el arreglo tmp con valores de la matriz artificiales
            for (int i = this.getLeavingRow(); i < this.getLeavingRow() + 1; i++) {
                for (int j = 0; j < this.Artificial[0].length; j++) {
                    this.tmpArtificials[j] = (double) this.Artificial[i][j];
                }
            }

            //Guardamos el valor de tmpSolution
            this.tmpSolution = (double) this.Solutions.get(this.getLeavingRow() - 1);
        }

    }

    public void multiplyTmpSolution(double value) {
        this.tmpSolution = this.tmpSolution * value;
    }

    public void multiplyTmpArtificials(double value) {
        for (int i = 0; i < this.tmpArtificials.length; i++) {
            this.tmpArtificials[i] = this.tmpArtificials[i] * value;
        }
    }

    public void multiplyTmpSlacks(double value) {
        for (int i = 0; i < this.tmpSlacks.length; i++) {
            this.tmpSlacks[i] = this.tmpSlacks[i] * value;
        }
    }

    public void multiplyTmpCoeficients(double value) {
        for (int i = 0; i < this.tmpCoeficients.length; i++) {
            this.tmpCoeficients[i] = this.tmpCoeficients[i] * value;
        }
    }

    public void mkRowPivot(boolean simplex) {
        if (!simplex) {
            //Guardamos el valor del pivote
            double denominator = this.MatrixArtificial[this.getLeavingRow()][this.getEnteringColumn()];
            //Realizamos la operacion con la matriz de coeficientes
            for (int i = this.getLeavingRow(); i < this.getLeavingRow() + 1; i++) {
                for (int j = 0; j < this.MatrixArtificial[0].length; j++) {
                    //Guardamos el valor temporal del numerador
                    double numerator = this.MatrixArtificial[i][j];
                    //Asignamos la division a la posicion actual
                    this.MatrixArtificial[i][j] = numerator / denominator;
                }
            }
            //Realizamos la operacion con la matriz de slacks
            for (int i = this.getLeavingRow(); i < this.getLeavingRow() + 1; i++) {
                for (int j = 0; j < this.Slacks[0].length; j++) {
                    //Guardamos el valor temporal del numerador
                    double numerator = this.Slacks[i][j];
                    //Asignamos la division a la posicion actual
                    this.Slacks[i][j] = numerator / denominator;
                }
            }

            //Realizamos la operacion con la matriz de artificiales
            for (int i = this.getLeavingRow(); i < this.getLeavingRow() + 1; i++) {
                for (int j = 0; j < this.Artificial[0].length; j++) {
                    //Guardamos el valor temporal del numerador
                    double numerator = this.Artificial[i][j];
                    //Asignamos la division a la posicion actual
                    this.Artificial[i][j] = numerator / denominator;
                }
            }

            //Realizamos la operacion con el arreglo de soluciones
            double numerator = (double) this.Solutions.get(this.getLeavingRow() - 1);
            this.Solutions.set(this.getLeavingRow() - 1, (numerator / denominator));
        } else {
            //Guardamos el valor del pivote
            double denominator = this.MatrixArtificial[this.getLeavingRow()][this.getEnteringColumn()];
            //Realizamos la operacion con la matriz de coeficientes
            for (int i = this.getLeavingRow(); i < this.getLeavingRow() + 1; i++) {
                for (int j = 0; j < this.MatrixArtificial[0].length; j++) {
                    //Guardamos el valor temporal del numerador
                    double numerator = this.MatrixArtificial[i][j];
                    //Asignamos la division a la posicion actual
                    this.MatrixArtificial[i][j] = numerator / denominator;
                }
            }
            //Realizamos la operacion con la matriz de slacks
            for (int i = this.getLeavingRow(); i < this.getLeavingRow() + 1; i++) {
                for (int j = 0; j < this.Slacks[0].length; j++) {
                    //Guardamos el valor temporal del numerador
                    double numerator = this.Slacks[i][j];
                    //Asignamos la division a la posicion actual
                    this.Slacks[i][j] = numerator / denominator;
                }
            }

            //Realizamos la operacion con la matriz de artificiales
            for (int i = this.getLeavingRow(); i < this.getLeavingRow() + 1; i++) {
                for (int j = 0; j < this.Artificial[0].length; j++) {
                    //Guardamos el valor temporal del numerador
                    double numerator = this.Artificial[i][j];
                    //Asignamos la division a la posicion actual
                    this.Artificial[i][j] = numerator / denominator;
                }
            }

            //Realizamos la operacion con el arreglo de soluciones
            double numerator = (double) this.Solutions.get(this.getLeavingRow() - 1);
            this.Solutions.set(this.getLeavingRow() - 1, (numerator / denominator));
        }
    }

    public void mkOperatTmpCoeficients(int rowAffected, String operation) {
        //Realiza una suma de filas
        if (operation.equals("+")) {
            for (int j = 0; j < this.MatrixArtificial[0].length; j++) {
                //Hacemos la operacion solicitada
                double aux = this.MatrixArtificial[rowAffected][j] + this.tmpCoeficients[j];
                //Actualizamos el valor de la posicion
                this.MatrixArtificial[rowAffected][j] = aux;
            }
        }
    }

    public void mkOperatTmpSlacks(int rowAffected, String operation) {
        if (operation.equals("+")) {
            for (int j = 0; j < this.Slacks[0].length; j++) {
                //Hacemos la operacion solicitada
                double aux = this.Slacks[rowAffected][j] + this.tmpSlacks[j];
                //Actualizamos el valor de la posicion
                this.Slacks[rowAffected][j] = aux;
            }
        }
    }

    public void mkOperatTmpArtificials(int rowAffected, String operation) {
        if (operation.equals("+")) {
            for (int j = 0; j < this.Artificial[0].length; j++) {
                //Hacemos la operacion solicitada
                double aux = this.Artificial[rowAffected][j] + this.tmpArtificials[j];
                //Actualizamos el valor de la posicion
                this.Artificial[rowAffected][j] = aux;
            }
        }
    }

    public void mkOperatTmpSolution(int position, String operation) {
        if (operation.equals("+")) {
            double aux = (double) this.Solutions.get(position) + this.tmpSolution;
            this.Solutions.set(position, aux);
        }
    }

    /*
        Realiza la operacion indicada utilizando el parametro rowAux para afectar a la fila rowAffected
        en la matriz de coeficientes
     */
    public void mkOperatCoeficients(int rowAffected, int rowAux, String operation) {
        //Realiza una suma de filas
        if (operation.equals("+")) {
            for (int j = 0; j < this.MatrixArtificial[0].length; j++) {
                //Hacemos la operacion solicitada
                double aux = this.MatrixArtificial[rowAffected][j] + this.MatrixArtificial[rowAux][j];
                //Actualizamos el valor de la posicion
                //Antes de redondear
                this.MatrixArtificial[rowAffected][j] = aux;
            }
        }
    }

    /*
        Realiza la operacion indicada utilizando el parametro rowAux para afectar a la fila rowAffected
        en la matriz de Slacks
     */
    public void mkOperatSlacks(int rowAffected, int rowAux, String operation) {
        if (operation.equals("+")) {
            for (int j = 0; j < this.Slacks[0].length; j++) {
                //Hacemos la operacion solicitada
                double aux = this.Slacks[rowAffected][j] + this.Slacks[rowAux][j];
                //Actualizamos el valor de la posicion
                this.Slacks[rowAffected][j] = aux;
            }
        }
    }

    /*
        Realiza la operacion indicada utilizando el parametro rowAux para afectar a la fila rowAffected
        en la matriz de artificiales
     */
    public void mkOperatArtificial(int rowAffected, int rowAux, String operation) {
        if (operation.equals("+")) {
            for (int j = 0; j < this.Artificial[0].length; j++) {
                //Hacemos la operacion solicitada
                double aux = this.Artificial[rowAffected][j] + this.Artificial[rowAux][j];
                //Actualizamos el valor de la posicion
                this.Artificial[rowAffected][j] = aux;
            }
        }
    }

    /*
        Realiza la operacion indicada utilizando el parametro posAux para afectar el elemento
        posAffected en la matriz de coeficientes, existe un caso especial para cuando el elemento
        correspondiente a la funcion objetivo es cero
     */
    public void mkOperatSolutionsObjective(int posAffected, int posAux, String operation) {
        int posInSolutions = posAux - 1, posAffectedSols = this.Solutions.size() - 1;

        if (operation.equals("+")) {
            if (Double.valueOf(this.Solutions.lastElement().toString()) == 0) {
                double aux = (int) this.Solutions.lastElement() + (double) this.Solutions.get(posInSolutions);
                this.Solutions.setElementAt(aux, posAffectedSols);
            } else {
                double aux = (double) this.Solutions.lastElement() + (double) this.Solutions.get(posInSolutions);
                this.Solutions.setElementAt(aux, posAffectedSols);
            }
        }

    }

    /*
        Analiza cual va a ser la columna que va a ser la entrante y lo guarda en la propiedad 
        EnteringColumn
     */
    public void setEnteringColumn(int type) {
        int position = 0;
        double maximum = Math.abs(this.MatrixArtificial[0][0]);
        double minimum = this.MatrixArtificial[0][0];
        //types: 1 minimizar, 2 maximizar
        if (type == 1) {
            //Vamos a recorrer la primera fila de la matriz de coeficientes para encontrar el numero mas positivo
            for (int i = 0; i < 1; i++) {
                for (int j = 1; j < this.MatrixArtificial[0].length; j++) {
                    //Comprobamos que todo sea mayor a cero para que nos de el numero mas positivo
                    if (Math.abs(this.MatrixArtificial[i][j]) > maximum) {
                        maximum = Math.abs(this.MatrixArtificial[i][j]); //nuevo maximo
                        position = j; //Posicion columna
                    }
                }
            }
        } else if (type == 2) { //Maximizar
            //Vamos a recorrer la primera fila de la matriz de coeficientes para encontrar el numero mas negativo
            for (int i = 0; i < 1; i++) {
                for (int j = 0; j < this.MatrixArtificial[0].length; j++) {
                    if (this.MatrixArtificial[i][j] < minimum && this.MatrixArtificial[i][j] < 0) {
                        minimum = this.MatrixArtificial[i][j]; //nuevo maximo
                        position = j; //Posicion columna
                    }
                }
            }
        }

        this.enteringColumn = position;
    }

    public void setLeavingRow() {
        //Utilizamos la columna que va a ser iterada
        int column = this.enteringColumn;
        //Variables creadas para guardar en numerador el valor de la solucion y en denominador el valor de la columna
        double numerator, denominator;
        //Creamos un arreglo para ir almacenando los resultados temporales, el tamano de este debe ser igual al numero de restricciones
        double[] tmpResults = new double[this.Constraints.length];
        //Comenzamos a recorrer la matriz de coeficientes y hacemos la operacion en conjunto con el array de soluciones a partir de la columna
        int indexSolutions = 0; //Variable para recorrer el arreglo de soluciones
        for (int i = 1; i < this.MatrixArtificial.length; i++) {
            //Aumentamos uno para que recorramos correctamente la columna
            for (int j = column; j < column + 1; j++) {
                //Guardamos el valor de la columna como denominador para hacer la division
                numerator = (double) this.Solutions.get(indexSolutions);
                //Guardamos el valor de la solucion como numerador para hacer la division
                denominator = this.MatrixArtificial[i][j];
                if (denominator != 0) //Guardamos el resultado en el arreglo temporal para poder decidir cual sera la fila de salida
                {
                    if (numerator != 0) {
                        if (numerator / denominator < 0) {
                            tmpResults[indexSolutions] = 1000000.00;//Numero gigante  
                        } else {
                            tmpResults[indexSolutions] = numerator / denominator;
                        }

                    } else {
                        tmpResults[indexSolutions] = 1000000.00;//Numero gigante     
                    }
                } else {
                    tmpResults[indexSolutions] = 1000000.00;//Numero gigante 
                }

                //Aumentamos el indice para seguir recorriendo las soluciones
                indexSolutions++;
            }
        }
        //Una vez hecha la division debemos de seleccionar la fila con el menor resultado positivo, la fila se seleccionara diciendo que la posicion de la solucion + 1 para encontrar el valor en las filas de las matrices artificial, coeficientes y slacks
        // Valor auxiliar para hacer las comparaciones
        double minimum = tmpResults[0];
        int position = 1;

        for (int i = 0; i < tmpResults.length; i++) {
            if (tmpResults[i] < minimum) {
                minimum = tmpResults[i];
                position = i + 1;
            }
        }

        //Asignamos el valor obtenido del menor positivo
        this.leavingRow = position;
    }


    /*
        Metodo que remplaza la funcion objecito z o r dependiendo
     */
    public void replaceRObjective(Objective x) {
        for (int i = 0; i < 1; i++) {
            for (int j = 0; j < this.Artificial[0].length; j++) {
                this.Artificial[i][j] = x.coeficients.get(j);
            }
        }
    }

    public void replaceZObjective() {
        //Remplazar los valores en la matriz de coeficientes
        for (int i = 0; i < 1; i++) {
            for (int j = 0; j < this.MatrixArtificial[0].length; j++) {
                this.MatrixArtificial[i][j] = this.ZObjective.coeficients.get(j);
            }
        }

        //Llenamos con ceros las demas posiciones
        //Matriz de slacks
        for (int i = 0; i < 1; i++) {
            for (int j = 0; j < this.Slacks[0].length; j++) {
                this.Slacks[i][j] = 0;
            }
        }

        //Matriz de artificial
        for (int i = 0; i < 1; i++) {
            for (int j = 0; j < this.Artificial[0].length; j++) {
                this.Artificial[i][j] = 0;
            }
        }
        //Arreglo de soluciones
        this.Solutions.set(this.Solutions.size() - 1, (double) 0);

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
