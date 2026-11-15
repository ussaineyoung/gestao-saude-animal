package mz.saudeanimal.modelo;

import java.time.LocalDate;

/** Um evento de saúde de um animal: vacina, desparasitação, tratamento, consulta... */
public class RegistoSaude {

    public static final String[] TIPOS = {
        "Vacinação", "Desparasitação", "Tratamento", "Consulta", "Cirurgia", "Outro"
    };

    private int id;
    private int animalId;
    private LocalDate data;
    private String tipo = TIPOS[0];
    private String descricao = "";
    private LocalDate proximaData;   // lembrete opcional (null = sem lembrete)
    private boolean concluido;       // true quando o lembrete já foi cumprido

    public RegistoSaude() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getAnimalId() { return animalId; }
    public void setAnimalId(int animalId) { this.animalId = animalId; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public LocalDate getProximaData() { return proximaData; }
    public void setProximaData(LocalDate proximaData) { this.proximaData = proximaData; }

    public boolean isConcluido() { return concluido; }
    public void setConcluido(boolean concluido) { this.concluido = concluido; }
}
