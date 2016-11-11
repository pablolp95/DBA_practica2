/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package practica2;

import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;
/**
 *
 * @author joseccf
 */
public class GugelCar extends SingleAgent{
    private final int IDENTIFICARSE=0, ESCUCHAR=1, PROCESAR=2, ENVIAR=3, FINALIZAR=4;
    private int matrizAuxiliar[][] = new int [1000] [1000];
    private ACLMessage[] inbox,outbox;
    private boolean exit;
    private int nivelBateria;
    private int status;
    private String Agente_Radar,Agente_Scanner,Agente_GPS;
    
    public GugelCar(AgentID aid, String Nombre_Radar,String Nombre_Scanner,String Nombre_GPS) throws Exception {
            super(aid);
            this.Agente_Radar=Nombre_Radar;
            this.Agente_GPS=Nombre_GPS;
            this.Agente_Scanner=Nombre_Scanner;
    }
    @Override
    public void init(){
        status=IDENTIFICARSE;
        nivelBateria=0;
        exit=false;
    }
    @Override
    public void execute(){
    
        while(!exit){
            switch(status){
                case IDENTIFICARSE:
                    //Pasamos mensaje de logeeo

                break;
                case ESCUCHAR:



                break;
                case PROCESAR:




                break;
                case ENVIAR:



                break;
                case FINALIZAR:

                break;

            }
        }
    }
    @Override
    public void finalize(){
    
    }
    private void CalcularMovimiento(){
        
    }
    
    
    
    
    
}
