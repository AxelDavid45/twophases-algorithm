package twophases;

public abstract class  Objective {
    double[] coeficients;
    int typeOptimization; // Max = 1, Min = 2
    boolean Stoppable;
    
    abstract void toStandard();
}
