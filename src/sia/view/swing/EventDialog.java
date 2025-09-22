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
 * Dialogo de alta/edición de Evento centrado en clicks.
 * Reemplaza entradas de texto libres por componentes gráficos:
 *  - Tipo: JComboBox editable poblado con tipos ya usados.
 *  - Fecha: JSpinner(Date).
 *  - Hora: JSpinner(SpinnerDateModel) con editor HH:mm.
 *  - Sala: JComboBox con las salas registradas (editable).
 *  - Capacidad: JSpinner(num), pre-rellena con capacidad de la Sala si existe.
 *
 *  // NEW (UI): requiere SistemaEventos para poblar combos de catálogo.
 */
public class EventDialog extends JDialog {

    // Entrada "libre" solo para ID y Nombre (validado).
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
        setMinimumSize(new Dimension(520, 380));
        getRootPane().setDefaultButton(btnOk);
    }

    private void initUI(Evento base) {
        JPanel main = new JPanel(new BorderLayout(10,10));
        main.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4,4,4,4);
        gc.fill = GridBagConstraints.HORIZONTAL;

        tfId = new JTextField(12);
        tfNombre = new JTextField(24);

        // --- Tipo: eventos existentes ---
        List<String> tipos = sistema.getEventos().stream()
                .map(Evento::getTipo)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
        
        // Combinar tipos predefinidos con los existentes
        Set<String> allTypes = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        allTypes.addAll(Arrays.asList(PREDEFINED_TYPES));
        allTypes.addAll(tipos);
        
        cbTipo = new JComboBox<>(allTypes.toArray(new String[0]));
        cbTipo.setEditable(true);
        cbTipo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (index == -1 && value == null) {
                    label.setText("Seleccione tipo...");
                }
                return label;
            }
        });

        // --- Fecha ---
        spFecha = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
        JSpinner.DateEditor fe = new JSpinner.DateEditor(spFecha, "yyyy-MM-dd");
        spFecha.setEditor(fe);

        // --- Hora ---
        spHora = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.MINUTE));
        JSpinner.DateEditor he = new JSpinner.DateEditor(spHora, "HH:mm");
        spHora.setEditor(he);

        // --- Sala: recursos disponibles (solo Sala) ---
        List<String> salas = sistema.getRecursosDisponibles().stream()
                .filter(r -> r instanceof Sala)
                .map(Recurso::getNombre)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
        
        // Combinar salas predefinidas con las existentes
        Set<String> allRooms = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        allRooms.addAll(Arrays.asList(PREDEFINED_ROOMS));
        allRooms.addAll(salas);
        
        cbSala = new JComboBox<>(allRooms.toArray(new String[0]));
        cbSala.setEditable(true);
        cbSala.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (index == -1 && value == null) {
                    label.setText("Seleccione sala...");
                }
                return label;
            }
        });

        // --- Capacidad ---
        spCapacidad = new JSpinner(new SpinnerNumberModel(50, 1, 1_000_000, 1));

        // Sugerir capacidad desde la sala elegida
        cbSala.addActionListener(e -> suggestCapFromSala());

        // Valores iniciales si editamos
        if (base != null) {
            tfId.setText(nvl(base.getIdEvento()));
            tfId.setEditable(false);
            tfNombre.setText(nvl(base.getNombre()));
            setComboValue(cbTipo, base.getTipo());
            setComboValue(cbSala, base.getSala());
            try { spFecha.setValue(new SimpleDateFormat("yyyy-MM-dd").parse(nvl(base.getFecha()))); } catch (Exception ignore) {}
            try { spHora .setValue(new SimpleDateFormat("HH:mm").parse(nvl(base.getHora()))); }   catch (Exception ignore) {}
            spCapacidad.setValue(Math.max(1, base.getCapacidad()));
        } else {
            tfId.setText(suggestNextId()); // sugerencia editable
            tfNombre.setText("");
        }

        int row = 0;
        addRow(form, row++, "ID*:", tfId); 
        addRow(form, row++, "Nombre*:", tfNombre);
        addRow(form, row++, "Tipo:", cbTipo);
        addRow(form, row++, "Fecha*:", spFecha);
        addRow(form, row++, "Hora (HH:mm):", spHora);
        addRow(form, row++, "Sala:", cbSala);
        addRow(form, row++, "Capacidad*:", spCapacidad);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCancel = new JButton("Cancelar");
        btnOk = new JButton("Aceptar");
        btnCancel.addActionListener(e -> dispose());
        btnOk.addActionListener(e -> validateAndSave());
        buttons.add(btnCancel);
        buttons.add(btnOk);

        main.add(form, BorderLayout.CENTER);
        main.add(buttons, BorderLayout.SOUTH);
        setContentPane(main);
    }

    private void addRow(JPanel form, int row, String label, JComponent comp) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4,4,4,4);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridy = row;

        gc.gridx = 0; gc.weightx = 0;
        form.add(new JLabel(label), gc);
        gc.gridx = 1; gc.weightx = 1;
        form.add(comp, gc);
    }

    private void setComboValue(JComboBox<String> combo, String v) {
        if (v == null || v.isBlank()) return;
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

    private void validateAndSave() {
        try {
            if (!Validators.isValidId(tfId.getText()))
                throw new IllegalArgumentException("ID inválido o vacío (máx. 20 caracteres)");
            if (!Validators.isValidName(tfNombre.getText()))
                throw new IllegalArgumentException("Nombre inválido o vacío (máx. 100 caracteres)");

            String fecha = new SimpleDateFormat("yyyy-MM-dd").format((Date) spFecha.getValue());
            String hora  = new SimpleDateFormat("HH:mm").format((Date)  spHora.getValue());

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