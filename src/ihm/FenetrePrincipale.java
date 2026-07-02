package ihm;

import model.Salle;
import model.TypeSalle;
import service.FichierIndexe;
import service.GestionReservations;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FenetrePrincipale extends JFrame {
    private GestionReservations gestion;
    private ModeleGrille modeleGrille;
    private JTable tableGrille;
    private List<Salle> salles;
    private JProgressBar progressBar;
    private Timer rafraichissementTimer;

    public FenetrePrincipale() {
        this.gestion = new GestionReservations();
        this.salles = creerSallesDemo();
        this.modeleGrille = new ModeleGrille(salles, gestion);

        initUI();
        lancerChargementAsynchrone();
    }

    private void initUI() {
        setTitle("Systeme de Reservation - Co-working");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tableGrille = new JTable(modeleGrille);
        tableGrille.setRowHeight(40);
        tableGrille.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableGrille.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = tableGrille.rowAtPoint(evt.getPoint());
                int col = tableGrille.columnAtPoint(evt.getPoint());
                if (row >= 0 && col >= 1) {
                    ouvrirFormulaire(row, col);
                }
            }
        });

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    if ("Occupe".equals(value)) {
                        c.setBackground(new Color(255, 182, 193));
                    } else if ("Libre".equals(value)) {
                        c.setBackground(new Color(144, 238, 144));
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                }
                return c;
            }
        };
        tableGrille.getColumnModel().getColumn(0).setPreferredWidth(60);
        for (int i = 1; i < tableGrille.getColumnCount(); i++) {
            tableGrille.getColumnModel().getColumn(i).setPreferredWidth(150);
            tableGrille.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        JScrollPane scrollPane = new JScrollPane(tableGrille);
        add(scrollPane, BorderLayout.CENTER);

        JPanel panelBoutons = new JPanel(new BorderLayout());
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setString("Pret");
        panelBoutons.add(progressBar, BorderLayout.CENTER);

        JPanel panelDroite = new JPanel();
        JButton btnQuitter = new JButton("Quitter");
        btnQuitter.addActionListener(e -> sauvegarderEtQuitter());
        panelDroite.add(btnQuitter);
        panelBoutons.add(panelDroite, BorderLayout.EAST);

        add(panelBoutons, BorderLayout.SOUTH);

        rafraichissementTimer = new Timer(5000, e -> rafraichirGrille());
        rafraichissementTimer.start();
    }

    private void lancerChargementAsynchrone() {
        progressBar.setIndeterminate(true);
        progressBar.setString("Chargement des donnees...");
        tableGrille.setEnabled(false);

        SwingWorker<Set<model.Reservation>, Void> worker = new SwingWorker<>() {
            @Override
            protected Set<model.Reservation> doInBackground() throws Exception {
                return FichierIndexe.charger();
            }

            @Override
            protected void done() {
                try {
                    Set<model.Reservation> reservations = get();
                    for (model.Reservation r : reservations) {
                        gestion.getReservations().add(r);
                    }
                    modeleGrille.fireTableDataChanged();
                    progressBar.setIndeterminate(false);
                    progressBar.setString("Pret - " + reservations.size() + " reservation(s) chargee(s)");
                } catch (Exception ex) {
                    progressBar.setIndeterminate(false);
                    progressBar.setString("Erreur chargement");
                } finally {
                    tableGrille.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private void sauvegarderEtQuitter() {
        progressBar.setIndeterminate(true);
        progressBar.setString("Sauvegarde en cours...");
        tableGrille.setEnabled(false);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                FichierIndexe.sauvegarder(gestion.getReservations());
                return null;
            }

            @Override
            protected void done() {
                System.exit(0);
            }
        };
        worker.execute();
    }

    private void rafraichirGrille() {
        if (tableGrille.isEnabled()) {
            modeleGrille.mettreAJour();
        }
    }

    private void ouvrirFormulaire(int row, int col) {
        int heureDebut = 8 + row;
        int heureFin = heureDebut + 1;
        Salle salle = salles.get(col - 1);

        LocalDateTime maintenant = LocalDateTime.now();
        LocalDateTime dateReservation = LocalDateTime.of(
            maintenant.getYear(), maintenant.getMonth(), maintenant.getDayOfMonth(),
            heureDebut, 0);
        LocalDateTime dateFin = LocalDateTime.of(
            maintenant.getYear(), maintenant.getMonth(), maintenant.getDayOfMonth(),
            heureFin, 0);

        FormulaireReservation formulaire = new FormulaireReservation(
            this, salle, dateReservation, dateFin, gestion);
        formulaire.setVisible(true);

        modeleGrille.fireTableDataChanged();
    }

    private List<Salle> creerSallesDemo() {
        List<Salle> liste = new ArrayList<>();
        liste.add(new Salle("S1", "Salle A", 10, TypeSalle.REUNION));
        liste.add(new Salle("S2", "Salle B", 20, TypeSalle.FORMATION));
        liste.add(new Salle("S3", "Salle C", 5, TypeSalle.BUREAU_PARTAGE));
        return liste;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new FenetrePrincipale().setVisible(true);
        });
    }
}
