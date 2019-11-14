package twophases;

public class ZObjective extends Objective {
    
    public ZObjective(double[] coeficients, int typeOptimization) {
        this.typeOptimization = typeOptimization;
        this.toStandard(coeficients);
    }

    @Override
    protected void toStandard(double[] coeficients) {
        //Creamos el tamano del array coeficients + 1 donde en la ultima posicion ira el valor de la solucion
        this.coeficients = new double[coeficients.length + 1];
        //Asignamos los valores y los cambiamos al signo contrario 
        for (int i = 0; i < coeficients.length; i++)
            this.coeficients[i] = coeficients[i] * -1;
        //Le asignamos la solucion inicial que es cero
        this.coeficients[this.coeficients.length - 1] = 0;
        
    }

    @Override
    protected void updateIndex(double value, int index) {
        this.coeficients[index] = value;
    }
    
    
}
