package sia.view.swing;

import sia.*;
import sia.exceptions.RecursoOcupadoException;
import sia.util.Validators;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;
import java.util.*;

public class RecursosEventoDialog extends JDialog {
    private final SistemaEventos sistema;
    private final Evento evento;
    private JTable table;
    private ResourceTableModel tableModel;
    private JTextField tfSearch;
    private JComboBox<String> cbFilter;

    public RecursosEventoDialog(Frame owner, SistemaEventos sistema, Evento evento) {
        super(owner, "Recursos para: " + evento.getNombre(), true);
        this.sistema = sistema;
        this.evento = evento;
        initUI();
        loadResources();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setSize(800, 500);
        setLocationRelativeTo(getOwner());

        // Header panel
        JPanel headerPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        headerPanel.add(new JLabel("Evento:"));
        headerPanel.add(new JLabel(evento.getNombre()));
        headerPanel.add(new JLabel("Tipo:"));
        headerPanel.add(new JLabel(evento.getTipo()));
        headerPanel.add(new JLabel("Fecha/Hora:"));
        headerPanel.add(new JLabel(evento.getFecha() + " " + evento.getHora()));
        headerPanel.add(new JLabel("Capacidad:"));
        headerPanel.add(new JLabel(String.valueOf(evento.getCapacidad())));

        // Toolbar
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.add(new JLabel("Buscar:"));
        tfSearch = new JTextField(20);
        tfSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterResources(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterResources(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterResources(); }
        });
        toolbar.add(tfSearch);
        toolbar.addSeparator();
        toolbar.add(new JLabel("Mostrar:"));
        cbFilter = new JComboBox<>(new String[]{"Todos", "Disponibles", "Ocupados"});
        cbFilter.addActionListener(e -> filterResources());
        toolbar.add(cbFilter);

        // Table
        tableModel = new ResourceTableModel();
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(table);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAsociar = new JButton("Asociar");
        JButton btnQuitar = new JButton("Quitar");
        JButton btnNuevo = new JButton("Nuevo Recurso...");
        JButton btnCerrar = new JButton("Cerrar");

        btnAsociar.addActionListener(e -> asociarRecurso());
        btnQuitar.addActionListener(e -> quitarRecurso());
        btnNuevo.addActionListener(e -> nuevoRecurso());
        btnCerrar.addActionListener(e -> dispose());

        buttonPanel.add(btnAsociar);
        buttonPanel.add(btnQuitar);
        buttonPanel.add(btnNuevo);
        buttonPanel.add(btnCerrar);

        add(headerPanel, BorderLayout.NORTH);
        add(toolbar, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadResources() {
        String filterText = tfSearch.getText().toLowerCase();
        String filter = (String) cbFilter.getSelectedItem();
        List<Recurso> recursos = sistema.listarRecursosOrdenadosPara(evento, filterText, filter);
        tableModel.setRecursos(recursos);
    }

    private void filterResources() { loadResources(); }

    private void asociarRecurso() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un recurso", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Recurso recurso = tableModel.getRecursoAt(row);
        try {
            sistema.asociarRecursoOrThrow(evento, recurso);
            loadResources();
            JOptionPane.showMessageDialog(this, "Recurso asociado correctamente");
        } catch (RecursoOcupadoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void quitarRecurso() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un recurso", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Recurso recurso = tableModel.getRecursoAt(row);
        if (sistema.desasociarRecurso(evento, recurso.getId())) {
            loadResources();
            JOptionPane.showMessageDialog(this, "Recurso desasociado correctamente");
        }
    }

    private void nuevoRecurso() {
        ManageRecursosDialog dlg = new ManageRecursosDialog((Frame) getOwner(), sistema);
        dlg.setVisible(true);
        loadResources();
    }

    class ResourceTableModel extends AbstractTableModel {
        private final String[] COLUMNS = {"Tipo", "Nombre", "Detalles", "Estado", "Asociado"};
        private List<Recurso> recursos = new ArrayList<>();

        public void setRecursos(List<Recurso> recursos) {
            this.recursos = recursos;
            fireTableDataChanged();
        }

        @Override public int getRowCount() { return recursos.size(); }
        @Override public int getColumnCount() { return COLUMNS.length; }
        @Override public String getColumnName(int column) { return COLUMNS[column]; }

        @Override
        public Object getValueAt(int row, int column) {
            Recurso r = recursos.get(row);
            switch (column) {
                case 0: return r.getTipo();
                case 1: return r.getNombre();
                case 2: 
                    if (r instanceof Sala) {
                        Sala s = (Sala) r;
                        return s.getCapacidad() + " personas, " + s.getUbicacion();
                    } else if (r instanceof Equipo) {
                        return ((Equipo) r).getTipoEquipo();
                    }
                    return "";
                case 3:
                    return sistema.isRecursoDisponible(r, evento.getFecha(), evento.getHora()) ? 
                           "Disponible" : "Ocupado";
                case 4:
                    return evento.getRecursos().contains(r) ? "Sí" : "No";
                default: return "";
            }
        }

        public Recurso getRecursoAt(int row) { return recursos.get(row); }
    }
}