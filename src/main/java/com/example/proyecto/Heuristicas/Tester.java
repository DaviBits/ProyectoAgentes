package com.example.proyecto.Heuristicas;

public class Tester {
    public static void main(String[] args){
        int [] e= {
                8, 1 ,3,
                4, 0, 2,
                7, 6, 5};
        Hamming h = new Hamming();
        Euclidiana eu = new Euclidiana();
        FichasDesubicadas f= new FichasDesubicadas();
        System.out.println(h.calcular(e));
        System.out.println(eu.calcular(e));
        System.out.println(f.calcular(e));
    }
}
