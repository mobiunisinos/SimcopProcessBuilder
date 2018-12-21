package br.unisinos.simcop.builder;

import br.unisinos.simcop.builder.gui.MainWindow;
import br.unisinos.simcop.builder.gui.MruList;
import br.unisinos.simcop.builder.repository.KnowClasses;

/**
 * Application to helps the building of a SimcopProcess ConfigurationFile
 * @author tiago
 */
public class Main {

    public static void main(String[] args) {        
        final KnowClasses knowClasses = new KnowClasses();
        knowClasses.refresh();
        final MruList mruList = new MruList(knowClasses);
        mruList.load();

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                MainWindow main = new MainWindow();
                main.setKnowClasses(knowClasses);
                main.setMruList(mruList);
                main.setVisible(true);
            }
        });
        
        
    }

}
