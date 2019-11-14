package twophases;

 
public class Constraint {
    double[] Coeficients;
    String Operator;
    int[] vArtificials = new int[1], vSlacks = new int[1];
    
    //Construimos nuestra restriccion
    public Constraint(double[] coeficients, String operator) {
        this.Operator = operator;
        //Convertimos a standard inmediatamente
        this.toStandard(coeficients);
    }
    
    //toStandard analiza y convierte a una forma estandar el modelo
    private void toStandard(double[] coeficients) {
        //Analizamos el tipo de operador que contiene la restriccion
        this.analyzeOperator(this.Operator);
        
        //Le asignamos el tamano a la matriz de coeficientes +2 para artificial y slack
        this.Coeficients = new double[coeficients.length + 2];
        
        //Iteramos hasta el numero de coeficientes que traiga el parametro, el ultimo parametro de los coeficientes es el resultado coeficientes.length - 2
        System.arraycopy(coeficients, 0, this.Coeficients, 0, coeficients.length);
        
        //Comprobamos si tiene holgura o slack y agregamos a los coeficientes
        if (this.hasVSlack() && this.hasVArtificial()) {
            //Agregamos la variable de holgura en el penultimo renglon de los coeficientes
            this.Coeficients[this.Coeficients.length - 2] = this.vSlacks[0];
            this.Coeficients[this.Coeficients.length-1] = this.vArtificials[0];
        }
        else if (this.hasVArtificial())
            //Asignamos el valor que se tenga la variable artificial al ultimo de los coeficientes
            this.Coeficients[this.Coeficients.length-1] = this.vArtificials[0];
        else
            this.Coeficients[this.Coeficients.length - 2] = this.vSlacks[0];
            
        
        
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

    public boolean hasVArtificial() {
        return vArtificials[0] == 1;
    }

    public boolean hasVSlack() {
        return vSlacks[0] == 1 || vSlacks[0] == -1;
    }
    
}
