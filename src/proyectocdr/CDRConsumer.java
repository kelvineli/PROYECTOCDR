package proyectocdr;

import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class CDRConsumer implements Runnable {
    private final CDRQueue queue;
    private final DefaultTableModel modeloConsumidores;
    private int totalMinutos = 0;
    private int localCount = 0;

    public CDRConsumer(CDRQueue queue, DefaultTableModel modeloConsumidores) {
        this.queue = queue;
        this.modeloConsumidores = modeloConsumidores;
    }

    @Override
    public void run() {
        String me = Thread.currentThread().getName();
        String horaInicio = java.time.LocalTime.now().toString();

        //Usamos contenedor para guardar el índice de fila
        final int[] rowIndex = new int[1];

        try {
            // Insertar fila y capturar índice
            SwingUtilities.invokeAndWait(() -> {
                modeloConsumidores.addRow(new Object[]{me, horaInicio, 0, 0});
                rowIndex[0] = modeloConsumidores.getRowCount() - 1;
            });

            while (true) {
                CDR cdr = queue.take();
                if (cdr.poison) {
                    System.out.println("[" + me + "] terminó.");
                    break;
                }

                // Actualizar métricas locales
                localCount++;
                totalMinutos += cdr.duracion;

                // Procesar el CDR en la base de datos
                DBManager.guardarCDR(cdr);
                DBManager.actualizarResumen(cdr);

                System.out.println("[" + me + "] procesó: " + cdr);

                final int countFinal = localCount;
                final int minutosFinal = totalMinutos;
                final int row = rowIndex[0];

                // Actualizar tabla
                SwingUtilities.invokeLater(() -> {
                    modeloConsumidores.setValueAt(countFinal, row, 2);
                    modeloConsumidores.setValueAt(minutosFinal, row, 3);
                });

                Thread.sleep(400); // simula tiempo de procesamiento
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}