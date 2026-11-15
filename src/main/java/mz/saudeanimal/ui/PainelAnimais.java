package mz.saudeanimal.ui;

import mz.saudeanimal.dados.Formatos;
import mz.saudeanimal.dados.Repositorio;
import mz.saudeanimal.modelo.Animal;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/** Separador "Animais": lista com pesquisa e formulário para criar, editar e eliminar. */
public class PainelAnimais extends JPanel {

    private final Repositorio repo;
    private final Runnable aoAlterar;

    private final ModeloTabela modelo = new ModeloTabela();
    private final JTable tabela = new JTable(modelo);
    private final JTextField pesquisa = new JTextField(20);

    private final JTextField nome = new JTextField();
    private final JComboBox<String> especie = new JComboBox<>(new String[]{
        "Bovino", "Caprino", "Ovino", "Suíno", "Galináceo", "Canino", "Felino", "Outro"});
    private final JTextField raca = new JTextField();
    private final JComboBox<String> sexo = new JComboBox<>(new String[]{"Macho", "Fêmea"});
    private final JTextField nascimento = new JTextField();
    private final JTextField peso = new JTextField();
    private final JTextField proprietario = new JTextField();
    private final JTextField localizacao = new JTextField();

    private final JLabel titulo = new JLabel("Novo animal");
    private Animal selecionado; // null = a criar um novo

