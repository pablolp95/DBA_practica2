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
          String Nombre_Radar = "radar";
          String Nombre_Scanner = "scanner";
          String Nombre_GPS = "gps";
          
          AgentsConnection.connect("isg2.ugr.es", 6000, "Girtab", "Gelman", "Orion", false);
          GugelCar car = new GugelCar(new AgentID("Car"), Nombre_Radar, Nombre_Scanner, Nombre_GPS);
          GPS gps = new GPS(new AgentID(Nombre_GPS));
          Radar radar = new Radar(new AgentID(Nombre_Radar));
          Scanner scanner = new Scanner(new AgentID(Nombre_Scanner));
          
          car.start();
          radar.start();
          gps.start();
          scanner.start();
           
    }
}
