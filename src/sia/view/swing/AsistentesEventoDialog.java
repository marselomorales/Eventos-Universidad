package sia.view.swing;

import sia.*;
import sia.exceptions.CapacidadLlenaException;
import sia.SessionContext;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;

public class AsistentesEventoDialog extends JDialog {
    private final SistemaEventos sistema;
    private final Evento evento;
    private JTable table;
    private AsistentesTableModel tableModel;
    private JLabel lblEstadoCapacidad;

    public AsistentesEventoDialog(Frame owner, SistemaEventos sistema, Evento evento) {
        super(owner, "Asistentes: " + evento.getNombre(), true);
        this.sistema = sistema;
        this.evento = evento;
        initUI();
        actualizarEstadoCapacidad();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setSize(700, 500);
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
        
        // Estado de capacidad
        lblEstadoCapacidad = new JLabel();
        lblEstadoCapacidad.setFont(lblEstadoCapacidad.getFont().deriveFont(Font.BOLD));
        headerPanel.add(new JLabel("Estado:"));
        headerPanel.add(lblEstadoCapacidad);

        // Toolbar
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        
        JTextField tfBusqueda = new JTextField(15);
        tfBusqueda.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filtrarAsistentes(tfBusqueda.getText()); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filtrarAsistentes(tfBusqueda.getText()); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filtrarAsistentes(tfBusqueda.getText()); }
        });
        
        toolbar.add(new JLabel("Buscar:"));
        toolbar.add(tfBusqueda);
        toolbar.addSeparator();
        
        JButton btnAgregarCatalogo = new JButton("Agregar desde catálogo");
        JButton btnNuevoAsistente = new JButton("Nuevo asistente");
        JButton btnQuitar = new JButton("Quitar");
        JButton btnAsignarme = new JButton("Asignarme");
        
        // Mejora: habilitar botón "Asignarme" si hay sesión activa
        btnAsignarme.setEnabled(SessionContext.get().isLoggedIn() && 
            SessionContext.get().getPersona() != null);
        btnAsignarme.setToolTipText(btnAsignarme.isEnabled() ? 
            "Inscribirme con mis datos" : "Requiere inicio de sesión con persona vinculada");
        btnAsignarme.addActionListener(e -> asignarme());
        
        btnAgregarCatalogo.addActionListener(e -> agregarDesdeCatalogo());
        btnNuevoAsistente.addActionListener(e -> nuevoAsistente());
        btnQuitar.addActionListener(e -> quitarAsistente());
        
        toolbar.add(btnAgregarCatalogo);
        toolbar.add(btnNuevoAsistente);
        toolbar.add(btnQuitar);
        toolbar.add(btnAsignarme);

        // Table
        tableModel = new AsistentesTableModel(evento.getAsistentes());
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(table);

        add(headerPanel, BorderLayout.NORTH);
        add(toolbar, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void actualizarEstadoCapacidad() {
        int inscritos = sistema.contarInscritos(evento);
        int capacidad = evento.getCapacidad();
        
        String texto;
        Color color;
        
        if (capacidad <= 0) {
            texto = inscritos + " inscritos (sin límite)";
            color = Color.BLUE;
        } else if (inscritos < capacidad) {
            texto = inscritos + " / " + capacidad + " inscritos";
            color = new Color(0, 128, 0); // Verde oscuro
        } else {
            texto = "COMPLETO - " + inscritos + " / " + capacidad + " inscritos";
            color = Color.RED;
        }
        
        lblEstadoCapacidad.setText(texto);
        lblEstadoCapacidad.setForeground(color);
    }

    private void filtrarAsistentes(String texto) {
        tableModel.filtrar(texto);
    }

    private void agregarDesdeCatalogo() {
        SeleccionarPersonasDialog dlg = new SeleccionarPersonasDialog(
            (Frame) getOwner(), sistema, evento);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
        
        tableModel.setAsistentes(evento.getAsistentes());
        actualizarEstadoCapacidad();
    }

    private void nuevoAsistente() {
        AddPersonaDialog dlg = new AddPersonaDialog((Frame) getOwner());
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
        
        Persona nuevaPersona = dlg.getPersonaCreada();
        if (nuevaPersona != null) {
            try {
                sistema.agregarAsistenteOrThrow(evento, nuevaPersona);
                tableModel.setAsistentes(evento.getAsistentes());
                actualizarEstadoCapacidad();
                JOptionPane.showMessageDialog(this, "Asistente agregado correctamente");
            } catch (CapacidadLlenaException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void quitarAsistente() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un asistente", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        Persona asistente = tableModel.getAsistenteAt(row);
        if (asistente != null) {
            evento.eliminarAsistente(asistente.getId());
            tableModel.setAsistentes(evento.getAsistentes());
            actualizarEstadoCapacidad();
            JOptionPane.showMessageDialog(this, "Asistente eliminado correctamente");
        }
    }

    // Inscribirse automáticamente como usuario actual
    private void asignarme() {
        Persona yo = SessionContext.get().getPersona();
        if (yo == null) {
            JOptionPane.showMessageDialog(this, 
                "No tiene una persona vinculada a su cuenta", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (sistema.yaInscrito(evento, yo)) {
            JOptionPane.showMessageDialog(this, 
                "Ya está inscrito en este evento", 
                "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        try {
            sistema.agregarAsistenteOrThrow(evento, yo);
            tableModel.setAsistentes(evento.getAsistentes());
            actualizarEstadoCapacidad();
            JOptionPane.showMessageDialog(this, 
                "Inscripción realizada correctamente");
        } catch (CapacidadLlenaException ex) {
            JOptionPane.showMessageDialog(this, 
                "No es posible inscribirse: el evento está lleno", 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    class AsistentesTableModel extends AbstractTableModel {
        private final String[] COLUMNS = {"Nombre", "Email", "Rol"};
        private List<Persona> asistentes;
        private List<Persona> asistentesFiltrados;

        public AsistentesTableModel(List<Persona> asistentes) {
            this.asistentes = new ArrayList<>(asistentes);
            this.asistentesFiltrados = new ArrayList<>(asistentes);
        }

        public void setAsistentes(List<Persona> asistentes) {
            this.asistentes = new ArrayList<>(asistentes);
            this.asistentesFiltrados = new ArrayList<>(asistentes);
            fireTableDataChanged();
        }

        public void filtrar(String texto) {
            asistentesFiltrados.clear();
            if (texto == null || texto.trim().isEmpty()) {
                asistentesFiltrados.addAll(asistentes);
            } else {
                String lowerText = texto.toLowerCase();
                for (Persona p : asistentes) {
                    if (p.getNombre().toLowerCase().contains(lowerText) || 
                        p.getEmail().toLowerCase().contains(lowerText)) {
                        asistentesFiltrados.add(p);
                    }
                }
            }
            fireTableDataChanged();
        }

        @Override public int getRowCount() { return asistentesFiltrados.size(); }
        @Override public int getColumnCount() { return COLUMNS.length; }
        @Override public String getColumnName(int column) { return COLUMNS[column]; }

        @Override
        public Object getValueAt(int row, int column) {
            Persona p = asistentesFiltrados.get(row);
            switch (column) {
                case 0: return p.getNombre();
                case 1: return p.getEmail();
                case 2: return p.getTipo();
                default: return "";
            }
        }

        public Persona getAsistenteAt(int row) {
            return asistentesFiltrados.get(row);
        }
    }
}
