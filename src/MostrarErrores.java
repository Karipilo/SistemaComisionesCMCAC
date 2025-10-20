// ===============================================================
// Clase: MostrarErrores
// Propósito: Visualizar la tabla ERROR_PROCESO con JTable
// ===============================================================

import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class MostrarErrores {

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

            String sql = "SELECT id_error, programa_origen, fecha_error, sql_code, sql_mensaje " +
                    "FROM ERROR_PROCESO ORDER BY fecha_error DESC";

            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("ID");
            model.addColumn("Programa Origen");
            model.addColumn("Fecha Error");
            model.addColumn("Código SQL");
            model.addColumn("Mensaje Error");

            int rowCount = 0;
            while (rs.next()) {
                model.addRow(new Object[] {
                        rs.getInt(1),
                        rs.getString(2),
                        rs.getTimestamp(3),
                        rs.getInt(4),
                        rs.getString(5)
                });
                rowCount++;
            }

            if (rowCount == 0) {
                JOptionPane.showMessageDialog(null,
                        "No hay errores registrados.\n¡El sistema está funcionando correctamente!",
                        "Sin Errores",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            JTable table = new JTable(model);
            table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

            // Ajustar ancho de columnas
            table.getColumnModel().getColumn(0).setPreferredWidth(50); // ID
            table.getColumnModel().getColumn(1).setPreferredWidth(150); // Programa
            table.getColumnModel().getColumn(2).setPreferredWidth(150); // Fecha
            table.getColumnModel().getColumn(3).setPreferredWidth(100); // SQL Code
            table.getColumnModel().getColumn(4).setPreferredWidth(400); // Mensaje

            JScrollPane scrollPane = new JScrollPane(table);

            JFrame frame = new JFrame("Registro de Errores - " + rowCount + " errores encontrados");
            frame.setSize(1100, 450);
            frame.add(scrollPane);
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setVisible(true);

            System.out.println("⚠️ Errores mostrados: " + rowCount + " registros");

        } catch (SQLException e) {
            System.err.println("Error SQL al mostrar errores: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    " Error al mostrar los errores:\n" + e.getMessage(),
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
                System.err.println(" Error al cerrar recursos: " + e.getMessage());
            }
        }
    }
}
