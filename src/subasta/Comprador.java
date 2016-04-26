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
import javax.swing.JOptionPane;

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
                listaLibros.add(new LibroComprador(titulo, precio));

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

                //Eliminamos el servicio
                for (ServiceDescription servicio : servicios) {
                    if (servicio.getName().equals(titulo)) {
                        servicios.remove(servicio);
                        dfd.removeServices(servicio);
                        break;
                    }
                }

                //Eliminamos libro de la lista
                for (LibroComprador libro : listaLibros) {
                    if (libro.getTitulo().equals(titulo)) {
                        listaLibros.remove(libro);
                        break;
                    }
                }

                //Actualizamos el agente
                try {
                    DFService.modify(myAgent, dfd);
                } catch (FIPAException ex) {
                    System.out.println("Excepcion eliminar libro: " + ex);
                }
            }

        }
        );
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
                String[] tokens = respuesta.getContent().split(", ");
                String tituloLibro = tokens[0];
                double precioLibro = Double.parseDouble(tokens[1]);
                int estadoLibro = Integer.parseInt(tokens[2]);

                ACLMessage reply = respuesta.createReply();

                //Buscamos el libro
                for (LibroComprador libro : listaLibros) {
                    if (libro.getTitulo().equals(tituloLibro)) {

                        //Actualizamos la información del libro
                        libro.setPrecio(precioLibro);
                        libro.setEstado(estadoLibro);
                        interfaz.actualizarEstado(listaLibros);

                        //Pujamos?
                        if (libro.getPuja() >= precioLibro) {

                            //Notificamos puja
                            System.out.println("El agente: " + myAgent.getName() + " puja");

                            //Mandamos propuesta
                            reply.setContent(tituloLibro);
                            reply.setPerformative(ACLMessage.PROPOSE);
                            myAgent.send(reply);
                        }

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

            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            ACLMessage respuesta = myAgent.receive(mt);

            MessageTemplate rechazo = MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL);
            ACLMessage respuestaRechazo = myAgent.receive(rechazo);

            //Esperamos a recibir mensaje
            if (respuesta != null) {

                String[] tokens = respuesta.getContent().split(", ");
                String tituloLibro = tokens[0];
                String precioLibro = tokens[1];

                //Notificamos
                System.out.println("El agente: " + myAgent.getName() + " ha ganado la subasta de -> " + tituloLibro + " por el precio de: " + precioLibro);
                JOptionPane.showMessageDialog(null, "Has ganado la subasta de -> " + tituloLibro + " por el precio de: " + precioLibro);

                //Eliminamos el servicio y borramos el libro de la lista
                eliminarLibro(tituloLibro);

            } else if (respuestaRechazo != null) {
                String[] tokens = respuesta.getContent().split(", ");
                String tituloLibro = tokens[0];
                String precioLibro = tokens[1];
                String estadoLibro = tokens[2];

                for (LibroComprador libro : listaLibros) {
                    if (libro.getTitulo().equals(tituloLibro)) {

                    }
                    //Actualizamos la información del libro
                    libro.setPrecio(Double.parseDouble(precioLibro));
                    libro.setEstado(Integer.parseInt(estadoLibro));
                    interfaz.actualizarEstado(listaLibros);
                }

            } else {
                block();
            }

        }
    }

}
