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
          String clave="";
          AgentsConnection.connect("isg2.ugr.es", 6000, "Girtab", "Gelman", "Orion", false);
          AgenteDecision Agentedecision=new AgenteDecision(new AgentID("Agentedecision"),clave);
          GPS gps=new GPS(new AgentID("gps"));
          Radar radar=new Radar(new AgentID("radar"));
          Scanner scanner=new Scanner(new AgentID("scanner"));
           
    }
}
