package sia.view.swing;

import sia.*;
import sia.exceptions.CapacidadLlenaException;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SeleccionarPersonasDialog extends JDialog {
    private final SistemaEventos sistema;
    private final Evento evento;
    private JTable table;
    private PersonasTableModel tableModel;

    public SeleccionarPersonasDialog(Frame owner, SistemaEventos sistema, Evento evento) {
        super(owner, "Seleccionar Personas del Catálogo", true);
        this.sistema = sistema;
        this.evento = evento;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setSize(600, 400);
        setLocationRelativeTo(getOwner());

        // Toolbar
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        
        JTextField tfBusqueda = new JTextField(15);
        JComboBox<String> cbFiltroRol = new JComboBox<>(new String[]{"Todos", "Estudiantes", "Profesores"});
        
        tfBusqueda.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filtrarPersonas(tfBusqueda.getText(), (String) cbFiltroRol.getSelectedItem()); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filtrarPersonas(tfBusqueda.getText(), (String) cbFiltroRol.getSelectedItem()); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filtrarPersonas(tfBusqueda.getText(), (String) cbFiltroRol.getSelectedItem()); }
        });
        
        cbFiltroRol.addActionListener(e -> filtrarPersonas(tfBusqueda.getText(), (String) cbFiltroRol.getSelectedItem()));
        
        toolbar.add(new JLabel("Buscar:"));
        toolbar.add(tfBusqueda);
        toolbar.addSeparator();
        toolbar.add(new JLabel("Filtrar por rol:"));
        toolbar.add(cbFiltroRol);

        // Table
        List<Persona> todasPersonas = sistema.listarPersonasCatalogo("", "TODOS");
        tableModel = new PersonasTableModel(todasPersonas, evento);
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(table);

        // Botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAgregar = new JButton("Agregar seleccionados");
        JButton btnCancelar = new JButton("Cancelar");
        
        btnAgregar.addActionListener(e -> agregarSeleccionados());
        btnCancelar.addActionListener(e -> dispose());
        
        buttonPanel.add(btnCancelar);
        buttonPanel.add(btnAgregar);

        add(toolbar, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void filtrarPersonas(String texto, String filtroRol) {
        String rolFiltro = "TODOS";
        if ("Estudiantes".equals(filtroRol)) rolFiltro = "ESTUDIANTES";
        if ("Profesores".equals(filtroRol)) rolFiltro = "PROFESORES";
        
        List<Persona> personasFiltradas = sistema.listarPersonasCatalogo(texto, rolFiltro);
        tableModel.setPersonas(personasFiltradas);
    }

    private void agregarSeleccionados() {
        int[] rows = table.getSelectedRows();
        if (rows.length == 0) {
            JOptionPane.showMessageDialog(this, "Seleccione al menos una persona", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int agregados = 0;
        int errores = 0;
        StringBuilder mensajeError = new StringBuilder();
        
        for (int row : rows) {
            Persona persona = tableModel.getPersonaAt(row);
            if (persona != null && !sistema.yaInscrito(evento, persona)) {
                try {
                    sistema.agregarAsistenteOrThrow(evento, persona);
                    agregados++;
                } catch (CapacidadLlenaException ex) {
                    errores++;
                    if (mensajeError.length() > 0) mensajeError.append("\n");
                    mensajeError.append("- ").append(ex.getMessage());
                    break; // Detener si no hay capacidad
                }
            } else {
                errores++;
            }
        }
        
        // Mostrar resultado
        if (agregados > 0) {
            JOptionPane.showMessageDialog(this, agregados + " personas agregadas correctamente");
        }
        
        if (errores > 0) {
            if (mensajeError.length() == 0) {
                mensajeError.append(errores).append(" personas no pudieron ser agregadas (posiblemente ya estaban inscritas)");
            }
            JOptionPane.showMessageDialog(this, mensajeError.toString(), "Advertencia", JOptionPane.WARNING_MESSAGE);
        }
        
        dispose();
    }

    class PersonasTableModel extends AbstractTableModel {
        private final String[] COLUMNS = {"Nombre", "Email", "Rol", "Estado"};
        private List<Persona> personas;
        private final Evento evento;

        public PersonasTableModel(List<Persona> personas, Evento evento) {
            this.personas = new ArrayList<>(personas);
            this.evento = evento;
        }

        public void setPersonas(List<Persona> personas) {
            this.personas = new ArrayList<>(personas);
            fireTableDataChanged();
        }

        @Override public int getRowCount() { return personas.size(); }
        @Override public int getColumnCount() { return COLUMNS.length; }
        @Override public String getColumnName(int column) { return COLUMNS[column]; }

        @Override
        public Object getValueAt(int row, int column) {
            Persona p = personas.get(row);
            switch (column) {
                case 0: return p.getNombre();
                case 1: return p.getEmail();
                case 2: return p.getTipo();
                case 3: return sistema.yaInscrito(evento, p) ? "Ya inscrito" : "Disponible";
                default: return "";
            }
        }
        
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
        
        public Persona getPersonaAt(int row) {
            return personas.get(row);
        }
    }
}