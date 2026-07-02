import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * IHM : calendrier visuel simplifie sous forme de grille horaire interactive.
 * Pilote directement GestionReservations / Reservation / Salle / CreneauHoraire
 * (classes du modele partage par l'equipe).
 */
public class MainFrame extends JFrame {

    private static final int HEURE_DEBUT_JOURNEE = 8;
    private static final int HEURE_FIN_JOURNEE = 18;

    private final GestionReservations gestion = new GestionReservations();
    private final Map<String, Salle> salles = new LinkedHashMap<>();

    private LocalDate dateAffichee = LocalDate.now();
    private JPanel grillePanel;
    private JLabel dateLabel;
    private JSpinner selecteurDate;

    public MainFrame() {
        super("Reservation et Allocation de Salles de Co-working");
        initSalles();
        chargerDonnees();
        construireInterface();
        gererFermeture();

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1060, 660);
        setLocationRelativeTo(null);
    }

    /** Quelques salles de demonstration (a remplacer/completer par la vraie source si besoin). */
    private void initSalles() {
        salles.put("S1", new Salle("S1", "Salle Everest", 4, TypeSalle.REUNION));
        salles.put("S2", new Salle("S2", "Salle Kilimandjaro", 6, TypeSalle.FORMATION));
        salles.put("S3", new Salle("S3", "Salle Andes", 2, TypeSalle.BUREAU_PARTAGE));
        salles.put("S4", new Salle("S4", "Salle Alpes", 8, TypeSalle.CONFERENCE));
    }

    private void chargerDonnees() {
        Set<Reservation> donnees = FichierIndexe.charger();
        for (Reservation r : donnees) {
            gestion.ajouterReservation(r);
        }
    }

    /** Sauvegarde automatique (via FichierIndexe) a la fermeture de l'application. */
    private void gererFermeture() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                FichierIndexe.sauvegarder(gestion.getReservations());
                dispose();
                System.exit(0);
            }
        });
    }

    private void construireInterface() {
        getContentPane().setBackground(StylesUI.FOND);
        setLayout(new BorderLayout(0, 0));

        add(construireBarreHaut(), BorderLayout.NORTH);

        grillePanel = new JPanel();
        grillePanel.setBackground(StylesUI.FOND_PANNEAU);
        JScrollPane scroll = new JScrollPane(grillePanel);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        scroll.getViewport().setBackground(StylesUI.FOND_PANNEAU);
        add(scroll, BorderLayout.CENTER);

        JLabel legende = new JLabel(
                "Clic sur une case verte (libre) = nouvelle reservation   |   Clic sur une case orange (occupee) = details / suppression",
                SwingConstants.CENTER);
        legende.setFont(StylesUI.POLICE_TEXTE);
        legende.setForeground(StylesUI.TEXTE_SECONDAIRE);
        legende.setBorder(BorderFactory.createEmptyBorder(10, 6, 14, 6));
        legende.setOpaque(true);
        legende.setBackground(StylesUI.FOND);
        add(legende, BorderLayout.SOUTH);

        rafraichirGrille();
    }

    private JPanel construireBarreHaut() {
        JPanel barre = new JPanel(new BorderLayout());
        barre.setBackground(StylesUI.PRIMAIRE);
        barre.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel titreApp = new JLabel("Salles de Co-working");
        titreApp.setFont(StylesUI.POLICE_TITRE);
        titreApp.setForeground(Color.WHITE);
        barre.add(titreApp, BorderLayout.WEST);

        JPanel navigation = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        navigation.setOpaque(false);

        JButton precedent = new JButton("<< Jour precedent");
        JButton suivant = new JButton("Jour suivant >>");
        StylesUI.styliserBoutonSecondaire(precedent);
        StylesUI.styliserBoutonSecondaire(suivant);

        dateLabel = new JLabel();
        dateLabel.setFont(StylesUI.POLICE_TITRE);
        dateLabel.setForeground(Color.WHITE);
        dateLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        precedent.addActionListener(e -> changerDate(dateAffichee.minusDays(1)));
        suivant.addActionListener(e -> changerDate(dateAffichee.plusDays(1)));

        SpinnerDateModel modeleDate = new SpinnerDateModel();
        selecteurDate = new JSpinner(modeleDate);
        selecteurDate.setEditor(new JSpinner.DateEditor(selecteurDate, "yyyy-MM-dd"));
        selecteurDate.setPreferredSize(new Dimension(110, 26));
        selecteurDate.setValue(Date.from(dateAffichee.atStartOfDay(ZoneId.systemDefault()).toInstant()));

        JButton aller = new JButton("Aller a cette date");
        StylesUI.styliserBoutonSecondaire(aller);
        aller.addActionListener(e -> {
            Date choisie = (Date) selecteurDate.getValue();
            LocalDate nouvelleDate = choisie.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            changerDate(nouvelleDate);
        });

        navigation.add(precedent);
        navigation.add(dateLabel);
        navigation.add(suivant);
        navigation.add(Box.createHorizontalStrut(16));
        navigation.add(selecteurDate);
        navigation.add(aller);
        barre.add(navigation, BorderLayout.CENTER);

        JButton historique = new JButton("Historique");
        StylesUI.styliserBoutonSecondaire(historique);
        historique.addActionListener(e -> ouvrirHistorique());
        barre.add(historique, BorderLayout.EAST);

        return barre;
    }

    private void changerDate(LocalDate nouvelleDate) {
        dateAffichee = nouvelleDate;
        selecteurDate.setValue(Date.from(dateAffichee.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        rafraichirGrille();
    }

    private void ouvrirHistorique() {
        HistoriqueDialog dialog = new HistoriqueDialog(this, gestion, this::rafraichirGrille);
        dialog.setVisible(true);
    }

    private void rafraichirGrille() {
        dateLabel.setText(dateAffichee.toString());
        grillePanel.removeAll();
        grillePanel.setBackground(StylesUI.FOND_PANNEAU);
        grillePanel.setBorder(BorderFactory.createEmptyBorder(16, 0, 16, 0));

        int nbHeures = HEURE_FIN_JOURNEE - HEURE_DEBUT_JOURNEE;
        int nbColonnes = salles.size() + 1;
        grillePanel.setLayout(new GridLayout(nbHeures + 1, nbColonnes, 6, 6));

        grillePanel.add(creerEnteteColonne("Heure"));
        for (Salle s : salles.values()) {
            grillePanel.add(creerEnteteColonne(s.getNom()));
        }

        Set<Reservation> reservationsDuJour = gestion.getReservations();

        for (int h = HEURE_DEBUT_JOURNEE; h < HEURE_FIN_JOURNEE; h++) {
            LocalTime debutCreneau = LocalTime.of(h, 0);
            LocalTime finCreneau = LocalTime.of(h + 1, 0);

            JLabel heureLabel = new JLabel(debutCreneau + " - " + finCreneau, SwingConstants.CENTER);
            heureLabel.setFont(StylesUI.POLICE_TEXTE);
            heureLabel.setForeground(StylesUI.TEXTE_SECONDAIRE);
            grillePanel.add(heureLabel);

            for (Salle salle : salles.values()) {
                Reservation existante = trouverReservation(reservationsDuJour, salle, debutCreneau, finCreneau);
                grillePanel.add(creerBoutonCreneau(salle, debutCreneau, finCreneau, existante));
            }
        }

        grillePanel.revalidate();
        grillePanel.repaint();
    }

    private JLabel creerEnteteColonne(String texte) {
        JLabel entete = new JLabel(texte, SwingConstants.CENTER);
        entete.setFont(StylesUI.POLICE_ENTETE);
        entete.setForeground(StylesUI.TEXTE_PRINCIPAL);
        return entete;
    }

    /** Comparaison par ID de salle (et non par equals()) car Salle ne redefinit pas equals(). */
    private Reservation trouverReservation(Set<Reservation> reservations, Salle salle, LocalTime debut, LocalTime fin) {
        for (Reservation r : reservations) {
            LocalDateTime rDebut = r.getCreneau().getDateDebut();
            LocalDateTime rFin = r.getCreneau().getDateFin();
            boolean memeSalle = r.getSalle().getId().equals(salle.getId());
            boolean memeJour = rDebut.toLocalDate().equals(dateAffichee);
            boolean chevauche = rDebut.toLocalTime().isBefore(fin) && debut.isBefore(rFin.toLocalTime());
            if (memeSalle && memeJour && chevauche) {
                return r;
            }
        }
        return null;
    }

    private JButton creerBoutonCreneau(Salle salle, LocalTime debut, LocalTime fin, Reservation existante) {
        JButton bouton = new JButton();
        bouton.setOpaque(true);
        bouton.setFocusPainted(false);
        bouton.setFont(StylesUI.POLICE_TEXTE);
        bouton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        if (existante != null) {
            bouton.setText(existante.getNomClient());
            bouton.setBackground(StylesUI.OCCUPE_FOND);
            bouton.setForeground(StylesUI.OCCUPE_TEXTE);
            bouton.setBorder(BorderFactory.createLineBorder(StylesUI.OCCUPE_BORD));
            bouton.addActionListener(e -> afficherDetails(existante));
        } else {
            bouton.setText("Libre");
            bouton.setBackground(StylesUI.LIBRE_FOND);
            bouton.setForeground(StylesUI.LIBRE_TEXTE);
            bouton.setBorder(BorderFactory.createLineBorder(StylesUI.LIBRE_BORD));
            bouton.addActionListener(e -> ouvrirFormulaire(salle, debut, fin));
        }
        return bouton;
    }

    private void ouvrirFormulaire(Salle salle, LocalTime debut, LocalTime fin) {
        FormulaireReservation formulaire = new FormulaireReservation(this, salle, dateAffichee, debut, fin);
        formulaire.setVisible(true);
        String client = formulaire.getNomClient();
        if (client == null) {
            return; // annule par l'utilisateur
        }

        String id = UUID.randomUUID().toString().substring(0, 8);
        try {
            CreneauHoraire creneau = new CreneauHoraire(
                    LocalDateTime.of(dateAffichee, debut),
                    LocalDateTime.of(dateAffichee, fin));
            Reservation reservation = new Reservation(id, salle, client, creneau);
            boolean ajoute = gestion.ajouterReservation(reservation);
            if (!ajoute) {
                JOptionPane.showMessageDialog(this,
                        "Creneau indisponible : la salle " + salle.getNom()
                                + " est deja reservee sur une plage qui chevauche " + debut + " - " + fin + ".",
                        "Reservation refusee", JOptionPane.WARNING_MESSAGE);
                return;
            }
            rafraichirGrille();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Reservation refusee", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void afficherDetails(Reservation reservation) {
        int choix = JOptionPane.showConfirmDialog(this,
                reservation + "\n\nSupprimer cette reservation ?",
                "Details de la reservation",
                JOptionPane.YES_NO_OPTION);
        if (choix == JOptionPane.YES_OPTION) {
            gestion.supprimerReservation(reservation.getId());
            rafraichirGrille();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
