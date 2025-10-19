// ===============================================================
// Clase: EjecutarProcedimiento
// Propósito: Ejecuta el procedimiento PRC_RESUMIR_COMISIONES
// ===============================================================

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class EjecutarProcedimiento {

    public static void procesarComisiones() {
        Connection conn = ConexionOracle.conectar();
        if (conn != null) {
            try {
                CallableStatement cs = conn.prepareCall("{call PRC_RESUMIR_COMISIONES(?, ?)}");
                cs.setInt(1, 8); // mes
                cs.setInt(2, 2021); // año
                cs.execute();
                JOptionPane.showMessageDialog(null, "✅ Proceso completado correctamente.");
                // Abre automáticamente la ventana de resumen
                MostrarResumen.mostrar();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null,
                        "Error al ejecutar el procedimiento: " + e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(null, "No se pudo conectar a la base de datos.");
        }
    }
}
