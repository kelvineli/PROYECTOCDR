package proyectocdr;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;

public class Resultados extends JFrame {

    private JTextField txtCuenta;
    private JButton btnBuscar;
    private JTable tablaResultados;      // Resumen
    private JTable tablaDetalles;        // Detalle
    private DefaultTableModel modeloResumen;
    private DefaultTableModel modeloDetalles;

    public Resultados() {
        setTitle("Resultados por Cuenta");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel superior de búsqueda
        JPanel panelBusqueda = new JPanel(new FlowLayout());
        panelBusqueda.add(new JLabel("Número de cuenta:"));
        txtCuenta = new JTextField(15);
        panelBusqueda.add(txtCuenta);

        btnBuscar = new JButton("Buscar");
        panelBusqueda.add(btnBuscar);

        // Tabla de resumen
        String[] columnasResumen = {"Cuenta", "Total Minutos", "Total Tarifa"};
        modeloResumen = new DefaultTableModel(columnasResumen, 0);
        tablaResultados = new JTable(modeloResumen);
        JScrollPane scrollResumen = new JScrollPane(tablaResultados);
        scrollResumen.setBorder(BorderFactory.createTitledBorder("Resumen por cuenta"));

        // Tabla de detalles
        String[] columnasDetalles = {
                "Cuenta", "Origen", "Destino", "Fecha", "Duración", "Tarifa", "Tipo", "Costo"
        };
        modeloDetalles = new DefaultTableModel(columnasDetalles, 0);
        tablaDetalles = new JTable(modeloDetalles);
        JScrollPane scrollDetalles = new JScrollPane(tablaDetalles);
        scrollDetalles.setBorder(BorderFactory.createTitledBorder("Detalle de CDR"));

        // Panel central dividido
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollResumen, scrollDetalles);
        splitPane.setDividerLocation(200);

        add(panelBusqueda, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        // Acción de buscar
        btnBuscar.addActionListener((ActionEvent e) -> {
            String cuenta = txtCuenta.getText().trim();
            if (cuenta.length() > 0) {
                buscarCuenta(cuenta);
                cargarDetalles(cuenta);
            } else {
                cargarTodos(); //Cargar todas las cuentas
                modeloDetalles.setRowCount(0); // Limpiar detalle si no se busca nada
            }
        });

        // Cargar todos al inicio
        cargarTodos();
    }

    private void buscarCuenta(String cuenta) {
        modeloResumen.setRowCount(0); // limpiar tabla
        String sql = "SELECT cuenta, total_minutos, total_tarifa FROM resumen_cuentas WHERE cuenta = ?";

        try (PreparedStatement ps = DBManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, cuenta);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                modeloResumen.addRow(new Object[]{
                        rs.getString("cuenta"),
                        rs.getInt("total_minutos"),
                        rs.getDouble("total_tarifa")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void cargarTodos() {
        modeloResumen.setRowCount(0);
        String sql = "SELECT cuenta, total_minutos, total_tarifa FROM resumen_cuentas";

        try (Statement st = DBManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                modeloResumen.addRow(new Object[]{
                        rs.getString("cuenta"),
                        rs.getInt("total_minutos"),
                        rs.getDouble("total_tarifa")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void cargarDetalles(String cuenta) {
        modeloDetalles.setRowCount(0);
        String sql = "SELECT cuenta, origen, destino, fecha_llamada, duracion, tarifa, tipo FROM cdrs WHERE cuenta = ?";

        try (PreparedStatement ps = DBManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, cuenta);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int duracion = rs.getInt("duracion");
                double tarifa = rs.getDouble("tarifa");
                double costo = duracion * tarifa;

                modeloDetalles.addRow(new Object[]{
                        rs.getString("cuenta"),
                        rs.getString("origen"),
                        rs.getString("destino"),
                        rs.getTimestamp("fecha_llamada"),
                        duracion,
                        tarifa,
                        rs.getString("tipo"),
                        costo
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

}
