package mz.saudeanimal.ui;

import mz.saudeanimal.dados.Formatos;
import mz.saudeanimal.dados.Repositorio;
import mz.saudeanimal.modelo.Animal;
import mz.saudeanimal.modelo.RegistoSaude;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/** Separador "Saúde": histórico de vacinas, tratamentos e consultas de cada animal. */
public class PainelSaude extends JPanel {

    private final Repositorio repo;
    private final Runnable aoAlterar;

    private final JComboBox<Animal> animalCombo = new JComboBox<>();
    private final ModeloTabela modelo = new ModeloTabela();
    private final JTable tabela = new JTable(modelo);

    private final JTextField data = new JTextField(Formatos.data(LocalDate.now()), 9);
    private final JComboBox<String> tipo = new JComboBox<>(RegistoSaude.TIPOS);
    private final JTextField descricao = new JTextField(28);
    private final JTextField proxima = new JTextField(9);

    private boolean aAtualizarCombo;

    public PainelSaude(Repositorio repo, Runnable aoAlterar) {
        this.repo = repo;
        this.aoAlterar = aoAlterar;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topo.add(new JLabel("Animal:"));
        topo.add(animalCombo);
        animalCombo.addActionListener(e -> {
            if (!aAtualizarCombo) {
                recarregarRegistos();
            }
        });
        add(topo, BorderLayout.NORTH);

        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setRowHeight(24);
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        // ---- formulário de novo registo
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        form.setBorder(BorderFactory.createTitledBorder("Novo registo de saúde"));
        form.add(new JLabel("Data:"));
        form.add(data);
        form.add(new JLabel("Tipo:"));
        form.add(tipo);
        form.add(new JLabel("Descrição:"));
        form.add(descricao);
        form.add(new JLabel("Próxima data (opcional):"));
        form.add(proxima);
        JButton adicionar = new JButton("Adicionar");
        JButton eliminar = new JButton("Eliminar selecionado");
        adicionar.addActionListener(e -> adicionar());
        eliminar.addActionListener(e -> eliminar());
        form.add(adicionar);
        form.add(eliminar);
        add(form, BorderLayout.SOUTH);

        recarregarAnimais();
    }

    /** Atualiza a lista de animais (chamado quando se muda de separador). */
    public void recarregarAnimais() {
        Animal anterior = (Animal) animalCombo.getSelectedItem();
        aAtualizarCombo = true;
        List<Animal> lista = repo.listarAnimais();
        DefaultComboBoxModel<Animal> m = new DefaultComboBoxModel<>(lista.toArray(new Animal[0]));
        animalCombo.setModel(m);
        if (anterior != null) {
            for (Animal a : lista) {
                if (a.getId() == anterior.getId()) {
                    animalCombo.setSelectedItem(a);
                }
            }
        }
        aAtualizarCombo = false;
        recarregarRegistos();
    }

    private void recarregarRegistos() {
        Animal a = (Animal) animalCombo.getSelectedItem();
        modelo.linhas = (a == null) ? new ArrayList<RegistoSaude>() : repo.registosDoAnimal(a.getId());
        modelo.fireTableDataChanged();
    }

    private void adicionar() {
        Animal a = (Animal) animalCombo.getSelectedItem();
        if (a == null) {
            aviso("Registe primeiro um animal no separador \"Animais\".");
            return;
        }
        LocalDate d;
        LocalDate prox;
        try {
            d = Formatos.lerData(data.getText());
            prox = Formatos.lerData(proxima.getText());
        } catch (DateTimeParseException ex) {
            aviso("Data inválida. Use o formato dd/MM/aaaa, por exemplo 15/03/2026.");
            return;
        }
        if (d == null) {
            aviso("A data do registo é obrigatória.");
            return;
        }
        if (descricao.getText().trim().isEmpty()) {
            aviso("Escreva uma descrição (por exemplo, o nome da vacina ou do medicamento).");
            return;
        }
        if (prox != null && prox.isBefore(d)) {
            aviso("A próxima data não pode ser anterior à data do registo.");
            return;
        }
        RegistoSaude r = new RegistoSaude();
        r.setAnimalId(a.getId());
        r.setData(d);
        r.setTipo(String.valueOf(tipo.getSelectedItem()));
        r.setDescricao(descricao.getText().trim());
        r.setProximaData(prox);
        repo.adicionarRegisto(r);
        if (gravar()) {
            descricao.setText("");
            proxima.setText("");
            recarregarRegistos();
            aoAlterar.run();
        }
    }

    private void eliminar() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            aviso("Selecione primeiro um registo na tabela.");
            return;
        }
        int r = JOptionPane.showConfirmDialog(this, "Eliminar o registo selecionado?",
                "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r == JOptionPane.YES_OPTION) {
            repo.removerRegisto(modelo.linhas.get(linha).getId());
            if (gravar()) {
                recarregarRegistos();
                aoAlterar.run();
            }
        }
    }

    private boolean gravar() {
        try {
            repo.guardar();
            return true;
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Não foi possível guardar os dados:\n" + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void aviso(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Atenção", JOptionPane.WARNING_MESSAGE);
    }

    private static class ModeloTabela extends AbstractTableModel {

        private final String[] colunas = {"Data", "Tipo", "Descrição", "Próxima data", "Lembrete"};
        List<RegistoSaude> linhas = new ArrayList<>();

        @Override
        public int getRowCount() {
            return linhas.size();
        }

        @Override
        public int getColumnCount() {
            return colunas.length;
        }

        @Override
        public String getColumnName(int c) {
            return colunas[c];
        }

        @Override
        public Object getValueAt(int r, int c) {
            RegistoSaude x = linhas.get(r);
            switch (c) {
                case 0: return Formatos.data(x.getData());
                case 1: return x.getTipo();
                case 2: return x.getDescricao();
                case 3: return Formatos.data(x.getProximaData());
                default:
                    if (x.getProximaData() == null) {
                        return "—";
                    }
                    return x.isConcluido() ? "Feito" : "Pendente";
            }
        }
    }
}
