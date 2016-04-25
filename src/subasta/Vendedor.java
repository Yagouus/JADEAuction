package subasta;

import gui.VendedorVentana;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.ArrayList;

public class Vendedor extends jade.core.Agent {

    private DFAgentDescription dfd;
    private ArrayList<Libro> listaLibros;
    private VendedorVentana interfaz;
    private ArrayList<ServiceDescription> servicios;
    private ArrayList<Behaviour>  subasta = new ArrayList<>();
    private Behaviour hacerSubasta;
    private Behaviour actualizar;

    

    @Override
    protected void setup() {

        //Saludamos
        System.out.println("Hola! " + getAID().getName() + " -> listo");

        listaLibros = new ArrayList();
        
        //GUI
        interfaz = new VendedorVentana(this);
        interfaz.setVisible(true);

        //Registramos al agente 
        dfd = new DFAgentDescription();
        dfd.setName(this.getAID());
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            System.out.println("Excepcion: " + fe);
        }

        //Comenzamos comportamientos
        hacerSubasta = new hacerSubasta();
        actualizar = new actualizarEstado();
        addBehaviour(hacerSubasta);
        addBehaviour(actualizar);

    }

    @Override
    public void takeDown() {

        //Desregistramos al agente
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            System.out.println("Excepcion: " + fe);
        }

        //Eliminamos los comportamientos
        removeBehaviour(hacerSubasta);
        removeBehaviour(actualizar);
        for(Behaviour comportamiento : subasta){
            removeBehaviour(comportamiento);
        }
        
        //Eliminamos los servicios
        
        

        //Cerramos la ventana
        interfaz.dispose();

        //Mensaje de despedida
        System.out.println("Adios!" + getAID().getName() + " ha finalizado.");

    }

    //Añade un libro a la subasta
    public void anadirLibro(final String titulo, final double precioSalida, final double incremento) {
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {

                //Añadimos el libro a la lista
                listaLibros.add(new Libro(titulo, precioSalida, incremento));

                //Mostramos mensaje
                System.out.println("El libro -> " + titulo + " ha sido introducido! || Su precio es: " + precioSalida);

                //Actualizamos la lista
                interfaz.actualizarEstado(listaLibros);
            }
        });
    }

    //Inicia la subasta de un libro
    public void iniciarSubasta(final String titulo) {
        addBehaviour(new OneShotBehaviour() {
            private Libro libro;

            @Override
            public void action() {

                //Buscamos el libro a subastar
                for (int i = 0; i < listaLibros.size(); i++) {
                    if (listaLibros.get(i).getTitulo().equals(titulo)) {
                        libro = listaLibros.get(i);
                    }
                }

                AID[] pujadores = new AID[0];
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription serviceDescription = new ServiceDescription();
                DFAgentDescription[] result;

                try {
                    //Creamos el servicio con el libro a subastar
                    serviceDescription.setType("Subasta");
                    serviceDescription.setName(libro.getTitulo());
                    template.addServices(serviceDescription);

                    //Buscamos interesados en el libro
                    result = DFService.search(myAgent, template);
                    pujadores = new AID[result.length];

                    //Mostramos los posibles compradores
                    for (int i = 0; i < result.length; i++) {
                        pujadores[i] = result[i].getName();
                        System.out.println("Interesado " + i + ": " + pujadores[i].getName());
                    }

                } catch (FIPAException fe) {
                    System.out.println("Excepcion: " + fe);
                }

                //Mostramos mensaje
                System.out.println("Empieza la subasta de: " + libro.getTitulo());

                //Cambiamos el estado del libro a iniciado
                libro.setEstado(1);
//
//                //Enviamos el mensaje de informacion inicio de la subasta
//                ACLMessage inicioSubasta = new ACLMessage(ACLMessage.INFORM);
//                inicioSubasta.setContent(libro.getTitulo() + ", " + libro.getPrecioSalida() + ", " + libro.getEstado());
//
//                //Añadimos los pujadores y les notificamos
//                for (int i = 0; i < pujadores.length; i++) {
//                    libro.setPujadores(pujadores[i]);
//                    inicioSubasta.addReceiver(pujadores[i]);
//                }
//                myAgent.send(inicioSubasta);
//
//                //Enviamos mensaje con puja actual
//                ACLMessage msgPrecioSalida = new ACLMessage(ACLMessage.CFP);
//                msgPrecioSalida.setContent(libro.getTitulo() + ", " + String.valueOf(libro.getPrecioSalida()));
//                for (int i = 0; i < pujadores.length; i++) {
//                    msgPrecioSalida.addReceiver(pujadores[i]);
//                }
//                myAgent.send(msgPrecioSalida);

                //Actualizamos la lista
                interfaz.actualizarEstado(listaLibros);

                //Comenzamos el comportamiento de subasta 
                subasta.add(new Subasta(myAgent, 10000, libro));
                addBehaviour(subasta.get(subasta.size() - 1));
            }

        });
    }

    //Se comprobarán las peticiones cada 10 segundos
    private class Subasta extends TickerBehaviour {

        private Libro libro;
        int contador = 0;
        int pujasAnteriores = 0;

        public Subasta(Agent a, long dt, Libro libro) {
            super(a, dt);
            this.libro = libro;
        }

        @Override
        protected void onTick() {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription serviceDescription = new ServiceDescription();
            DFAgentDescription[] result;

            try {

                serviceDescription.setType("Subasta");
                serviceDescription.setName(libro.getTitulo());
                template.addServices(serviceDescription);
                result = DFService.search(myAgent, template);

                System.out.println("Actualizando pujadores de: " + libro.getTitulo());
                ArrayList<AID> pujadores = new ArrayList<>();

                //Comprobamos los pujadores
                for (int i = 0; i < result.length; i++) {
                    pujadores.add(result[i].getName());

                    //Notificamos nuevos pujadores
                    if (!libro.getPujadores().contains(pujadores.get(i))) {
                        System.out.println("Nuevo posible comprador: " + result[i].getName());

//                        //Enviamos mensaje de informacion de inicio de la subasta
//                        ACLMessage iniciaSubasta = new ACLMessage(ACLMessage.INFORM);
//                        iniciaSubasta.setContent(libro.getTitulo() + ", " + libro.getPrecioSalida() + ", " + libro.getEstado());
//                        iniciaSubasta.addReceiver(pujadores.get(i));
//                        myAgent.send(iniciaSubasta);
//
//                        //Añadimos el pujador a la lista
//                        libro.setPujadores(pujadores.get(i));

                        //Enviamos mensaje con puja actual
                        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
                        msg.setContent(libro.getTitulo() + ", " + libro.getPrecioSalida() + ", " + libro.getEstado());
                        msg.addReceiver(pujadores.get(i));
                        myAgent.send(msg);

                        //Comprobamos si algun pujador se ha retirado
                        for (int j = 0; j < libro.getPujadores().size(); j++) {
                            if (!pujadores.contains(libro.getPujadores().get(j))) {
                                System.out.println("El pujador: " + libro.getPujadores().get(j) + "se ha retirado");
                                libro.getPujadores().remove(libro.getPujadores().get(j));
                            }
                        }

                    }
                }

            } catch (FIPAException ex) {
                System.out.println("Excepcion en Subasta: " + ex);
            }

            //Si hay pujas para el libro
            if (libro.getPujas() > 1) {

                //Recuperamos las pujas previas
                pujasAnteriores = libro.getPujas();

                //Aumentamos el precio del libro
                libro.incrementarPrecio();

                //Establecemos las pujas a 0
                libro.setPujas(0);

                //Enviamos ACCEPT_PROPOSAL al mejor pujador
                ACLMessage aceptar = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                aceptar.addReceiver(libro.getMejorPujador());
                myAgent.send(aceptar);

                //Enviamos REJECT_PROPOSAL a los demas
                ACLMessage rechazar = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
                for (int i = 0; i < libro.getPujadores().size(); i++) {
                    if (!libro.getPujadores().get(i).equals(libro.getMejorPujador())) {
                        rechazar.addReceiver(libro.getPujadores().get(i));
                    }

                }
                myAgent.send(rechazar);

                //if (contador > 3) {
                //Finaliza la puja
                if (libro.getPujas() == 0) {
                    libro.setDecrementar(1);
                }
                libro.setEstado(2);

                if (libro.getPujadores().size() > 0) {
                    if (libro.getDecrementar() == 1) {
                        libro.setPrecioSubasta(libro.getPrecioSubasta() - libro.getIncrementoPrecio());
                    }

                    //Enviamos informe al ganador
                    ACLMessage ganador = new ACLMessage(ACLMessage.INFORM);
                    ganador.setContent(libro.getTitulo() + ", " + libro.getPrecioSubasta() + ", " + 3);
                    ganador.addReceiver(libro.getMejorPujador());
                    myAgent.send(ganador);

                    //enviamos informe al resto de participantes
                    ACLMessage perdedores = new ACLMessage(ACLMessage.INFORM);
                    perdedores.setContent(libro.getTitulo() + ", " + libro.getPrecioSubasta() + ", " + libro.getEstado());
                    for (int i = 0; i < libro.getPujadores().size(); i++) {
                        if (!libro.getPujadores().get(i).equals(libro.getMejorPujador())) {
                            perdedores.addReceiver(libro.getPujadores().get(i));
                        }
                    }
                    myAgent.send(perdedores);
                }
                this.stop();
                // } else {
                //    contador++;
                // }
            }
        }

        @Override
        public int onEnd() {
            System.out.println("La subasta se termina...");
            return 0;
        }

    }

    //Recibe las pujas
    private class hacerSubasta extends CyclicBehaviour {

        @Override
        public void action() {

            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
            ACLMessage msg = myAgent.receive(mt);

            if (msg != null) {

                //Buscamos el libro
                for (int i = 0; i < listaLibros.size(); i++) {
                    if (listaLibros.get(i).getTitulo().equals(msg.getContent())) {

                        //Si aun no tiene pujas
                        if (listaLibros.get(i).getPujas() == 0) {
                            if (msg.getSender().equals(listaLibros.get(i).getMejorPujador())) {
                                listaLibros.get(i).setMejorPujadorOK(1);
                            } else {
                                listaLibros.get(i).setMejorPujador(msg.getSender());
                                listaLibros.get(i).setMejorPujadorOK(0);
                            }
                        }

                        //Mostramos mensaje
                        System.out.println(msg.getSender() + "Nuevo precio, acepta");

                        //Aumentamos el numero de pujas al libro
                        listaLibros.get(i).setPujas(listaLibros.get(i).getPujas() + 1);
                    }
                }
            } else {
                block();
            }
        }

    }

    //Actualiza el estado de los libros en la interfaz
    private class actualizarEstado extends CyclicBehaviour {

        @Override
        public void action() {
            block(5000);
            interfaz.actualizarEstado(listaLibros);
        }

    }
}
