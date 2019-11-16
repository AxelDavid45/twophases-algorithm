package twophases;
import java.util.ArrayList;

public abstract class  Objective {
    ArrayList<Double> coeficients;
    int typeOptimization; // Max = 1, Min = 2
    boolean Stoppable;
    
    protected abstract void toStandard(double[] coeficients);
    
    protected abstract void updateIndex(double value, int index);
    
    protected int numberCoeficients() {
        return coeficients.size();
    };
}
