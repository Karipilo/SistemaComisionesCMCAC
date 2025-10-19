import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConexionOracle {

    private static final String URL = "jdbc:oracle:thin:@twqz0kc12atcw6lw_high?TNS_ADMIN=C:/Users/Duoc/Downloads/Wallet_entrega2";
    private static final String USER = "Eval_2";
    private static final String PASSWORD = "DuocUc..2025";

    public static Connection conectar() {
        try {
            // Establecemos propiedades SSL
            System.setProperty("oracle.net.tns_admin", "C:/Users/Duoc/Downloads/Wallet_entrega2");
            System.setProperty("oracle.net.ssl_server_dn_match", "true");
            System.setProperty("javax.net.ssl.trustStore", "C:/Users/Duoc/Downloads/Wallet_entrega2/truststore.jks");
            System.setProperty("javax.net.ssl.trustStorePassword", "password");
            System.setProperty("javax.net.ssl.keyStore", "C:/Users/Duoc/Downloads/Wallet_entrega2/keystore.jks");
            System.setProperty("javax.net.ssl.keyStorePassword", "password");

            // Propiedades de autenticación
            Properties props = new Properties();
            props.setProperty("user", USER);
            props.setProperty("password", PASSWORD);

            // Intentamos conectar
            Connection conn = DriverManager.getConnection(URL, props);
            System.out.println("✅ Conectado correctamente a Oracle Cloud.");
            return conn;

        } catch (SQLException e) {
            System.err.println("Error al conectar con Oracle Cloud: " + e.getMessage());
            return null;
        }
    }
}
