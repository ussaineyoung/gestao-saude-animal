package mz.saudeanimal.ui;

import mz.saudeanimal.dados.Formatos;
import mz.saudeanimal.dados.Repositorio;
import mz.saudeanimal.modelo.Animal;
import mz.saudeanimal.modelo.RegistoSaude;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/** Separador "Alertas": vacinas e tratamentos atrasados ou a vencer nos próximos dias. */
public class PainelAlertas extends JPanel {

    private final Repositorio repo;
    private final Runnable aoAlterar;

    private final SpinnerNumberModel dias = new SpinnerNumberModel(30, 1, 365, 1);
    private final ModeloTabela modelo = new ModeloTabela();
    private final JTable tabela = new JTable(modelo);

    public PainelAlertas(Repositorio repo, Runnable aoAlterar) {
        this.repo = repo;
        this.aoAlterar = aoAlterar;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topo.add(new JLabel("Mostrar o que vence nos próximos"));
        JSpinner spinner = new JSpinner(dias);
        topo.add(spinner);
        topo.add(new JLabel("dias (os atrasados aparecem sempre)"));
        spinner.addChangeListener(e -> recarregar());
        add(topo, BorderLayout.NORTH);

        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setRowHeight(24);
        tabela.setDefaultRenderer(Object.class, new RenderizadorEstado());
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        JButton feito = new JButton("Marcar como feito");
        feito.addActionListener(e -> marcarFeito());
        JPanel baixo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        baixo.add(feito);
        add(baixo, BorderLayout.SOUTH);

        recarregar();
    }

    public void recarregar() {
        modelo.linhas = repo.alertas((Integer) dias.getValue());
        modelo.repo = repo;
        modelo.fireTableDataChanged();
    }

    private void marcarFeito() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            JOptionPane.showMessageDialog(this, "Selecione primeiro um alerta na tabela.",
                    "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }
        modelo.linhas.get(linha).setConcluido(true);
        try {
            repo.guardar();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Não foi possível guardar os dados:\n" + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        recarregar();
        aoAlterar.run();
    }

    private static String estado(RegistoSaude r) {
        long d = ChronoUnit.DAYS.between(LocalDate.now(), r.getProximaData());
        if (d < 0) {
            return "Atrasado há " + (-d) + (d == -1 ? " dia" : " dias");
        }
        if (d == 0) {
            return "Hoje";
        }
        return "Daqui a " + d + (d == 1 ? " dia" : " dias");
    }

    private static class ModeloTabela extends AbstractTableModel {

        private final String[] colunas = {"Animal", "Tipo", "Descrição", "Data prevista", "Estado"};
        List<RegistoSaude> linhas = new ArrayList<>();
        Repositorio repo;

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
                case 0:
                    Animal a = repo.procurarAnimal(x.getAnimalId());
                    return a == null ? "?" : a.getNome();
                case 1: return x.getTipo();
                case 2: return x.getDescricao();
                case 3: return Formatos.data(x.getProximaData());
                default: return estado(x);
            }
        }
    }

    /** Pinta de vermelho as linhas atrasadas e de amarelo as que vencem hoje. */
    private class RenderizadorEstado extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                       boolean foco, int linha, int coluna) {
            Component c = super.getTableCellRendererComponent(t, v, sel, foco, linha, coluna);
            if (!sel) {
                String est = String.valueOf(modelo.getValueAt(t.convertRowIndexToModel(linha), 4));
                if (est.startsWith("Atrasado")) {
                    c.setBackground(new Color(255, 218, 218));
                } else if (est.equals("Hoje")) {
                    c.setBackground(new Color(255, 243, 200));
                } else {
                    c.setBackground(Color.WHITE);
                }
                c.setForeground(Color.BLACK);
            }
            return c;
        }
    }
}
