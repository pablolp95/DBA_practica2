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
    private float matrizAuxiliar[][];
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
    double mejorOpcion;
    private VentanaPrincipal v;
    
    /* @author pablolp
     * @author joseccf
     * @author antoniojl
     * @author josepv
     */
    
    public GugelCar(AgentID aid, String nombreRadar, String nombreScanner, String nombreGPS, String map, VentanaPrincipal vent) throws Exception {
            super(aid);
            this.agenteRadar = nombreRadar;
            this.agenteGPS = nombreGPS;
            this.agenteScanner = nombreScanner;
            this.datosRadar = new int[5][5];
            this.datosScanner = new double[5][5];
            this.map = map;
            this.position = new Position();
            this.matrizAuxiliar = new float [1000][1000];
            for(int i=0;i<1000;i++){
                for(int j=0;j<1000;j++){
                    this.matrizAuxiliar[i][j]=(float) -10.0;
                }
            }
            this.v = vent;
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
        contadorPasos=0;
        mejorOpcion=100000;
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
                    if(comando.equals("logout")){
                        status = FINALIZAR;
                        System.out.println("Notificando desconexión");
                    }
                    else
                        status = ESCUCHAR;
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
    
    /* @author pablolp
     * @author joseccf
     * @author antoniojl
     */
    
    boolean recibirMensajes(){
        JsonObject objeto;
        boolean correcto = true;
        ACLMessage inbox;
        try {
            for (int i = 0; i<4 && correcto; ++i){
                inbox = receiveACLMessage();
                if(inbox.getContent().equals("CRASHED")){
                    correcto = false;
                }
                else{
                    objeto = Json.parse(inbox.getContent()).asObject();
                    for (Member member : objeto) {
                        String name = member.getName();
                        JsonValue value = member.getValue();
                        switch (name) {
                            case "radar":
                               // System.out.println("Contenido del sensor radar: " + inbox.getContent());
                                correcto = recibirRadar(inbox);
                                break;
                            case "scanner":
                                //System.out.println("Contenido del sensor scanner: " + inbox.getContent());
                                correcto = recibirScanner(inbox);
                                break;
                            case "gps":
                                //System.out.println("Contenido del sensor GPS: " + inbox.getContent());
                                correcto = recibirGPS(inbox);
                                break;
                            default:
                                System.out.println("Mensaje recibido de controlador: " + inbox.getContent());
                                correcto = recibirControlador(inbox);
                                break;
                        }
                    }
                    
                }
            }
            
        } catch (InterruptedException ex) {
            Logger.getLogger(GugelCar.class.getName()).log(Level.SEVERE, null, ex);
        }
        return correcto;
    }
    
    
    
    /**
     * @author pablolp
     * @author joseccf
     * @author antoniojl
     */
    String decidirMovimiento(){
        double mejorOpcion2=1000000;
        int iMejor2 = 0,jMejor2 = 0;
        
        int x=this.position.getX(),y=this.position.getY();
        if(mejorOpcion==-10000){
            System.out.println("Objetivo alcanzado.");
            System.out.println("Pasos realizados: "+this.contadorPasos);
            return "logout";
        }
        else if(this.nivelBateria <= 1){
            this.nivelBateria = 100;
            contadorPasos++;
            return Accion.refuel.toString();
        }    
        else{//Decision
            mejorOpcion=100000;
            int iMejor = 0,jMejor = 0;
            for(int i=1;i<datosScanner[i].length-1;i++){
                for (int j=1;j<datosScanner[j].length-1;j++){
                    if(datosRadar[i][j]==2){
                        mejorOpcion=-10000;
                        iMejor=i;
                        jMejor=j;
                    }
                    else if(datosRadar[i][j]!=1 && 
                            mejorOpcion>(datosScanner[i][j]+this.matrizAuxiliar[y+i-2][x+j-2])
                            && !(i==2 && j==2)){
                        mejorOpcion=datosScanner[i][j]+this.matrizAuxiliar[y+i-2][x+j-2];
                        iMejor=i;
                        jMejor=j;
                    }
                }

            }

            System.out.println(datosScanner[iMejor][jMejor]);
            System.out.println(this.nivelBateria);
            Accion a = null;
            a=obtenerMovimiento(iMejor,jMejor);
            System.out.println(this.matrizAuxiliar[y+iMejor-2][x+jMejor-2]);
            System.out.println(this.mejorOpcion);
            if(map=="map2"){
                this.matrizAuxiliar[y+iMejor-2][x+jMejor-2]+=2.0;
            }
            else this.matrizAuxiliar[y+iMejor-2][x+jMejor-2]+=10.0;
            this.nivelBateria--;    
            contadorPasos++;
            return a.toString();
        }
        
    }
    
    
    /**
     * @author antoniojl
     */
    Accion obtenerMovimiento(int iMejor, int jMejor){
        Accion a = null;
        if(iMejor==1){
                if(jMejor==1){
                    a=Accion.moveNW;
                }
                else if(jMejor==2){
                    a=Accion.moveN;
                }
                else if(jMejor==3){
                    a=Accion.moveNE;
                }

            }  
            else if(iMejor==2){
                if(jMejor==1){
                    a=Accion.moveW;
                }
                else if(jMejor==3){
                    a=Accion.moveE;
                }
            }
            else if(iMejor==3){
                if(jMejor==1){
                    a=Accion.moveSW;
                }
                else if(jMejor==2){
                    a=Accion.moveS;
                }
                else if(jMejor==3){
                    a=Accion.moveSE;
                }
            }
        return a;
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
        return true;
    }
    
    /**
     * @author antoniojl
     * @author josepv
     */
    void generarImagen() throws FileNotFoundException, IOException{
        ACLMessage inbox;
        try {
            
            System.out.println("Recibiendo traza");
            inbox = receiveACLMessage();
            JsonObject objeto2 = Json.parse(inbox.getContent()).asObject();
            if(objeto2.get("result").asString().equals("OK")){
                inbox = receiveACLMessage();
                JsonObject objeto = Json.parse(inbox.getContent()).asObject();
                JsonArray ja=objeto.get("trace").asArray();
                byte data[]=new byte[ja.size()];
                for(int i=0;i<data.length;i++){
                    data[i]=(byte) ja.get(i).asInt();
                }
                FileOutputStream fos=new FileOutputStream("images/traza-"+this.map+".png");
                fos.write(data);
                fos.close();
                System.out.println("Traza Guardada");
                try {
                    File f; 
                    f = new File("images/traza-"+map+".png");
                    BufferedImage img = ImageIO.read(f);                    
                    int w = img.getWidth();
                    int h = img.getHeight();
                    BufferedImage imagenRedimensionada = new BufferedImage(w*3, h*3, img.getType());
                    Graphics2D g = imagenRedimensionada.createGraphics();
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g.drawImage(img, 0, 0, w*3, h*3, 0, 0, w, h, null);
                    g.dispose();
                    VentanaTraza traza = new VentanaTraza();
                    traza.panelTraza1.setImage(imagenRedimensionada);
                    v.add(traza);
                    traza.setVisible(true);
                    
                } catch(Exception ex){ System.err.println("Error al leer la imagen");}
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(GugelCar.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    
}
