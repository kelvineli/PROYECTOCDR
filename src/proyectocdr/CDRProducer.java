package proyectocdr;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.concurrent.atomic.AtomicLong;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class CDRProducer implements Runnable {
    private final String filePath;
    private final CDRQueue queue;
    private final AtomicLong producedCount;
    private final DefaultTableModel modeloProductores;

    public CDRProducer(String filePath, CDRQueue queue,
                       AtomicLong producedCount,
                       DefaultTableModel modeloProductores) {
        this.filePath = filePath;
        this.queue = queue;
        this.producedCount = producedCount;
        this.modeloProductores = modeloProductores;
    }

    @Override
    public void run() {
        String me = Thread.currentThread().getName();
        long count = 0;
        String horaInicio = java.time.LocalTime.now().toString();

        // Agregar fila en la tabla de productores
        SwingUtilities.invokeLater(() -> {
            modeloProductores.addRow(new Object[]{me, horaInicio, 0});
        });

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 7) continue;

                CDR cdr = new CDR(
                        parts[0],  // cuenta
                        parts[1],  // origen
                        parts[2],  // destino
                        parts[3],  // timestamp
                        Integer.parseInt(parts[4]),  // duración
                        Double.parseDouble(parts[5]), // tarifa
                        parts[6]   // tipo
                );

                queue.put(cdr);
                count = producedCount.incrementAndGet();

                long finalCount = count;
                SwingUtilities.invokeLater(() -> {
                    // Actualizar la última fila de este productor
                    int lastRow = modeloProductores.getRowCount() - 1;
                    modeloProductores.setValueAt(finalCount, lastRow, 2);
                });

                Thread.sleep(300); // Simula latencia
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
