package org.example.demoagenteia.Heuristicas;

public class Euclidiana implements Heuristica {
    //para cada ficha suma la distancia en linea recta entre su pos actual y su pos meta
    @Override
    public String nombre() {
        return "Euclidiana";
    }

    @Override
    public double calcular(int[] e) {
        double sum=0;
        for (int i=0;  i< e.length; i++){
            if(e[i]!=0){
                int j=indice(e[i]);
                double dx=i/3-j/3;
                double dy=i%3-j%3;
                sum += Math.sqrt(dx*dx+dy*dy);
            }
        }
        return sum;
    }

    public int indice(int vertice){
        return vertice-1;
    }
}