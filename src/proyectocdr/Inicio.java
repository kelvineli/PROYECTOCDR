package proyectocdr;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Inicio extends JFrame {

    private JLabel lblArchivo;
    private JButton btnSeleccionarArchivo;
    private JButton btnIniciar;
    private JTable tablaProductores;
    private JTable tablaConsumidores;
    private DefaultTableModel modeloProductores;
    private DefaultTableModel modeloConsumidores;
    private List<CDR> listaCDRs = new ArrayList<>();

    //Variable para guardar la ruta del archivo seleccionado
    private String rutaArchivo = null;

    public Inicio() {

        //Creación de Ventana
        setTitle("Procesamiento de CDR");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        //Panel superior selección de archivo y ejecución de la lectura
        JPanel panelArchivo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblArchivo = new JLabel("Seleccione el archivo CSV con los CDR:");
        btnSeleccionarArchivo = new JButton("Seleccionar archivo");
        btnIniciar = new JButton("Iniciar procesamiento");

        panelArchivo.add(lblArchivo);
        panelArchivo.add(btnSeleccionarArchivo);
        panelArchivo.add(btnIniciar);

        //Tablas
        String[] columnasProductores = {"ID Productor", "Hora de inicio", "Registros producidos"};
        modeloProductores = new DefaultTableModel(columnasProductores, 0);
        tablaProductores = new JTable(modeloProductores);
        JScrollPane scrollProductores = new JScrollPane(tablaProductores);
        scrollProductores.setBorder(BorderFactory.createTitledBorder("PRODUCTORES"));

        String[] columnasConsumidores = {"ID Consumidor", "Hora de inicio", "Registros consumidos", "Total minutos procesados"};
        modeloConsumidores = new DefaultTableModel(columnasConsumidores, 0);
        tablaConsumidores = new JTable(modeloConsumidores);
        JScrollPane scrollConsumidores = new JScrollPane(tablaConsumidores);
        scrollConsumidores.setBorder(BorderFactory.createTitledBorder("CONSUMIDORES"));

        JPanel panelTablas = new JPanel(new GridLayout(2, 1, 10, 10));
        panelTablas.add(scrollProductores);
        panelTablas.add(scrollConsumidores);

        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.add(panelArchivo, BorderLayout.NORTH);

        add(panelSuperior, BorderLayout.NORTH);
        add(panelTablas, BorderLayout.CENTER);

        // Acción para seleccionar archivo
        btnSeleccionarArchivo.addActionListener((ActionEvent e) -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Seleccionar archivo CSV");

            int result = fileChooser.showOpenDialog(Inicio.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File archivo = fileChooser.getSelectedFile();
                rutaArchivo = archivo.getAbsolutePath();
                lblArchivo.setText("Archivo seleccionado: " + rutaArchivo);
            }
        });

        // Acción para iniciar procesamiento
        btnIniciar.addActionListener((ActionEvent e) -> {
            if (rutaArchivo == null) {
                JOptionPane.showMessageDialog(Inicio.this,
                        "Por favor seleccione un archivo CSV primero.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                leerArchivoCSV(rutaArchivo);

                if (listaCDRs.isEmpty()) {
                    JOptionPane.showMessageDialog(Inicio.this,
                            "El archivo no contiene registros válidos.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                //Configuración de hilos
                final int QUEUE_CAP = 200;
                final int N_PRODUCERS = 2;
                final int N_CONSUMERS = 3;

                CDRQueue queue = new CDRQueue(QUEUE_CAP);
                CountDownLatch doneProducers = new CountDownLatch(N_PRODUCERS);

                // Iniciar consumidores
                for (int i = 0; i < N_CONSUMERS; i++) {
                    Thread consumer = new Thread(new CDRConsumer(queue, modeloConsumidores), "C" + (i + 1));
                    consumer.start();
                }

                // Dividir lista de CDRs entre productores
                int total = listaCDRs.size();
                int chunkSize = (int) Math.ceil((double) total / N_PRODUCERS);

                for (int i = 0; i < N_PRODUCERS; i++) {
                    int start = i * chunkSize;
                    int end = Math.min(start + chunkSize, total);

                    if (start >= end) break; // por si hay menos registros que productores

                    List<CDR> subLista = listaCDRs.subList(start, end);

                    Thread producer = new Thread(() -> {
                        try {
                            new CDRProducer(subLista, queue, modeloProductores).run();
                        } finally {
                            doneProducers.countDown();
                        }
                    }, "P" + (i + 1));
                    producer.start();
                }

                // Hilo que espera a que terminen los productores
                new Thread(() -> {
                    try {
                        doneProducers.await();
                        for (int i = 0; i < N_CONSUMERS; i++) {
                            queue.put(CDR.poison());
                        }
                        System.out.println("Todos los productores y consumidores finalizaron.");
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            }
        });
    }

    private void leerArchivoCSV(String ruta) {
        listaCDRs.clear(); // limpiar lista antes de cargar nuevo archivo
        try (BufferedReader br = new BufferedReader(new FileReader(ruta))) {
            String linea;
            int contador = 0;
            System.out.println("Leyendo archivo: " + ruta);
            System.out.println("---------------------------------------------------");

            while ((linea = br.readLine()) != null) {
                contador++;
                String[] partes = linea.split(",");

                if (partes.length >= 7) {
                    try {
                        // Crear objeto CDR
                        CDR cdr = new CDR(
                                partes[0],  // cuenta
                                partes[1],  // origen
                                partes[2],  // destino
                                partes[3],  // timestamp
                                Integer.parseInt(partes[4]),  // duración
                                Double.parseDouble(partes[5]), // tarifa
                                partes[6]   // tipo
                        );

                        listaCDRs.add(cdr);

                        System.out.println("Línea " + contador + ": " + cdr);

                    } catch (NumberFormatException nfe) {
                        System.out.println("Error en formato numérico en la línea " + contador + ": " + linea);
                    }
                } else {
                    System.out.println("Línea " + contador + " incompleta: " + linea);
                }
            }

            System.out.println("---------------------------------------------------");
            JOptionPane.showMessageDialog(this,
                    "Se leyeron " + contador + " líneas del archivo.\nTotal de objetos CDR almacenados: " + listaCDRs.size(),
                    "Lectura completa", JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al leer el archivo: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
