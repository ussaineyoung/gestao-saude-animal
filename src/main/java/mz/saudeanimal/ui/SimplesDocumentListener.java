package mz.saudeanimal.ui;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** DocumentListener que executa a mesma ação em qualquer alteração de texto. */
class SimplesDocumentListener implements DocumentListener {

    private final Runnable acao;

    SimplesDocumentListener(Runnable acao) {
        this.acao = acao;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        acao.run();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        acao.run();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        acao.run();
    }
}
