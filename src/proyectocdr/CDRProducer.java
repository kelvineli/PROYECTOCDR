package proyectocdr;

import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public class CDRProducer implements Runnable {
    private final List<CDR> subLista;
    private final CDRQueue queue;
    private final DefaultTableModel modeloProductores;

    public CDRProducer(List<CDR> subLista, CDRQueue queue, DefaultTableModel modeloProductores) {
        this.subLista = subLista;
        this.queue = queue;
        this.modeloProductores = modeloProductores;
    }

    @Override
    public void run() {
        String me = Thread.currentThread().getName();
        String horaInicio = java.time.LocalTime.now().toString();

        //Usamos un array como "contenedor mutable para el índice
        final int[] rowIndex = new int[1];

        try {
            // Agregar la fila y capturar el índice
            SwingUtilities.invokeAndWait(() -> {
                modeloProductores.addRow(new Object[]{me, horaInicio, 0});
                rowIndex[0] = modeloProductores.getRowCount() - 1; // fila recién agregada
            });

            int localCount = 0;

            for (CDR cdr : subLista) {
                queue.put(cdr);
                localCount++;

                final int countFinal = localCount;
                final int row = rowIndex[0];

                SwingUtilities.invokeLater(() -> {
                    modeloProductores.setValueAt(countFinal, row, 2);
                });

                Thread.sleep(200);
            }

            System.out.printf("[%s] terminó con %d registros producidos.%n", me, localCount);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