    public PainelAnimais(Repositorio repo, Runnable aoAlterar) {
        this.repo = repo;
        this.aoAlterar = aoAlterar;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        especie.setEditable(true);

        // ---- lado esquerdo: pesquisa + tabela
        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topo.add(new JLabel("Pesquisar:"));
        topo.add(pesquisa);
        pesquisa.setToolTipText("Procura por nome, espécie, raça, proprietário ou localização");
        pesquisa.getDocument().addDocumentListener(new SimplesDocumentListener(this::recarregar));

        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setAutoCreateRowSorter(true);
        tabela.setRowHeight(24);
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int linha = tabela.getSelectedRow();
                if (linha >= 0) {
                    preencher(modelo.linhas.get(tabela.convertRowIndexToModel(linha)));
                }
            }
        });

        JPanel esquerda = new JPanel(new BorderLayout(0, 6));
        esquerda.add(topo, BorderLayout.NORTH);
        esquerda.add(new JScrollPane(tabela), BorderLayout.CENTER);

        // ---- lado direito: formulário
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        titulo.setFont(titulo.getFont().deriveFont(java.awt.Font.BOLD, 15f));
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = 0;
        g.gridwidth = 2;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(0, 0, 10, 0);
        form.add(titulo, g);

        int linha = 1;
        linha = adicionarLinha(form, linha, "Nome *", nome);
        linha = adicionarLinha(form, linha, "Espécie", especie);
        linha = adicionarLinha(form, linha, "Raça", raca);
        linha = adicionarLinha(form, linha, "Sexo", sexo);
        linha = adicionarLinha(form, linha, "Nascimento (dd/MM/aaaa)", nascimento);
        linha = adicionarLinha(form, linha, "Peso (kg)", peso);
        linha = adicionarLinha(form, linha, "Proprietário", proprietario);
        linha = adicionarLinha(form, linha, "Localização", localizacao);

        JButton novo = new JButton("Novo");
        JButton guardar = new JButton("Guardar");
        JButton eliminar = new JButton("Eliminar");
        novo.addActionListener(e -> limparFormulario());
        guardar.addActionListener(e -> guardar());
        eliminar.addActionListener(e -> eliminar());
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        botoes.add(novo);
        botoes.add(guardar);
        botoes.add(eliminar);
        g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = linha;
        g.gridwidth = 2;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(14, 0, 0, 0);
        form.add(botoes, g);

        // empurra tudo para cima
        g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = linha + 1;
        g.weighty = 1;
        form.add(new JPanel(), g);

        JSplitPane divisao = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, esquerda, form);
        divisao.setResizeWeight(0.65);
        divisao.setBorder(null);
        add(divisao, BorderLayout.CENTER);

        recarregar();
    }

    private static int adicionarLinha(JPanel form, int linha, String rotulo, JComponent campo) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = linha;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(3, 0, 3, 8);
        form.add(new JLabel(rotulo), g);

        g = new GridBagConstraints();
        g.gridx = 1;
        g.gridy = linha;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(3, 0, 3, 0);
        form.add(campo, g);
        return linha + 1;
    }

    // ------------------------------------------------------------ ações

    public void recarregar() {
        String f = pesquisa.getText().trim().toLowerCase();
        List<Animal> filtrados = new ArrayList<>();
        for (Animal a : repo.listarAnimais()) {
            String texto = (a.getNome() + " " + a.getEspecie() + " " + a.getRaca() + " "
                    + a.getProprietario() + " " + a.getLocalizacao()).toLowerCase();
            if (f.isEmpty() || texto.contains(f)) {
                filtrados.add(a);
            }
        }
        modelo.linhas = filtrados;
        modelo.fireTableDataChanged();
    }

    private void preencher(Animal a) {
        selecionado = a;
        titulo.setText("Editar animal #" + a.getId());
        nome.setText(a.getNome());
        especie.setSelectedItem(a.getEspecie());
        raca.setText(a.getRaca());
        sexo.setSelectedItem(a.getSexo());
        nascimento.setText(Formatos.data(a.getDataNascimento()));
        peso.setText(a.getPesoKg() == 0 ? "" : String.valueOf(a.getPesoKg()));
        proprietario.setText(a.getProprietario());
        localizacao.setText(a.getLocalizacao());
    }

    private void limparFormulario() {
        selecionado = null;
        tabela.clearSelection();
        titulo.setText("Novo animal");
        nome.setText("");
        especie.setSelectedIndex(0);
        raca.setText("");
        sexo.setSelectedIndex(0);
        nascimento.setText("");
        peso.setText("");
        proprietario.setText("");
        localizacao.setText("");
        nome.requestFocusInWindow();
    }

    private void guardar() {
        if (nome.getText().trim().isEmpty()) {
            aviso("O nome é obrigatório.");
            return;
        }
        LocalDate nasc;
        try {
            nasc = Formatos.lerData(nascimento.getText());
        } catch (DateTimeParseException ex) {
            aviso("Data de nascimento inválida. Use o formato dd/MM/aaaa, por exemplo 15/03/2023.");
            return;
        }
        if (nasc != null && nasc.isAfter(LocalDate.now())) {
            aviso("A data de nascimento não pode estar no futuro.");
            return;
        }
        double pesoKg = 0;
        String pt = peso.getText().trim().replace(',', '.');
        if (!pt.isEmpty()) {
            try {
                pesoKg = Double.parseDouble(pt);
            } catch (NumberFormatException ex) {
                aviso("Peso inválido. Escreva só números, por exemplo 380 ou 12,5.");
                return;
            }
            if (pesoKg < 0) {
                aviso("O peso não pode ser negativo.");
                return;
            }
        }

        boolean novo = (selecionado == null);
        Animal a = novo ? new Animal() : selecionado;
        a.setNome(nome.getText().trim());
        a.setEspecie(String.valueOf(especie.getSelectedItem()).trim());
        a.setRaca(raca.getText().trim());
        a.setSexo(String.valueOf(sexo.getSelectedItem()));
        a.setDataNascimento(nasc);
        a.setPesoKg(pesoKg);
        a.setProprietario(proprietario.getText().trim());
        a.setLocalizacao(localizacao.getText().trim());
        if (novo) {
            repo.adicionarAnimal(a);
        }
        if (!gravar()) {
            return;
        }
        recarregar();
        selecionarNaTabela(a);
        aoAlterar.run();
    }

    private void eliminar() {
        if (selecionado == null) {
            aviso("Selecione primeiro um animal na tabela.");
            return;
        }
        int r = JOptionPane.showConfirmDialog(this,
                "Eliminar \"" + selecionado.getNome() + "\" e todos os seus registos de saúde?",
                "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r != JOptionPane.YES_OPTION) {
            return;
        }
        repo.removerAnimal(selecionado.getId());
        if (gravar()) {
            limparFormulario();
            recarregar();
            aoAlterar.run();
        }
    }

    private void selecionarNaTabela(Animal a) {
        int i = modelo.linhas.indexOf(a);
        if (i >= 0) {
            int v = tabela.convertRowIndexToView(i);
            tabela.setRowSelectionInterval(v, v);
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

    // ------------------------------------------------------------ modelo da tabela

    private static class ModeloTabela extends AbstractTableModel {

        private final String[] colunas = {"ID", "Nome", "Espécie", "Raça", "Sexo", "Idade",
            "Peso (kg)", "Proprietário", "Localização"};
        List<Animal> linhas = new ArrayList<>();

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
        public Class<?> getColumnClass(int c) {
            return c == 0 ? Integer.class : (c == 6 ? Double.class : String.class);
        }

        @Override
        public Object getValueAt(int r, int c) {
            Animal a = linhas.get(r);
            switch (c) {
                case 0: return a.getId();
                case 1: return a.getNome();
                case 2: return a.getEspecie();
                case 3: return a.getRaca();
                case 4: return a.getSexo();
                case 5: return a.getIdadeTexto();
                case 6: return a.getPesoKg();
                case 7: return a.getProprietario();
                default: return a.getLocalizacao();
            }
        }
    }
}
