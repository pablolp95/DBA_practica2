/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package practica2;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonObject.Member;
import com.eclipsesource.json.JsonValue;
import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
/**
 *
 * @author joseccf
 * @author pablolp
 * @author antoniojl
 */

public class GugelCar extends SingleAgent{
    private final int IDENTIFICARSE = 0, ESCUCHAR = 1, PROCESAR = 2, ENVIAR = 3, FINALIZAR = 4;
    private final int SUR = 0, NORTE = 1, ESTE = 3, OESTE = 4, NADA = 5;
    private double matrizAuxiliar[][];
    private boolean exit;
    private boolean connected;
    private int nivelBateria,contadorPasos;
    private int status;
    private String key;
    private String agenteRadar, agenteScanner, agenteGPS;
    private int [][] datosRadar;
    private double [][] datosScanner;
    private String map;
    private Position position;
    private boolean conectedNow;
    private double mejorOpcion;
    private int filaRecorrida;
    private int columnaRecorrida;
    private int dirVertical;
    private int dirHorizontal;
    private boolean disconnect;
    
    /* @author pablolp
     * @author joseccf
     * @author antoniojl
     * @author josepv
     */
    
    public GugelCar(AgentID aid, String nombreRadar, String nombreScanner, String nombreGPS, String map) throws Exception {
            super(aid);
            this.agenteRadar = nombreRadar;
            this.agenteGPS = nombreGPS;
            this.agenteScanner = nombreScanner;
            this.datosRadar = new int[5][5];
            this.datosScanner = new double[5][5];
            this.map = map;
            this.position = new Position();
            this.matrizAuxiliar = new double [1000][1000];
            this.status = IDENTIFICARSE;
            this.nivelBateria = 0;
            this.contadorPasos = 0;
            this.mejorOpcion = 100000;
            this.exit = false;
            this.connected = false;
            this.dirHorizontal = NADA;
            this.dirVertical = NADA;
            this.disconnect = false;
            
            for(int i = 0; i < 1000; i++){
                for(int j = 0; j < 1000; j++){
                    this.matrizAuxiliar[i][j] = (float) 0.0;
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
                    if(correcto)
                        status = PROCESAR;
                    else
                        status = FINALIZAR;
                    break;
                    
                case PROCESAR:
                    /*if(contadorPasos == 10){
                        comando = Accion.logout.toString();
                    }
                    else*/
                        comando = decidirMovimiento();
                    status = ENVIAR;
                    break;
                    
                case ENVIAR:
                    enviarComando(comando);
                    System.out.println("Comando enviado:" + comando);
                    if(comando.equals("logout")){
                        status = FINALIZAR;
                        disconnect = true;
                        System.out.println("Notificando desconexión");
                    }
                    else{
                        status = ESCUCHAR;
                    }
                    break;
                    
                case FINALIZAR:
                    notificarSensores();
                    exit = true;
            
                    try {
                        generarImagen();
                    } catch (IOException ex) {
                        Logger.getLogger(GugelCar.class.getName()).log(Level.SEVERE, null, ex);
                    }
            
                    System.out.println("Notificado la desconexión");
                    break;

            }
        }
    }
    
    /* @author pablolp
     * @author joseccf
     * @author antoniojl
     */
    @Override
    public void finalize(){
          System.out.println("Agente(" +this.getName()+") Terminando");
          super.finalize();
    }
    
