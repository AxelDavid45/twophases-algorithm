package twophases;

 
public class Constraints {
    double[] Coeficients;
    String Operator;
    boolean vArtificial, vSlack;
    
    private void toStandard() {
        
    }

    public boolean hasVArtificial() {
        return vArtificial;
    }

    public void setvArtificial(boolean vArtificial) {
        this.vArtificial = vArtificial;
    }

    public boolean hasVSlack() {
        return vSlack;
    }

    public void setvSlack(boolean vSlack) {
        this.vSlack = vSlack;
    }
    
}
