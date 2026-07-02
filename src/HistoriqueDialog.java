import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Fenetre listant toutes les reservations (GestionReservations.getReservations()),
 * triees par date/heure. Permet la suppression via GestionReservations.supprimerReservation(id).
 */
public class HistoriqueDialog extends JDialog {

    private final GestionReservations gestion;
    private final Runnable apresModification;
    private final DefaultTableModel modele;
    private final JTable table;
    private List<Reservation> reservationsAffichees;

    public HistoriqueDialog(Frame parent, GestionReservations gestion, Runnable apresModification) {
        super(parent, "Historique des reservations", true);
        this.gestion = gestion;
        this.apresModification = apresModification;

        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(StylesUI.FOND);
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JLabel titre = new JLabel("Toutes les reservations");
        titre.setFont(StylesUI.POLICE_TITRE);
        titre.setForeground(StylesUI.TEXTE_PRINCIPAL);
        add(titre, BorderLayout.NORTH);

        modele = new DefaultTableModel(new Object[]{"Date", "Salle", "Creneau", "Client"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(modele);
        table.setFont(StylesUI.POLICE_TEXTE);
        table.setRowHeight(26);
        table.getTableHeader().setFont(StylesUI.POLICE_ENTETE);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel boutons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        boutons.setBackground(StylesUI.FOND);
        JButton supprimer = new JButton("Supprimer la selection");
        JButton fermer = new JButton("Fermer");
        StylesUI.styliserBoutonSecondaire(supprimer);
        StylesUI.styliserBoutonPrimaire(fermer);

        supprimer.addActionListener(e -> supprimerSelection());
        fermer.addActionListener(e -> dispose());

        boutons.add(supprimer);
        boutons.add(fermer);
        add(boutons, BorderLayout.SOUTH);

        rafraichirTable();

        setSize(650, 420);
        setLocationRelativeTo(parent);
    }

    private void rafraichirTable() {
        Set<Reservation> toutes = gestion.getReservations();
        reservationsAffichees = toutes.stream()
                .sorted(Comparator.comparing((Reservation r) -> r.getCreneau().getDateDebut()))
                .collect(Collectors.toList());

        modele.setRowCount(0);
        for (Reservation r : reservationsAffichees) {
            modele.addRow(new Object[]{
                    r.getCreneau().getDateDebut().toLocalDate(),
                    r.getSalle().getNom(),
                    r.getCreneau().getDateDebut().toLocalTime() + " - " + r.getCreneau().getDateFin().toLocalTime(),
                    r.getNomClient()
            });
        }
    }

    private void supprimerSelection() {
        int ligne = table.getSelectedRow();
        if (ligne < 0) {
            JOptionPane.showMessageDialog(this, "Selectionne d'abord une ligne a supprimer.",
                    "Aucune selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Reservation reservation = reservationsAffichees.get(ligne);
        int choix = JOptionPane.showConfirmDialog(this,
                "Supprimer la reservation de " + reservation.getNomClient()
                        + " le " + reservation.getCreneau().getDateDebut().toLocalDate() + " ?",
                "Confirmer la suppression", JOptionPane.YES_NO_OPTION);
        if (choix == JOptionPane.YES_OPTION) {
            gestion.supprimerReservation(reservation.getId());
            rafraichirTable();
            apresModification.run();
        }
    }
}
