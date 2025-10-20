// ===============================================================
// Clase: MostrarResumen
// Propósito: Visualizar los datos de RESUMEN_COMISIONES_AUDITORIAS_MES
// ===============================================================

import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class MostrarResumen {

    public static void mostrar() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = ConexionOracle.conectar();
            if (conn == null) {
                JOptionPane.showMessageDialog(null,
                        "No se pudo conectar a la base de datos.\nRevisa la consola para más detalles.",
                        "Error de Conexión",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Usar los nombres reales de las columnas de la tabla
            String sql = "SELECT mes_proceso, anno_proceso, total_auditores, " +
                    "NVL(suma_comision_audit, 0) as suma_comision_audit, " +
                    "NVL(suma_costo_empresa, 0) as suma_costo_empresa " +
                    "FROM RESUMEN_COMISIONES_AUDITORIAS_MES " +
                    "ORDER BY anno_proceso DESC, mes_proceso DESC";

            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("Mes");
            model.addColumn("Año");
            model.addColumn("Total Auditores");
            model.addColumn("Suma Comisión Auditoría");
            model.addColumn("Suma Costo Empresa");

            int rowCount = 0;
            while (rs.next()) {
                model.addRow(new Object[] {
                        rs.getInt(1),
                        rs.getInt(2),
                        rs.getInt(3),
                        String.format("$%,.2f", rs.getDouble(4)),
                        String.format("$%,.2f", rs.getDouble(5))
                });
                rowCount++;
            }

            if (rowCount == 0) {
                JOptionPane.showMessageDialog(null,
                        "No hay datos en el resumen.\n" +
                                "Ejecuta 'Procesar Comisiones' primero.",
                        "Sin Datos",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            JTable table = new JTable(model);
            table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

            JScrollPane scrollPane = new JScrollPane(table);

            JFrame frame = new JFrame(" Resumen de Comisiones Mensual - " + rowCount + " registros");
            frame.setSize(800, 450);
            frame.add(scrollPane);
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setVisible(true);

            System.out.println("Resumen mostrado: " + rowCount + " registros");

        } catch (SQLException e) {
            System.err.println("Error SQL al mostrar resumen: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    "Error al mostrar el resumen:\n" + e.getMessage(),
                    "Error SQL",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            // Cerrar recursos en orden inverso
            try {
                if (rs != null)
                    rs.close();
                if (ps != null)
                    ps.close();
                if (conn != null)
                    ConexionOracle.cerrarConexion(conn);
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos: " + e.getMessage());
            }
        }
    }
}
