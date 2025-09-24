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
        // Cambiar a BoxLayout vertical para mejor control
        setLayout(new BorderLayout(10, 10));
        setSize(900, 650); // Aumentar tamaño para mejor visualización
        setMinimumSize(new Dimension(800, 500));

        // 1. Panel superior con información del evento
        JPanel headerPanel = crearHeaderPanel();
        
        // 2. Panel central con tabla y toolbar
        JPanel centerPanel = crearCenterPanel();
        
        // 3. Panel inferior con botones principales
        JPanel buttonPanel = crearButtonPanel();

        // Añadir todos los paneles en el orden correcto
        add(headerPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel crearHeaderPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("📊 Información del Evento"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        panel.setBackground(new Color(240, 245, 255));

        panel.add(crearEtiquetaNegrita("Evento:"));
        panel.add(new JLabel(evento.getNombre()));
        
        panel.add(crearEtiquetaNegrita("Tipo:"));
        panel.add(new JLabel(evento.getTipo()));
        
        panel.add(crearEtiquetaNegrita("Fecha/Hora:"));
        panel.add(new JLabel(evento.getFecha() + " " + evento.getHora()));
        
        panel.add(crearEtiquetaNegrita("Capacidad:"));
        panel.add(new JLabel(String.valueOf(evento.getCapacidad())));
        
        lblEstadoCapacidad = new JLabel();
        lblEstadoCapacidad.setFont(lblEstadoCapacidad.getFont().deriveFont(Font.BOLD, 12));
        panel.add(crearEtiquetaNegrita("Estado:"));
        panel.add(lblEstadoCapacidad);

        return panel;
    }

    private JPanel crearCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
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
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBorder(BorderFactory.createTitledBorder("🔍 Buscar y Filtrar"));
        
        JTextField tfBusqueda = new JTextField(25);
        tfBusqueda.setToolTipText("Buscar por nombre, email o ID...");
        
        JComboBox<String> cbFiltro = new JComboBox<>(new String[]{"Todos", "Estudiantes", "Profesores"});
        
        tfBusqueda.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filtrarAsistentes(tfBusqueda.getText()); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filtrarAsistentes(tfBusqueda.getText()); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filtrarAsistentes(tfBusqueda.getText()); }
        });
        
        panel.add(new JLabel("Texto:"));
        panel.add(tfBusqueda);
        panel.add(new JLabel("Filtrar por:"));
        panel.add(cbFiltro);
        
        return panel;
    }

    private JScrollPane crearTablaPanel() {
        tableModel = new AsistentesTableModel(evento.getAsistentes());
        table = new JTable(tableModel);
        
        // Mejorar apariencia de la tabla
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(30);
        table.setFont(table.getFont().deriveFont(13f));
        table.getTableHeader().setFont(table.getFont().deriveFont(Font.BOLD, 13f));
        table.setGridColor(new Color(220, 220, 220));
        
        // Ajustar anchos de columnas
        table.getColumnModel().getColumn(0).setPreferredWidth(200); // Nombre
        table.getColumnModel().getColumn(1).setPreferredWidth(250); // Email
        table.getColumnModel().getColumn(2).setPreferredWidth(100); // Tipo
        table.getColumnModel().getColumn(3).setPreferredWidth(120); // Estado
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("👥 Lista de Asistentes"));
        scrollPane.setPreferredSize(new Dimension(800, 300));
        
        return scrollPane;
    }

    private JPanel crearButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Crear botones con colores distintivos
        JButton btnAgregarCatalogo = crearBotonGrande("📚 Agregar desde Catálogo", new Color(70, 130, 180));
        JButton btnNuevoAsistente = crearBotonGrande("➕ Nuevo Asistente", new Color(60, 179, 113));
        JButton btnQuitar = crearBotonGrande("❌ Quitar Seleccionado", new Color(205, 92, 92));
        JButton btnAsignarme = crearBotonGrande("👤 Asignarme a Mí", new Color(106, 90, 205));
        JButton btnCerrar = crearBotonGrande("🚪 Cerrar", new Color(120, 120, 120));

        // Hacer el botón SIEMPRE visible
        boolean puedeAsignarse = SessionContext.get().isLoggedIn() && 
                                SessionContext.get().getPersona() != null;

        if (!puedeAsignarse) {
            // En lugar de deshabilitarlo, cambiar el color y tooltip
            btnAsignarme.setBackground(new Color(180, 180, 180)); // Gris
            btnAsignarme.setToolTipText("💡 Inicie sesión con una persona vinculada para usar esta función");
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
        firstRow.add(btnAgregarCatalogo);
        firstRow.add(btnNuevoAsistente);
        firstRow.add(btnQuitar);

        JPanel secondRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        secondRow.add(btnAsignarme);
        secondRow.add(btnCerrar);

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(firstRow);
        panel.add(secondRow);

        return panel;
    }

    private JButton crearBotonGrande(String texto, Color colorFondo) {
        JButton boton = new JButton(texto);
        boton.setFont(boton.getFont().deriveFont(Font.BOLD, 13f));
        boton.setForeground(Color.WHITE);
        boton.setBackground(colorFondo);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(colorFondo.darker(), 2),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setPreferredSize(new Dimension(220, 45));
        
        // Efecto hover
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                boton.setBackground(colorFondo.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                boton.setBackground(colorFondo);
            }
        });
        
        return boton;
    }

    private JLabel crearEtiquetaNegrita(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        return label;
    }

    private void actualizarEstadoCapacidad() {
        int inscritos = evento.getAsistentes().size();
        int capacidad = evento.getCapacidad();
        
        String texto;
        Color color;
        
        if (capacidad <= 0) {
            texto = "✅ " + inscritos + " inscritos (sin límite)";
            color = new Color(0, 100, 200);
        } else if (inscritos < capacidad) {
            int disponibles = capacidad - inscritos;
            texto = "✅ " + inscritos + " / " + capacidad + " inscritos (" + disponibles + " disponibles)";
            color = new Color(0, 128, 0);
        } else {
            texto = "❌ COMPLETO - " + inscritos + " / " + capacidad + " inscritos";
            color = Color.RED;
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
                    "❌ El evento está completo. No se pueden agregar más asistentes.", 
                    "Capacidad Llena", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Validar duplicados
            for (Persona p : evento.getAsistentes()) {
                if (p.getId().equals(nuevaPersona.getId())) {
                    JOptionPane.showMessageDialog(this, 
                        "⚠️ Esta persona ya está inscrita en el evento.", 
                        "Duplicado", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            
            evento.agregarAsistente(nuevaPersona);
            tableModel.setAsistentes(evento.getAsistentes());
            actualizarEstadoCapacidad();
            JOptionPane.showMessageDialog(this, 
                "✅ Asistente agregado correctamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void quitarAsistente() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, 
                "ℹ️ Seleccione un asistente de la lista", 
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
                    "✅ Asistente eliminado correctamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void asignarme() {
        Persona yo = SessionContext.get().getPersona();
    
        if (yo == null) {
            JOptionPane.showMessageDialog(this, 
                "❌ Para asignarse a este evento, primero debe:\n\n" +
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
                    "ℹ️ Ya está inscrito en este evento.", 
                    "Información", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }
        
        // Validar capacidad
        int capacidad = evento.getCapacidad();
        if (capacidad > 0 && evento.getAsistentes().size() >= capacidad) {
            JOptionPane.showMessageDialog(this, 
                "❌ No es posible inscribirse: el evento está completo.", 
                "Capacidad Llena", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Agregar asistente
        evento.agregarAsistente(yo);
        tableModel.setAsistentes(evento.getAsistentes());
        actualizarEstadoCapacidad();
        JOptionPane.showMessageDialog(this, 
            "✅ ¡Inscripción realizada correctamente!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
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
                case 3: return sistema.yaInscrito(evento, p) ? "❌ Ya inscrito" : "✅ Disponible";
                default: return "";
            }
        }

        public Persona getAsistenteAt(int row) {
            return (row >= 0 && row < asistentesFiltrados.size()) ? 
                   asistentesFiltrados.get(row) : null;
        }
    }
}   