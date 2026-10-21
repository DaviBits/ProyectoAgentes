package org.example.Heuristicas;

public class Hamming implements Heuristica{

    //suma +1 por cada ficha que no este en su lugar
    @Override
    public String nombre() {
        return "Hamming";
    }

    @Override
    public double calcular(int[] e) {
        double sum=0;
        for(int i=0; i< e.length; i++){
            if(e[i]!=0 && (e[i]!=meta[i])){
                sum++;
            }
        }
        return sum;
    }
}
