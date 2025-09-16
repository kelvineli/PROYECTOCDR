package proyectocdr;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.util.concurrent.atomic.AtomicLong;

public class CDRConsumer implements Runnable {
    private final CDRQueue queue;
    private final DefaultTableModel modeloConsumidores;
    private final AtomicLong consumedCount = new AtomicLong(0);
    private int totalMinutos = 0;

    public CDRConsumer(CDRQueue queue, DefaultTableModel modeloConsumidores) {
        this.queue = queue;
        this.modeloConsumidores = modeloConsumidores;
    }

    @Override
    public void run() {
        String me = Thread.currentThread().getName();
        String horaInicio = java.time.LocalTime.now().toString();

        // Agregar fila en la tabla de consumidores
        SwingUtilities.invokeLater(() -> {
            modeloConsumidores.addRow(new Object[]{me, horaInicio, 0, 0});
        });

        try {
            while (true) {
                CDR cdr = queue.take();
                if (cdr.poison) {
                    System.out.println("[" + me + "] terminó.");
                    break;
                }

                consumedCount.incrementAndGet();
                totalMinutos += cdr.duracion;

                long count = consumedCount.get();
                int minutos = totalMinutos;

                SwingUtilities.invokeLater(() -> {
                    int lastRow = modeloConsumidores.getRowCount() - 1;
                    modeloConsumidores.setValueAt(count, lastRow, 2);
                    modeloConsumidores.setValueAt(minutos, lastRow, 3);
                });

                Thread.sleep(400); // Simula procesamiento
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
