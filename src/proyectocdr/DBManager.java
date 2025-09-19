package proyectocdr;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DBManager {
    private static final String URL = "jdbc:mysql://localhost:3306/cdrdb";
    private static final String USER = "root";
    private static final String PASSWORD = "sandia500";

    private static Connection conn;

    public static Connection getConnection() throws SQLException {
        if (conn == null || conn.isClosed()) {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return conn;
    }

    //Guardar todos los registros de cada cuenta
    public static void guardarCDR(CDR cdr) {
        String sql = "INSERT INTO cdrs (cuenta, origen, destino, fecha_llamada, duracion, tarifa, tipo) VALUES (?,?,?,?,?,?,?)";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, cdr.cuenta);
            ps.setString(2, cdr.origen);
            ps.setString(3, cdr.destino);

            // Parsear timestamp de "26/07/2024 16:02:00" a DATETIME
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            LocalDateTime fecha = LocalDateTime.parse(cdr.timestamp, formatter);
            ps.setTimestamp(4, Timestamp.valueOf(fecha));

            ps.setInt(5, cdr.duracion);
            ps.setDouble(6, cdr.tarifa);
            ps.setString(7, cdr.tipo);

            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Guardar el acumulado para cada cuenta
    public static void actualizarResumen(CDR cdr) {
        String sql = "INSERT INTO resumen_cuentas (cuenta, total_minutos, total_tarifa) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE total_minutos = total_minutos + VALUES(total_minutos), " +
                "total_tarifa = total_tarifa + VALUES(total_tarifa)";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, cdr.cuenta);
            ps.setInt(2, cdr.duracion);
            ps.setDouble(3, cdr.duracion * cdr.tarifa);

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Reiniciar las tablas al leer un nuevo archivo
    public static void limpiarTablas() {
        String[] sqls = {
                "TRUNCATE TABLE cdrs",
                "TRUNCATE TABLE resumen_cuentas"
        };

        try (Statement st = getConnection().createStatement()) {
            for (String sql : sqls) {
                st.executeUpdate(sql);
            }
            System.out.println("Tablas reiniciadas correctamente.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}

