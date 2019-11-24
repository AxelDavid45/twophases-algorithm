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

    @Override
    protected void toStandard(double[] coeficients) { }
    
}
