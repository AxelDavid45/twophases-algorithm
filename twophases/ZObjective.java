package twophases;

import java.util.ArrayList;

public class ZObjective extends Objective {
    
    public ZObjective(double[] coeficients, int typeOptimization) {
        this.typeOptimization = typeOptimization;
        this.toStandard(coeficients);
    }

    @Override
    protected void toStandard(double[] coeficients) {
        //Creamos el tamano del array coeficients + 1 donde en la ultima posicion ira el valor de la solucion
        this.coeficients = new ArrayList<>();
        //Asignamos los valores y los cambiamos al signo contrario 
        for (int i = 0; i < coeficients.length; i++)
            this.coeficients.add(coeficients[i] * -1);
    }

    @Override
    protected void updateIndex(double value, int index) {
        this.coeficients.add(index, value);
    }
    
    
    
}
