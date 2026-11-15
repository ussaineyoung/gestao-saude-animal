package mz.saudeanimal.dados;

import mz.saudeanimal.modelo.Animal;
import mz.saudeanimal.modelo.RegistoSaude;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Guarda os dados em dois ficheiros CSV (animais.csv e registos.csv)
 * dentro da pasta indicada. Não precisa de base de dados.
 */
public class Repositorio {

    private final Path pasta;
    private final Path ficheiroAnimais;
    private final Path ficheiroRegistos;
    private final List<Animal> animais = new ArrayList<>();
    private final List<RegistoSaude> registos = new ArrayList<>();

    public Repositorio(Path pasta) {
        this.pasta = pasta;
        this.ficheiroAnimais = pasta.resolve("animais.csv");
        this.ficheiroRegistos = pasta.resolve("registos.csv");
    }

    // ------------------------------------------------------------ carregar / guardar

    public void carregar() throws IOException {
        animais.clear();
        registos.clear();

        if (!Files.exists(ficheiroAnimais)) {
            criarDadosExemplo();
            guardar();
            return;
        }

        for (String linha : Files.readAllLines(ficheiroAnimais, StandardCharsets.UTF_8)) {
            if (linha.trim().isEmpty() || linha.startsWith("#")) {
                continue;
            }
            try {
                String[] c = linha.split(";", -1);
                Animal a = new Animal();
                a.setId(Integer.parseInt(c[0]));
                a.setNome(c[1]);
                a.setEspecie(c[2]);
                a.setRaca(c[3]);
                a.setSexo(c[4]);
                a.setDataNascimento(c[5].isEmpty() ? null : LocalDate.parse(c[5]));
                a.setPesoKg(c[6].isEmpty() ? 0 : Double.parseDouble(c[6]));
                a.setProprietario(c[7]);
                a.setLocalizacao(c[8]);
                animais.add(a);
            } catch (RuntimeException ex) {
                // linha inválida: ignora e segue
            }
        }

        if (Files.exists(ficheiroRegistos)) {
            for (String linha : Files.readAllLines(ficheiroRegistos, StandardCharsets.UTF_8)) {
                if (linha.trim().isEmpty() || linha.startsWith("#")) {
                    continue;
                }
                try {
                    String[] c = linha.split(";", -1);
                    RegistoSaude r = new RegistoSaude();
                    r.setId(Integer.parseInt(c[0]));
                    r.setAnimalId(Integer.parseInt(c[1]));
                    r.setData(LocalDate.parse(c[2]));
                    r.setTipo(c[3]);
                    r.setDescricao(c[4]);
                    r.setProximaData(c[5].isEmpty() ? null : LocalDate.parse(c[5]));
                    r.setConcluido(Boolean.parseBoolean(c[6]));
                    registos.add(r);
                } catch (RuntimeException ex) {
                    // linha inválida: ignora e segue
                }
            }
        }
    }

    public void guardar() throws IOException {
        Files.createDirectories(pasta);

        List<String> la = new ArrayList<>();
        la.add("#id;nome;especie;raca;sexo;nascimento;pesoKg;proprietario;localizacao");
        for (Animal a : animais) {
            la.add(a.getId() + ";" + limpar(a.getNome()) + ";" + limpar(a.getEspecie()) + ";"
                    + limpar(a.getRaca()) + ";" + limpar(a.getSexo()) + ";"
                    + (a.getDataNascimento() == null ? "" : a.getDataNascimento().toString()) + ";"
                    + a.getPesoKg() + ";" + limpar(a.getProprietario()) + ";" + limpar(a.getLocalizacao()));
        }
        Files.write(ficheiroAnimais, la, StandardCharsets.UTF_8);

        List<String> lr = new ArrayList<>();
        lr.add("#id;animalId;data;tipo;descricao;proximaData;concluido");
        for (RegistoSaude r : registos) {
            lr.add(r.getId() + ";" + r.getAnimalId() + ";" + r.getData() + ";" + limpar(r.getTipo()) + ";"
                    + limpar(r.getDescricao()) + ";"
                    + (r.getProximaData() == null ? "" : r.getProximaData().toString()) + ";"
                    + r.isConcluido());
        }
        Files.write(ficheiroRegistos, lr, StandardCharsets.UTF_8);
    }

    /** Remove caracteres que quebrariam o formato CSV. */
    private static String limpar(String s) {
        return s == null ? "" : s.replace(';', ',').replace('\n', ' ').replace('\r', ' ');
    }

    // ------------------------------------------------------------ animais

    public List<Animal> listarAnimais() {
        return new ArrayList<>(animais);
    }

    public Animal procurarAnimal(int id) {
        for (Animal a : animais) {
            if (a.getId() == id) {
                return a;
            }
        }
        return null;
    }

