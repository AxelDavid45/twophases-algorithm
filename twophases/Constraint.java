package twophases;

 
public class Constraint {
    double[] Coeficients;
    String Operator;
    double[] vArtificials = new double[1], vSlacks = new double[1];
    double solution;
    //Construimos nuestra restriccion
    public Constraint(double[] coeficients, String operator, double solution) {
        this.Operator = operator;
        //Guardamos el ultimo elemento de la restriccion para usarla mas adelante en clase tabla
        this.solution = (double) solution;
        //Convertimos a standard inmediatamente
        this.toStandard(coeficients);
    }
    
    //toStandard analiza y convierte a una forma estandar el modelo
    private void toStandard(double[] coeficients) {
        //Analizamos el tipo de operador que contiene la restriccion
        this.analyzeOperator(this.Operator);
        
        //Le asignamos el tamano a la matriz de coeficientes +2 para artificial y slack
        this.Coeficients = new double[coeficients.length];
        
        //Copiamos el arreglo que viene a la propiedad de coeficientes
        System.arraycopy(coeficients, 0, this.Coeficients, 0, coeficients.length);
        
    }
    
    
    private void analyzeOperator(String operator) {
        if (operator.equals("<") || operator.equals("<=")){
            //Caso 1 operador < o <= agregamos una de holgura sumando
            this.vSlacks[0] = 1;
        }
        else if (operator.equals(">") || operator.equals(">=")) {
            //Caso 2 operador > o >= agregamos una de holgura restando y una artificial sumando
            this.vSlacks[0] = -1;
            this.vArtificials[0] = 1;
        } else if (operator.equals("=")) {
            //caso 3 operador = agregamos sumando una artificial
            this.vArtificials[0] = 1;
        }
            
           
    }

    public double[] getvArtificials() {
        return vArtificials;
    }

    public double[] getvSlacks() {
        return vSlacks;
    }
    
    

    public boolean hasVArtificial() {
        return vArtificials[0] == 1;
    }

    public boolean hasVSlack() {
        return vSlacks[0] == 1 || vSlacks[0] == -1;
    }

    public double getSolution() {
        return solution;
    }

    public void setSolution(double solution) {
        this.solution = solution;
    }
    
    public int getNumberCoeficients() {
        return this.Coeficients.length;
    }
    
}
