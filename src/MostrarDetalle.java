// ===============================================================
// Clase: MostrarDetalle
// Propósito: Mostrar los registros de la tabla DETALLE_COMISIONES_AUDITORIAS_MES
// ===============================================================

import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class MostrarDetalle {

    public void mostrarDetalle(int mes, int anno) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionOracle.conectar(); // Usa tu clase de conexión existente

            String sql = "SELECT * FROM DETALLE_COMISIONES_AUDITORIAS_MES WHERE MES_PROCESO = ? AND ANNO_PROCESO = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, mes);
            stmt.setInt(2, anno);
            rs = stmt.executeQuery();

            // Crear el modelo de tabla
            DefaultTableModel modelo = new DefaultTableModel();
            JTable tabla = new JTable(modelo);

            // Definir las columnas
            modelo.addColumn("Mes");
            modelo.addColumn("Año");
            modelo.addColumn("RUN");
            modelo.addColumn("Auditor");
            modelo.addColumn("Profesión");
            modelo.addColumn("Comisión Total");
            modelo.addColumn("Monto");
            modelo.addColumn("Crítica");
            modelo.addColumn("Extra");
            modelo.addColumn("Total Auditor");
            modelo.addColumn("Total Empresa");

            // Llenar la tabla con los resultados del query
            while (rs.next()) {
                Object[] fila = new Object[11];
                fila[0] = rs.getInt("MES_PROCESO");
                fila[1] = rs.getInt("ANNO_PROCESO");
                fila[2] = rs.getString("RUN_AUDITOR");
                fila[3] = rs.getString("NOMBRE_AUDITOR");
                fila[4] = rs.getString("NOMBRE_PROFESION");
                fila[5] = rs.getDouble("COMISION_TOTAL_AUDIT");
                fila[6] = rs.getDouble("COMISION_MONTO_AUDIT");
                fila[7] = rs.getDouble("COMISION_PROF_CRITICA");
                fila[8] = rs.getDouble("COMISION_EXTRA");
                fila[9] = rs.getDouble("TOTAL_COMISION_AUDIT");
                fila[10] = rs.getDouble("TOTAL_COMISION_EMPRESA");
                modelo.addRow(fila);
            }

            // Mostrar resultados en una nueva ventana
            JScrollPane scrollPane = new JScrollPane(tabla);
            JFrame frame = new JFrame("Detalle de Comisiones - " + mes + "/" + anno);
            frame.add(scrollPane);
            frame.setSize(1000, 500);
            frame.setVisible(true);
            frame.setLocationRelativeTo(null);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al mostrar detalle: " + e.getMessage());
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (stmt != null)
                    stmt.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
