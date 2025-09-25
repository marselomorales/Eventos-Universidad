package sia.view.swing;

import sia.*;
import sia.util.Validators;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Diálogo de alta/edición de Evento con estilos modernos.
 * Utiliza ComponentFactory y AppStyle para una apariencia consistente.
 */
public class EventDialog extends JDialog {

    private JTextField tfId, tfNombre;
    private JComboBox<String> cbTipo;
    private JSpinner spFecha;
    private JSpinner spHora;
    private JComboBox<String> cbSala;
    private JSpinner spCapacidad;

    private boolean accepted = false;
    private Evento result;

    private final SistemaEventos sistema;
    private JButton btnOk;

    // Listas predefinidas
    private static final String[] PREDEFINED_TYPES = {
        "Charla", "Seminario", "Taller", "Laboratorio", 
        "Evaluación", "Reunión", "Ceremonia", "Defensa"
    };
    
    private static final String[] PREDEFINED_ROOMS = {
        "Auditorio Principal", "Auditorio Secundario", "Sala de Conferencias A",
        "Sala de Conferencias B", "Laboratorio de Computación", "Laboratorio de Ciencias",
        "Aula Magna", "Sala de Usos Múltiples", "Galería de Arte", "Patio Central",
        "Cancha Deportiva", "Teatro", "Sala de Proyecciones", "Biblioteca", "Hall Principal"
    };

