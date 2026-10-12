package com.ies21.gestionproductos.infrastructure.navigation;

import java.awt.CardLayout;
import javax.swing.*;

public final class ScreenRouter extends JPanel {
    private final CardLayout cards = new CardLayout();
    private final java.util.Map<String, JPanel> screens = new java.util.HashMap<>();

    public ScreenRouter() { setLayout(cards); }
    public void register(String route, JPanel screen) {
        JPanel previous = screens.put(route, screen);
        if (previous != null) remove(previous);
        add(screen, route);
        revalidate();
    }
    public void navigate(String route) { cards.show(this, route); }
}
