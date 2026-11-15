package mz.saudeanimal.ui;

import mz.saudeanimal.dados.Repositorio;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

/** Janela principal com três separadores: Animais, Saúde e Alertas. */
public class JanelaPrincipal extends JFrame {

    private final Repositorio repo;
    private final JLabel estado = new JLabel();

    private final PainelAnimais painelAnimais;
    private final PainelSaude painelSaude;
    private final PainelAlertas painelAlertas;

    public JanelaPrincipal(Repositorio repo) {
        super("Gestão de Saúde Animal");
        this.repo = repo;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 520));
        setSize(1100, 640);
        setLocationRelativeTo(null);

        Runnable aoAlterar = this::atualizarEstado;
        painelAnimais = new PainelAnimais(repo, aoAlterar);
        painelSaude = new PainelSaude(repo, aoAlterar);
        painelAlertas = new PainelAlertas(repo, aoAlterar);

        JTabbedPane separadores = new JTabbedPane();
        separadores.addTab("Animais", painelAnimais);
        separadores.addTab("Saúde", painelSaude);
        separadores.addTab("Alertas", painelAlertas);
        // Ao mudar de separador, atualiza o conteúdo com os dados mais recentes
        separadores.addChangeListener(e -> {
            painelAnimais.recarregar();
            painelSaude.recarregarAnimais();
            painelAlertas.recarregar();
            atualizarEstado();
        });

        JLabel cabecalho = new JLabel("  Gestão de Saúde Animal");
        cabecalho.setFont(cabecalho.getFont().deriveFont(Font.BOLD, 18f));
        cabecalho.setBorder(BorderFactory.createEmptyBorder(10, 8, 6, 8));

        estado.setBorder(BorderFactory.createEmptyBorder(4, 12, 6, 12));
        JPanel rodape = new JPanel(new BorderLayout());
        rodape.add(estado, BorderLayout.WEST);

        add(cabecalho, BorderLayout.NORTH);
        add(separadores, BorderLayout.CENTER);
        add(rodape, BorderLayout.SOUTH);

        atualizarEstado();
    }

    private void atualizarEstado() {
        int total = repo.listarAnimais().size();
        int atrasados = repo.contarAtrasados();
        estado.setText(total + (total == 1 ? " animal registado" : " animais registados")
                + "   |   " + atrasados + (atrasados == 1 ? " alerta atrasado" : " alertas atrasados"));
        estado.setForeground(atrasados > 0 ? new Color(170, 30, 30) : Color.DARK_GRAY);
    }
}
