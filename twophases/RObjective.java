package twophases;

public class RObjective extends Objective {
    
    public RObjective(int nArtificials) {
        //Creamos el arreglo con los coeficientes negativos
        this.coeficients = new double[nArtificials];
        for(int i = 0; i < nArtificials - 1; i++)
            coeficients[i] = -1; //Llenamos el arreglo con los coeficientes negativos
        coeficients[nArtificials] = 0; //Colocamos el resultado de la funcion R en 0
        this.Stoppable = false; //Colocamos que no es detenible al inicio
    }
    
    //Metodo que actualiza la funcion R
    @Override
    protected void updateIndex(double value, int index) {
        this.coeficients[index] = value;
    }
    
    // Determina cuando la funcion objetivo vuelve a ser -1
    private boolean isStoppable() {
        //Recorremos los coeficientes para saber si son diferentes a -1 y seguir con el algoritmo
        for(int i = 0; i < (this.coeficients.length - 1); i++)
           if(coeficients[i] != -1)
               this.Stoppable = false;
        return this.Stoppable;
    }
    
    //Si los coeficientes no son iguales a -1 y la solucion no es igual a 0  no tiene solucion
    private boolean hasInitialSolution() {
        return this.coeficients[this.coeficients.length] == 0 && this.isStoppable();
    }

    @Override
    protected void toStandard(double[] coeficients) { }
    
}
