import javax.swing.*;
import java.awt.*;

/**
 * Palette de couleurs et polices partagees par toute l'IHM.
 */
final class StylesUI {

    private StylesUI() {
    }

    static final Color FOND = new Color(244, 246, 250);
    static final Color FOND_PANNEAU = Color.WHITE;
    static final Color PRIMAIRE = new Color(47, 92, 214);
    static final Color TEXTE_PRINCIPAL = new Color(33, 37, 45);
    static final Color TEXTE_SECONDAIRE = new Color(105, 112, 128);

    static final Color LIBRE_FOND = new Color(214, 245, 219);
    static final Color LIBRE_TEXTE = new Color(28, 110, 45);
    static final Color LIBRE_BORD = new Color(170, 222, 180);

    static final Color OCCUPE_FOND = new Color(255, 223, 214);
    static final Color OCCUPE_TEXTE = new Color(163, 40, 20);
    static final Color OCCUPE_BORD = new Color(240, 175, 160);

    static final Font POLICE_TITRE = new Font("Segoe UI", Font.BOLD, 16);
    static final Font POLICE_ENTETE = new Font("Segoe UI", Font.BOLD, 13);
    static final Font POLICE_TEXTE = new Font("Segoe UI", Font.PLAIN, 13);
    static final Font POLICE_BOUTON = new Font("Segoe UI", Font.BOLD, 12);

    static void styliserBoutonPrimaire(AbstractButton bouton) {
        bouton.setFont(POLICE_BOUTON);
        bouton.setForeground(Color.WHITE);
        bouton.setBackground(PRIMAIRE);
        bouton.setOpaque(true);
        bouton.setFocusPainted(false);
        bouton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        bouton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    static void styliserBoutonSecondaire(AbstractButton bouton) {
        bouton.setFont(POLICE_BOUTON);
        bouton.setForeground(PRIMAIRE);
        bouton.setBackground(Color.WHITE);
        bouton.setOpaque(true);
        bouton.setFocusPainted(false);
        bouton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMAIRE, 1),
                BorderFactory.createEmptyBorder(7, 15, 7, 15)));
        bouton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}
