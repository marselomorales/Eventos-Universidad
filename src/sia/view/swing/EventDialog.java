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
 *  - Tipo: JComboBox editable poblado con tipos ya usados + placeholder.
 *  - Fecha: JSpinner(Date) con aspecto de calendario.
 *  - Hora: JSpinner(SpinnerDateModel) con editor HH:mm y aspecto de reloj.
 *  - Sala: JComboBox con las salas registradas (editable) + placeholder.
 *  - Capacidad: JSpinner(num), pre-rellena con capacidad de la Sala si existe.
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
        JPanel main = new JPanel(new BorderLayout(10,10));
        main.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
        main.setBackground(new Color(245, 245, 245));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(new Color(245, 245, 245));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6,6,6,6);
        gc.fill = GridBagConstraints.HORIZONTAL;

        // Configuración estética de componentes
        Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 13);
        
        // ID no editable
        tfId = new JTextField(12);
        tfId.setFont(fieldFont);
        tfId.setEditable(false); // ID no editable
        tfId.setBackground(new Color(240, 240, 240));
        
        tfNombre = new JTextField(24);
        tfNombre.setFont(fieldFont);

        // --- Tipo: eventos existentes con placeholder ---
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
        
        cbTipo = new JComboBox<>(allTypes.toArray(new String[0]));
        cbTipo.setEditable(true);
        cbTipo.setFont(fieldFont);
        // Placeholder para tipo
        cbTipo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null || value.toString().trim().isEmpty()) {
                    label.setText("Selecciona un tipo...");
                    label.setForeground(Color.GRAY);
                } else {
                    label.setForeground(Color.BLACK);
                }
                return label;
            }
        });
        cbTipo.setSelectedItem(null);

        // --- Fecha: Calendario mejorado ---
        spFecha = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
        JSpinner.DateEditor fe = new JSpinner.DateEditor(spFecha, "dd/MM/yyyy");
        spFecha.setEditor(fe);
        spFecha.setFont(fieldFont);
        
        // Botón de calendario mejorado
        JButton fechaButton = new JButton("📅");
        fechaButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        fechaButton.setMargin(new Insets(2, 6, 2, 6));
        fechaButton.addActionListener(e -> showCalendarDialog());
        
        JPanel fechaPanel = new JPanel(new BorderLayout());
        fechaPanel.add(spFecha, BorderLayout.CENTER);
        fechaPanel.add(fechaButton, BorderLayout.EAST);

        // --- Hora: Reloj mejorado ---
        spHora = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.MINUTE));
        JSpinner.DateEditor he = new JSpinner.DateEditor(spHora, "HH:mm");
        spHora.setEditor(he);
        spHora.setFont(fieldFont);
        
        // Botón de reloj mejorado
        JButton horaButton = new JButton("🕒");
        horaButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        horaButton.setMargin(new Insets(2, 6, 2, 6));
        horaButton.addActionListener(e -> showTimeDialog());
        
        JPanel horaPanel = new JPanel(new BorderLayout());
        horaPanel.add(spHora, BorderLayout.CENTER);
        horaPanel.add(horaButton, BorderLayout.EAST);

        // --- Sala: recursos disponibles con placeholder ---
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
        
        cbSala = new JComboBox<>(allRooms.toArray(new String[0]));
        cbSala.setEditable(true);
        cbSala.setFont(fieldFont);
        // Placeholder para sala
        cbSala.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null || value.toString().trim().isEmpty()) {
                    label.setText("Selecciona una sala...");
                    label.setForeground(Color.GRAY);
                } else {
                    label.setForeground(Color.BLACK);
                }
                return label;
            }
        });
        cbSala.setSelectedItem(null);

        // --- Capacidad ---
        spCapacidad = new JSpinner(new SpinnerNumberModel(50, 1, 1_000_000, 1));
        spCapacidad.setFont(fieldFont);

        // Sugerir capacidad desde la sala elegida
        cbSala.addActionListener(e -> suggestCapFromSala());

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

        int row = 0;
        addRow(form, labelFont, row++, "ID*:", tfId); 
        addRow(form, labelFont, row++, "Nombre*:", tfNombre);
        addRow(form, labelFont, row++, "Tipo:", cbTipo);
        addRow(form, labelFont, row++, "Fecha*:", fechaPanel);
        addRow(form, labelFont, row++, "Hora (HH:mm):", horaPanel);
        addRow(form, labelFont, row++, "Sala:", cbSala);
        addRow(form, labelFont, row++, "Capacidad*:", spCapacidad);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setBackground(new Color(245, 245, 245));
        
        JButton btnCancel = new JButton("Cancelar");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCancel.setBackground(new Color(220, 220, 220));
        
        btnOk = new JButton("Aceptar");
        btnOk.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnOk.setBackground(new Color(70, 130, 180));
        btnOk.setForeground(Color.WHITE);
        
        btnCancel.addActionListener(e -> dispose());
        btnOk.addActionListener(e -> validateAndSave());
        
        buttons.add(btnCancel);
        buttons.add(btnOk);

        main.add(form, BorderLayout.CENTER);
        main.add(buttons, BorderLayout.SOUTH);
        setContentPane(main);
    }

    private void addRow(JPanel form, Font labelFont, int row, String label, JComponent comp) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6,6,6,6);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridy = row;

        gc.gridx = 0; 
        gc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(labelFont);
        form.add(lbl, gc);
        
        gc.gridx = 1; 
        gc.weightx = 1;
        form.add(comp, gc);
    }

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
        // Diálogo de calendario mejorado
        JDialog calendarDialog = new JDialog(this, "Seleccionar Fecha", true);
        calendarDialog.setLayout(new BorderLayout());
        calendarDialog.setSize(300, 300);
        calendarDialog.setLocationRelativeTo(this);

        JPanel calendarPanel = new JPanel(new BorderLayout());
        
        // Panel de control (mes y año)
        JPanel controlPanel = new JPanel(new FlowLayout());
        JButton prevMonth = new JButton("←");
        JButton nextMonth = new JButton("→");
        JLabel monthLabel = new JLabel("", JLabel.CENTER);
        JButton prevYear = new JButton("←←");
        JButton nextYear = new JButton("→→");
        JLabel yearLabel = new JLabel("", JLabel.CENTER);
        
        controlPanel.add(prevYear);
        controlPanel.add(prevMonth);
        controlPanel.add(monthLabel);
        controlPanel.add(yearLabel);
        controlPanel.add(nextMonth);
        controlPanel.add(nextYear);
        
        // Panel de días de la semana
        JPanel weekDaysPanel = new JPanel(new GridLayout(1, 7));
        String[] days = {"Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb"};
        for (String day : days) {
            JLabel dayLabel = new JLabel(day, JLabel.CENTER);
            dayLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            weekDaysPanel.add(dayLabel);
        }
        
        // Panel de días del mes
        JPanel daysPanel = new JPanel(new GridLayout(0, 7));
        
        calendarPanel.add(controlPanel, BorderLayout.NORTH);
        calendarPanel.add(weekDaysPanel, BorderLayout.CENTER);
        calendarPanel.add(daysPanel, BorderLayout.SOUTH);
        
        // Inicializar calendario
        Calendar cal = Calendar.getInstance();
        cal.setTime((Date) spFecha.getValue());
        updateCalendarDisplay(cal, monthLabel, yearLabel, daysPanel, calendarDialog);
        
        // Listeners para navegación
        prevMonth.addActionListener(e -> {
            cal.add(Calendar.MONTH, -1);
            updateCalendarDisplay(cal, monthLabel, yearLabel, daysPanel, calendarDialog);
        });
        
        nextMonth.addActionListener(e -> {
            cal.add(Calendar.MONTH, 1);
            updateCalendarDisplay(cal, monthLabel, yearLabel, daysPanel, calendarDialog);
        });
        
        prevYear.addActionListener(e -> {
            cal.add(Calendar.YEAR, -1);
            updateCalendarDisplay(cal, monthLabel, yearLabel, daysPanel, calendarDialog);
        });
        
        nextYear.addActionListener(e -> {
            cal.add(Calendar.YEAR, 1);
            updateCalendarDisplay(cal, monthLabel, yearLabel, daysPanel, calendarDialog);
        });
        
        JButton selectButton = new JButton("Seleccionar");
        selectButton.addActionListener(e -> {
            spFecha.setValue(cal.getTime());
            calendarDialog.dispose();
        });
        
        calendarDialog.add(calendarPanel, BorderLayout.CENTER);
        calendarDialog.add(selectButton, BorderLayout.SOUTH);
        calendarDialog.setVisible(true);
    }

    private void updateCalendarDisplay(Calendar cal, JLabel monthLabel, JLabel yearLabel, 
                                     JPanel daysPanel, JDialog dialog) {
        // Actualizar labels de mes y año
        String[] months = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", 
                          "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        monthLabel.setText(months[cal.get(Calendar.MONTH)]);
        yearLabel.setText(String.valueOf(cal.get(Calendar.YEAR)));
        
        // Limpiar panel de días
        daysPanel.removeAll();
        
        // Obtener el primer día del mes y la cantidad de días
        Calendar tempCal = (Calendar) cal.clone();
        tempCal.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK);
        int daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH);
        
        // Días vacíos al inicio
        for (int i = 1; i < firstDayOfWeek; i++) {
            daysPanel.add(new JLabel(""));
        }
        
        // Días del mes
        for (int day = 1; day <= daysInMonth; day++) {
            JButton dayButton = new JButton(String.valueOf(day));
            dayButton.setMargin(new Insets(2, 2, 2, 2));
            final int selectedDay = day;
            dayButton.addActionListener(e -> {
                cal.set(Calendar.DAY_OF_MONTH, selectedDay);
                spFecha.setValue(cal.getTime());
                dialog.dispose();
            });
            daysPanel.add(dayButton);
        }
        
        daysPanel.revalidate();
        daysPanel.repaint();
    }

    private void showTimeDialog() {
        // Diálogo de hora mejorado
        JDialog timeDialog = new JDialog(this, "Seleccionar Hora", true);
        timeDialog.setLayout(new BorderLayout());
        timeDialog.setSize(200, 150);
        timeDialog.setLocationRelativeTo(this);
        
        JPanel timePanel = new JPanel(new GridLayout(3, 1));
        
        // Selector de hora
        JPanel hourPanel = new JPanel(new FlowLayout());
        hourPanel.add(new JLabel("Hora:"));
        JSpinner hourSpinner = new JSpinner(new SpinnerNumberModel(12, 0, 23, 1));
        hourPanel.add(hourSpinner);
        
        // Selector de minutos
        JPanel minutePanel = new JPanel(new FlowLayout());
        minutePanel.add(new JLabel("Minutos:"));
        JSpinner minuteSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));
        minutePanel.add(minuteSpinner);
        
        timePanel.add(hourPanel);
        timePanel.add(minutePanel);
        
        // Establecer valores actuales
        Calendar cal = Calendar.getInstance();
        cal.setTime((Date) spHora.getValue());
        hourSpinner.setValue(cal.get(Calendar.HOUR_OF_DAY));
        minuteSpinner.setValue(cal.get(Calendar.MINUTE));
        
        JButton selectButton = new JButton("Seleccionar");
        selectButton.addActionListener(e -> {
            cal.set(Calendar.HOUR_OF_DAY, (Integer) hourSpinner.getValue());
            cal.set(Calendar.MINUTE, (Integer) minuteSpinner.getValue());
            spHora.setValue(cal.getTime());
            timeDialog.dispose();
        });
        
        timeDialog.add(timePanel, BorderLayout.CENTER);
        timeDialog.add(selectButton, BorderLayout.SOUTH);
        timeDialog.setVisible(true);
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