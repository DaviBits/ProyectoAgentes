package org.example.demoagenteia.Heuristicas;

public class FichasDesubicadas  implements Heuristica{
    //suma +1 por cada ficha que no esté ni en su fila ni en su columna
    @Override
    public String nombre() {
        return "FichasDesubicadas";
    }

    @Override
    public double calcular(int[] e) {
        double sum=0;

        for(int i=0; i<e.length; i++){
            if(e[i]!=0){
                int j=indice(e[i]);
                boolean malFila=(i/3!=j/3);
                boolean malColumna=(i%3!=j%3);
                if (malFila && malColumna) {
                    sum++;
                }
            }
        }

        return sum;

    }

    public int indice(int vertice){
        return vertice-1;
    }

}