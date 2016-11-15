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
/**
 *
 * @author joseccf
 * @author pablolp
 * @author antoniojl
 */

public class GugelCar extends SingleAgent{
    private final int IDENTIFICARSE = 0, ESCUCHAR = 1, PROCESAR = 2, ENVIAR = 3, FINALIZAR = 4;
    private int matrizAuxiliar[][];
    private boolean exit;
    private boolean connected;
    private int nivelBateria;
    private int status;
    private String key;
    private String agenteRadar, agenteScanner, agenteGPS;
    private ArrayList<Integer> datosRadar;
    private ArrayList<Double> datosScanner;
    private String map;
    private Position position;
    private Accion accion;
    
    public GugelCar(AgentID aid, String nombreRadar, String nombreScanner, String nombreGPS, String map) throws Exception {
            super(aid);
            this.agenteRadar = nombreRadar;
            this.agenteGPS = nombreGPS;
            this.agenteScanner = nombreScanner;
            this.datosRadar = new ArrayList<>(25);
            this.datosScanner = new ArrayList<>(25);
            this.map = map;
            
            this.matrizAuxiliar = new int [1000][1000];
            for(int i=0;i<1000;i++){
                for(int j=0;j<1000;j++){
                    this.matrizAuxiliar[i][j]=-1;
                }
            }
    }
    
    /**
     *
     * @author pablolp
     * @author joseccf
     * @author antoniojl
     */
    @Override
    public void init(){
        System.out.println("Agente(" +this.getName()+") Iniciando");
        status = IDENTIFICARSE;
        nivelBateria = 0;
        exit = false;
        connected = false;
        
    }

    /**
     *
     * @author joseccf
     * @author pablolp
     * @author antoniojl
     */
    @Override
    public void execute(){
        String comando = null;
        boolean correcto;
        
        while(!exit){
            switch(status){
                case IDENTIFICARSE:
                    enviarLogin();
                    status = ESCUCHAR;
                    break;
                    
                case ESCUCHAR:
                    correcto = recibirMensajes();
                    //System.out.println("Correcto:"+correcto);
                    if(correcto)
                        status = PROCESAR;
                    else
                        status = FINALIZAR;
                    break;
                    
                case PROCESAR:
                    comando = decidirMovimiento();
                    //comando = "logout";
                    status = ENVIAR;
                    break;
                    
                case ENVIAR:
                    enviarComando(comando);
                    if(comando.equals("logout"))
                        status = FINALIZAR;
                    else
                        status = ESCUCHAR;
                    break;
                    
                case FINALIZAR:
                    notificarSensores();
                    exit = true;
                    break;

            }
        }
    }
    
    /**
     * @author joseccf
     */
    @Override
    public void finalize(){
          System.out.println("Agente(" +this.getName()+") Terminando");
          super.finalize();
    }
    
    /**
     * @author pablolp
     */
    void enviarLogin(){
        String mensaje;
        ACLMessage outbox;
        JsonObject objeto = new JsonObject();
        
        objeto.add("command","login");
        objeto.add("world", map);
        objeto.add("gps", agenteGPS);
        objeto.add("radar", agenteRadar);
        objeto.add("scanner", agenteScanner);
        mensaje = objeto.toString();
        
        outbox = new ACLMessage();
        outbox.setSender(this.getAid());
        outbox.setReceiver(new AgentID("Girtab"));
        outbox.setContent(mensaje);
        this.send(outbox);
    }
    
    /**
     * @author pablolp
     */
    boolean recibirMensajes(){
        JsonObject objeto;
        boolean correcto = true;
        ACLMessage inbox;
        try {
            for (int i = 0; i<4 && correcto; ++i){
                inbox = receiveACLMessage();
                if(inbox.getReceiver().equals(agenteRadar)){
                    System.out.println("Mensaje recibido de radar: " + inbox.getContent());
                    correcto = recibirRadar(inbox);
                }
                else if(inbox.getReceiver().equals(agenteScanner)){
                    System.out.println("Mensaje recibido de scanner: " + inbox.getContent());
                    correcto = recibirScanner(inbox);
                }
                else if(inbox.getReceiver().equals(agenteGPS)){
                    System.out.println("Mensaje recibido de GPS: " + inbox.getContent());
                    correcto = recibirGPS(inbox);
                }
                else{
                    System.out.println("Mensaje recibido de controlador: " + inbox.getContent());
                    correcto = recibirControlador(inbox);
                }
            }
            
        } catch (InterruptedException ex) {
            Logger.getLogger(GugelCar.class.getName()).log(Level.SEVERE, null, ex);
        }
        return correcto;
    }
    
    /**
     * @author pablolp
     */
    String decidirMovimiento(){
        return null;
    }
    
    /**
     * @author pablolp
     */
    void enviarComando(String comando){
        JsonObject objeto = new JsonObject();
        ACLMessage outbox;
                
        objeto.add("command",comando);
        objeto.add("key", this.key);
        comando = objeto.toString();
        
        outbox = new ACLMessage();
        outbox.setSender(this.getAid());
        outbox.setReceiver(new AgentID("Girtab"));
        outbox.setContent(comando);
        this.send(outbox);
    }
    
    /**
     * @author pablolp
     */
    void notificarSensores(){
        ACLMessage outbox;
        
        outbox = new ACLMessage();
        outbox.setSender(this.getAid());
        outbox.setContent("fin");
        
        outbox.setReceiver(new AgentID(agenteGPS));
        this.send(outbox);
        
        outbox.setReceiver(new AgentID(agenteRadar));
        this.send(outbox);
        
        outbox.setReceiver(new AgentID(agenteScanner));
        this.send(outbox);  
    }
    
    /**
     * @author pablolp
     */
    boolean recibirRadar(ACLMessage inbox){
        if(inbox.getContent().equals("CRASHED")){
            return false;
        }
        else{
            JsonObject objeto = Json.parse(inbox.getContent()).asObject();
            int pos = 0;
            for (JsonValue j : objeto.get("radar").asArray()){
                datosRadar.set(pos, j.asInt());
                pos++;
            }
        }
        return true;
    }
    
    /**
     * @author pablolp
     */
    boolean recibirScanner(ACLMessage inbox){
        if(inbox.getContent().equals("CRASHED")){
            return false;
        }
        else{
            JsonObject objeto = Json.parse(inbox.getContent()).asObject();
            int pos = 0;
            for (JsonValue j : objeto.get("scanner").asArray()){
                datosScanner.set(pos, j.asDouble());
                pos++;
            }
        }
        return true;
    }
    
    /**
     * @author pablolp
     */
    boolean recibirGPS(ACLMessage inbox){
        if(inbox.getContent().equals("CRASHED")){
            return false;
        }
        else{
            JsonObject objeto = Json.parse(inbox.getContent()).asObject().get("gps").asObject();
            
            position.setX(objeto.get("x").asInt());
            position.setY(objeto.get("y").asInt());
        }
        
        return true;
    }
    
    /**
     * @author pablolp
     */
    boolean recibirControlador(ACLMessage inbox){
        boolean correcto;
        JsonObject objeto = Json.parse(inbox.getContent()).asObject();
        //Sino me he conectado aún, solo puedo recibir tres respuestas
        if(!this.connected){
            if(objeto.get("result").asString().equals("BAD_MAP") || objeto.get("result").asString().equals("BAD_PROTOCOL"))
                correcto = false;
            else{
                this.key = objeto.get("result").asString();
                this.connected = true;
                correcto = true;
            }
        }
        else{
            if(objeto.get("result").asString().equals("OK"))
                correcto = true;
            else{
                correcto = false;
            }
        }
        return true;
    }
}
