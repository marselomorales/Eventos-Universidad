package sia.view.swing;

import sia.*;
import sia.exceptions.RecursoOcupadoException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public class RecursosEventoDialog extends JDialog {
    private final SistemaEventos sistema;
    private final Evento evento;
    private JTable table;
    private ResourceTableModel tableModel;
    private JTextField tfSearch;
    private JComboBox<String> cbFilter;

    // Formulario para agregar equipos
    private JTextField txtId;
    private JTextField txtNombre;
    private JComboBox<String> cmbTipoEquipo;
    private JCheckBox chkDisponible;
    private JLabel lblErrorId;

    // Colores modernos
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SUCCESS_COLOR = new Color(39, 174, 96);
    private static final Color WARNING_COLOR = new Color(243, 156, 18);
    private static final Color DANGER_COLOR = new Color(231, 76, 60);
    private static final Color LIGHT_GRAY = new Color(250, 250, 250);
    private static final Color BORDER_COLOR = new Color(220, 220, 220);

    // Listas predefinidas de tipos de equipo
    private static final String[] EQUIPMENT_TYPES = {
        "Proyector", "Micrófono", "Sistema de Sonido", "Computadora",
        "Pantalla", "Pizarra Interactiva", "Cámara", "Trípode",
        "Iluminación", "Mesa de Mezclas", "Router Wi-Fi", "Impresora",
        "Escáner", "Tableta Gráfica", "Equipo de Video conferencia",
        "Laptop", "Tablet", "Monitor", "Teclado", "Mouse", "Auriculares"
    };

    public RecursosEventoDialog(Frame owner, SistemaEventos sistema, Evento evento) {
        super(owner, " Gestión de Recursos - " + evento.getNombre(), true);
        this.sistema = sistema;
        this.evento = evento;
        initUI();
        loadResources();
    }

    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setSize(1200, 700);
        setLocationRelativeTo(getOwner());
        getContentPane().setBackground(LIGHT_GRAY);

        // Panel superior con información del evento
        JPanel headerPanel = createHeaderPanel();
        
        // Panel central con tabla y formulario
        JPanel centerPanel = createCenterPanel();
        
        // Panel inferior con botones de acción
        JPanel buttonPanel = createButtonPanel();

        add(headerPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 5));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        panel.setBackground(Color.WHITE);
        panel.setOpaque(true);

        Font labelFont = new Font("Segoe UI", Font.BOLD, 12);
        Font valueFont = new Font("Segoe UI", Font.PLAIN, 12);

        addHeaderLabel(panel, "Evento:", labelFont);
        addHeaderValue(panel, evento.getNombre(), valueFont);
        addHeaderLabel(panel, "Tipo:", labelFont);
        addHeaderValue(panel, evento.getTipo(), valueFont);
        addHeaderLabel(panel, "Fecha/Hora:", labelFont);
        addHeaderValue(panel, evento.getFecha() + " " + evento.getHora(), valueFont);
        addHeaderLabel(panel, "Capacidad:", labelFont);
        addHeaderValue(panel, String.valueOf(evento.getCapacidad()), valueFont);

        return panel;
    }

    private void addHeaderLabel(JPanel panel, String text, Font font) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(Color.DARK_GRAY);
        panel.add(label);
    }

    private void addHeaderValue(JPanel panel, String text, Font font) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(PRIMARY_COLOR);
        panel.add(label);
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(LIGHT_GRAY);
        
        // Panel izquierdo: Tabla de recursos
        JPanel tablePanel = createTablePanel();
        
        // Panel derecho: Formulario para agregar equipos
        JPanel formPanel = createFormPanel();
        
        panel.add(tablePanel, BorderLayout.CENTER);
        panel.add(formPanel, BorderLayout.EAST);
        
        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                " Lista de Recursos",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12),
                PRIMARY_COLOR
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        panel.setBackground(Color.WHITE);
        panel.setOpaque(true);
        
        // Toolbar de búsqueda y filtros
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        toolbar.setBackground(Color.WHITE);
        
        JLabel searchLabel = new JLabel(" Buscar:");
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        toolbar.add(searchLabel);
        
        tfSearch = new JTextField(20);
        tfSearch.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tfSearch.setToolTipText("Buscar por ID, nombre o tipo de recurso");
        tfSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterResources(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterResources(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterResources(); }
        });
        toolbar.add(tfSearch);
        
        toolbar.add(Box.createHorizontalStrut(20));
        
        JLabel filterLabel = new JLabel(" Filtrar:");
        filterLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        toolbar.add(filterLabel);
        
        cbFilter = new JComboBox<>(new String[]{"Todos", "Disponibles", "Ocupados", "Asociados"});
        cbFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbFilter.addActionListener(e -> filterResources());
        toolbar.add(cbFilter);

        // Tabla de recursos
        tableModel = new ResourceTableModel();
        table = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    String estado = (String) getValueAt(row, 3);
                    String asociado = (String) getValueAt(row, 4);
                    
                    if ("Sí".equals(asociado)) {
                        c.setBackground(new Color(220, 245, 220)); // Verde claro para asociados
                    } else if ("Ocupado".equals(estado)) {
                        c.setBackground(new Color(255, 230, 230)); // Rojo claro para ocupados
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                }
                return c;
            }
        };
        
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(PRIMARY_COLOR);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setGridColor(BORDER_COLOR);
        
        // Ajustar anchos de columnas
        table.getColumnModel().getColumn(0).setPreferredWidth(80);  // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(200); // Nombre
        table.getColumnModel().getColumn(2).setPreferredWidth(150); // Tipo
        table.getColumnModel().getColumn(3).setPreferredWidth(100); // Estado
        table.getColumnModel().getColumn(4).setPreferredWidth(80);  // Asociado
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                "+ Agregar Nuevo Recurso",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12),
                SUCCESS_COLOR
            ),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        panel.setBackground(Color.WHITE);
        panel.setOpaque(true);
        panel.setPreferredSize(new Dimension(320, 400));
        
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 10, 8, 10);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.NORTHWEST;
        
        int y = 0;

        Font labelFont = new Font("Segoe UI", Font.BOLD, 12);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 12);

        // ID del equipo
        gc.gridx = 0; gc.gridy = y; gc.weightx = 0.3;
        JLabel lblId = new JLabel("ID*:");
        lblId.setFont(labelFont);
        panel.add(lblId, gc);
        
        txtId = new JTextField();
        txtId.setFont(fieldFont);
        txtId.setToolTipText("Identificador único del recurso");
        gc.gridx = 1; gc.gridy = y++; gc.weightx = 0.7;
        panel.add(txtId, gc);

        // Mensaje de error para ID
        lblErrorId = new JLabel(" ");
        lblErrorId.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblErrorId.setForeground(DANGER_COLOR);
        gc.gridx = 0; gc.gridy = y; gc.gridwidth = 2;
        gc.insets = new Insets(0, 10, 10, 10);
        panel.add(lblErrorId, gc);
        y++;

        // Nombre del equipo
        gc.gridx = 0; gc.gridy = y; gc.weightx = 0.3; gc.insets = new Insets(8, 10, 8, 10);
        JLabel lblNombre = new JLabel("Nombre*:");
        lblNombre.setFont(labelFont);
        panel.add(lblNombre, gc);
        
        txtNombre = new JTextField();
        txtNombre.setFont(fieldFont);
        txtNombre.setToolTipText("Nombre descriptivo del recurso");
        gc.gridx = 1; gc.gridy = y++; gc.weightx = 0.7;
        panel.add(txtNombre, gc);

        // Tipo de equipo
        gc.gridx = 0; gc.gridy = y; gc.weightx = 0.3;
        JLabel lblTipo = new JLabel("Tipo*:");
        lblTipo.setFont(labelFont);
        panel.add(lblTipo, gc);
        
        Set<String> allEquipmentTypes = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        allEquipmentTypes.addAll(Arrays.asList(EQUIPMENT_TYPES));
        cmbTipoEquipo = new JComboBox<>(allEquipmentTypes.toArray(new String[0]));
        cmbTipoEquipo.setFont(fieldFont);
        cmbTipoEquipo.setEditable(true);
        cmbTipoEquipo.setToolTipText("Seleccione o escriba el tipo de recurso");
        gc.gridx = 1; gc.gridy = y++; gc.weightx = 0.7;
        panel.add(cmbTipoEquipo, gc);

        // Disponibilidad
        gc.gridx = 0; gc.gridy = y; gc.weightx = 0.3;
        JLabel lblDisp = new JLabel("Disponible:");
        lblDisp.setFont(labelFont);
        panel.add(lblDisp, gc);
        
        chkDisponible = new JCheckBox("Sí", true);
        chkDisponible.setFont(fieldFont);
        chkDisponible.setBackground(Color.WHITE);
        gc.gridx = 1; gc.gridy = y++; gc.weightx = 0.7;
        panel.add(chkDisponible, gc);

        // Botón agregar
        JButton btnAgregar = new JButton("+ Agregar al Catálogo");
        btnAgregar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAgregar.setBackground(SUCCESS_COLOR);
        btnAgregar.setForeground(Color.WHITE);
        btnAgregar.setFocusPainted(false);
        btnAgregar.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        btnAgregar.setToolTipText("Agregar nuevo recurso al catálogo general");
        btnAgregar.addActionListener(e -> agregarEquipo());
        
        gc.gridx = 0; gc.gridy = y; gc.gridwidth = 2;
        gc.insets = new Insets(20, 10, 10, 10);
        gc.anchor = GridBagConstraints.CENTER;
        panel.add(btnAgregar, gc);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        panel.setBackground(LIGHT_GRAY);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JButton btnAsociar = createStyledButton(" Asociar al Evento", SUCCESS_COLOR);
        JButton btnQuitar = createStyledButton(" Quitar del Evento", DANGER_COLOR);
        JButton btnCerrar = createStyledButton(" Cerrar", new Color(120, 120, 120));

        btnAsociar.addActionListener(e -> asociarRecurso());
        btnQuitar.addActionListener(e -> quitarRecurso());
        btnCerrar.addActionListener(e -> dispose());

        panel.add(btnAsociar);
        panel.add(btnQuitar);
        panel.add(btnCerrar);

        return panel;
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        return button;
    }

    private void loadResources() {
        String filterText = tfSearch.getText().toLowerCase();
        String filter = (String) cbFilter.getSelectedItem();
        List<Recurso> recursos = sistema.listarRecursosOrdenadosPara(evento, filterText, filter);
        tableModel.setRecursos(recursos);
    }

    private void filterResources() { 
        loadResources(); 
    }

    private void agregarEquipo() {
        String id = safe(txtId.getText());
        String nombre = safe(txtNombre.getText());
        String tipoEquipo = safe((String) cmbTipoEquipo.getSelectedItem());

        if (id.isEmpty() || nombre.isEmpty() || tipoEquipo.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                " ID, Nombre y Tipo son campos obligatorios.", 
                "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Verificar ID duplicado
        for (Recurso r : sistema.getRecursosDisponibles()) {
            if (Objects.equals(r.getId(), id)) {
                lblErrorId.setText(" Ya existe un recurso con este ID");
                JOptionPane.showMessageDialog(this, 
                    " Ya existe un recurso con ese ID.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // Crear y agregar el equipo
        boolean disponible = chkDisponible.isSelected();
        Equipo nuevoEquipo = new Equipo(id, nombre, tipoEquipo, disponible);
        sistema.getRecursosDisponibles().add(nuevoEquipo);
        
        // Actualizar tabla y limpiar formulario
        loadResources();
        limpiarFormulario();
        
        JOptionPane.showMessageDialog(this, 
            " Recurso agregado correctamente al catálogo.", 
            "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    private void asociarRecurso() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, 
                " Seleccione un recurso de la tabla.", 
                "Selección Requerida", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Recurso recurso = tableModel.getRecursoAt(row);
        try {
            sistema.asociarRecursoOrThrow(evento, recurso);
            loadResources();
            JOptionPane.showMessageDialog(this, 
                " Recurso asociado correctamente al evento.", 
                "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (RecursoOcupadoException ex) {
            JOptionPane.showMessageDialog(this, 
                " " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void quitarRecurso() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, 
                " Seleccione un recurso asociado al evento.", 
                "Selección Requerida", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Recurso recurso = tableModel.getRecursoAt(row);
        if (sistema.desasociarRecurso(evento, recurso.getId())) {
            loadResources();
            JOptionPane.showMessageDialog(this, 
                " Recurso desasociado correctamente del evento.", 
                "Éxito", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void limpiarFormulario() {
        txtId.setText("");
        txtNombre.setText("");
        cmbTipoEquipo.setSelectedIndex(0);
        chkDisponible.setSelected(true);
        lblErrorId.setText(" ");
        txtId.requestFocus();
    }

    private static String safe(String s) { 
        return s == null ? "" : s.trim(); 
    }

    class ResourceTableModel extends AbstractTableModel {
        private final String[] COLUMNS = {"ID", "Nombre", "Tipo", "Estado", "Asociado"};
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
            if (r instanceof Equipo) {
                Equipo eq = (Equipo) r;
                switch (column) {
                    case 0: return eq.getId();
                    case 1: return eq.getNombre();
                    case 2: return eq.getTipoEquipo();
                    case 3: 
                        return sistema.isRecursoDisponible(r, evento.getFecha(), evento.getHora()) ? 
                               " Disponible" : " Ocupado";
                    case 4:
                        return evento.getRecursos().contains(r) ? " Sí" : " No";
                    default: return "";
                }
            }
            return "";
        }

        public Recurso getRecursoAt(int row) { return recursos.get(row); }
    }
}

