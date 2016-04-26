package subasta;

import gui.CompradorVentana;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Comprador extends jade.core.Agent {

    private DFAgentDescription dfd;
    private CompradorVentana interfaz;
    private ArrayList<LibroComprador> listaLibros;
    private ArrayList<ServiceDescription> servicios;
    private Behaviour aceptar;
    private Behaviour resultado;
    private Behaviour actualizar;

    @Override //Inicializacion del agente
    protected void setup() {

        //Saludamos
        System.out.println("Hola! " + getAID().getName() + " -> listo");

        //Creamos lista libros
        listaLibros = new ArrayList();

        //GUI
        interfaz = new CompradorVentana(this);
        interfaz.setVisible(true);
        
        //Creamos los servicios
        servicios = new ArrayList();

        //Registramos al agente
        dfd = new DFAgentDescription();
        dfd.setName(this.getAID());      

        try {
            DFService.register(this, dfd);
        } catch (FIPAException ex) {
            Logger.getLogger(Comprador.class.getName()).log(Level.SEVERE, null, ex);
        }

        //Añadimos comportamientos
        aceptar = new aceptarPuja();
        resultado = new resultadoSubasta();
        actualizar = new actualizarEstado();
        addBehaviour(aceptar);
        addBehaviour(resultado);
        addBehaviour(actualizar);

    }

    @Override //Cierre del agente
    public void takeDown() {

        //Desregistramos el agente
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            System.out.println("Excepcion: " + fe);
        }

        //Cerramos comportamientos
        removeBehaviour(aceptar);
        removeBehaviour(resultado);
        removeBehaviour(actualizar);
        

        //Eliminamos los servicios
        for (ServiceDescription servicio : servicios) {
            dfd.removeServices(servicio);
        }

        //Cerramos la gui
        interfaz.dispose();

        //Mensaje despedida
        System.out.println("Adios! Comprador:  " + getAID().getName() + "  -> terminando");
    }

    //Añadimos un libro que nos interesa
    public void anadirLibro(final String titulo, final double precio) {
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {

                //Creamos el libro
                LibroComprador libro = new LibroComprador(titulo, precio);
                listaLibros.add(libro);

                //Creamos el servicio y lo añadimos
                ServiceDescription sd = new ServiceDescription();
                sd.setType("Subasta");
                sd.setName(titulo);
                dfd.addServices(sd);
                servicios.add(sd);
                try {
                    DFService.modify(myAgent, dfd);
                } catch (FIPAException ex) {
                    System.out.println("Excepcion: " + ex);
                }
            }

        });
    }

    //Eliminamos un libro que ya no nos interesa
    public void eliminarLibro(final String titulo) {
        addBehaviour(new OneShotBehaviour() {

            @Override
            public void action() {

                //Buscamos el servicio
                for (int j = 0; j < servicios.size(); j++) {
                    if (servicios.get(j).getName().equals(titulo)) {

                        //Eliminamos el servicio
                        dfd.removeServices(servicios.get(j));
                        servicios.remove(servicios.get(j));

                        //Eliminamos el libro de la lista
                        for (int i = 0; i < listaLibros.size(); i++) {
                            if (listaLibros.get(i).getTitulo().equals(titulo)) {
                                listaLibros.remove(listaLibros.get(i));
                            }
                        }
                    }

                    //Actualizamos el agente
                    try {
                        DFService.modify(myAgent, dfd);
                    } catch (FIPAException ex) {
                        Logger.getLogger(Vendedor.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
    }

    //Actualiza el estado de los libros en la interfaz
    private class actualizarEstado extends CyclicBehaviour {

        @Override
        public void action() {
            block(5000);
            interfaz.actualizarEstado(listaLibros);
        }
    }

    //Espera a recibir puja aceptada
    private class aceptarPuja extends CyclicBehaviour {

        @Override
        public void action() {

            //Plantilla del mensaje
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage respuesta = myAgent.receive(mt);

            //Esperamos a recibir un mensaje
            if (respuesta != null) {
                double precioLibro;
                String tituloLibro;
                String[] tokens = respuesta.getContent().split(", ");
                tituloLibro = tokens[0];
                precioLibro = Double.parseDouble(tokens[1]);

                //System.out.println(myAgent.getName() + ", se ha ido incrementando el precio. El libro: " + tituloLibro + " pasa a: " + precioLibro);
                
                ACLMessage reply = respuesta.createReply();

                //Buscamos el libro
                for (int i = 0; i < listaLibros.size(); i++) {
                    if (listaLibros.get(i).getTitulo().equals(tituloLibro)) {
                        listaLibros.get(i).setPrecio(precioLibro);

                    }//Si el precio es menor a nuestro tope respondemos a la oferta
                    if (listaLibros.get(i).getPuja() >= precioLibro) {
                        System.out.println("El agente: " + myAgent.getName() + " puja");
                        reply.setContent(tituloLibro);
                        reply.setPerformative(ACLMessage.PROPOSE);
                        myAgent.send(reply);
                    }
                }
            } else {
                block();
            }
        }

    }

    //Muestra el resultado de una subasta
    private class resultadoSubasta extends CyclicBehaviour {

        @Override
        public void action() {

            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage respuesta = myAgent.receive(mt);

            //Esperamos a recibir mensaje
            if (respuesta != null) {

                //Dividimos el mensaje
                String tituloLibro;
                int estadoLibro;
                double precioLibro;
                String[] tokens = respuesta.getContent().split(", ");
                tituloLibro = tokens[0];
                precioLibro = Double.parseDouble(tokens[1]);
                estadoLibro = Integer.parseInt(tokens[2]);

                //Actualizamos el libro
                for (int i = 0; i < listaLibros.size(); i++) {
                    if (listaLibros.get(i).getTitulo().equals(tituloLibro)) {
                        listaLibros.get(i).setEstado(estadoLibro);
                        listaLibros.get(i).setPrecio(precioLibro);

//                        //Si el libro ya se ha vendido lo eliminamos
//                        if (listaLibros.get(i).getEstado() > 1) {
//                            for (int j = 0; j < servicios.size(); j++) {
//                                if (servicios.get(j).getName().equals(listaLibros.get(i).getTitulo())) {
//                                    dfd.removeServices(servicios.get(j));
//                                    servicios.remove(servicios.get(j));
//                                }
//                            }
//                            
//                            try {
//                                DFService.modify(myAgent, dfd);
//                            } catch (FIPAException ex) {
//                                Logger.getLogger(Vendedor.class.getName()).log(Level.SEVERE, null, ex);
//                            }
//                        }
                    }
                }
                System.out.println("Agente: " + getAID().getName() + " actualiza info libro -> " + tituloLibro + "Estado: " + estadoLibro);
            } else {
                block();
            }

        }
    }
}
