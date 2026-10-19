/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.ies21.gestionproductos;
import com.ies21.gestionproductos.infrastructure.navigation.MainWindow;
import com.ies21.gestionproductos.infrastructure.config.AppConfig;
import com.ies21.gestionproductos.domain.service.ProductService;
import com.ies21.gestionproductos.infrastructure.components.Ui;

/**
 *
 * @author Nahuel Gigena
 */
public class GestionProductos {

    public static void main(String[] args) {
        // El hilo principal conecta antes de crear Swing; no se bloquea el EDT.
        try {
            ProductService service = AppConfig.createProductService();
            javax.swing.SwingUtilities.invokeLater(() -> {
                try { new MainWindow(service).setVisible(true); }
                catch (RuntimeException ex) { AppConfig.close(); Ui.runSafely(null, () -> { throw ex; }); }
            });
        } catch (RuntimeException ex) {
            javax.swing.SwingUtilities.invokeLater(() -> Ui.runSafely(null, () -> { throw ex; }));
        }
    }
}
