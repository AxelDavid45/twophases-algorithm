package twophases;

public abstract class  Objective {
    double[] coeficients;
    int typeOptimization; // Max = 1, Min = 2
    boolean Stoppable;
    
    protected abstract void toStandard(double[] coeficients);
    protected abstract void updateIndex(double value, int index);
}