    /**
     * @author pablolp
     * @author joseccf
     * @author antoniojl
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
     * @author joseccf
     * @author antoniojl
     */
    String decidirMovimiento(){
        String comando = null;
        int xGPS = this.position.getX();
        int yGPS = this.position.getY();
        
        //Si estamos situados sobre el objetivo
        if(this.mejorOpcion == -10000){
            System.out.println("Objetivo alcanzado.");
            System.out.println("Pasos realizados: " + this.contadorPasos);
            comando = Accion.logout.toString();
        }
        //Si el nivel de bateria es bajo
        else if(this.nivelBateria <= 1){
            this.nivelBateria = 100;
            comando = Accion.refuel.toString();
        }
        //Decision
        else{
            this.mejorOpcion = 100000;
            int iMejor = 0;
            int jMejor = 0;
            boolean listo = false;
            double valor;
            
            for(int i = 1; i < datosScanner[i].length-1 && !listo; i++){
                for(int j = 1; j < datosScanner[j].length-1 && !listo; j++){
                    valor = 0.0;
                    if(this.dirVertical == NORTE){
                        switch (this.dirHorizontal) {
                            case ESTE:
                                if(i > 2 || j < 2)
                                    valor = .5;
                                break;
                            case OESTE:
                                if(i > 2 || j > 2)
                                    valor = .5;
                                break;
                            default:
                                if(i > 2)
                                    valor = .5;
                                break;
                        }
                    }
                    else if (this.dirVertical == SUR){
                        switch (this.dirHorizontal) {
                            case ESTE:
                                if(i < 2 || j < 2)
                                    valor = .5;
                                break;
                            case OESTE:
                                if(i < 2 || j > 2)
                                    valor = .5;
                                break;
                            default:
                                if(i < 2)
                                    valor = .5;
                                break;
                        }
                    }
                    else{
                        switch (this.dirHorizontal) {
                            case ESTE:
                                if(j < 2)
                                    valor = .5;
                                break;
                            case OESTE:
                                if(j > 2)
                                    valor = .5;
                                break;
                        }
                    }
                    
                    if(this.datosRadar[i][j] == 2){
                        this.mejorOpcion = -10000;
                        iMejor = i;
                        jMejor = j;
                        listo = true;
                    }
                    else if(this.datosRadar[i][j] != 1 && 
                            this.mejorOpcion > (this.datosScanner[i][j] + this.matrizAuxiliar[yGPS+i][xGPS+j] + valor)
                            && !(i == 2 && j == 2)){
                        this.mejorOpcion = this.datosScanner[i][j] + this.matrizAuxiliar[yGPS+i][xGPS+j] + valor;
                        iMejor = i;
                        jMejor = j;
                    }
                }

            }


            Accion accion = null;
            accion = obtenerMovimiento(iMejor,jMejor);

            if(map == "map2"){
                this.matrizAuxiliar[yGPS+iMejor][xGPS+jMejor] += 5.0;
            }
            else 
                this.matrizAuxiliar[yGPS+iMejor][xGPS+jMejor] += 5.0;
            
            this.nivelBateria--;    
            this.contadorPasos++;
            comando = accion.toString();
        }
        
        return comando;
    }
    
    
    /**
     * @author antoniojl
     */
    Accion obtenerMovimiento(int iMejor, int jMejor){
        Accion accion = null;
        
        if(iMejor == 1){
            this.dirVertical = NORTE;
            if(jMejor == 1){
                accion = Accion.moveNW;
                this.dirHorizontal = OESTE;
            }
            else if(jMejor == 2){
                accion = Accion.moveN;
                //this.dirHorizontal = NADA;
            }
            else if(jMejor == 3){
                accion = Accion.moveNE;
                this.dirHorizontal = ESTE;
            }
        }  
        else if(iMejor == 2){
            //this.dirVertical = NADA;
            if(jMejor == 1){
                accion = Accion.moveW;
                this.dirHorizontal = OESTE;
            }
            else if(jMejor == 3){
                accion = Accion.moveE;
                this.dirHorizontal = ESTE;
            }
        }
        else if(iMejor == 3){
            this.dirVertical = SUR;
            if(jMejor ==1){
                accion = Accion.moveSW;
                this.dirHorizontal = OESTE;
            }
            else if(jMejor==2){
                accion = Accion.moveS;
                //this.dirHorizontal = NADA;
            }
            else if(jMejor==3){
                accion = Accion.moveSE;
                this.dirHorizontal = ESTE;
            }
        }
        
        return accion;
    }
    
