/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package practica2;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;
/**
 *
 * @author joseccf
 */

public class GugelCar extends SingleAgent{
    Accion accion;
    private final int IDENTIFICARSE=0, ESCUCHAR=1, PROCESAR=2, ENVIAR=3, FINALIZAR=4;
    private int matrizAuxiliar[][] = new int [1000] [1000];
    private ArrayList<ACLMessage> inbox=new ArrayList<ACLMessage>(4);
    private ACLMessage outbox=new ACLMessage();
    private JsonObject object;
    private boolean exit;
    private int nivelBateria;
    private int status;
    private String key;
    private String Agente_Radar,Agente_Scanner,Agente_GPS;
    private ArrayList<Integer> datos_radar=new ArrayList<Integer>(25);
    private ArrayList<Double> datos_scanner=new ArrayList<Double>(25);
    private Pair<Integer,Integer> datos_gps;
    private String map="map1";
    
    public GugelCar(AgentID aid, String Nombre_Radar,String Nombre_Scanner,String Nombre_GPS) throws Exception {
            super(aid);
            this.Agente_Radar=Nombre_Radar;
            this.Agente_GPS=Nombre_GPS;
            this.Agente_Scanner=Nombre_Scanner;
            for(int i=0;i<1000;i++){
                for(int j=0;j<1000;j++){
                    this.matrizAuxiliar[i][j]=-1;
                }
            }
    }
    @Override
    public void init(){
        System.out.println("Agente(" +this.getName()+") ARRANCANDO");
        status=IDENTIFICARSE;
        nivelBateria=0;
        exit=false;
        
    }
    @Override
    public void execute(){
        while(!exit){
            switch(status){
                case IDENTIFICARSE:
                    codificar(accion.login);
                    this.outbox.setSender(this.getAid());
                    this.outbox.addReceiver(new AgentID("Girtab"));
                    this.outbox.setContent(this.object.toString());
                    this.send(outbox);
                    status=ESCUCHAR;
                    break;
                case ESCUCHAR:
                        try {
                                                System.out.print("Escuchando ");

                            inbox.set(0, receiveACLMessage());
                            System.out.print("Escuchado");
                    
                    decodificar(inbox.get(0));
                    if(status!=IDENTIFICARSE){
                        inbox.set(1, receiveACLMessage());
                        inbox.set(2, receiveACLMessage());
                        inbox.set(3, receiveACLMessage());
                        for(int i=1;i<inbox.size();i++){
                            decodificar(inbox.get(i));
                        }   
                        status=PROCESAR;
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(GugelCar.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
                case PROCESAR:
                    CalcularMovimiento();
                    switch(accion){
                        case objective_reached:
                            status=FINALIZAR;
                            break;
                        default:
                            status=ENVIAR;
                            break;
                }
                break;
                case ENVIAR:
                    codificar(accion);
                    outbox.addReceiver(new AgentID("Girtab"));
                    outbox.setSender(this.getAid());
                    this.send(outbox);
                    status=ESCUCHAR;
                break;
                case FINALIZAR:
                    
                break;

            }
        }
    }
    @Override
    public void finalize(){
          System.out.println("Agente(" +this.getName()+") terminando");
          super.finalize();
    }
    private void CalcularMovimiento(){
        
    }

    private void decodificar(ACLMessage get) {
        int index=0;
        JsonObject objeto=Json.parse(get.getContent()).asObject();
        if((get.getContent().equals("CRASHED")) || objeto.get("result").asString()=="BAD_MAP" || objeto.get("result").asString()=="BAD_PROTOCOL" || objeto.get("result").asString()=="BAD_KEY" ){
            this.status=IDENTIFICARSE;
        }
        else if(get.getSender().toString()==this.Agente_Radar){
            for(JsonValue j:objeto.get("radar").asArray()){
                this.datos_radar.set(index, j.asInt());
               index++;
            }
        }
        else if(get.getSender().toString()==this.Agente_Scanner){
            for(JsonValue j:objeto.get("scanner").asArray()){
                this.datos_scanner.set(index, j.asDouble());
               index++;
            }
        }
        else if(get.getSender().toString()==this.Agente_GPS){
          this.datos_gps=new Pair(objeto.get("x").asInt(),objeto.get("y").asInt());
        }
        else {
            if(!objeto.get("result").equals("OK")){
                this.key=objeto.get("result").toString();
        
            }
        }  
    }
    
    public void codificar(Accion Command){
        object=new JsonObject() ;
        String command=Command.toString();
        System.out.print("Comando recibido "+ command +"\n");
        switch(command){
            case "login":
                this.object.add("command", command);
                this.object.add("world", this.map);
                this.object.add("radar", this.Agente_Radar);
                this.object.add("scanner", this.Agente_Scanner);
                this.object.add("gps", this.Agente_GPS);
                
            break; 
            default :
                this.object.add("command", command);
                this.object.add("key",this.key);
            break;

        }

    }
    
    
    
    
}
