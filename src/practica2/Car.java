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
public class Car {
    
    public static void main(String [ ] args) throws Exception
    {
          String Nombre_Agente_Radar="radar",Nombre_Agente_Scanner="scanner",Nombre_Agente_GPS="gps";
          AgentsConnection.connect("isg2.ugr.es", 6000, "Girtab", "Gelman", "Orion", false);
          AgenteDecision agentedecision=new AgenteDecision(new AgentID("Agentedecision"),Nombre_Agente_Radar,Nombre_Agente_Scanner,Nombre_Agente_GPS);
          GPS gps=new GPS(new AgentID(Nombre_Agente_GPS));
          Radar radar=new Radar(new AgentID(Nombre_Agente_Radar));
          Scanner scanner=new Scanner(new AgentID(Nombre_Agente_Scanner));
          
          agentedecision.start();
          radar.start();
          gps.start();
          scanner.start();
           
    }
}
