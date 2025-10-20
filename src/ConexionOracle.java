// Gestionar la conexión a Oracle Cloud Autonomous Database

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConexionOracle {

    // Configuración por defecto
    private static final String SERVICE_NAME = "twqz0kc12atcw6lw_high";
    private static final String USER = "Eval_2";
    private static final String PASSWORD = "DuocUc..2025";

    // Ruta relativa al wallet desde la raíz del proyecto
    private static final String WALLET_PATH = "./config/wallet";

    // Obtiene la ruta absoluta del wallet

    private static String getWalletAbsolutePath() {
        File walletDir = new File(WALLET_PATH);
        String absolutePath = walletDir.getAbsolutePath();

        // Convertir barras invertidas a barras normales para Java
        absolutePath = absolutePath.replace("\\", "/");

        System.out.println("Ruta del wallet: " + absolutePath);
        return absolutePath;
    }

    // Intenta cargar configuración desde archivo properties (opcional)

    private static Properties loadConfig() {
        Properties config = new Properties();
        File configFile = new File("./config/database.properties");

        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                config.load(fis);
                System.out.println("Configuración cargada desde database.properties");
            } catch (IOException e) {
                System.out.println(" No se pudo cargar database.properties, usando valores por defecto");
            }
        }
        return config;
    }

    // Establece conexión con Oracle Cloud

    public static Connection conectar() {
        try {
            // Cargar configuración (si existe)
            Properties config = loadConfig();

            // Obtener ruta absoluta del wallet
            String walletPath = getWalletAbsolutePath();

            // Verificar que el wallet existe
            File walletDir = new File(walletPath);
            if (!walletDir.exists() || !walletDir.isDirectory()) {
                System.err.println("ERROR: No se encontró el wallet en: " + walletPath);
                System.err.println("   Por favor, coloca los archivos del wallet en la carpeta config/wallet/");
                return null;
            }

            // Establecer propiedades SSL y del wallet
            System.setProperty("oracle.net.tns_admin", walletPath);
            System.setProperty("oracle.net.ssl_server_dn_match", "true");

            // Para Oracle Autonomous Database, usar el wallet SSO (más simple)
            // No es necesario configurar trustStore y keyStore manualmente
            // El driver JDBC los encuentra automáticamente en TNS_ADMIN

            // Configurar propiedades de autenticación
            Properties props = new Properties();
            props.setProperty("user", config.getProperty("db.user", USER));
            props.setProperty("password", config.getProperty("db.password", PASSWORD));

            // URL de conexión con TNS_ADMIN incorporado
            // Usar URI encoding para la ruta si tiene espacios
            String serviceName = config.getProperty("db.service", SERVICE_NAME);
            String encodedWalletPath = walletPath.replace(" ", "%20");
            String url = "jdbc:oracle:thin:@" + serviceName + "?TNS_ADMIN=" + encodedWalletPath;

            System.out.println("🔌 Conectando a Oracle Cloud...");
            System.out.println("   Servicio: " + serviceName);
            System.out.println("   Usuario: " + props.getProperty("user"));

            // Intentar la conexión
            Connection conn = DriverManager.getConnection(url, props);

            System.out.println("Conectado correctamente a Oracle Cloud");
            System.out.println(" Usuario: " + props.getProperty("user"));

            return conn;

        } catch (SQLException e) {
            System.err.println(" Error al conectar con Oracle Cloud:");
            System.err.println("   Código: " + e.getErrorCode());
            System.err.println("   Mensaje: " + e.getMessage());
            System.err.println("\n Posibles soluciones:");
            System.err.println("   1. Verifica que el wallet esté en config/wallet/");
            System.err.println("   2. Verifica las credenciales (usuario y contraseña)");
            System.err.println("   3. Verifica que la URL de conexión sea correcta");
            System.err.println("   4. Verifica que la base de datos esté activa en Oracle Cloud");
            return null;
        }
    }

    // Cerrar la conexión de forma segura
    public static void cerrarConexion(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("🔌 Conexión cerrada correctamente");
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }
}
