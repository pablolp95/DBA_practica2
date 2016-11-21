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
    private final int NADA = 0, SUR = 1, NORTE = 2, ESTE = 3, OESTE = 4, NOROESTE = 5, NORESTE = 6, SUDOESTE = 7, SUDESTE = 8;
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
    private int direccion;
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
            this.direccion = NADA;
            this.disconnect = false;
            
            for(int i = 0; i < 1000; i++){
                for(int j = 0; j < 1000; j++){
                    this.matrizAuxiliar[i][j] = 0.0;
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
                    System.out.println(contadorPasos);
                    if(contadorPasos == 89){
                        comando = Accion.logout.toString();
                    }
                    else
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
        double mejorValor = 10000.0;
        int mejorCol = 0;
        int mejorFila = 0;
        int fila;
        int col;
        
        //Si estamos situados sobre el objetivo
        if(this.datosRadar[2][2] == 2){
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
            if(hayPared()){
                if(this.direccion == ESTE || this.direccion == OESTE){
                    if(this.datosRadar[1][2] != 1 && this.datosRadar[3][2] != 1){
                        if(this.datosScanner[1][2] < this.datosScanner[3][2]){
                            comando = Accion.moveN.toString();
                            this.direccion = NORTE;
                        }
                        else{
                            comando = Accion.moveS.toString();
                            this.direccion = SUR;
                        }
                    }
                    else if(this.datosRadar[1][2] != 1){
                        comando = Accion.moveN.toString();
                        this.direccion = NORTE;
                    }
                    else{
                        comando = Accion.moveS.toString();
                        this.direccion = SUR;
                    }
                }
                else if(this.direccion == NORTE || this.direccion == SUR){
                    if(this.datosRadar[2][1] != 1 && this.datosRadar[2][3] != 1){
                        if(this.datosScanner[2][1] < this.datosScanner[2][3]){
                            comando = Accion.moveW.toString();
                            this.direccion = OESTE;
                        }
                        else{
                            comando = Accion.moveE.toString();
                            this.direccion = ESTE;
                        }
                    }
                    else if(this.datosRadar[2][1] != 1){
                        comando = Accion.moveW.toString();
                            this.direccion = OESTE;
                    }
                    else{
                        comando = Accion.moveE.toString();
                        this.direccion = ESTE;
                    }
                }
            }
            //No hay pared en mi direccion
            else{
                switch (this.direccion) {
                    case NADA:
                        for(int i = 1; i < 4; ++i){
                            for(int j = 1; j < 4; ++j){
                                if(this.datosRadar[i][j] != 1 && this.datosScanner[i][j] < mejorValor){
                                    mejorValor = this.datosScanner[i][j];
                                    mejorFila = i;
                                    mejorCol = j;
                                }
                            }
                        }   
                        break;
                    case SUR:
                        mejorFila = 3;
                        //Miro la fila de abajo
                        for(int j = 1; j < 4; j++){
                            if(this.datosRadar[mejorFila][j] != 1 && this.datosScanner[mejorFila][j] < mejorValor){
                                mejorValor = this.datosScanner[mejorFila][j];
                                mejorCol = j;
                            }
                        }   
                        break;
                    case NORTE:
                        mejorFila = 1;
                        //Miro la fila de arriba
                        for(int j = 1; j < 4; j++){
                            if(this.datosRadar[mejorFila][j] != 1 && this.datosScanner[mejorFila][j] < mejorValor){
                                mejorValor = this.datosScanner[mejorFila][j];
                                mejorCol = j;
                            }
                        }   
                        break;
                    case ESTE:
                        mejorCol = 3;
                        //Miro la fila de la dcha
                        for(int i = 1; i < 4; i++){
                            if(this.datosRadar[i][mejorCol] != 1 && this.datosScanner[i][mejorCol] < mejorValor){
                                mejorValor = this.datosScanner[i][mejorCol];
                                mejorFila = i;
                            }
                        }   
                        break;
                    case OESTE:
                        mejorCol = 1;
                        //Miro la fila de la izq
                        for(int i = 1; i < 4; i++){
                            if(this.datosRadar[i][mejorCol] != 1 && this.datosScanner[i][mejorCol] < mejorValor){
                                mejorValor = this.datosScanner[i][mejorCol];
                                mejorFila = i;
                            }
                        }   
                        break;
                    case NOROESTE:
                        fila = 1;
                        col = 1;
                        mejorFila = 1;
                        mejorCol = 1;
                        //Miro las filas de arriba(Norte)
                        for(int j = 1; j < 4; j++){
                            if(this.datosRadar[fila][j] != 1 && this.datosScanner[fila][j] < mejorValor){
                                mejorValor = this.datosScanner[fila][j];
                                mejorCol = j;
                            }
                        }
                        //Miro las filas de las izq(Oeste)
                        for(int i = 1; i < 4; i++){
                            if(this.datosRadar[i][col] != 1 && this.datosScanner[i][col] < mejorValor){
                                mejorValor = this.datosScanner[i][col];
                                mejorFila = i;
                                mejorCol = 1;
                            }
                        } 
                        break;
                    case NORESTE:
                        fila = 1;
                        col = 3;
                        mejorFila = 1;
                        mejorCol = 3;
                        //Miro las filas de arriba(Norte)
                        for(int j = 1; j < 4; j++){
                            if(this.datosRadar[fila][j] != 1 && this.datosScanner[fila][j] < mejorValor){
                                mejorValor = this.datosScanner[fila][j];
                                mejorCol = j;
                            }
                        }
                        //Miro las filas de las dcha(Este)
                        for(int i = 1; i < 4; i++){
                            if(this.datosRadar[i][col] != 1 && this.datosScanner[i][col] < mejorValor){
                                mejorValor = this.datosScanner[i][col];
                                mejorFila = i;
                                mejorCol = 3;
                            }
                        } 
                        break;
                    case SUDOESTE:
                        fila = 3;
                        col = 1;
                        mejorFila = 3;
                        mejorCol = 1;
                        //Miro las filas de abajo(Sur)
                        for(int j = 1; j < 4; j++){
                            if(this.datosRadar[fila][j] != 1 && this.datosScanner[fila][j] < mejorValor){
                                mejorValor = this.datosScanner[fila][j];
                                mejorCol = j;
                            }
                        }
                        //Miro las filas de las izq(Sudoeste)
                        for(int i = 1; i < 4; i++){
                            if(this.datosRadar[i][col] != 1 && this.datosScanner[i][col] < mejorValor){
                                mejorValor = this.datosScanner[i][col];
                                mejorFila = i;
                                mejorCol = 1;
                            }
                        } 
                        break;
                    case SUDESTE:
                        fila = 3;
                        col = 3;
                        mejorFila = 3;
                        mejorCol = 3;
                        //Miro las filas de abajo(Sur)
                        for(int j = 1; j < 4; j++){
                            if(this.datosRadar[fila][j] != 1 && this.datosScanner[fila][j] < mejorValor){
                                mejorValor = this.datosScanner[fila][j];
                                mejorCol = j;
                            }
                        }
                        //Miro las filas de las dcha(Este)
                        for(int i = 1; i < 4; i++){
                            if(this.datosRadar[i][col] != 1 && this.datosScanner[i][col] < mejorValor){
                                mejorValor = this.datosScanner[i][col];
                                mejorFila = i;
                                mejorCol = 3;
                            }
                        } 
                        break;
                }
                comando = this.obtenerMovimiento(mejorFila, mejorCol);
            }
            
            this.nivelBateria--;    
            this.contadorPasos++;
        }
        
        return comando;
    }
    
    
    /**
     * @author antoniojl
     */
    String obtenerMovimiento(int iMejor, int jMejor){
        String accion = null;
        
        if(iMejor == 1){
            if(jMejor == 1){
                accion = Accion.moveNW.toString();
                this.direccion = NOROESTE;
            }
            else if(jMejor == 2){
                accion = Accion.moveN.toString();
                this.direccion = NORTE;
            }
            else if(jMejor == 3){
                accion = Accion.moveNE.toString();
                this.direccion = NORESTE;
            }
        }  
        else if(iMejor == 2){
            if(jMejor == 1){
                accion = Accion.moveW.toString();
                this.direccion = OESTE;
            }
            else if(jMejor == 3){
                accion = Accion.moveE.toString();
                this.direccion = ESTE;
            }
        }
        else if(iMejor == 3){
            if(jMejor ==1){
                accion = Accion.moveSW.toString();
                this.direccion = SUDOESTE;
            }
            else if(jMejor==2){
                accion = Accion.moveS.toString();
                this.direccion = SUR;
            }
            else if(jMejor==3){
                accion = Accion.moveSE.toString();
                this.direccion = SUDESTE;
            }
        }
        
        return accion;
    }
    
    public boolean hayPared(){
        boolean pared = false;
        
        if(this.direccion == ESTE){
            if(this.datosRadar[1][3] == 1 && this.datosRadar[2][3] == 1 && this.datosRadar[3][3] == 1)
                pared = true;
        }
        else if(this.direccion == OESTE){
            if(this.datosRadar[1][1] == 1 && this.datosRadar[2][1] == 1 && this.datosRadar[3][1] == 1)
                pared = true;
        }
        else if(this.direccion == NORTE){
            if(this.datosRadar[1][1] == 1 && this.datosRadar[1][2] == 1 && this.datosRadar[1][3] == 1)
                pared = true;
        }
        else if(this.direccion == SUR){
            if(this.datosRadar[3][1] == 1 && this.datosRadar[3][2] == 1 && this.datosRadar[3][3] == 1)
                pared = true;
        }
        
        return pared;
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
