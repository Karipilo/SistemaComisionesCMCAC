// Interfaz principal del sistema con botones de acción

import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;

public class VentanaPrincipal extends JFrame {

    public VentanaPrincipal() {
        setTitle("Taller de Base de Datos - Evaluación 2");
        setSize(480, 280);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(4, 1, 10, 10));

        JLabel lblTitulo = new JLabel("Sistema de Cálculo de Comisiones", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        add(lblTitulo);

        JButton btnProcesar = new JButton("Procesar Comisiones");
        JButton btnResumen = new JButton("Ver Resumen Mensual");
        JButton btnErrores = new JButton("Ver Registro de Errores");

        add(btnProcesar);
        add(btnResumen);
        add(btnErrores);

        // Acción del botón "Procesar"
        btnProcesar.addActionListener((ActionEvent e) -> {
            EjecutarProcedimiento.procesarComisiones();
        });

        // Acción del botón "Ver Resumen"
        btnResumen.addActionListener((ActionEvent e) -> {
            MostrarResumen.mostrar();
        });

        // Acción del botón "Ver Errores"
        btnErrores.addActionListener((ActionEvent e) -> {
            MostrarErrores.mostrar();
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VentanaPrincipal v = new VentanaPrincipal();
            v.setVisible(true);
        });
    }
}
