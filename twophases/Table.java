package twophases;

import java.util.Vector;

public class Table {
    double[][] Matrix; //Contiene los datos para hacer iteraciones
    Vector Solutions; //Contiene las soluciones de cada fila el ultimo elemento es la sol de R o Z
    Constraint[] Constraints; //Contiene las restricciones
    int nArtificial = 0, nSlack = 0; //Guarda el numero de variables artificiales y holgura utilizadas
    Objective ZObjective = null, RObjective = null; //Contiene la funcion objetivo o la R
    int enteringColumn, leavingRow; //Contiene la posicion de la columna que entra y la fila que sale
    
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
    
    public void buildMatrix() {
        
    }
    
    public void doSimplex(boolean type) {
        
    }
    
    public void phase1() {
        
    }
    
    public void replaceZtoR(Objective z) {
        
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
}
