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
    private final int ESCUCHAR=1, ENVIAR=2, FINALIZAR=3;
    private boolean exit;
    private int status;
    public Scanner(AgentID aid) throws Exception{
        super(aid);
    }
    
    @Override
    public void init(){
        System.out.println("Agente(" +this.getName()+") iniciando");
        status = ESCUCHAR;
        exit = false;
    }

    /**
     *@author pablo
     *@author joseccf
     */
    @Override
    public void execute(){
        ACLMessage inbox;
        ACLMessage outbox;
        inbox = new ACLMessage();
        outbox = new ACLMessage();
        System.out.println("Agente(" +this.getName()+") ejecutandose");
        while(!exit){
            switch(status){
                           
                case ESCUCHAR:
                    System.out.println("Agente(" +this.getName()+"): Esperando datos");
                    try {
                        inbox = receiveACLMessage();
                        status = ENVIAR;
                    } catch (InterruptedException ex) {
                        System.err.println("Agente(" +this.getName()+"): Error de comunicación");
                        exit = true;
                    }
                    break;

                case ENVIAR:
                   outbox = new ACLMessage();
                   outbox.setSender(this.getAid());
                   outbox.addReceiver(new AgentID("Car"));
                   outbox.setContent(inbox.getContent());
                   this.send(outbox);
                   status = ESCUCHAR;
                   break;
                 
                case FINALIZAR:
                    System.out.println("Agente(" +this.getName()+") Terminando Ejecución");
                    exit = true;
                    break;
            }
        }
    }
    
    /**
     *@joseccf
     */
    @Override
    public void finalize(){
          System.out.println("Agente(" +this.getName()+") terminando");
          super.finalize();
    }
}