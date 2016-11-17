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
        String nombreRadar = "agenteRadar4";
        String nombreScanner = "agenteScanner4";
        String nombreGPS = "agenteGPS4";
        String map = "map10";

        AgentsConnection.connect("isg2.ugr.es", 6000, "Girtab", "Gelman", "Orion", false);
        GugelCar car = new GugelCar(new AgentID("Car"), nombreRadar, nombreScanner, nombreGPS, map);
        GPS gps = new GPS(new AgentID(nombreGPS));
        Radar radar = new Radar(new AgentID(nombreRadar));
        Scanner scanner = new Scanner(new AgentID(nombreScanner));
        
        car.start();
        gps.start();
        radar.start();
        scanner.start();
           
    }
}
