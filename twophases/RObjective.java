package twophases;
import java.util.ArrayList;
public class RObjective extends Objective {
    public double Solution;
    public RObjective(int nArtificials) {
        //Creamos el arreglo con los coeficientes negativos
        this.coeficients = new ArrayList<>();
        //Agregamos valores -1 dependiento del numero de nArtificiales
        for(int i = 0; i < nArtificials; i++)
            this.coeficients.add((double) -1); //Llenamos el arreglo con los coeficientes negativos
        this.Solution = 0; //Colocamos el resultado de la funcion R en 0
        this.Stoppable = true; //Colocamos que no es detenible al inicio
    }
    
    public double getSolution() {
        return this.Solution;
    }
    
    //Metodo que actualiza la funcion R
    @Override
    protected void updateIndex(double value, int index) {
        this.coeficients.set(index, value);
    }
    
    /*
        Determina cuando la funcion objetivo vuelve a ser -1, si cualquiera de los elementos es diferente a -1 
        no puede ser detenible
    */
    public boolean isStoppable() {
        //Recorremos los coeficientes para saber si son diferentes a -1 y seguir con el algoritmo
        for(int i = 0; i < (this.coeficients.size()-1); i++)
           if(coeficients.get(i) != -1)
               this.Stoppable = false;
        return this.Stoppable;
    }
    
    //Si los coeficientes no son iguales a -1 y la solucion no es igual a 0  no tiene solucion
    public boolean hasInitialSolution() {
        return this.coeficients.get(this.coeficients.size()-1) == 0 && this.isStoppable();
          
    }

    @Override
    protected void toStandard(double[] coeficients) { }
    
}
