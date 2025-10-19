// ===============================================================
// Clase: MostrarResumen
// Propósito: Visualizar los datos de RESUMEN_COMISIONES_AUDITORIAS_MES
// ===============================================================

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class MostrarResumen {

    public static void mostrar() {
        Connection conn = ConexionOracle.conectar();
        if (conn == null)
            return;

        String sql = "SELECT mes_proceso, anno_proceso, nombre_profesion, " +
                "total_auditores, total_con_auditorias, total_sin_auditorias, " +
                "monto_total_auditorias, monto_total_comisiones " +
                "FROM RESUMEN_COMISIONES_AUDITORIAS_MES";

        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("Mes");
            model.addColumn("Año");
            model.addColumn("Profesión");
            model.addColumn("Total Auditores");
            model.addColumn("Con Auditorías");
            model.addColumn("Sin Auditorías");
            model.addColumn("Monto Auditorías");
            model.addColumn("Monto Comisiones");

            while (rs.next()) {
                model.addRow(new Object[] {
                        rs.getInt(1), rs.getInt(2), rs.getString(3),
                        rs.getInt(4), rs.getInt(5), rs.getInt(6),
                        rs.getDouble(7), rs.getDouble(8)
                });
            }

            JTable table = new JTable(model);
            JScrollPane scrollPane = new JScrollPane(table);

            JFrame frame = new JFrame("Resumen de Comisiones Mensual");
            frame.setSize(900, 400);
            frame.add(scrollPane);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    " Error al mostrar el resumen: " + e.getMessage());
        }
    }
}
