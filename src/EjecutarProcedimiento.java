// Ejecuta el procedimiento PRC_RESUMIR_COMISIONES

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class EjecutarProcedimiento {

    public static void procesarComisiones() {
        Connection conn = null;
        CallableStatement cs = null;

        try {
            // Conectar a la base de datos
            conn = ConexionOracle.conectar();
            if (conn == null) {
                JOptionPane.showMessageDialog(null,
                        "No se pudo conectar a la base de datos.\nRevisa la consola para más detalles.",
                        "Error de Conexión",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Solicitar mes y año al usuario
            String mesStr = JOptionPane.showInputDialog(null,
                    "Ingresa el mes a procesar (1-12):",
                    "Mes",
                    JOptionPane.QUESTION_MESSAGE);

            if (mesStr == null)
                return; // Usuario canceló

            String annoStr = JOptionPane.showInputDialog(null,
                    "Ingresa el año a procesar:",
                    "Año",
                    JOptionPane.QUESTION_MESSAGE);

            if (annoStr == null)
                return; // Usuario canceló

            // Validar y convertir los valores
            int mes = Integer.parseInt(mesStr.trim());
            int anno = Integer.parseInt(annoStr.trim());

            // Validar rango del mes
            if (mes < 1 || mes > 12) {
                JOptionPane.showMessageDialog(null,
                        " El mes debe estar entre 1 y 12",
                        "Error de Validación",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validar año
            if (anno < 2000 || anno > 2100) {
                JOptionPane.showMessageDialog(null,
                        "El año debe estar entre 2000 y 2100",
                        "Error de Validación",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Preparar y ejecutar el procedimiento
            cs = conn.prepareCall("{call PRC_RESUMIR_COMISIONES(?, ?)}");
            cs.setInt(1, mes);
            cs.setInt(2, anno);

            System.out.println("Ejecutando procedimiento para " + mes + "/" + anno + "...");
            cs.execute();
            System.out.println("Procedimiento ejecutado correctamente");

            JOptionPane.showMessageDialog(null,
                    "Proceso completado correctamente.\n" +
                            "Mes: " + mes + "\nAño: " + anno,
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);

            // Abre automáticamente la ventana de resumen
            MostrarResumen.mostrar();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null,
                    "Error: Debes ingresar valores numéricos válidos.\n" + e.getMessage(),
                    "Error de Formato",
                    JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            System.err.println("Error SQL: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    "Error al ejecutar el procedimiento:\n" +
                            e.getMessage() + "\n\nRevisa la consola para más detalles.",
                    "Error SQL",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            // Cerrar recursos
            try {
                if (cs != null)
                    cs.close();
                if (conn != null)
                    ConexionOracle.cerrarConexion(conn);
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos: " + e.getMessage());
            }
        }
    }
}
