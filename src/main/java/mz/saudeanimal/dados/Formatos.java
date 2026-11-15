package mz.saudeanimal.dados;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** Conversão de datas no formato dd/MM/yyyy. */
public final class Formatos {

    public static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Formatos() {
    }

    public static String data(LocalDate d) {
        return d == null ? "" : d.format(DATA);
    }

    /** Devolve null se o texto estiver vazio; lança DateTimeParseException se for inválido. */
    public static LocalDate lerData(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }
        return LocalDate.parse(texto.trim(), DATA);
    }
}
