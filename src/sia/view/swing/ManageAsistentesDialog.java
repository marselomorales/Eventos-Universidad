package sia.view.swing;

import sia.*;
import sia.exceptions.CapacidadLlenaException;
import sia.SessionContext;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;

public class ManageAsistentesDialog extends JDialog {
    private final SistemaEventos sistema;
    private final Evento evento;
    private JTable table;
    private AsistentesTableModel tableModel;
    private JLabel lblEstadoCapacidad;

    public ManageAsistentesDialog(Frame owner, SistemaEventos sistema, Evento evento) {
        super(owner, "Gestión de Asistentes: " + evento.getNombre(), true);
        this.sistema = sistema;
        this.evento = evento;
        initUI();
        actualizarEstadoCapacidad();
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setSize(900, 650);
        getContentPane().setBackground(AppStyle.LIGHT_GRAY);

        // 1. Panel superior con información del evento
        JPanel headerPanel = crearHeaderPanel();
        
        // 2. Panel central con tabla y toolbar
        JPanel centerPanel = crearCenterPanel();
        
        // 3. Panel inferior con botones principales
        JPanel buttonPanel = crearButtonPanel();

        add(headerPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel crearHeaderPanel() {
        JPanel panel = ComponentFactory.createCardPanel();
        panel.setLayout(new GridLayout(0, 2, 5, 5));

        panel.add(ComponentFactory.createLabel("Evento:"));
        panel.add(ComponentFactory.createLabel(evento.getNombre()));
        
        panel.add(ComponentFactory.createLabel("Tipo:"));
        panel.add(ComponentFactory.createLabel(evento.getTipo()));
        
        panel.add(ComponentFactory.createLabel("Fecha/Hora:"));
        panel.add(ComponentFactory.createLabel(evento.getFecha() + " " + evento.getHora()));
        
        panel.add(ComponentFactory.createLabel("Capacidad:"));
        panel.add(ComponentFactory.createLabel(String.valueOf(evento.getCapacidad())));
        
        lblEstadoCapacidad = ComponentFactory.createLabel("");
        panel.add(ComponentFactory.createLabel("Estado:"));
        panel.add(lblEstadoCapacidad);

        return panel;
    }

    private JPanel crearCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(AppStyle.LIGHT_GRAY);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Toolbar de búsqueda
        JPanel searchPanel = crearSearchPanel();
        
        // Tabla de asistentes
        JScrollPane tablePanel = crearTablaPanel();
        
        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(tablePanel, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel crearSearchPanel() {
        JPanel panel = ComponentFactory.createCardPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
        
        JTextField tfBusqueda = ComponentFactory.createTextField(25);
        tfBusqueda.setToolTipText("Buscar por nombre, email o ID...");
        
        JComboBox<String> cbFiltro = ComponentFactory.createComboBox(new String[]{"Todos", "Estudiantes", "Profesores"});
        
        tfBusqueda.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filtrarAsistentes(tfBusqueda.getText()); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filtrarAsistentes(tfBusqueda.getText()); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filtrarAsistentes(tfBusqueda.getText()); }
        });
        
        panel.add(ComponentFactory.createLabel("Texto:"));
        panel.add(tfBusqueda);
        panel.add(ComponentFactory.createLabel("Filtrar por:"));
        panel.add(cbFiltro);
        
        return panel;
    }