    public void adicionarAnimal(Animal a) {
        int max = 0;
        for (Animal x : animais) {
            max = Math.max(max, x.getId());
        }
        a.setId(max + 1);
        animais.add(a);
    }

    /** Remove o animal e todos os seus registos de saúde. */
    public void removerAnimal(int id) {
        for (Iterator<Animal> it = animais.iterator(); it.hasNext();) {
            if (it.next().getId() == id) {
                it.remove();
            }
        }
        for (Iterator<RegistoSaude> it = registos.iterator(); it.hasNext();) {
            if (it.next().getAnimalId() == id) {
                it.remove();
            }
        }
    }

    // ------------------------------------------------------------ registos de saúde

    public List<RegistoSaude> registosDoAnimal(int animalId) {
        List<RegistoSaude> r = new ArrayList<>();
        for (RegistoSaude x : registos) {
            if (x.getAnimalId() == animalId) {
                r.add(x);
            }
        }
        r.sort(Comparator.comparing(RegistoSaude::getData).reversed());
        return r;
    }

    public void adicionarRegisto(RegistoSaude r) {
        int max = 0;
        for (RegistoSaude x : registos) {
            max = Math.max(max, x.getId());
        }
        r.setId(max + 1);
        registos.add(r);
    }

    public void removerRegisto(int id) {
        for (Iterator<RegistoSaude> it = registos.iterator(); it.hasNext();) {
            if (it.next().getId() == id) {
                it.remove();
            }
        }
    }

    // ------------------------------------------------------------ alertas

    /** Lembretes pendentes com data até hoje + dias (inclui os atrasados), ordenados por data. */
    public List<RegistoSaude> alertas(int dias) {
        LocalDate limite = LocalDate.now().plusDays(dias);
        List<RegistoSaude> r = new ArrayList<>();
        for (RegistoSaude x : registos) {
            if (x.getProximaData() != null && !x.isConcluido() && !x.getProximaData().isAfter(limite)) {
                r.add(x);
            }
        }
        r.sort(Comparator.comparing(RegistoSaude::getProximaData));
        return r;
    }

    public int contarAtrasados() {
        int n = 0;
        LocalDate hoje = LocalDate.now();
        for (RegistoSaude x : registos) {
            if (x.getProximaData() != null && !x.isConcluido() && x.getProximaData().isBefore(hoje)) {
                n++;
            }
        }
        return n;
    }

    // ------------------------------------------------------------ dados de exemplo

    private void criarDadosExemplo() {
        LocalDate hoje = LocalDate.now();

        Animal a1 = novo("Mimosa", "Bovino", "Nguni", "Fêmea", hoje.minusYears(4).minusMonths(2), 380,
                "Machamba Nampula Sul", "Nampula - Curral 1");
        Animal a2 = novo("Trovão", "Bovino", "Brahman", "Macho", hoje.minusYears(2), 410,
                "Machamba Nampula Sul", "Nampula - Curral 1");
        Animal a3 = novo("Pintada", "Caprino", "Boer", "Fêmea", hoje.minusMonths(18), 42,
                "Associação Agrária de Montepuez", "Cabo Delgado - Montepuez");
        Animal a4 = novo("Rex", "Canino", "Pastor Alemão", "Macho", hoje.minusYears(3), 31,
                "Família Mussa", "Nampula - Cidade");

        registo(a1, hoje.minusDays(40), "Vacinação", "Vacina contra carbúnculo sintomático", hoje.plusDays(10));
        registo(a1, hoje.minusDays(120), "Desparasitação", "Ivermectina, dose por peso", hoje.minusDays(5));
        registo(a2, hoje.minusDays(20), "Tratamento", "Tratamento de carraças (banho carrapaticida)", hoje.plusDays(15));
        registo(a3, hoje.minusDays(60), "Consulta", "Controlo de rotina, sem alterações", null);
        registo(a3, hoje.minusDays(10), "Vacinação", "Vacina contra peste dos pequenos ruminantes", hoje.plusDays(355));
        registo(a4, hoje.minusDays(300), "Vacinação", "Antirrábica anual", hoje.plusDays(65));
    }

    private Animal novo(String nome, String especie, String raca, String sexo, LocalDate nasc,
                        double peso, String proprietario, String local) {
        Animal a = new Animal();
        a.setNome(nome);
        a.setEspecie(especie);
        a.setRaca(raca);
        a.setSexo(sexo);
        a.setDataNascimento(nasc);
        a.setPesoKg(peso);
        a.setProprietario(proprietario);
        a.setLocalizacao(local);
        adicionarAnimal(a);
        return a;
    }

    private void registo(Animal a, LocalDate data, String tipo, String desc, LocalDate proxima) {
        RegistoSaude r = new RegistoSaude();
        r.setAnimalId(a.getId());
        r.setData(data);
        r.setTipo(tipo);
        r.setDescricao(desc);
        r.setProximaData(proxima);
        adicionarRegisto(r);
    }
}
