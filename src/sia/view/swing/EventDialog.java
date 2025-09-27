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

    private JTextField tfId, tfNombre, tfFecha;
    private JComboBox<String> cbTipo;
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
        
        // Campo de fecha
        tfFecha = ComponentFactory.createTextField(10);
        tfFecha.setEditable(false);

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
                tfFecha.setText(new SimpleDateFormat("dd/MM/yyyy").format(
                    new SimpleDateFormat("yyyy-MM-dd").parse(nvl(base.getFecha()))));
            } catch (Exception ignore) {
                tfFecha.setText(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
            }
            try { 
                spHora.setValue(new SimpleDateFormat("HH:mm").parse(nvl(base.getHora()))); 
            } catch (Exception ignore) {}
            spCapacidad.setValue(Math.max(1, base.getCapacidad()));
        } else {
            tfId.setText(suggestNextId());
            tfNombre.setText("");
            tfFecha.setText(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
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
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBackground(AppStyle.CARD_BG);
        
        panel.add(tfFecha, BorderLayout.CENTER);
        
        JButton fechaButton = new JButton("📅");
        fechaButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        fechaButton.setBackground(AppStyle.SECONDARY);
        fechaButton.setForeground(Color.WHITE);
        fechaButton.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        fechaButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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
        JDialog calendarDialog = new JDialog(this, "Seleccionar Fecha", true);
        calendarDialog.setLayout(new BorderLayout(10, 10));
        calendarDialog.setSize(350, 400);
        calendarDialog.setLocationRelativeTo(this);
        calendarDialog.getContentPane().setBackground(AppStyle.CARD_BG);

        // Panel principal del calendario
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(AppStyle.CARD_BG);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Panel de navegación (mes y año)
        JPanel navPanel = new JPanel(new BorderLayout());
        navPanel.setBackground(AppStyle.CARD_BG);
        
        JButton prevYearBtn = createNavButton("<<");
        JButton prevMonthBtn = createNavButton("<");
        JButton nextMonthBtn = createNavButton(">");
        JButton nextYearBtn = createNavButton(">>");
        
        JLabel monthYearLabel = new JLabel("", SwingConstants.CENTER);
        monthYearLabel.setFont(AppStyle.FONT_SUBTITLE);
        monthYearLabel.setForeground(AppStyle.TEXT_PRIMARY);
        
        JPanel centerNav = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        centerNav.setBackground(AppStyle.CARD_BG);
        centerNav.add(prevMonthBtn);
        centerNav.add(monthYearLabel);
        centerNav.add(nextMonthBtn);
        
        JPanel extremeNav = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        extremeNav.setBackground(AppStyle.CARD_BG);
        extremeNav.add(prevYearBtn);
        extremeNav.add(nextYearBtn);
        
        navPanel.add(centerNav, BorderLayout.CENTER);
        navPanel.add(extremeNav, BorderLayout.EAST);

        // Panel de días de la semana
        JPanel daysPanel = new JPanel(new GridLayout(1, 7, 5, 5));
        daysPanel.setBackground(AppStyle.CARD_BG);
        String[] days = {"Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb"};
        for (String day : days) {
            JLabel lbl = new JLabel(day, SwingConstants.CENTER);
            lbl.setFont(AppStyle.FONT_SMALL);
            lbl.setForeground(AppStyle.TEXT_SECONDARY);
            daysPanel.add(lbl);
        }

        // Panel de calendario (días del mes)
        JPanel calendarPanel = new JPanel(new GridLayout(0, 7, 5, 5));
        calendarPanel.setBackground(AppStyle.CARD_BG);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(AppStyle.CARD_BG);
        
        JButton todayBtn = ComponentFactory.createSecondaryButton("Hoy");
        JButton cancelBtn = ComponentFactory.createSecondaryButton("Cancelar");
        JButton okBtn = ComponentFactory.createPrimaryButton("Aceptar");
        
        buttonPanel.add(todayBtn);
        buttonPanel.add(cancelBtn);
        buttonPanel.add(okBtn);

        mainPanel.add(navPanel, BorderLayout.NORTH);
        mainPanel.add(daysPanel, BorderLayout.CENTER);
        mainPanel.add(new JScrollPane(calendarPanel), BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        calendarDialog.add(mainPanel);

        // Lógica del calendario
        Calendar calendar = Calendar.getInstance();
        try {
            if (!tfFecha.getText().isEmpty()) {
                Date currentDate = new SimpleDateFormat("dd/MM/yyyy").parse(tfFecha.getText());
                calendar.setTime(currentDate);
            }
        } catch (Exception ex) {
            calendar.setTime(new Date());
        }

        updateCalendar(calendar, calendarPanel, monthYearLabel);

        // Listeners de los botones de navegación
        prevYearBtn.addActionListener(e -> {
            calendar.add(Calendar.YEAR, -1);
            updateCalendar(calendar, calendarPanel, monthYearLabel);
        });

        nextYearBtn.addActionListener(e -> {
            calendar.add(Calendar.YEAR, 1);
            updateCalendar(calendar, calendarPanel, monthYearLabel);
        });

        prevMonthBtn.addActionListener(e -> {
            calendar.add(Calendar.MONTH, -1);
            updateCalendar(calendar, calendarPanel, monthYearLabel);
        });

        nextMonthBtn.addActionListener(e -> {
            calendar.add(Calendar.MONTH, 1);
            updateCalendar(calendar, calendarPanel, monthYearLabel);
        });

        todayBtn.addActionListener(e -> {
            calendar.setTime(new Date());
            updateCalendar(calendar, calendarPanel, monthYearLabel);
        });

        cancelBtn.addActionListener(e -> calendarDialog.dispose());

        okBtn.addActionListener(e -> {
            calendarDialog.dispose();
        });

        calendarDialog.setVisible(true);
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(AppStyle.FONT_BUTTON);
        btn.setBackground(AppStyle.SECONDARY);
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void updateCalendar(Calendar calendar, JPanel calendarPanel, JLabel monthYearLabel) {
        calendarPanel.removeAll();
        
        // Establecer el primer día del mes
        Calendar tempCalendar = (Calendar) calendar.clone();
        tempCalendar.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = tempCalendar.get(Calendar.DAY_OF_WEEK);
        int daysInMonth = tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        
        // Ajustar para que la semana comience en domingo
        firstDayOfWeek = firstDayOfWeek - 1;
        if (firstDayOfWeek == 0) firstDayOfWeek = 7;
        
        // Espacios en blanco para los días antes del primer día del mes
        for (int i = 1; i < firstDayOfWeek; i++) {
            calendarPanel.add(new JLabel(""));
        }
        
        // Botones para cada día del mes
        for (int day = 1; day <= daysInMonth; day++) {
            JButton dayBtn = new JButton(String.valueOf(day));
            dayBtn.setFont(AppStyle.FONT_INPUT);
            dayBtn.setBackground(Color.WHITE);
            dayBtn.setBorder(BorderFactory.createLineBorder(AppStyle.BORDER));
            dayBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            
            final int selectedDay = day;
            dayBtn.addActionListener(e -> {
                calendar.set(Calendar.DAY_OF_MONTH, selectedDay);
                tfFecha.setText(new SimpleDateFormat("dd/MM/yyyy").format(calendar.getTime()));
                ((Window) dayBtn.getTopLevelAncestor()).dispose();
            });
            
            // Resaltar el día actual
            Calendar today = Calendar.getInstance();
            if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                day == today.get(Calendar.DAY_OF_MONTH)) {
                dayBtn.setBackground(AppStyle.PRIMARY);
                dayBtn.setForeground(Color.WHITE);
            }
            
            calendarPanel.add(dayBtn);
        }
        
        // Actualizar etiqueta de mes y año
        String monthYear = new SimpleDateFormat("MMMM yyyy").format(calendar.getTime());
        monthYearLabel.setText(monthYear.substring(0, 1).toUpperCase() + monthYear.substring(1));
        
        calendarPanel.revalidate();
        calendarPanel.repaint();
    }

    private void showTimeDialog() {
    JDialog timeDialog = new JDialog(this, "Seleccionar Hora y Minutos", true);
    timeDialog.setLayout(new BorderLayout(10, 10));
    timeDialog.setSize(300, 300);
    timeDialog.setLocationRelativeTo(this);
    timeDialog.getContentPane().setBackground(AppStyle.CARD_BG);

    JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
    mainPanel.setBackground(AppStyle.CARD_BG);
    mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

    // Panel principal para horas y minutos
    JPanel timeSelectionPanel = new JPanel(new GridLayout(2, 1, 5, 10));
    timeSelectionPanel.setBackground(AppStyle.CARD_BG);

    // Panel para horas
    JPanel hourPanel = new JPanel(new GridLayout(0, 6, 5, 5)); // 6 columnas
    hourPanel.setBackground(AppStyle.CARD_BG);
    hourPanel.setBorder(BorderFactory.createTitledBorder("Horas"));
    
    // Panel para minutos
    JPanel minutePanel = new JPanel(new GridLayout(0, 6, 5, 5)); // 6 columnas
    minutePanel.setBackground(AppStyle.CARD_BG);
    minutePanel.setBorder(BorderFactory.createTitledBorder("Minutos"));

    // Botones para horas (00-23)
    for (int hour = 0; hour < 24; hour++) {
        JButton hourBtn = createTimeButton(String.format("%02d", hour));
        hourBtn.addActionListener(e -> {
            // Resetear estilo de todos los botones de hora
            for (Component comp : hourPanel.getComponents()) {
                if (comp instanceof JButton) {
                    comp.setBackground(Color.WHITE);
                    comp.setForeground(Color.BLACK);
                }
            }
            // Resaltar botón seleccionado
            hourBtn.setBackground(AppStyle.PRIMARY);
            hourBtn.setForeground(Color.WHITE);
        });
        hourPanel.add(hourBtn);
    }

    // Botones para minutos (00-55, en incrementos de 5)
    for (int minute = 0; minute < 60; minute += 5) {
        JButton minuteBtn = createTimeButton(String.format("%02d", minute));
        minuteBtn.addActionListener(e -> {
            // Resetear estilo de todos los botones de minuto
            for (Component comp : minutePanel.getComponents()) {
                if (comp instanceof JButton) {
                    comp.setBackground(Color.WHITE);
                    comp.setForeground(Color.BLACK);
                }
            }
            // Resaltar botón seleccionado
            minuteBtn.setBackground(AppStyle.PRIMARY);
            minuteBtn.setForeground(Color.WHITE);
        });
        minutePanel.add(minuteBtn);
    }

    timeSelectionPanel.add(hourPanel);
    timeSelectionPanel.add(minutePanel);

    // Panel de botones de acción
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttonPanel.setBackground(AppStyle.CARD_BG);
    
    JButton cancelBtn = ComponentFactory.createSecondaryButton("Cancelar");
    JButton okBtn = ComponentFactory.createPrimaryButton("Aceptar");
    
    buttonPanel.add(cancelBtn);
    buttonPanel.add(okBtn);

    // Panel para mostrar la hora seleccionada
    JLabel selectedTimeLabel = new JLabel("Hora seleccionada: --:--", SwingConstants.CENTER);
    selectedTimeLabel.setFont(AppStyle.FONT_SUBTITLE);
    selectedTimeLabel.setForeground(AppStyle.TEXT_PRIMARY);

    mainPanel.add(selectedTimeLabel, BorderLayout.NORTH);
    mainPanel.add(new JScrollPane(timeSelectionPanel), BorderLayout.CENTER);
    mainPanel.add(buttonPanel, BorderLayout.SOUTH);

    timeDialog.add(mainPanel);

    // Variables para almacenar la selección
    final String[] selectedHour = {null};
    final String[] selectedMinute = {null};

    // Actualizar etiqueta cuando se selecciona hora o minuto
    Runnable updateTimeLabel = () -> {
        if (selectedHour[0] != null && selectedMinute[0] != null) {
            selectedTimeLabel.setText("Hora seleccionada: " + selectedHour[0] + ":" + selectedMinute[0]);
        } else if (selectedHour[0] != null) {
            selectedTimeLabel.setText("Hora seleccionada: " + selectedHour[0] + ":--");
        } else if (selectedMinute[0] != null) {
            selectedTimeLabel.setText("Hora seleccionada: --:" + selectedMinute[0]);
        } else {
            selectedTimeLabel.setText("Hora seleccionada: --:--");
        }
    };

    // Configurar action listeners para los botones de hora
    for (Component comp : hourPanel.getComponents()) {
        if (comp instanceof JButton) {
            JButton btn = (JButton) comp;
            btn.addActionListener(e -> {
                selectedHour[0] = btn.getText();
                updateTimeLabel.run();
            });
        }
    }

    // Configurar action listeners para los botones de minuto
    for (Component comp : minutePanel.getComponents()) {
        if (comp instanceof JButton) {
            JButton btn = (JButton) comp;
            btn.addActionListener(e -> {
                selectedMinute[0] = btn.getText();
                updateTimeLabel.run();
            });
        }
    }

    // Configurar hora actual como selección por defecto
    Calendar now = Calendar.getInstance();
    String currentHour = String.format("%02d", now.get(Calendar.HOUR_OF_DAY));
    String currentMinute = String.format("%02d", (now.get(Calendar.MINUTE) / 5) * 5); // Redondear a múltiplo de 5
    
    // Seleccionar hora y minuto actuales
    for (Component comp : hourPanel.getComponents()) {
        if (comp instanceof JButton && ((JButton) comp).getText().equals(currentHour)) {
            comp.setBackground(AppStyle.PRIMARY);
            comp.setForeground(Color.WHITE);
            selectedHour[0] = currentHour;
        }
    }
    
    for (Component comp : minutePanel.getComponents()) {
        if (comp instanceof JButton && ((JButton) comp).getText().equals(currentMinute)) {
            comp.setBackground(AppStyle.PRIMARY);
            comp.setForeground(Color.WHITE);
            selectedMinute[0] = currentMinute;
        }
    }
    
    updateTimeLabel.run();

    cancelBtn.addActionListener(e -> timeDialog.dispose());

    okBtn.addActionListener(e -> {
        if (selectedHour[0] != null && selectedMinute[0] != null) {
            try {
                String selectedTime = selectedHour[0] + ":" + selectedMinute[0];
                spHora.setValue(new SimpleDateFormat("HH:mm").parse(selectedTime));
                timeDialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(timeDialog, 
                    "Error al establecer la hora: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(timeDialog, 
                "Por favor seleccione hora y minutos", 
                "Selección incompleta", JOptionPane.WARNING_MESSAGE);
        }
    });

    timeDialog.setVisible(true);
}

    private JButton createTimeButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(AppStyle.FONT_INPUT);
        btn.setBackground(Color.WHITE);
        btn.setBorder(BorderFactory.createLineBorder(AppStyle.BORDER));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void validateAndSave() {
        try {
            if (!Validators.isValidId(tfId.getText()))
                throw new IllegalArgumentException("ID inválido o vacío (máx. 20 caracteres)");
            if (!Validators.isValidName(tfNombre.getText()))
                throw new IllegalArgumentException("Nombre inválido o vacío (máx. 100 caracteres)");

            // Validar y formatear fecha
            String fechaText = tfFecha.getText().trim();
            if (fechaText.isEmpty()) {
                throw new IllegalArgumentException("La fecha no puede estar vacía");
            }
            
            String fecha;
            try {
                Date date = new SimpleDateFormat("dd/MM/yyyy").parse(fechaText);
                fecha = new SimpleDateFormat("yyyy-MM-dd").format(date);
            } catch (Exception ex) {
                throw new IllegalArgumentException("Formato de fecha inválido. Use dd/MM/yyyy");
            }

            String hora = new SimpleDateFormat("HH:mm").format((Date) spHora.getValue());

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
