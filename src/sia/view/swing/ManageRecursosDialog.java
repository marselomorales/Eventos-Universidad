package sia.view.swing;

import sia.*;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public class ManageRecursosDialog extends JDialog {

    private final SistemaEventos sistema;

    private DefaultListModel<Recurso> modelo;
    private JList<Recurso> lst;

    // Formulario alta
    private JComboBox<String> cmbTipo;
    private JTextField txtId;
    private JTextField txtNombre;
    // Sala
    private JSpinner spCapacidad;
    private JTextField txtUbicacion;
    // Equipo
    private JComboBox<String> cmbTipoEquipo;
    private JCheckBox chkDisponible;

    // Para manejar el CardLayout
    private JPanel cardPanel;
    private CardLayout cardLayout;

    // Listas predefinidas
    private static final String[] EQUIPMENT_TYPES = {
        "Proyector", "Micrófono", "Sistema de Sonido", "Computadora",
        "Pantalla", "Pizarra Interactiva", "Cámara", "Trípode",
        "Iluminación", "Mesa de Mezclas", "Router Wi-Fi", "Impresora",
        "Escáner", "Tableta Gráfica", "Equipo de Video conferencia"
    };

    public ManageRecursosDialog(Frame owner, SistemaEventos sistema) {
        super(owner, "Recursos disponibles del sistema", true);
        this.sistema = sistema;
        initUI();
    }

    private void initUI() {
        setMinimumSize(new Dimension(800, 520)); // Tamaño aumentado
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout(10, 10));

        // Crear un panel con pestañas para Salas y Equipos
        JTabbedPane tabbedPane = new JTabbedPane();

        // Panel para Salas
        JPanel salaPanel = new JPanel(new BorderLayout());
        DefaultListModel<Recurso> modelSalas = new DefaultListModel<>();
        JList<Recurso> listSalas = new JList<>(modelSalas);
        JScrollPane scrollSalas = new JScrollPane(listSalas);
        scrollSalas.setBorder(BorderFactory.createTitledBorder("Salas Disponibles"));
        salaPanel.add(scrollSalas, BorderLayout.CENTER);

        // Panel para Equipos
        JPanel equipoPanel = new JPanel(new BorderLayout());
        DefaultListModel<Recurso> modelEquipos = new DefaultListModel<>();
        JList<Recurso> listEquipos = new JList<>(modelEquipos);
        JScrollPane scrollEquipos = new JScrollPane(listEquipos);
        scrollEquipos.setBorder(BorderFactory.createTitledBorder("Equipos Disponibles"));
        equipoPanel.add(scrollEquipos, BorderLayout.CENTER);

        // Agregar pestañas
        tabbedPane.addTab("Salas", salaPanel);
        tabbedPane.addTab("Equipos", equipoPanel);

        // Cargar datos en las listas
        for (Recurso r : sistema.getRecursosDisponibles()) {
            if (r instanceof Sala) {
                modelSalas.addElement(r);
            } else if (r instanceof Equipo) {
                modelEquipos.addElement(r);
            }
        }

        // Formulario para agregar recursos (ya existente)
        JPanel form = buildForm();

        // Botones
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnEliminar = new JButton("Eliminar seleccionado");
        JButton btnCerrar = new JButton("Cerrar");
        south.add(btnEliminar);
        south.add(btnCerrar);

        btnEliminar.addActionListener(e -> onDelete());
        btnCerrar.addActionListener(e -> setVisible(false));

        add(tabbedPane, BorderLayout.CENTER);
        add(form, BorderLayout.EAST);
        add(south, BorderLayout.SOUTH);
        pack();
    }

    private JPanel buildForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Agregar recurso"));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4,8,4,8);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        int y = 0;

        // Tipo
        gc.gridx=0; gc.gridy=y; gc.weightx=0; panel.add(new JLabel("Tipo:"), gc);
        cmbTipo = new JComboBox<>(new String[]{"Sala","Equipo"});
        cmbTipo.addActionListener(e -> updateFormFields());
        gc.gridx=1; gc.gridy=y++; gc.weightx=1; panel.add(cmbTipo, gc);

        // ID
        gc.gridx=0; gc.gridy=y; panel.add(new JLabel("ID:"), gc);
        txtId = new JTextField();
        gc.gridx=1; gc.gridy=y++; panel.add(txtId, gc);

        // Nombre
        gc.gridx=0; gc.gridy=y; panel.add(new JLabel("Nombre:"), gc);
        txtNombre = new JTextField();
        gc.gridx=1; gc.gridy=y++; panel.add(txtNombre, gc);

        // Panel Sala
        JPanel pnlSala = new JPanel(new GridBagLayout());
        GridBagConstraints gs = new GridBagConstraints();
        gs.insets = new Insets(2,6,2,6);
        gs.fill = GridBagConstraints.HORIZONTAL;
        gs.weightx = 1.0;
        int ys = 0;

        gs.gridx=0; gs.gridy=ys; pnlSala.add(new JLabel("Capacidad:"), gs);
        spCapacidad = new JSpinner(new SpinnerNumberModel(0, 0, 100000, 1));
        gs.gridx=1; gs.gridy=ys++; pnlSala.add(spCapacidad, gs);

        gs.gridx=0; gs.gridy=ys; pnlSala.add(new JLabel("Ubicación:"), gs);
        txtUbicacion = new JTextField();
        gs.gridx=1; gs.gridy=ys++; pnlSala.add(txtUbicacion, gs);

        // Panel Equipo
        JPanel pnlEquipo = new JPanel(new GridBagLayout());
        GridBagConstraints ge = new GridBagConstraints();
        ge.insets = new Insets(2,6,2,6);
        ge.fill = GridBagConstraints.HORIZONTAL;
        ge.weightx = 1.0;
        int ye = 0;

        ge.gridx=0; ge.gridy=ye; pnlEquipo.add(new JLabel("Tipo de equipo:"), ge);
        
        // Usar combobox predefinido en lugar de textfield
        Set<String> allEquipmentTypes = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        allEquipmentTypes.addAll(Arrays.asList(EQUIPMENT_TYPES));
        
        cmbTipoEquipo = new JComboBox<>(allEquipmentTypes.toArray(new String[0]));
        cmbTipoEquipo.setEditable(true);
        cmbTipoEquipo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (index == -1 && value == null) {
                    label.setText("Seleccione tipo...");
                }
                return label;
            }
        });
        
        ge.gridx=1; ge.gridy=ye++; pnlEquipo.add(cmbTipoEquipo, ge);

        ge.gridx=0; ge.gridy=ye; pnlEquipo.add(new JLabel("Disponible:"), ge);
        chkDisponible = new JCheckBox();
        chkDisponible.setSelected(true);
        ge.gridx=1; ge.gridy=ye++; pnlEquipo.add(chkDisponible, ge);

        // CardLayout según tipo
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.add(pnlSala, "Sala");
        cardPanel.add(pnlEquipo, "Equipo");

        // Botón agregar
        JButton btnAgregar = new JButton("Agregar recurso");
        btnAgregar.addActionListener(e -> onAdd());

        // Layout del form
        gc.gridx=0; gc.gridy=y; gc.gridwidth=2; panel.add(cardPanel, gc); y++;
        gc.gridx=0; gc.gridy=y; gc.gridwidth=2; panel.add(btnAgregar, gc); y++;

        // Mostrar panel inicial según selección
        updateFormFields();

        return panel;
    }

    private void updateFormFields() {
        cardLayout.show(cardPanel, (String) cmbTipo.getSelectedItem());
    }

    private void onAdd() {
        String tipo = (String) cmbTipo.getSelectedItem();
        String id = safe(txtId.getText());
        String nombre = safe(txtNombre.getText());
        if (id.isEmpty() || nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "ID y Nombre son obligatorios.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Evitar duplicados por ID en catálogo
        for (Recurso r : sistema.getRecursosDisponibles()) {
            if (Objects.equals(r.getId(), id)) {
                JOptionPane.showMessageDialog(this, "Ya existe un recurso con ese ID.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }

        Recurso rec;
        if ("Sala".equalsIgnoreCase(tipo)) {
            int cap = (Integer) spCapacidad.getValue();
            String ubic = safe(txtUbicacion.getText());
            rec = new Sala(id, nombre, cap, ubic);
        } else {
            String teq = safe((String) cmbTipoEquipo.getSelectedItem());
            boolean disp = chkDisponible.isSelected();
            rec = new Equipo(id, nombre, teq, disp);
        }

        sistema.getRecursosDisponibles().add(rec);
        modelo.addElement(rec);

        // limpiar form
        txtId.setText("");
        txtNombre.setText("");
        spCapacidad.setValue(0);
        txtUbicacion.setText("");
        cmbTipoEquipo.setSelectedIndex(0);
        chkDisponible.setSelected(true);
        txtId.requestFocus();
    }

    private void onDelete() {
        Recurso sel = lst.getSelectedValue();
        if (sel == null) return;

        // Quitar del catálogo del sistema
        sistema.getRecursosDisponibles().removeIf(r -> Objects.equals(r.getId(), sel.getId()));
        // Quitar de la UI
        modelo.removeElement(sel);

        // Nota: si el recurso estaba asociado a algún evento, esa asociación se mantiene hasta
        // que el usuario la quite desde la gestión del evento (tu flujo actual).
    }

    private static String safe(String s) { return s == null ? "" : s.trim(); }
}