package com.ies21.gestionproductos.infrastructure.navigation;

import java.awt.CardLayout;
import javax.swing.*;

/** Navegación entre paneles mediante CardLayout, sin crear ventanas principales nuevas. */
public final class ScreenRouter extends JPanel {
    private final CardLayout cards = new CardLayout();
    private final java.util.Map<String, JPanel> screens = new java.util.HashMap<>();

    public ScreenRouter() { setLayout(cards); }
    /**
     * Registra una vista y retira la anterior si la ruta ya estaba ocupada.
     * @param route nombre interno de la pantalla
     * @param screen panel que se mostrará en esa ruta
     */
    public void register(String route, JPanel screen) {
        JPanel previous = screens.put(route, screen);
        if (previous != null) remove(previous);
        add(screen, route);
        revalidate();
    }
    /**
     * @param route nombre de una vista registrada que se desea mostrar
     */
    public void navigate(String route) { cards.show(this, route); }
}