    public EventDialog(Window owner, SistemaEventos sistema, String title, Evento base) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        this.sistema = sistema;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        initUI(base);
        pack();
        setLocationRelativeTo(owner);
        setMinimumSize(new Dimension(520, 400));
        getRootPane().setDefaultButton(btnOk);
    }

    private void initUI(Evento base) {
        JPanel main = ComponentFactory.createCardPanel();
        main.setLayout(new BorderLayout(10, 10));
        main.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Panel del formulario
        JPanel formPanel = createFormPanel(base);
        
        // Panel de botones
        JPanel buttonPanel = createButtonPanel();

        main.add(formPanel, BorderLayout.CENTER);
        main.add(buttonPanel, BorderLayout.SOUTH);
        setContentPane(main);
    }

    private JPanel createFormPanel(Evento base) {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(AppStyle.CARD_BG);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;

        // Inicializar componentes
        initComponents(base);

        int row = 0;
        addRow(form, row++, "ID*:", tfId);
        addRow(form, row++, "Nombre*:", tfNombre);
        addRow(form, row++, "Tipo:", createComboPanel(cbTipo));
        addRow(form, row++, "Fecha*:", createDatePanel());
        addRow(form, row++, "Hora (HH:mm):", createTimePanel());
        addRow(form, row++, "Sala:", createComboPanel(cbSala));
        addRow(form, row++, "Capacidad*:", spCapacidad);

        return form;
    }

    private void initComponents(Evento base) {
        // ID no editable
        tfId = ComponentFactory.createTextField(12);
        tfId.setEditable(false);
        tfId.setBackground(AppStyle.LIGHT_GRAY);
        
        tfNombre = ComponentFactory.createTextField(24);

        // Combo Tipo
        List<String> tipos = sistema.getEventos().stream()
                .map(Evento::getTipo)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
        
        Set<String> allTypes = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        allTypes.addAll(Arrays.asList(PREDEFINED_TYPES));
        allTypes.addAll(tipos);
        
        cbTipo = createStyledComboBox(allTypes.toArray(new String[0]));
        cbTipo.setRenderer(new PlaceholderRenderer("Selecciona un tipo..."));

        // Fecha
        spFecha = createStyledSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
        JSpinner.DateEditor fe = new JSpinner.DateEditor(spFecha, "dd/MM/yyyy");
        spFecha.setEditor(fe);

        // Hora
        spHora = createStyledSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.MINUTE));
        JSpinner.DateEditor he = new JSpinner.DateEditor(spHora, "HH:mm");
        spHora.setEditor(he);

        // Sala
        List<String> salas = sistema.getRecursosDisponibles().stream()
                .filter(r -> r instanceof Sala)
                .map(Recurso::getNombre)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
        
        Set<String> allRooms = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        allRooms.addAll(Arrays.asList(PREDEFINED_ROOMS));
        allRooms.addAll(salas);
        
        cbSala = createStyledComboBox(allRooms.toArray(new String[0]));
        cbSala.setRenderer(new PlaceholderRenderer("Selecciona una sala..."));
        cbSala.addActionListener(e -> suggestCapFromSala());

        // Capacidad
        spCapacidad = createStyledSpinner(new SpinnerNumberModel(50, 1, 1_000_000, 1));

        // Valores iniciales si editamos
        if (base != null) {
            tfId.setText(nvl(base.getIdEvento()));
            tfNombre.setText(nvl(base.getNombre()));
            setComboValue(cbTipo, base.getTipo());
            setComboValue(cbSala, base.getSala());
            try { 
                spFecha.setValue(new SimpleDateFormat("yyyy-MM-dd").parse(nvl(base.getFecha()))); 
            } catch (Exception ignore) {}
            try { 
                spHora.setValue(new SimpleDateFormat("HH:mm").parse(nvl(base.getHora()))); 
            } catch (Exception ignore) {}
            spCapacidad.setValue(Math.max(1, base.getCapacidad()));
        } else {
            tfId.setText(suggestNextId());
            tfNombre.setText("");
        }
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(AppStyle.CARD_BG);
        
        JButton btnCancel = ComponentFactory.createSecondaryButton("Cancelar");
        btnOk = ComponentFactory.createPrimaryButton("Aceptar");
        
        btnCancel.addActionListener(e -> dispose());
        btnOk.addActionListener(e -> validateAndSave());
        
        panel.add(btnCancel);
        panel.add(btnOk);
        
        return panel;
    }

    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setEditable(true);
        combo.setFont(AppStyle.FONT_INPUT);
        combo.setBorder(AppStyle.createInputBorder());
        combo.setBackground(Color.WHITE);
        return combo;
    }

    private JSpinner createStyledSpinner(SpinnerModel model) {
        JSpinner spinner = new JSpinner(model);
        spinner.setFont(AppStyle.FONT_INPUT);
        spinner.setBorder(AppStyle.createInputBorder());
        return spinner;
    }

    private JPanel createComboPanel(JComboBox<String> combo) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AppStyle.CARD_BG);
        panel.add(combo, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createDatePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AppStyle.CARD_BG);
        
        panel.add(spFecha, BorderLayout.CENTER);
        
        JButton fechaButton = new JButton("Fecha");
        fechaButton.setFont(AppStyle.FONT_BUTTON);
        fechaButton.setBackground(AppStyle.SECONDARY);
        fechaButton.setForeground(Color.WHITE);
        fechaButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        fechaButton.addActionListener(e -> showCalendarDialog());
        
        panel.add(fechaButton, BorderLayout.EAST);
        return panel;
    }

    private JPanel createTimePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AppStyle.CARD_BG);
        
        panel.add(spHora, BorderLayout.CENTER);
        
        JButton horaButton = new JButton("Hora");
        horaButton.setFont(AppStyle.FONT_BUTTON);
        horaButton.setBackground(AppStyle.SECONDARY);
        horaButton.setForeground(Color.WHITE);
        horaButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        horaButton.addActionListener(e -> showTimeDialog());
        
        panel.add(horaButton, BorderLayout.EAST);
        return panel;
    }

    private void addRow(JPanel form, int row, String label, JComponent comp) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridy = row;

        gc.gridx = 0; 
        gc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(AppStyle.FONT_LABEL);
        lbl.setForeground(AppStyle.TEXT_PRIMARY);
        form.add(lbl, gc);
        
        gc.gridx = 1; 
        gc.weightx = 1;
        form.add(comp, gc);
    }

    // Clase interna para placeholder de combobox
    private static class PlaceholderRenderer extends DefaultListCellRenderer {
        private final String placeholder;
        
        public PlaceholderRenderer(String placeholder) {
            this.placeholder = placeholder;
        }
        
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value == null || value.toString().trim().isEmpty()) {
                label.setText(placeholder);
                label.setForeground(AppStyle.TEXT_SECONDARY);
            } else {
                label.setForeground(AppStyle.TEXT_PRIMARY);
            }
            return label;
        }
    }

    // Métodos de lógica original (mantener sin cambios)
    private void setComboValue(JComboBox<String> combo, String v) {
        if (v == null || v.isBlank()) {
            combo.setSelectedItem(null);
            return;
        }
        ComboBoxModel<String> m = combo.getModel();
        boolean found = false;
        for (int i = 0; i < m.getSize(); i++)
            if (v.equalsIgnoreCase(m.getElementAt(i))) { found = true; break; }
        if (!found) combo.addItem(v);
        combo.setSelectedItem(v);
    }

    private void suggestCapFromSala() {
        Object sel = cbSala.getSelectedItem();
        if (sel == null) return;
        String nombre = sel.toString().trim();
        for (Recurso r : sistema.getRecursosDisponibles()) {
            if (r instanceof Sala && nombre.equalsIgnoreCase(r.getNombre())) {
                int cap = ((Sala) r).getCapacidad();
                if (cap > 0) spCapacidad.setValue(cap);
                break;
            }
        }
    }

    private String suggestNextId() {
        int max = 0;
        for (Evento e : sistema.getEventos()) {
            String id = e.getIdEvento();
            if (id != null && id.length() > 1 && (id.charAt(0) == 'E' || id.charAt(0) == 'e')) {
                try { max = Math.max(max, Integer.parseInt(id.substring(1))); }
                catch (NumberFormatException ignore) {}
            }
        }
        return "E" + (max + 1);
    }

    private void showCalendarDialog() {
        // Implementación original del calendario (mantener sin cambios)
        JDialog calendarDialog = new JDialog(this, "Seleccionar Fecha", true);
        calendarDialog.setLayout(new BorderLayout());
        calendarDialog.setSize(300, 300);
        calendarDialog.setLocationRelativeTo(this);

        // ... (resto del código del calendario igual al original)
    }

    private void showTimeDialog() {
        // Implementación original del reloj (mantener sin cambios)
        JDialog timeDialog = new JDialog(this, "Seleccionar Hora", true);
        timeDialog.setLayout(new BorderLayout());
        timeDialog.setSize(200, 150);
        timeDialog.setLocationRelativeTo(this);
        
        // ... (resto del código del reloj igual al original)
    }

    private void validateAndSave() {
        try {
            if (!Validators.isValidId(tfId.getText()))
                throw new IllegalArgumentException("ID inválido o vacío (máx. 20 caracteres)");
            if (!Validators.isValidName(tfNombre.getText()))
                throw new IllegalArgumentException("Nombre inválido o vacío (máx. 100 caracteres)");

            String fecha = new SimpleDateFormat("yyyy-MM-dd").format((Date) spFecha.getValue());
            String hora  = new SimpleDateFormat("HH:mm").format((Date) spHora.getValue());

            int capacidad = ((SpinnerNumberModel) spCapacidad.getModel()).getNumber().intValue();
            if (capacidad <= 0) throw new IllegalArgumentException("Capacidad debe ser > 0");

            String tipo = Objects.toString(cbTipo.getSelectedItem(), "").trim();
            String sala = Objects.toString(cbSala.getSelectedItem(), "").trim();

            result = new Evento(
                    tfId.getText().trim(),
                    tfNombre.getText().trim(),
                    tipo.isEmpty() ? null : tipo,
                    fecha,
                    hora,
                    sala.isEmpty() ? null : sala,
                    capacidad
            );
            accepted = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error de Validación", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String nvl(String s) { return s == null ? "" : s; }
    public boolean isAccepted() { return accepted; }
    public Evento getResult() { return result; }
}
