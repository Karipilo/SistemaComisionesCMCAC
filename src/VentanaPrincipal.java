// ===============================================================
// Clase: VentanaPrincipal
// Propósito: Interfaz principal del sistema con botones de acción
// ===============================================================

import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;

public class VentanaPrincipal extends JFrame {

    public VentanaPrincipal() {
        setTitle("Taller de Base de Datos - Evaluación 2");
        setSize(480, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(4, 1, 10, 10));

        // ---------------------------------------------------------------
        // ENCABEZADO: Título del sistema
        // ---------------------------------------------------------------
        JLabel lblTitulo = new JLabel("Sistema de Cálculo de Comisiones", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        add(lblTitulo);

        // ---------------------------------------------------------------
        // BOTONES PRINCIPALES
        // ---------------------------------------------------------------
        JButton btnProcesar = new JButton("Procesar Comisiones");
        JButton btnResumen = new JButton("Ver Resumen Mensual");
        JButton btnErrores = new JButton("Ver Registro de Errores");

        add(btnProcesar);
        add(btnResumen);
        add(btnErrores);

        // ---------------------------------------------------------------
        // ACCIÓN DEL BOTÓN "PROCESAR COMISIONES"
        // ---------------------------------------------------------------
        btnProcesar.addActionListener((ActionEvent e) -> {
            try {
                // 🔹 1. Pedir mes y año al usuario
                String mesStr = JOptionPane.showInputDialog("Ingrese el mes (1-12):");
                String annoStr = JOptionPane.showInputDialog("Ingrese el año (ej: 2021):");

                if (mesStr == null || annoStr == null) {
                    JOptionPane.showMessageDialog(null, "Operación cancelada por el usuario.");
                    return;
                }

                int mes = Integer.parseInt(mesStr);
                int anno = Integer.parseInt(annoStr);

                // 🔹 2. Ejecutar procedimiento PL/SQL que procesa las comisiones
                EjecutarProcedimiento.ejecutarProcedimiento(mes, anno);

                // 🔹 3. Mostrar los resultados de la tabla DETALLE_COMISIONES_AUDITORIAS_MES
                MostrarDetalle mostrar = new MostrarDetalle();
                mostrar.mostrarDetalle(mes, anno);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Error al procesar comisiones: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        // ---------------------------------------------------------------
        // ACCIÓN DEL BOTÓN "VER RESUMEN"
        // ---------------------------------------------------------------
        btnResumen.addActionListener((ActionEvent e) -> {
            MostrarResumen.mostrar();
        });

        // ---------------------------------------------------------------
        // ACCIÓN DEL BOTÓN "VER ERRORES"
        // ---------------------------------------------------------------
        btnErrores.addActionListener((ActionEvent e) -> {
            MostrarErrores.mostrar();
        });
    }

    // ===============================================================
    // Método principal: inicia la ventana
    // ===============================================================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VentanaPrincipal v = new VentanaPrincipal();
            v.setVisible(true);
        });
    }
}
