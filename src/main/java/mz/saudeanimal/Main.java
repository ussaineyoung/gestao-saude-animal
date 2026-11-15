package mz.saudeanimal;

import mz.saudeanimal.dados.Repositorio;
import mz.saudeanimal.ui.JanelaPrincipal;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.io.IOException;
import java.nio.file.Paths;

/** Ponto de entrada. Os dados ficam na pasta "dados" dentro da pasta do projeto. */
public class Main {

    public static void main(String[] args) {
        // Aspeto mais moderno (Nimbus), se estiver disponível
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignorado) {
            // fica com o aspeto por defeito
        }

        SwingUtilities.invokeLater(() -> {
            Repositorio repo = new Repositorio(Paths.get("dados"));
            try {
                repo.carregar();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Não foi possível ler os dados:\n" + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
            new JanelaPrincipal(repo).setVisible(true);
        });
    }
}
