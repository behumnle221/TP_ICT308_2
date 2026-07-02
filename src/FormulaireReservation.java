import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.regex.Pattern;

/**
 * Boite de dialogue de saisie d'une nouvelle reservation, adaptee au modele
 * de l'equipe (Salle avec TypeSalle, CreneauHoraire en LocalDateTime).
 */
public class FormulaireReservation extends JDialog {

    private static final Pattern NOM_VALIDE =
            Pattern.compile("^[\\p{L}0-9][\\p{L}0-9 '\\-]{1,39}$");

    private final JTextField champClient = new JTextField(20);
    private final JLabel messageErreur = new JLabel(" ");
    private String nomClient;

    public FormulaireReservation(Frame parent, Salle salle, LocalDate date, LocalTime debut, LocalTime fin) {
        super(parent, "Nouvelle reservation", true);
        getContentPane().setBackground(StylesUI.FOND);

        JPanel contenu = new JPanel();
        contenu.setLayout(new BoxLayout(contenu, BoxLayout.Y_AXIS));
        contenu.setBackground(StylesUI.FOND);
        contenu.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        JLabel titre = new JLabel("Nouvelle reservation");
        titre.setFont(StylesUI.POLICE_TITRE);
        titre.setForeground(StylesUI.TEXTE_PRINCIPAL);
        titre.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel infoSalle = creerLabelInfo("Salle : " + salle.getNom()
                + " (" + salle.getType() + ", capacite " + salle.getCapacite() + ")");
        JLabel infoDate = creerLabelInfo("Date : " + date);
        JLabel infoCreneau = creerLabelInfo("Creneau : " + debut + " - " + fin);

        JLabel labelClient = new JLabel("Nom du client / de l'equipe :");
        labelClient.setFont(StylesUI.POLICE_ENTETE);
        labelClient.setForeground(StylesUI.TEXTE_PRINCIPAL);
        labelClient.setAlignmentX(Component.LEFT_ALIGNMENT);
        labelClient.setBorder(BorderFactory.createEmptyBorder(12, 0, 4, 0));

        champClient.setFont(StylesUI.POLICE_TEXTE);
        champClient.setAlignmentX(Component.LEFT_ALIGNMENT);
        champClient.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        messageErreur.setForeground(new Color(190, 40, 30));
        messageErreur.setFont(StylesUI.POLICE_TEXTE.deriveFont(11f));
        messageErreur.setAlignmentX(Component.LEFT_ALIGNMENT);

        contenu.add(titre);
        contenu.add(Box.createVerticalStrut(10));
        contenu.add(infoSalle);
        contenu.add(infoDate);
        contenu.add(infoCreneau);
        contenu.add(labelClient);
        contenu.add(champClient);
        contenu.add(messageErreur);

        JPanel boutons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        boutons.setBackground(StylesUI.FOND);
        JButton valider = new JButton("Reserver");
        JButton annuler = new JButton("Annuler");
        StylesUI.styliserBoutonPrimaire(valider);
        StylesUI.styliserBoutonSecondaire(annuler);

        valider.addActionListener(e -> validerSaisie());
        annuler.addActionListener(e -> {
            nomClient = null;
            dispose();
        });

        getRootPane().setDefaultButton(valider);
        boutons.add(annuler);
        boutons.add(valider);

        setLayout(new BorderLayout());
        add(contenu, BorderLayout.CENTER);
        add(boutons, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
    }

    private JLabel creerLabelInfo(String texte) {
        JLabel label = new JLabel(texte);
        label.setFont(StylesUI.POLICE_TEXTE);
        label.setForeground(StylesUI.TEXTE_SECONDAIRE);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void validerSaisie() {
        String saisie = champClient.getText().trim();
        if (saisie.isEmpty()) {
            messageErreur.setText("Merci de saisir un nom.");
            return;
        }
        if (!NOM_VALIDE.matcher(saisie).matches()) {
            messageErreur.setText("Nom invalide : 2 a 40 caracteres, lettres/chiffres/espaces/tirets uniquement.");
            return;
        }
        nomClient = saisie;
        dispose();
    }

    /** Renvoie le nom saisi, ou null si l'utilisateur a annule. */
    public String getNomClient() {
        return nomClient;
    }
}