    private JScrollPane crearTablaPanel() {
        tableModel = new AsistentesTableModel(evento.getAsistentes());
        table = new JTable(tableModel);
        
        // Mejorar apariencia de la tabla
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(30);
        table.setFont(AppStyle.FONT_INPUT);
        table.getTableHeader().setFont(AppStyle.FONT_LABEL);
        table.setGridColor(AppStyle.BORDER);
        table.setBackground(Color.WHITE);
        
        // Ajustar anchos de columnas
        table.getColumnModel().getColumn(0).setPreferredWidth(200); // Nombre
        table.getColumnModel().getColumn(1).setPreferredWidth(250); // Email
        table.getColumnModel().getColumn(2).setPreferredWidth(100); // Tipo
        table.getColumnModel().getColumn(3).setPreferredWidth(120); // Estado
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(" Lista de Asistentes"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        scrollPane.setPreferredSize(new Dimension(800, 300));
        
        return scrollPane;
    }

    private JPanel crearButtonPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(AppStyle.LIGHT_GRAY);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Crear botones con ComponentFactory
        JButton btnAgregarCatalogo = ComponentFactory.createPrimaryButton("§ Agregar desde Catálogo");
        JButton btnNuevoAsistente = ComponentFactory.createSuccessButton("+ Nuevo Asistente");
        JButton btnQuitar = ComponentFactory.createDangerButton(" Quitar Seleccionado");
        JButton btnAsignarme = ComponentFactory.createSecondaryButton(" Asignarme a Mí");
        JButton btnCerrar = ComponentFactory.createWarningButton(" Cerrar");

        // Configurar botón "Asignarme a Mí"
        boolean puedeAsignarse = SessionContext.get().isLoggedIn() && 
                                SessionContext.get().getPersona() != null;

        if (!puedeAsignarse) {
            btnAsignarme.setBackground(AppStyle.TEXT_SECONDARY);
            btnAsignarme.setToolTipText(" Inicie sesión con una persona vinculada para usar esta función");
        } else {
            btnAsignarme.setToolTipText("Inscribirme con mis datos de usuario");
        }

        // Asegurar que todos los botones tengan tamaño consistente
        Dimension buttonSize = new Dimension(200, 45);
        btnAgregarCatalogo.setPreferredSize(buttonSize);
        btnNuevoAsistente.setPreferredSize(buttonSize);
        btnQuitar.setPreferredSize(buttonSize);
        btnAsignarme.setPreferredSize(buttonSize);
        btnCerrar.setPreferredSize(buttonSize);

        // Acciones de los botones
        btnAgregarCatalogo.addActionListener(e -> agregarDesdeCatalogo());
        btnNuevoAsistente.addActionListener(e -> nuevoAsistente());
        btnQuitar.addActionListener(e -> quitarAsistente());
        btnAsignarme.addActionListener(e -> asignarme());
        btnCerrar.addActionListener(e -> dispose());

        // Mejorar el layout para evitar desbordamiento
        JPanel firstRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        firstRow.setBackground(AppStyle.LIGHT_GRAY);
        firstRow.add(btnAgregarCatalogo);
        firstRow.add(btnNuevoAsistente);
        firstRow.add(btnQuitar);

        JPanel secondRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        secondRow.setBackground(AppStyle.LIGHT_GRAY);
        secondRow.add(btnAsignarme);
        secondRow.add(btnCerrar);

        panel.add(firstRow);
        panel.add(secondRow);

        return panel;
    }

    private void actualizarEstadoCapacidad() {
        int inscritos = evento.getAsistentes().size();
        int capacidad = evento.getCapacidad();
        
        String texto;
        Color color;
        
        if (capacidad <= 0) {
            texto = " " + inscritos + " inscritos (sin límite)";
            color = AppStyle.SUCCESS;
        } else if (inscritos < capacidad) {
            int disponibles = capacidad - inscritos;
            texto = " " + inscritos + " / " + capacidad + " inscritos (" + disponibles + " disponibles)";
            color = AppStyle.SUCCESS;
        } else {
            texto = " COMPLETO - " + inscritos + " / " + capacidad + " inscritos";
            color = AppStyle.DANGER;
        }
        
        lblEstadoCapacidad.setText(texto);
        lblEstadoCapacidad.setForeground(color);
        lblEstadoCapacidad.setFont(lblEstadoCapacidad.getFont().deriveFont(Font.BOLD));
    }

    private void filtrarAsistentes(String texto) {
        tableModel.filtrar(texto);
    }

    private void agregarDesdeCatalogo() {
        SeleccionarPersonasDialog dlg = new SeleccionarPersonasDialog(
            (Frame) getOwner(), sistema, evento);
        dlg.setVisible(true);
        
        tableModel.setAsistentes(evento.getAsistentes());
        actualizarEstadoCapacidad();
    }

