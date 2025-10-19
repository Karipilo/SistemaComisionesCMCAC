// ===============================================================
// Clase: MostrarErrores
// Propósito: Visualizar la tabla ERROR_PROCESO con JTable
// ===============================================================

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class MostrarErrores {

    public static void mostrar() {
        Connection conn = ConexionOracle.conectar();
        if (conn == null)
            return;

        String sql = "SELECT id_error, programa_origen, fecha_error, sql_code, sql_mensaje " +
                "FROM ERROR_PROCESO ORDER BY id_error DESC";

        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("ID");
            model.addColumn("Programa Origen");
            model.addColumn("Fecha Error");
            model.addColumn("Código SQL");
            model.addColumn("Mensaje Error");

            while (rs.next()) {
                model.addRow(new Object[] {
                        rs.getInt(1), rs.getString(2), rs.getTimestamp(3),
                        rs.getInt(4), rs.getString(5)
                });
            }

            JTable table = new JTable(model);
            JScrollPane scrollPane = new JScrollPane(table);

            JFrame frame = new JFrame("Registro de Errores - ERROR_PROCESO");
            frame.setSize(900, 400);
            frame.add(scrollPane);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Error al mostrar los errores: " + e.getMessage());
        }
    }
}
