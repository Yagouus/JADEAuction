package subasta;

public class LibroComprador {
    
    private String titulo;
    private double precio;
    private double puja;
    private int estado;

    public LibroComprador(String titulo, double puja) {
        this.titulo = titulo;
        this.puja = puja;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public double getPuja() {
        return puja;
    }

    public void setPuja(double puja) {
        this.puja = puja;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }
    
    
    
}
