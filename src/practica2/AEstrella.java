/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package practica2;

import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author pablo
 */
public class AEstrella {
    private ArrayList<Mapa> listaAbierta;
    private ArrayList<Mapa> listaCerrada;
    private Mapa mapaInicio;
    
    public AEstrella(Mapa mapaInicio){
        this.mapaInicio = mapaInicio;
        this.listaAbierta = new ArrayList();
        this.listaCerrada = new ArrayList();
    }
    
    public ArrayList<String> rutaIda(){
        ArrayList<String> ruta = new ArrayList();
        //Compruebo si existe camino hacia el destino
        Mapa destino = algoritmoPathfinding();
        if(destino != null){
            ruta = obtenerRecorrido(destino);
        }
        
        return ruta;
    }
    
    private ArrayList<String> obtenerRecorrido(Mapa destino){
        ArrayList<String> ruta = new ArrayList();
        Mapa actual = destino;
        
        while(actual.getPadre() != null){
            ruta.add(actual.getMovimientoAnterior());
            actual = actual.getPadre();
        }
        
        Collections.reverse(ruta);
        return ruta;
    }
    
    private Mapa algoritmoPathfinding(){
        //Ponemos el nodo inicial en la lista con f = 0 (g=0 y h=0)
        boolean listo = false;
        Mapa actual;
        ArrayList<Mapa> hijos;
        this.mapaInicio.setG(0);
        this.mapaInicio.setH(0);
        this.listaAbierta.add(mapaInicio);
        
        while(!this.listaAbierta.isEmpty() && !listo){
            actual = menorF(this.listaAbierta);
            hijos = actual.generarHijos();
            this.listaAbierta.remove(actual);

            for(Mapa hijo : hijos){
                if(hijo.objetivoAlcanzado()){
                    return hijo;
                }
                
                if(nodoEnAbiertaF(hijo) || nodoEnCerradaF(hijo)){
                    this.listaAbierta.add(hijo);
                }
            }
        }
        //La lista esta vacía por lo que no hay camino al objetivo
        //Devuelvo null entonces
        return null;
    }
    
    private Mapa menorF(ArrayList<Mapa> lista){
        Mapa menorF;
        menorF = lista.get(0);
        
        for(Mapa mapa : lista){
            if(mapa.calcularF() < menorF.calcularF())
                menorF = mapa;
        }
        
        return menorF;
    }
    
    private boolean nodoEnAbiertaF(Mapa mapa){
        for(Mapa other : this.listaAbierta){
            if(mapa.equals(other)){
                if(mapa.calcularF() < other.calcularF())
                    return true;
                else
                    return false;
            }
        }
        
        return true;
    }
    
    private boolean nodoEnCerradaF(Mapa mapa){
        for(Mapa other : this.listaCerrada){
            if(mapa.equals(other)){
                if(mapa.calcularF() < other.calcularF())
                    return true;
                else
                    return false;
            }
        }
        
        return true;
    }
}