    /**
     * @author pablolp
     * @author joseccf
     * @author antoniojl
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
    
    /* @author pablolp
     * @author joseccf
     * @author antoniojl
     */
    boolean recibirMensajes(){
        JsonObject objeto;
        boolean correcto = true;
        ACLMessage inbox;
        try {
            for (int i = 0; i < 4 && correcto; ++i){
                inbox = receiveACLMessage();

                objeto = Json.parse(inbox.getContent()).asObject();
                for (Member member : objeto) {
                    String name = member.getName();
                    JsonValue value = member.getValue();
                    switch (name) {
                        case "radar":
                            System.out.println("Contenido del sensor radar: " + inbox.getContent());
                            correcto = recibirRadar(inbox);
                            break;
                        case "scanner":
                            System.out.println("Contenido del sensor scanner: " + inbox.getContent());
                            correcto = recibirScanner(inbox);
                            break;
                        case "gps":
                            System.out.println("Contenido del sensor GPS: " + inbox.getContent());
                            correcto = recibirGPS(inbox);
                            break;
                        default:
                            System.out.println("Mensaje recibido de controlador: " + inbox.getContent());
                            correcto = recibirControlador(inbox);
                            break;
                    }
                }
            }
            
        } catch (InterruptedException ex) {
            Logger.getLogger(GugelCar.class.getName()).log(Level.SEVERE, null, ex);
        }
        return correcto;
    }
    
    /* @author pablolp
     * @author joseccf
     *
     */
    boolean recibirRadar(ACLMessage inbox){
        if(inbox.getContent().equals("CRASHED")){
            return false;
        }
        else{
            JsonObject objeto = Json.parse(inbox.getContent()).asObject();
            int posx = 0;
            int posy =0;
            for (JsonValue j : objeto.get("radar").asArray()){
                
                datosRadar[posy][posx] = j.asInt();
                posx++;
                
                if(posx == 5){
                    posx = 0;
                    posy++;
                }
            }
        }
        return true;
    }
    
    /* @author pablolp
     * @author joseccf
     *
     */
    
    boolean recibirScanner(ACLMessage inbox){
        if(inbox.getContent().equals("CRASHED")){
            return false;
        }
        else{
            JsonObject objeto = Json.parse(inbox.getContent()).asObject();
            int posx=0,posy = 0;
            for (JsonValue j : objeto.get("scanner").asArray()){
                datosScanner[posy][posx] = j.asDouble();              
                posx++;
                
                if(posx == 5){
                    posx = 0;
                    posy++;
                }
            }
        }
                

        return true;
    }
    /* @author pablolp
     * @author joseccf
     *
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
     * @author joseccf
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
        return correcto;
    }
    
    /**
     * @author antoniojl
     * @author josepv
     */
    void generarImagen() throws FileNotFoundException, IOException{
        ACLMessage inbox;
        try {
            
            System.out.println("Recibiendo traza");
            
            //Recibo el OK del logout
            if(disconnect){
                inbox = receiveACLMessage();
                JsonObject objeto2 = Json.parse(inbox.getContent()).asObject();
            }
            
            //Tanto como si me he chocado como si he hecho logout, genero imagen
            inbox = receiveACLMessage();
            JsonObject objeto = Json.parse(inbox.getContent()).asObject();
            JsonArray ja = objeto.get("trace").asArray();
            byte data[] = new byte[ja.size()];
            for(int i = 0;i < data.length; i++){
                data[i] = (byte) ja.get(i).asInt();
            }
            FileOutputStream fos=new FileOutputStream("images/traza-"+this.map+".png");
            fos.write(data);
            fos.close();
            System.out.println("Traza Guardada");
            
        } catch (InterruptedException ex) {
            Logger.getLogger(GugelCar.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    
}
