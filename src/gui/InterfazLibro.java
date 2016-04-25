package gui;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import subasta.Libro;


public class InterfazLibro extends javax.swing.JPanel {
    
    public Libro libro;
    public String estado;
    
    
    public InterfazLibro(Libro libro) {
        initComponents();
        
        jLabel1.setText("Información del libro: "+libro.getTitulo());
        switch (libro.getEstado()) {
            case 0:
                estado="No iniciada";
                jTextArea1.setText(
                        "Precio de salida: \t"+libro.getPrecioSalida()+".\n"+
                                "Incremento: \t\t"+libro.getIncrementoPrecio()+".\n"+
                                "Precio actual: \t"+libro.getPrecioSubasta()+".\n"+
                                "Numero de participantes: \t"+libro.getPujadores().size()+".\n"+
                                "Estado: \t"+estado);
                break;
            case 1:
                estado="En curso";
                jTextArea1.setText(
                        "Precio de salida: \t"+libro.getPrecioSalida()+".\n"+
                                "Incremento: \t\t"+libro.getIncrementoPrecio()+".\n"+
                                "Precio actual: \t"+libro.getPrecioSubasta()+".\n"+
                                "Numero de participantes: \t"+libro.getPujadores().size()+".\n"+
                                "Mejor pujador: \t"+libro.getMejorPujador().getName()+".\n"+
                                "Estado: \t"+estado);
                break;
            default:
                estado = "Finalizada";
                jTextArea1.setText(
                        "Precio de salida: \t"+libro.getPrecioSalida()+".\n"+
                                "Incremento: \t\t"+libro.getIncrementoPrecio()+".\n"+
                                "Precio Actual: \t" + libro.getPrecioSubasta()+".\n"+
                                "Numero de participantes: \t"+libro.getPujadores().size()+".\n"+
                                "Mejor pujador: \t"+libro.getMejorPujador().getName()+".\n"+
                                "Estado: \t"+estado);
                break;
        }
    }


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jButton1 = new javax.swing.JButton();

        jLabel1.setText("Información del libro: ");

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        jButton1.setText("Salir");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton1))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 242, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed


        JFrame frame = (JFrame)SwingUtilities.getRoot(this);
        frame.setVisible(false);
    }//GEN-LAST:event_jButton1ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    // End of variables declaration//GEN-END:variables
}
