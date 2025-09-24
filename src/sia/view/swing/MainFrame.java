package sia.view.swing;

import sia.Evento;
import sia.SistemaEventos;
import sia.persistence.CsvStorage;
import sia.AuthService;
import sia.SessionContext;
import sia.Usuario;  
import sia.view.swing.LoginFrame;


import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import java.util.Optional;       

public class MainFrame extends JFrame {

    // ===== Campos =====
    private final SistemaEventos sistema;
    private EventTableModel tableModel;
    private JTable table;
    private TableRowSorter<EventTableModel> sorter;
    private JLabel statusLabel;
    private JComboBox<String> cbScope;

    // === Sesión ===
    private AuthService authService;
    private JLabel sessionLabel;

    // ===== Ctor =====
    // Acepta AuthService y asigna this.authService ===
    public MainFrame(SistemaEventos sistema, AuthService authService) {
        super("Organizador de Eventos Universitario");
        this.sistema = sistema;
        this.authService = authService;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 700));
        initUI(); // Construcción de la interfaz
        updateSessionInfo();
    }

    // ===== Util =====
    private JButton big(String text) {
        JButton b = new JButton(text);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 13f));
        b.setMargin(new Insets(8, 14, 8, 14));
        b.setFocusable(false);
        return b;
    }

    // ===== UI principal =====
    private void initUI() {
        // --- Banner superior ---
        BannerPanel banner = new BannerPanel(
                "Organizador de Eventos Universitario",
                "Planifica, gestiona asistentes y recursos."
        );

        // --- Toolbar con búsqueda y alcance ---
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        cbScope = new JComboBox<>(new String[]{"Todo", "Próximos", "Pasados", "Con cupo", "Completos"});
        cbScope.setFocusable(false);

        JTextField tfSearch = new JTextField();
        tfSearch.setPreferredSize(new Dimension(300, 34));
        tfSearch.setFont(tfSearch.getFont().deriveFont(13f));


        // Aplicar filtros en vivo al escribir
        tfSearch.getDocument().addDocumentListener(new DocumentListener() {
            private void apply() { applyFilters(tfSearch.getText()); }
            public void insertUpdate(DocumentEvent e) { apply(); }
            public void removeUpdate(DocumentEvent e) { apply(); }
            public void changedUpdate(DocumentEvent e) { apply(); }
        });
        // Aplicar filtro de alcance
        cbScope.addActionListener(e -> applyFilters(tfSearch.getText()));

        JLabel lblBuscar = new JLabel("Buscar:");
        lblBuscar.setFont(lblBuscar.getFont().deriveFont(Font.BOLD, 13f));
        JLabel lblScope = new JLabel("  •  Ámbito:");
        lblScope.setFont(lblScope.getFont().deriveFont(13f));

        toolbar.add(lblBuscar);
        toolbar.add(Box.createHorizontalStrut(6));
        toolbar.add(tfSearch);
        toolbar.add(lblScope);
        toolbar.add(Box.createHorizontalStrut(6));
        toolbar.add(cbScope);

        // --- Tabla de eventos ---
        tableModel = new EventTableModel(sistema.getEventos());
        table = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer r, int row, int column) {
                Component c = super.prepareRenderer(r, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground((row % 2 == 0) ? new Color(252, 252, 252) : new Color(245, 248, 252));
                }
                return c;
            }
        };
        table.setFillsViewportHeight(true);
        table.setRowHeight(24);
        table.setFont(table.getFont().deriveFont(12f));

        // Encabezado
        JTableHeader th = table.getTableHeader();
        th.setFont(th.getFont().deriveFont(Font.BOLD, 12.5f));
        th.setReorderingAllowed(false);

        // Sorter + filtro
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        // === Renderer "badge" para columna Estado ===
        DefaultTableCellRenderer badgeRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel badge = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                badge.setHorizontalAlignment(JLabel.CENTER);
                badge.setFont(badge.getFont().deriveFont(Font.BOLD));
                String txt = value == null ? "" : value.toString();
                String low = txt.toLowerCase();
                Color bg;
                if (low.contains("sin cupo") || low.contains("completo")) {
                    bg = new Color(255, 200, 200);
                } else if (low.contains("disponible")) {
                    bg = new Color(200, 255, 200);
                } else {
                    bg = new Color(210, 210, 210);
                }
                badge.setOpaque(true);
                badge.setBackground(isSelected ? table.getSelectionBackground() : bg);
                badge.setForeground(Color.DARK_GRAY);
                return badge;
            }
        };
        int estadoCol = tableModel.getColumnCount() - 1;
        if (estadoCol >= 0) {
            table.getColumnModel().getColumn(estadoCol).setCellRenderer(badgeRenderer);
        }

        // Centrado para numéricas
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        if (tableModel.getColumnCount() >= 8) {
            table.getColumnModel().getColumn(6).setCellRenderer(center); // Capacidad
            table.getColumnModel().getColumn(7).setCellRenderer(center); // Inscritos
        }

        // Tamaños por columna + orden por fecha DESC
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int c = 0;
        table.getColumnModel().getColumn(c++).setPreferredWidth(70);   // ID
        table.getColumnModel().getColumn(c++).setPreferredWidth(240);  // Nombre
        table.getColumnModel().getColumn(c++).setPreferredWidth(130);  // Tipo
        table.getColumnModel().getColumn(c++).setPreferredWidth(110);  // Fecha
        table.getColumnModel().getColumn(c++).setPreferredWidth(80);   // Hora
        table.getColumnModel().getColumn(c++).setPreferredWidth(220);  // Sala
        table.getColumnModel().getColumn(c++).setPreferredWidth(90);   // Capacidad
        table.getColumnModel().getColumn(c++).setPreferredWidth(90);   // Inscritos
        table.getColumnModel().getColumn(c++).setPreferredWidth(160);  // Estado

        java.util.List<RowSorter.SortKey> keys = new java.util.ArrayList<>();
        keys.add(new RowSorter.SortKey(3, SortOrder.DESCENDING));
        sorter.setSortKeys(keys);
        sorter.sort();

        // === Menú contextual ===
        JPopupMenu rowMenu = new JPopupMenu();
        JMenuItem miEdit = new JMenuItem("Editar");
        JMenuItem miDel = new JMenuItem("Eliminar");
        JMenuItem miAsis = new JMenuItem("Gestionar Asistentes");
        JMenuItem miRec = new JMenuItem("Gestionar Recursos");

        miEdit.addActionListener(this::onEdit);
        miDel.addActionListener(this::onDelete);
        miAsis.addActionListener(e -> onAsistentes());
        miRec.addActionListener(e -> onRecursos());

        rowMenu.add(miEdit);
        rowMenu.add(miDel);
        rowMenu.addSeparator();
        rowMenu.add(miAsis);
        rowMenu.add(miRec);

        table.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { maybeShow(e); }
            @Override public void mouseReleased(MouseEvent e) { maybeShow(e); }
            private void maybeShow(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int r = table.rowAtPoint(e.getPoint());
                    if (r >= 0 && r < table.getRowCount()) {
                        table.setRowSelectionInterval(r, r);
                    }
                    rowMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        // --- Sidebar de acciones ---
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JLabel titleActions = new JLabel("Acciones");
        titleActions.setFont(titleActions.getFont().deriveFont(Font.BOLD, 13f));
        titleActions.setBorder(BorderFactory.createEmptyBorder(0,4,8,4));
        side.add(titleActions);

        ActionCardButton bNew  = new ActionCardButton("  Nuevo Evento", IconKit.plus());
        bNew.addActionListener(this::onNew);
        ActionCardButton bEdit = new ActionCardButton("  Editar Evento", IconKit.edit());
        bEdit.addActionListener(this::onEdit);
        ActionCardButton bDel  = new ActionCardButton("  Eliminar Evento", IconKit.trash());
        bDel.addActionListener(this::onDelete);
        ActionCardButton bAsis = new ActionCardButton("  Gestionar Asistentes", IconKit.users());
        bAsis.addActionListener(e -> onAsistentes());
        ActionCardButton bRec  = new ActionCardButton("  Gestionar Recursos", IconKit.box());
        bRec.addActionListener(e -> onRecursos());
        ActionCardButton bRep  = new ActionCardButton("  Generar Reporte", IconKit.report());
        bRep.addActionListener(e -> onReporte());
        ActionCardButton bSave = new ActionCardButton("  Guardar Todo", IconKit.save());
        bSave.addActionListener(e -> onGuardar());
        ActionCardButton bAdv  = new ActionCardButton("  Búsqueda Avanzada", IconKit.searchPlus());
        bAdv.addActionListener(e -> onAdvancedSearch());

        for (JButton b : new JButton[]{bNew,bEdit,bDel,bAsis,bRec,bRep,bSave,bAdv}) {
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
            b.setMaximumSize(new Dimension(220, 36));
            side.add(b);
            side.add(Box.createVerticalStrut(8));
        }

        // --- Barra inferior de estado ---
        statusLabel = new JLabel();
        statusLabel.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        statusLabel.setFont(statusLabel.getFont().deriveFont(12f));
        updateStatus();

        // --- Atajos globales ---
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), "save");
        getRootPane().getActionMap().put("save", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { onGuardar(); }
        });
        table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK), "new");
        table.getActionMap().put("new", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { onNew(e); }
        });
        table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
        table.getActionMap().put("delete", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { onDelete(e); }
        });
        // ESC limpia búsqueda
        tfSearch.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "clear");
        tfSearch.getActionMap().put("clear", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { tfSearch.setText(""); }
        });

        // --- Layout principal ---
        JPanel north = new JPanel(new BorderLayout());
        north.add(banner, BorderLayout.NORTH);
        north.add(toolbar, BorderLayout.SOUTH);
        // Banner de sesión al centro
        north.add(createSessionBanner(), BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(north, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(side, BorderLayout.EAST);
        add(statusLabel, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(null);
    }

    // ===== Banner de sesión =====
    private JPanel createSessionBanner() {
        JPanel sessionPanel = new JPanel(new BorderLayout());
        sessionPanel.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        sessionPanel.setBackground(new Color(240, 240, 240));

        sessionLabel = new JLabel("Sesión: Invitado");
        JButton btnLogin = new JButton("Iniciar sesión");
        JButton btnLogout = new JButton("Cerrar sesión");

        btnLogin.addActionListener(e -> showLoginDialog());
        btnLogout.addActionListener(e -> logout());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.add(btnLogin);
        buttonPanel.add(btnLogout);
        buttonPanel.setOpaque(false);

        sessionPanel.add(sessionLabel, BorderLayout.WEST);
        sessionPanel.add(buttonPanel, BorderLayout.EAST);

        updateSessionUI();
        return sessionPanel;
    }

    // ===== HELPERS de sesión =====
    private void showLoginDialog() {
        LoginFrame loginFrame = new LoginFrame(authService, sistema);
        loginFrame.setVisible(true);
        updateSessionUI();
    }

    private void logout() {
        SessionContext.get().logout();
        updateSessionUI();
    }

    private void updateSessionUI() {
        if (SessionContext.get().isLoggedIn()) {
            String username = SessionContext.get().getUsuario().getUsername();
            String rol = SessionContext.get().getUsuario().getRol();
            sessionLabel.setText("Sesión: " + username + " (" + rol + ")");
        } else {
            sessionLabel.setText("Sesión: Invitado");
        }
    }

    // ===== Filtros & Helpers =====
    /** Aplica el filtro por texto + filtro de alcance. */
    private void applyFilters(String text) {
        if (sorter == null) return;
        RowFilter<EventTableModel, Integer> rf = null;
        if (text != null && !text.trim().isEmpty()) {
            rf = RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text.trim()));
        }
        RowFilter<EventTableModel, Integer> scope = scopeFilter();
        if (rf == null) {
            sorter.setRowFilter(scope);
        } else if (scope == null) {
            sorter.setRowFilter(rf);
        } else {
            sorter.setRowFilter(RowFilter.andFilter(java.util.Arrays.asList(rf, scope)));
        }
        updateStatus();
    }

    /** Filtro de alcance por (fecha/estado/cupos) en columnas fijas del modelo. */
    private RowFilter<EventTableModel, Integer> scopeFilter() {
        String sel = (String) cbScope.getSelectedItem();
        if (sel == null || "Todo".equals(sel)) return null;
        int colFecha = 3, colCap = 6, colIns = 7, colEstado = 8;
        java.util.Date now = new java.util.Date();
        switch (sel) {
            case "Próximos":
                return new RowFilter<EventTableModel, Integer>() {
                    @Override public boolean include(Entry<? extends EventTableModel, ? extends Integer> e) {
                        Object val = e.getValue(colFecha);
                        if (val == null) return false;
                        try {
                            java.sql.Date d = java.sql.Date.valueOf(val.toString());
                            return d.after(now);
                        } catch (Exception ex) { return false; }
                    }
                };
            case "Pasados":
                return new RowFilter<EventTableModel, Integer>() {
                    @Override public boolean include(Entry<? extends EventTableModel, ? extends Integer> e) {
                        Object val = e.getValue(colFecha);
                        if (val == null) return false;
                        try {
                            java.sql.Date d = java.sql.Date.valueOf(val.toString());
                            return d.before(now);
                        } catch (Exception ex) { return false; }
                    }
                };
            case "Con cupo":
                return new RowFilter<EventTableModel, Integer>() {
                    @Override public boolean include(Entry<? extends EventTableModel, ? extends Integer> e) {
                        try {
                            int cap = Integer.parseInt(String.valueOf(e.getValue(colCap)));
                            int ins = Integer.parseInt(String.valueOf(e.getValue(colIns)));
                            return ins < cap;
                        } catch (Exception ex) { return false; }
                    }
                };
            case "Completos":
                return new RowFilter<EventTableModel, Integer>() {
                    @Override public boolean include(Entry<? extends EventTableModel, ? extends Integer> e) {
                        Object estado = e.getValue(colEstado);
                        return estado != null && estado.toString().toLowerCase().contains("completo");
                    }
                };
            default: return null;
        }
    }

    private void updateStatus() {
        int count = tableModel.getRowCount();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        statusLabel.setText("Eventos: " + count + "    |    " + sdf.format(new Date()));
    }

    private Evento getSelectedEvento() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return null;
        int modelRow = table.convertRowIndexToModel(viewRow);
        java.util.List<Evento> eventos = tableModel.getEventos();
        if (modelRow < 0 || modelRow >= eventos.size()) return null;
        return eventos.get(modelRow);
    }

    // ===== Acciones =====
    private void onNew(ActionEvent e) {
        EventDialog dlg = new EventDialog(this, sistema, "Nuevo evento", null);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
        if (dlg.isAccepted()) {
            tableModel.addEvento(dlg.getResult());
            sistema.setEventos(tableModel.getEventos());
            updateStatus();
        }
    }

    private void onEdit(ActionEvent e) {
        Evento sel = getSelectedEvento();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un evento", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        EventDialog dlg = new EventDialog(this, sistema, "Editar evento", sel);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
        if (dlg.isAccepted()) {
            int viewRow = table.getSelectedRow();
            int modelRow = table.convertRowIndexToModel(viewRow);
            tableModel.updateEvento(modelRow, dlg.getResult());
            sistema.setEventos(tableModel.getEventos());
            updateStatus();
        }
    }

    private void onDelete(ActionEvent e) {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return;
        int confirm = JOptionPane.showConfirmDialog(this, "¿Eliminar el evento seleccionado?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        int modelRow = table.convertRowIndexToModel(viewRow);
        tableModel.removeEvento(modelRow);
        sistema.setEventos(tableModel.getEventos());
        updateStatus();
    }

    private void onAsistentes() {
        Evento sel = getSelectedEvento();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un evento", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        ManageAsistentesDialog  dlg = new ManageAsistentesDialog(this, sistema, sel);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
        tableModel.fireTableDataChanged(); // refresca conteo de inscritos/estado
        updateStatus();
    }

    private void onRecursos() {
        Evento sel = getSelectedEvento();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un evento", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        RecursosEventoDialog dlg = new RecursosEventoDialog(this, sistema, sel);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
        tableModel.fireTableDataChanged();
        updateStatus();
    }

    private void onAdvancedSearch() {
        AdvancedSearchDialog dlg = new AdvancedSearchDialog(this, sistema, tableModel);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
        updateStatus();
    }

    private void onGuardar() {
        try {
            CsvStorage.saveAll(sistema);
            JOptionPane.showMessageDialog(this, "Datos guardados en carpeta 'data/'", "OK", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onReporte() {
        try {
            File f = new File("reporte_eventos.csv");
            CsvStorage.exportReport(sistema, f.getAbsolutePath());
            JOptionPane.showMessageDialog(this, "Reporte exportado a: " + f.getAbsolutePath(), "Reporte", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error exportando: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSessionInfo() {
        if (SessionContext.get().isLoggedIn()) {
            Usuario usuario = SessionContext.get().getUsuario();
            sessionLabel.setText("Usuario: " + usuario.getUsername() + " | Rol: " + usuario.getRol());
        } else {
            sessionLabel.setText("Sesión: Invitado");
        }
    }
}
