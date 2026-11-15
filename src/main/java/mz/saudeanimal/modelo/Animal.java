package mz.saudeanimal.modelo;

import java.time.LocalDate;
import java.time.Period;

/** Um animal (ou lote) registado no sistema. */
public class Animal {

    private int id;
    private String nome = "";
    private String especie = "";
    private String raca = "";
    private String sexo = "Macho";
    private LocalDate dataNascimento; // pode ser null
    private double pesoKg;
    private String proprietario = "";
    private String localizacao = "";

    public Animal() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEspecie() { return especie; }
    public void setEspecie(String especie) { this.especie = especie; }

    public String getRaca() { return raca; }
    public void setRaca(String raca) { this.raca = raca; }

    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }

    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }

    public double getPesoKg() { return pesoKg; }
    public void setPesoKg(double pesoKg) { this.pesoKg = pesoKg; }

    public String getProprietario() { return proprietario; }
    public void setProprietario(String proprietario) { this.proprietario = proprietario; }

    public String getLocalizacao() { return localizacao; }
    public void setLocalizacao(String localizacao) { this.localizacao = localizacao; }

    /** Idade em texto, por exemplo "2 anos e 3 meses". */
    public String getIdadeTexto() {
        if (dataNascimento == null || dataNascimento.isAfter(LocalDate.now())) {
            return "—";
        }
        Period p = Period.between(dataNascimento, LocalDate.now());
        int anos = p.getYears();
        int meses = p.getMonths();
        if (anos == 0) {
            return meses + (meses == 1 ? " mês" : " meses");
        }
        String a = anos + (anos == 1 ? " ano" : " anos");
        return meses == 0 ? a : a + " e " + meses + (meses == 1 ? " mês" : " meses");
    }

    /** Texto usado nas listas de seleção. */
    @Override
    public String toString() {
        return "#" + id + " " + nome + " (" + especie + ")";
    }
}