    private void nuevoAsistente() {
        AddPersonaDialog dlg = new AddPersonaDialog((Frame) getOwner());
        dlg.setVisible(true);
        
        Persona nuevaPersona = dlg.getPersonaCreada();
        if (nuevaPersona != null) {
            // Validar capacidad
            int capacidad = evento.getCapacidad();
            if (capacidad > 0 && evento.getAsistentes().size() >= capacidad) {
                JOptionPane.showMessageDialog(this, 
                    " El evento está completo. No se pueden agregar más asistentes.", 
                    "Capacidad Llena", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Validar duplicados
            for (Persona p : evento.getAsistentes()) {
                if (p.getId().equals(nuevaPersona.getId())) {
                    JOptionPane.showMessageDialog(this, 
                        " Esta persona ya está inscrita en el evento.", 
                        "Duplicado", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            
            evento.agregarAsistente(nuevaPersona);
            tableModel.setAsistentes(evento.getAsistentes());
            actualizarEstadoCapacidad();
            JOptionPane.showMessageDialog(this, 
                " Asistente agregado correctamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void quitarAsistente() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, 
                " Seleccione un asistente de la lista", 
                "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        Persona asistente = tableModel.getAsistenteAt(row);
        if (asistente != null) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de que desea eliminar a " + asistente.getNombre() + " del evento?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                evento.eliminarAsistente(asistente.getId());
                tableModel.setAsistentes(evento.getAsistentes());
                actualizarEstadoCapacidad();
                JOptionPane.showMessageDialog(this, 
                    " Asistente eliminado correctamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void asignarme() {
        Persona yo = SessionContext.get().getPersona();
    
        if (yo == null) {
            JOptionPane.showMessageDialog(this, 
                " Para asignarse a este evento, primero debe:\n\n" +
                "1. Iniciar sesión en el sistema\n" +
                "2. Tener una persona vinculada a su cuenta\n\n" +
                "Contacte al administrador si necesita vincular una persona.",
                "Acceso no disponible", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Verificar si ya está inscrito
        for (Persona p : evento.getAsistentes()) {
            if (p.getId().equals(yo.getId())) {
                JOptionPane.showMessageDialog(this, 
                    " Ya está inscrito en este evento.", 
                    "Información", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }
        
        // Validar capacidad
        int capacidad = evento.getCapacidad();
        if (capacidad > 0 && evento.getAsistentes().size() >= capacidad) {
            JOptionPane.showMessageDialog(this, 
                " No es posible inscribirse: el evento está completo.", 
                "Capacidad Llena", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Agregar asistente
        evento.agregarAsistente(yo);
        tableModel.setAsistentes(evento.getAsistentes());
        actualizarEstadoCapacidad();
        JOptionPane.showMessageDialog(this, 
            " ¡Inscripción realizada correctamente!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    // Modelo de tabla para asistentes
    class AsistentesTableModel extends AbstractTableModel {
        private final String[] COLUMNS = {"Nombre", "Email", "Tipo", "Estado"};
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
                        p.getEmail().toLowerCase().contains(lowerText) ||
                        p.getId().toLowerCase().contains(lowerText)) {
                        asistentesFiltrados.add(p);
                    }
                }
            }
            fireTableDataChanged();
        }

        @Override 
        public int getRowCount() { 
            return asistentesFiltrados.size(); 
        }
        
        @Override 
        public int getColumnCount() { 
            return COLUMNS.length; 
        }
        
        @Override 
        public String getColumnName(int column) { 
            return COLUMNS[column]; 
        }

        @Override
        public Object getValueAt(int row, int column) {
            Persona p = asistentesFiltrados.get(row);
            switch (column) {
                case 0: return p.getNombre();
                case 1: return p.getEmail();
                case 2: return p.getTipo();
                case 3: return sistema.yaInscrito(evento, p) ? " Ya inscrito" : " Disponible";
                default: return "";
            }
        }

        public Persona getAsistenteAt(int row) {
            return (row >= 0 && row < asistentesFiltrados.size()) ? 
                   asistentesFiltrados.get(row) : null;
        }
    }
}

