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
public class Scanner extends SingleAgent{
    private final int ESCUCHAR=1, ENVIAR=3, FINALIZAR=4;
    private ACLMessage inbox;
    private ACLMessage outbox;
    private boolean exit;
    private int status;
    public Scanner(AgentID aid) throws Exception{
        super(aid);
    }
    
    @Override
    public void init(){
          System.out.println("Agente(" +this.getName()+") iniciando");
        status=ESCUCHAR;
        inbox=null;
        outbox=null;
        exit=false;
    }

    @Override
    public void execute()  {
          System.out.println("Agente(" +this.getName()+") ejecutandose");
        while(!exit){
           switch(status){
                           
                case ESCUCHAR:
                {
                    System.out.println("Agente(" +this.getName()+") Esperando datos");

                   
                    boolean repetir=true;
                    while(repetir){
                        try {
                           inbox=receiveACLMessage();
                           /*Json
                           if(){ 
                                status=ENVIAR;
                                repetir=false;
                           }*/
                        } catch (InterruptedException ex) {
                            System.err.println("Agente(" +this.getName()+") Error de comunicación");
                            repetir=false;
                            exit=true;
                        }
                    }
                }

                break;

               case ENVIAR:
                   outbox=new ACLMessage();
                   outbox.addReceiver(new AgentID("Agentedecision"));
                   outbox.setSender(this.getAid());
                   //Contenido recibido tras la llegada de los datos del radar.
                   //outbox.setContent(..);
                   this.send(outbox);
                   status=ESCUCHAR;
                
                   
                break;
                 
                case FINALIZAR:
                    System.out.println("Agente(" +this.getName()+") Terminando Ejecución");

                exit=true;
                break;

            }
        }
    }
    
    @Override
    public void finalize(){
        System.out.println("Agente(" +this.getName()+") terminando");
        super.finalize();
    }
}
