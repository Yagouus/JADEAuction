package subasta;

import jade.core.AID;
import java.util.ArrayList;

public class Libro {

    private String titulo;
    private double incrementoPrecio;
    private double precioSalida;
    private double precioSubasta;
    private AID mejorPujador;
    private ArrayList<AID> pujadores;
    private int pujas;
    private int estado;
    private int mejorPujadorOK;
    private int decrementar;

    public Libro(String titulo, double precioSalida, double incrementoPrecio) {
        this.titulo = titulo;
        this.incrementoPrecio = incrementoPrecio;
        this.precioSalida = precioSalida;
        this.precioSubasta = precioSalida;
        
        pujadores = new ArrayList<>();
        pujas = 0;
        estado = 0;
        mejorPujadorOK = 0;
        decrementar = 0;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public double getIncrementoPrecio() {
        return incrementoPrecio;
    }

    public void setIncrementoPrecio(double incrementoPrecio) {
        this.incrementoPrecio = incrementoPrecio;
    }

    public double getPrecioSalida() {
        return precioSalida;
    }

    public void setPrecioSalida(double precioSalida) {
        this.precioSalida = precioSalida;
    }

    public double getPrecioSubasta() {
        return precioSubasta;
    }

    public void setPrecioSubasta(double precioSubasta) {
        this.precioSubasta = precioSubasta;
    }

    public AID getMejorPujador() {
        return mejorPujador;
    }

    public void setMejorPujador(AID mejorPujador) {
        this.mejorPujador = mejorPujador;
    }

    public ArrayList<AID> getPujadores() {
        return pujadores;
    }

    public void setPujadores(AID pujador) {
        this.pujadores.add(pujador);
    }

    public int getPujas() {
        return pujas;
    }

    public void setPujas(int pujas) {
        this.pujas = pujas;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public int getMejorPujadorOK() {
        return mejorPujadorOK;
    }

    public void setMejorPujadorOK(int mejorPujadorOK) {
        this.mejorPujadorOK = mejorPujadorOK;
    }

    public int getDecrementar() {
        return decrementar;
    }

    public void setDecrementar(int decrementar) {
        this.decrementar = decrementar;
    }

    public void incrementarPrecio() {
        precioSubasta += incrementoPrecio;
    }

}
