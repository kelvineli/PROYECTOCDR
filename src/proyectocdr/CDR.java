package proyectocdr;

public class CDR {
    public final String cuenta;
    public final String origen;
    public final String destino;
    public final String timestamp;
    public final int duracion;
    public final double tarifa;
    public final String tipo;
    public final boolean poison;

    public CDR(String cuenta, String origen, String destino, String timestamp,
               int duracion, double tarifa, String tipo, boolean poison) {
        this.cuenta = cuenta;
        this.origen = origen;
        this.destino = destino;
        this.timestamp = timestamp;
        this.duracion = duracion;
        this.tarifa = tarifa;
        this.tipo = tipo;
        this.poison = poison;
    }

    public CDR(String cuenta, String origen, String destino, String timestamp,
               int duracion, double tarifa, String tipo) {
        this(cuenta, origen, destino, timestamp, duracion, tarifa, tipo, false);
    }

    public static CDR poison() {
        return new CDR("", "", "", "", -1, 0.0, "", true);
    }

    @Override
    public String toString() {
        return String.format(
                "CDR -> Cuenta: %s | Origen: %s | Destino: %s | Fecha: %s | Duración: %d min | Tarifa: %.2f | Tipo: %s",
                cuenta, origen, destino, timestamp, duracion, tarifa, tipo
        );
    }
}
