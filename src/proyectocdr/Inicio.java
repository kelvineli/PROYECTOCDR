/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package proyectocdr;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class Inicio extends JFrame {
    
    private JLabel lblArchivo;
    private JButton btnSeleccionarArchivo;
    private JButton btnIniciar;
    private JTable tablaProductores;
    private JTable tablaConsumidores;
    private DefaultTableModel modeloProductores;
    private DefaultTableModel modeloConsumidores;

    public Inicio() {
        
        // ----- Creación de Ventana -----
        setTitle("Procesamiento de CDR");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // ----- Panel superior: selección de archivo y ejecución de la lectura -----
        JPanel panelArchivo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblArchivo = new JLabel("Seleccione el archivo CSV con los CDR:");
        btnSeleccionarArchivo = new JButton("Seleccionar archivo");
        btnIniciar = new JButton("Iniciar procesamiento");

        panelArchivo.add(lblArchivo);
        panelArchivo.add(btnSeleccionarArchivo);
        panelArchivo.add(btnIniciar);

        // ----- Tablas -----
        // Tabla productores
        String[] columnasProductores = {"ID Productor", "Hora de inicio", "Registros producidos"};
        modeloProductores = new DefaultTableModel(columnasProductores, 0);
        tablaProductores = new JTable(modeloProductores);
        JScrollPane scrollProductores = new JScrollPane(tablaProductores);
        scrollProductores.setBorder(BorderFactory.createTitledBorder("PRODUCTORES"));

        // Tabla consumidores
        String[] columnasConsumidores = {"ID Consumidor", "Hora de inicio", "Registros consumidos", "Total minutos procesados"};
        modeloConsumidores = new DefaultTableModel(columnasConsumidores, 0);
        tablaConsumidores = new JTable(modeloConsumidores);
        JScrollPane scrollConsumidores = new JScrollPane(tablaConsumidores);
        scrollConsumidores.setBorder(BorderFactory.createTitledBorder("CONSUMIDORES"));

        // Panel central con las tablas
        JPanel panelTablas = new JPanel(new GridLayout(2, 1, 10, 10));
        panelTablas.add(scrollProductores);
        panelTablas.add(scrollConsumidores);

        // ----- Añadir todo a la ventana -----
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.add(panelArchivo, BorderLayout.NORTH);

        add(panelSuperior, BorderLayout.NORTH);
        add(panelTablas, BorderLayout.CENTER);

    }
}