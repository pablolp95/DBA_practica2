/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package practica2;

import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.AgentsConnection;


/**
 *
 * @author joseccf
 */
public class Main {
    
    public static void main(String [ ] args) throws Exception
    {   
        String nombre = "Car";
        String nombreRadar = "agenteRadar13";
        String nombreScanner = "agenteScanner13";
        String nombreGPS = "agenteGPS13";
        String map = "map2";

        AgentsConnection.connect("isg2.ugr.es", 6000, "Girtab", "Gelman", "Orion", false);
        GugelCar car = new GugelCar(new AgentID(nombre), nombreRadar, nombreScanner, nombreGPS, map);
        GPS gps = new GPS(new AgentID(nombreGPS), nombre);
        Radar radar = new Radar(new AgentID(nombreRadar), nombre);
        Scanner scanner = new Scanner(new AgentID(nombreScanner), nombre);
        
        car.start();
        gps.start();
        radar.start();
        scanner.start();
           
    }
}
