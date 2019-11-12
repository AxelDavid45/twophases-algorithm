package twophases;

public abstract class  Objective {
    double[] coeficients;
    int typeOptimization; // Max = 1, Min = 2
    
    abstract void toStandard();
}
