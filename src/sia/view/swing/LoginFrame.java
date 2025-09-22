package sia.view.swing;

import sia.AuthService;
import sia.SistemaEventos;
import sia.Usuario;
import sia.Persona;
import sia.SessionContext;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.Optional;
import java.util.Arrays;
import java.util.prefs.Preferences;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Optional;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Pantalla de Login/Registro con mejoras de UX/UI y seguridad
 */
public class LoginFrame extends JFrame {

    private static final int FIELD_COLUMNS = 24;
    private static final int BANNER_HEIGHT = 120;
    private static final Color PRIMARY_COLOR = new Color(66, 133, 244);
    private static final Color SECONDARY_COLOR = new Color(52, 168, 83);
    private static final Color ACCENT_COLOR = new Color(219, 68, 55);
    private static final Color BACKGROUND_COLOR = new Color(248, 249, 250);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(33, 37, 41);
    private static final Color TEXT_SECONDARY = new Color(108, 117, 125);
    private static final Color BORDER_COLOR = new Color(206, 212, 218);
    
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 28);
    private static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font INPUT_FONT = new Font("Segoe UI", Font.PLAIN, 15);
    private static final Font ERROR_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font LINK_FONT = new Font("Segoe UI", Font.PLAIN, 13);

    private final AuthService authService;
    private final SistemaEventos sistema;

    // Tabs
    private JTabbedPane tabbedPane;

    // Login
    private JTextField tfUsernameLogin;
    private JPasswordField pfPasswordLogin;
    private JButton btnLogin;
    private JButton btnExit;
    private JCheckBox cbRememberMe;
    private JCheckBox cbShowPasswordLogin;
    private JLabel lblCapsLockWarning;
    private JLabel lblLoginError;

    // Registro
    private JTextField tfUsernameRegister;
    private JPasswordField pfPasswordRegister;
    private JPasswordField pfConfirmPassword;
    private JComboBox<String> cbRol;
    private JButton btnRegister;
    private JCheckBox cbShowPasswordRegister;
    private JLabel lblRegisterError;

    // Banner
    private JLabel lblImage;
    
    // Intentos fallidos
    private int failedAttempts = 0;
    private static final int MAX_ATTEMPTS = 3;
    private Timer lockoutTimer;

    public LoginFrame(AuthService authService, SistemaEventos sistema) {
        super("Organizador de Eventos Universitarios - Inicio de Sesión");
        this.authService = authService;
        this.sistema = sistema;

        // Look and Feel consistente
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            // Mejorar el aspecto de los componentes
            UIManager.put("TabbedPane.background", BACKGROUND_COLOR);
            UIManager.put("TabbedPane.foreground", TEXT_PRIMARY);
            UIManager.put("TabbedPane.selected", PRIMARY_COLOR);
        } catch (Exception e) {
            e.printStackTrace();
        }

        initUI();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 750);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(BACKGROUND_COLOR);

        // ==== ENCABEZADO (BANNER) ====
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setPreferredSize(new Dimension(0, BANNER_HEIGHT));
        imagePanel.setBackground(PRIMARY_COLOR);
        imagePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        lblImage = new BannerLabel("/images/portada.png");
        imagePanel.add(lblImage, BorderLayout.CENTER);
        mainPanel.add(imagePanel, BorderLayout.NORTH);

        // ==== PESTAÑAS ====
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setBackground(BACKGROUND_COLOR);
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        tabbedPane.addTab("Iniciar Sesión", createLoginPanel());
        tabbedPane.addTab("Registrarse", createRegisterPanel());
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // ==== PIE ====
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(new Color(240, 240, 240));
        footerPanel.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));
        JLabel footerLabel = new JLabel("Sistema de Gestión de Eventos Universitarios v1.0");
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footerLabel.setForeground(TEXT_SECONDARY);
        footerPanel.add(footerLabel);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        // Validaciones + recuperar último usuario
        setupFieldValidation();
        restoreLastUser();
        setupCapsLockDetection();

        // Forzar estado inicial de botones
        validateSubmitButtons();

        // Establecer botones por defecto
        getRootPane().setDefaultButton(btnLogin);
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 0) {
                getRootPane().setDefaultButton(btnLogin);
            } else {
                getRootPane().setDefaultButton(btnRegister);
            }
        });
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(222, 226, 230), 1, true),
                BorderFactory.createEmptyBorder(30, 30, 25, 30)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel lblTitle = new JLabel("Accede a tu cuenta", SwingConstants.CENTER);
        lblTitle.setFont(TITLE_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        // Mensaje de bienvenida
        JLabel lblWelcome = new JLabel(
            "<html><div style='text-align:center; font-size:13px; color:#6c757d;'>" +
            "¡Bienvenido/a al Organizador de Eventos Universitarios!<br>" +
            "Accede con tus credenciales para comenzar a organizar" +
            "</div></html>",
            SwingConstants.CENTER
        );
        lblWelcome.setFont(SUBTITLE_FONT);

        JLabel lblUsername = new JLabel("Tu usuario:");
        lblUsername.setFont(LABEL_FONT);
        lblUsername.setForeground(TEXT_PRIMARY);
        tfUsernameLogin = new JTextField(FIELD_COLUMNS);
        tfUsernameLogin.setFont(INPUT_FONT);
        tfUsernameLogin.setBorder(createInputBorder());
        tfUsernameLogin.setToolTipText("Ingresa tu nombre de usuario");

        JLabel lblPassword = new JLabel("Ingresa tu clave");
        lblPassword.setFont(LABEL_FONT);
        lblPassword.setForeground(TEXT_PRIMARY);
        pfPasswordLogin = new JPasswordField(FIELD_COLUMNS);
        pfPasswordLogin.setFont(INPUT_FONT);
        pfPasswordLogin.setBorder(createInputBorder());
        pfPasswordLogin.setToolTipText("Ingresa tu contraseña");
        
        // Mostrar contraseña
        cbShowPasswordLogin = new JCheckBox("Ver contraseña");
        cbShowPasswordLogin.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbShowPasswordLogin.setBackground(CARD_COLOR);
        cbShowPasswordLogin.addActionListener(e -> {
            if (cbShowPasswordLogin.isSelected()) {
                pfPasswordLogin.setEchoChar((char) 0);
            } else {
                pfPasswordLogin.setEchoChar('•');
            }
        });
        
        // Recordarme
        cbRememberMe = new JCheckBox("Recordar mi usuario");
        cbRememberMe.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbRememberMe.setBackground(CARD_COLOR);
        
        // Advertencia de Bloq Mayús
        lblCapsLockWarning = new JLabel("Mayúsculas activadas️");
        lblCapsLockWarning.setFont(ERROR_FONT);
        lblCapsLockWarning.setForeground(Color.ORANGE);
        lblCapsLockWarning.setVisible(false);
        
        // Mensaje de error
        lblLoginError = new JLabel(" ");
        lblLoginError.setFont(ERROR_FONT);
        lblLoginError.setForeground(ACCENT_COLOR);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        buttonPanel.setBackground(CARD_COLOR);
        btnLogin = createSolidButton("Entrar", PRIMARY_COLOR);
        btnExit  = createOutlineButton("Mejor después", ACCENT_COLOR);
        buttonPanel.add(btnLogin);
        buttonPanel.add(btnExit);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(lblTitle, gbc);

        gbc.gridy = 1;
        panel.add(lblWelcome, gbc);

        gbc.gridwidth = 1; gbc.gridy = 2; gbc.gridx = 0;
        panel.add(lblUsername, gbc);
        gbc.gridx = 1;
        panel.add(tfUsernameLogin, gbc);

        gbc.gridy = 3; gbc.gridx = 0;
        panel.add(lblPassword, gbc);
        gbc.gridx = 1;
        panel.add(pfPasswordLogin, gbc);
        
        gbc.gridy = 4; gbc.gridx = 1;
        panel.add(cbShowPasswordLogin, gbc);
        
        gbc.gridy = 5; gbc.gridx = 1;
        panel.add(lblCapsLockWarning, gbc);
        
        gbc.gridy = 6; gbc.gridx = 1;
        panel.add(lblLoginError, gbc);

        gbc.gridy = 7; gbc.gridx = 0; gbc.gridwidth = 2;
        JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        optionsPanel.setBackground(CARD_COLOR);
        optionsPanel.add(cbRememberMe);
        panel.add(optionsPanel, gbc);

        gbc.gridy = 8; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);
        
        // Enlace para ir a Registro
        gbc.gridy = 9; gbc.gridx = 0; gbc.gridwidth = 2;
        JButton btnIrARegistro = createLinkButton("¿Primera vez aquí?  Crea tu cuenta");
        btnIrARegistro.addActionListener(ev -> animateTabChange(1));
        JPanel linkPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        linkPanel.setBackground(CARD_COLOR);
        linkPanel.add(btnIrARegistro);
        panel.add(linkPanel, gbc);

        btnLogin.addActionListener(this::performLogin);
        btnExit.addActionListener(e -> confirmExit());

        return panel;
    }

    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(222, 226, 230), 1, true),
                BorderFactory.createEmptyBorder(30, 30, 25, 30)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel lblTitle = new JLabel("¡Únete ahora!", SwingConstants.CENTER);
        lblTitle.setFont(TITLE_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        // Mensaje de bienvenida
        JLabel lblWelcome = new JLabel(
            "<html><div style='text-align:center; font-size:13px; color:#6c757d;'>" +
            "Crea tu cuenta para acceder al sistema de eventos universitarios<br>" +
            "Completa todos los campos para registrar tu cuenta" +
            "</div></html>",
            SwingConstants.CENTER
        );
        lblWelcome.setFont(SUBTITLE_FONT);

        JLabel lblUsername = new JLabel("Tu usuario:");
        lblUsername.setFont(LABEL_FONT);
        lblUsername.setForeground(TEXT_PRIMARY);
        tfUsernameRegister = new JTextField(FIELD_COLUMNS);
        tfUsernameRegister.setFont(INPUT_FONT);
        tfUsernameRegister.setBorder(createInputBorder());
        tfUsernameRegister.setToolTipText("Elige un nombre de usuario");

        JLabel lblPassword = new JLabel("Ingresa tu clave");
        lblPassword.setFont(LABEL_FONT);
        lblPassword.setForeground(TEXT_PRIMARY);
        pfPasswordRegister = new JPasswordField(FIELD_COLUMNS);
        pfPasswordRegister.setFont(INPUT_FONT);
        pfPasswordRegister.setBorder(createInputBorder());
        pfPasswordRegister.setToolTipText("Crea una contraseña segura");
        
        // Mostrar contraseñas
        cbShowPasswordRegister = new JCheckBox("Ver claves");
        cbShowPasswordRegister.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbShowPasswordRegister.setBackground(CARD_COLOR);
        cbShowPasswordRegister.addActionListener(e -> {
            boolean mostrar = cbShowPasswordRegister.isSelected();
            pfPasswordRegister.setEchoChar(mostrar ? (char) 0 : '•');
            pfConfirmPassword.setEchoChar(mostrar ? (char) 0 : '•');
        });

        JLabel lblConfirm = new JLabel("Confirma tu contraseña");
        lblConfirm.setFont(LABEL_FONT);
        lblConfirm.setForeground(TEXT_PRIMARY);
        pfConfirmPassword = new JPasswordField(FIELD_COLUMNS);
        pfConfirmPassword.setFont(INPUT_FONT);
        pfConfirmPassword.setBorder(createInputBorder());
        pfConfirmPassword.setToolTipText("Repite tu contraseña");

        JLabel lblRol = new JLabel("Soy...");
        lblRol.setFont(LABEL_FONT);
        lblRol.setForeground(TEXT_PRIMARY);
        cbRol = new JComboBox<>(new String[]{"ALUMNO", "PROFESOR"});
        cbRol.setFont(INPUT_FONT);
        cbRol.setBackground(CARD_COLOR);
        cbRol.setBorder(createInputBorder());
        
        // Mensaje de error
        lblRegisterError = new JLabel(" ");
        lblRegisterError.setFont(ERROR_FONT);
        lblRegisterError.setForeground(ACCENT_COLOR);

        btnRegister = createSolidButton("Crear cuenta", SECONDARY_COLOR);

        // Layout
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(lblTitle, gbc);

        gbc.gridy = 1;
        panel.add(lblWelcome, gbc);

        gbc.gridwidth = 1; gbc.gridy = 2; gbc.gridx = 0;
        panel.add(lblUsername, gbc);
        gbc.gridx = 1;
        panel.add(tfUsernameRegister, gbc);

        gbc.gridy = 3; gbc.gridx = 0;
        panel.add(lblPassword, gbc);
        gbc.gridx = 1;
        panel.add(pfPasswordRegister, gbc);
        
        gbc.gridy = 4; gbc.gridx = 1;
        panel.add(cbShowPasswordRegister, gbc);

        gbc.gridy = 5; gbc.gridx = 0;
        panel.add(lblConfirm, gbc);
        gbc.gridx = 1;
        panel.add(pfConfirmPassword, gbc);
        
        gbc.gridy = 6; gbc.gridx = 0;
        panel.add(lblRol, gbc);
        gbc.gridx = 1;
        panel.add(cbRol, gbc);
        
        gbc.gridy = 7; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(lblRegisterError, gbc);

        gbc.gridy = 8; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(btnRegister, gbc);
        
        // Enlace para ir a Login
        gbc.gridy = 9; gbc.gridx = 0; gbc.gridwidth = 2;
        JButton btnIrALogin = createLinkButton("¿Ya tienes cuenta? Iniciar sesión");
        btnIrALogin.addActionListener(ev -> animateTabChange(0));
        JPanel linkPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        linkPanel.setBackground(CARD_COLOR);
        linkPanel.add(btnIrALogin);
        panel.add(linkPanel, gbc);

        // Acción
        btnRegister.addActionListener(this::performRegister);

        return panel;
    }

    private Border createInputBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        );
    }

    /** Botones con color sólido */
    private JButton createSolidButton(String text, Color color) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (isEnabled()) {
                    if (getModel().isPressed()) {
                        g2.setColor(color.darker());
                    } else if (getModel().isRollover()) {
                        g2.setColor(color.brighter());
                    } else {
                        g2.setColor(color);
                    }
                } else {
                    g2.setColor(new Color(200, 200, 200));
                }

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                
                super.paintComponent(g);
            }
        };

        button.setFont(BUTTON_FONT);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
    
    /** Botón con borde */
    private JButton createOutlineButton(String text, Color color) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2.setColor(color);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.setColor(Color.WHITE);
                } else {
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.setColor(color);
                    g2.setStroke(new BasicStroke(2));
                    g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 10, 10);
                    
                    if (getModel().isRollover()) {
                        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 30));
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    }
                }
                g2.dispose();
                
                super.paintComponent(g);
            }
        };

        button.setFont(BUTTON_FONT);
        button.setForeground(ACCENT_COLOR);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
    
    /** Botón estilo enlace */
    private JButton createLinkButton(String text) {
        JButton link = new JButton(text);
        link.setFont(LINK_FONT);
        link.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        link.setContentAreaFilled(false);
        link.setFocusPainted(false);
        link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        link.setForeground(PRIMARY_COLOR);
        link.setToolTipText(text);
        link.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                link.setText("<html><u>" + text + "</u></html>");
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                link.setText(text);
            }
        });
        return link;
    }
    
    // Resto del código se mantiene igual (métodos saveLastUser, restoreLastUser, performLogin, performRegister, etc.)
    // ... [El resto de los métodos permanecen sin cambios] ...
    
    /** Label que dibuja imagen "cover" */
    private static class BannerLabel extends JLabel {
        private BufferedImage img;
        public BannerLabel(String resource) {
            setOpaque(true);
            setBackground(new Color(66, 133, 244));
            try { img = ImageIO.read(getClass().getResource(resource)); }
            catch (Exception e) { 
                img = null; 
                setBackground(new Color(66, 133, 244));
            }
        }

        @Override public void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img == null) {
                // Dibujar gradiente si no hay imagen
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(66, 133, 244), 
                    getWidth(), getHeight(), new Color(25, 103, 210)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                return;
            }
            
            int w = getWidth(), h = getHeight();
            double rw = w / (double) img.getWidth();
            double rh = h / (double) img.getHeight();
            double r = Math.max(rw, rh); // cover
            int nw = (int) (img.getWidth() * r);
            int nh = (int) (img.getHeight() * r);
            int x = (w - nw) / 2;
            int y = (h - nh) / 2;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.drawImage(img, x, y, nw, nh, null);
            g2.dispose();
        }

        @Override public Dimension getPreferredSize() {
            return new Dimension(1, BANNER_HEIGHT);
        }
    }
    
    private void setupFieldValidation() {
        // Validación en tiempo real para campos de login
        DocumentListener loginListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { validateLoginFields(); }
            public void removeUpdate(DocumentEvent e) { validateLoginFields(); }
            public void changedUpdate(DocumentEvent e) { validateLoginFields(); }
        };

        tfUsernameLogin.getDocument().addDocumentListener(loginListener);
        pfPasswordLogin.getDocument().addDocumentListener(loginListener);

        // Validación en tiempo real para campos de registro
        DocumentListener registerListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { validateRegisterFields(); }
            public void removeUpdate(DocumentEvent e) { validateRegisterFields(); }
            public void changedUpdate(DocumentEvent e) { validateRegisterFields(); }
        };

        tfUsernameRegister.getDocument().addDocumentListener(registerListener);
        pfPasswordRegister.getDocument().addDocumentListener(registerListener);
        pfConfirmPassword.getDocument().addDocumentListener(registerListener);
    }

    private void validateLoginFields() {
        String username = tfUsernameLogin.getText().trim();
        char[] password = pfPasswordLogin.getPassword();
        boolean valid = !username.isEmpty() && password.length > 0;
        btnLogin.setEnabled(valid);
    }

    private void validateRegisterFields() {
        String username = tfUsernameRegister.getText().trim();
        char[] password = pfPasswordRegister.getPassword();
        char[] confirmPassword = pfConfirmPassword.getPassword();

        boolean valid = !username.isEmpty() && 
                       password.length >= 4 && 
                       Arrays.equals(password, confirmPassword);
        btnRegister.setEnabled(valid);

        // Mostrar mensaje de error si las contraseñas no coinciden
        if (password.length > 0 && confirmPassword.length > 0 && !Arrays.equals(password, confirmPassword)) {
            lblRegisterError.setText("Las contraseñas no coinciden");
        } else {
            lblRegisterError.setText(" ");
        }
    }

    private void restoreLastUser() {
        // Implementación simple - podrías usar Preferences para persistencia
        tfUsernameLogin.setText("");
        cbRememberMe.setSelected(false);
    }

    private void setupCapsLockDetection() {
        KeyAdapter capsLockDetector = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_CAPS_LOCK) {
                    updateCapsLockWarning();
                }
            }
        };

        tfUsernameLogin.addKeyListener(capsLockDetector);
        pfPasswordLogin.addKeyListener(capsLockDetector);
        tfUsernameRegister.addKeyListener(capsLockDetector);
        pfPasswordRegister.addKeyListener(capsLockDetector);
        pfConfirmPassword.addKeyListener(capsLockDetector);
    }

    private void updateCapsLockWarning() {
        boolean capsLockOn = Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK);
        lblCapsLockWarning.setVisible(capsLockOn);
    }

    private void validateSubmitButtons() {
        validateLoginFields();
        validateRegisterFields();
    }

    private void animateTabChange(int tabIndex) {
        // Animación simple cambiando directamente la pestaña
        tabbedPane.setSelectedIndex(tabIndex);
    }

    private void performLogin(ActionEvent e) {
        String username = tfUsernameLogin.getText().trim();
        char[] password = pfPasswordLogin.getPassword();

        if (username.isEmpty() || password.length == 0) {
            JOptionPane.showMessageDialog(this, "Por favor complete todos los campos", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Optional<Usuario> usuarioOpt = authService.login(username, password);
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                SessionContext.get().login(usuario, null);

                // Cerrar login y abrir ventana principal
                this.dispose();
                MainFrame mainFrame = new MainFrame(sistema, authService);
                mainFrame.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Usuario o contraseña incorrectos", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error durante el login: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            // Limpiar contraseña por seguridad
            Arrays.fill(password, ' ');
        }
    }

    private void confirmExit() {
        int result = JOptionPane.showConfirmDialog(this, 
            "¿Está seguro que desea salir de la aplicación?", 
            "Confirmar salida", 
            JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    private void performRegister(ActionEvent e) {
        String username = tfUsernameRegister.getText().trim();
        char[] password = pfPasswordRegister.getPassword();
        char[] confirmPassword = pfConfirmPassword.getPassword();
        String rol = (String) cbRol.getSelectedItem();

        // Validaciones básicas
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre de usuario es requerido", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (password.length < 4) {
            JOptionPane.showMessageDialog(this, "La contraseña debe tener al menos 4 caracteres", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!Arrays.equals(password, confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Las contraseñas no coinciden", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Verificar si el usuario ya existe
            if (authService.findByUsername(username).isPresent()) {
                JOptionPane.showMessageDialog(this, "El nombre de usuario ya existe", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Registrar nuevo usuario
            Usuario nuevoUsuario = authService.register(username, password, rol, null);
            JOptionPane.showMessageDialog(this, "Usuario registrado exitosamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);

            // Limpiar campos y cambiar a pestaña de login
            tfUsernameRegister.setText("");
            pfPasswordRegister.setText("");
            pfConfirmPassword.setText("");
            animateTabChange(0);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error durante el registro: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            // Limpiar contraseñas por seguridad
            Arrays.fill(password, ' ');
            Arrays.fill(confirmPassword, ' ');
        }
    }  
}