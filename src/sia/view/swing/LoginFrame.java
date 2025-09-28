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
 * Pantalla de Login/Registro con diseño moderno y mejorado
 */
public class LoginFrame extends JFrame {

    private static final int FIELD_COLUMNS = 24;
    private static final int BANNER_HEIGHT = 140;
    private static final Color PRIMARY_COLOR = new Color(66, 133, 244);
    private static final Color SECONDARY_COLOR = new Color(52, 168, 83);
    private static final Color ACCENT_COLOR = new Color(219, 68, 55);
    private static final Color BACKGROUND_COLOR = new Color(248, 249, 250);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(33, 37, 41);
    private static final Color TEXT_SECONDARY = new Color(108, 117, 125);
    private static final Color BORDER_COLOR = new Color(206, 212, 218);
    private static final Color SUCCESS_COLOR = new Color(40, 167, 69);
    
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 32);
    private static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.PLAIN, 15);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font INPUT_FONT = new Font("Segoe UI", Font.PLAIN, 15);
    private static final Font ERROR_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font LINK_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    private final AuthService authService;
    private final SistemaEventos sistema;

    // CardLayout para alternar entre login y registro
    private CardLayout cardLayout;
    private JPanel cardPanel;

    // Login components
    private JTextField tfUsernameLogin;
    private JPasswordField pfPasswordLogin;
    private JButton btnLogin;
    private JCheckBox cbRememberMe;
    private JCheckBox cbShowPasswordLogin;
    private JLabel lblCapsLockWarning;
    private JLabel lblLoginError;

    // Registro components
    private JTextField tfUsernameRegister;
    private JTextField tfEmailRegister;
    private JPasswordField pfPasswordRegister;
    private JPasswordField pfConfirmPassword;
    private JComboBox<String> cbRol;
    private JButton btnRegister;
    private JCheckBox cbShowPasswordRegister;
    private JLabel lblRegisterError;
    private JLabel lblStrengthIndicator;

    // Banner
    private JLabel lblImage;
    
    // Intentos fallidos
    private int failedAttempts = 0;
    private static final int MAX_ATTEMPTS = 3;
    private Timer lockoutTimer;

    public LoginFrame(AuthService authService, SistemaEventos sistema) {
        super("Organizador de Eventos Universitarios");
        this.authService = authService;
        this.sistema = sistema;

        // Look and Feel consistente
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.put("Panel.background", BACKGROUND_COLOR);
        } catch (Exception e) {
            e.printStackTrace();
        }

        initUI();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 850);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(BACKGROUND_COLOR);

        // ==== ENCABEZADO (BANNER MEJORADO) ====
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setPreferredSize(new Dimension(0, BANNER_HEIGHT));
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        lblImage = new BannerLabel("/images/portada.png");
        headerPanel.add(lblImage, BorderLayout.CENTER);
        
        // Título principal
        JLabel mainTitle = new JLabel("Organizador de Eventos Universitarios", SwingConstants.CENTER);
        mainTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        mainTitle.setForeground(Color.WHITE);
        mainTitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        headerPanel.add(mainTitle, BorderLayout.SOUTH);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // ==== PANEL PRINCIPAL CON CARDLAYOUT ====
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(BACKGROUND_COLOR);
        cardPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        cardPanel.add(createLoginPanel(), "login");
        cardPanel.add(createRegisterPanel(), "register");

        mainPanel.add(cardPanel, BorderLayout.CENTER);

        // ==== PIE ====
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(new Color(240, 240, 240));
        footerPanel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        JLabel footerLabel = new JLabel("Sistema de Gestión de Eventos Universitarios • © 2025");
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

        // Establecer botón por defecto
        getRootPane().setDefaultButton(btnLogin);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(createCardBorder());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel lblTitle = new JLabel("Iniciar Sesión", SwingConstants.CENTER);
        lblTitle.setFont(TITLE_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        // Mensaje de bienvenida mejorado
        JLabel lblWelcome = new JLabel(
            "<html><div style='text-align:center; font-size:14px; color:#6c757d; line-height:1.5;'>" +
            "¡Bienvenido/a al Organizador de Eventos Universitarios!<br>" +
            "</div></html>",
            SwingConstants.CENTER
        );
        lblWelcome.setFont(SUBTITLE_FONT);

        JLabel lblUsername = new JLabel("Usuario:");
        lblUsername.setFont(LABEL_FONT);
        lblUsername.setForeground(TEXT_PRIMARY);
        tfUsernameLogin = new JTextField(FIELD_COLUMNS);
        tfUsernameLogin.setFont(INPUT_FONT);
        tfUsernameLogin.setBorder(createInputBorder());
        tfUsernameLogin.setToolTipText("Ingresa tu nombre de usuario");

        JLabel lblPassword = new JLabel("Contraseña:");
        lblPassword.setFont(LABEL_FONT);
        lblPassword.setForeground(TEXT_PRIMARY);
        pfPasswordLogin = new JPasswordField(FIELD_COLUMNS);
        pfPasswordLogin.setFont(INPUT_FONT);
        pfPasswordLogin.setBorder(createInputBorder());
        pfPasswordLogin.setToolTipText("Ingresa tu contraseña");
        
        // Mostrar contraseña
        cbShowPasswordLogin = new JCheckBox("Mostrar contraseña");
        cbShowPasswordLogin.setFont(new Font("Segoe UI", Font.PLAIN, 13));
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
        cbRememberMe.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbRememberMe.setBackground(CARD_COLOR);
        
        // Advertencia de Bloq Mayús
        lblCapsLockWarning = new JLabel("⚠ Mayúsculas activadas");
        lblCapsLockWarning.setFont(ERROR_FONT);
        lblCapsLockWarning.setForeground(Color.ORANGE);
        lblCapsLockWarning.setVisible(false);
        
        // Mensaje de error
        lblLoginError = new JLabel(" ");
        lblLoginError.setFont(ERROR_FONT);
        lblLoginError.setForeground(ACCENT_COLOR);

        btnLogin = createSolidButton("Entrar al Sistema", PRIMARY_COLOR);

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
        panel.add(btnLogin, gbc);
        
        // Enlace para ir a Registro
        gbc.gridy = 9; gbc.gridx = 0; gbc.gridwidth = 2;
        JButton btnIrARegistro = createLinkButton("¿No tienes cuenta? Regístrate aquí");
        btnIrARegistro.addActionListener(ev -> showRegisterPanel());
        JPanel linkPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        linkPanel.setBackground(CARD_COLOR);
        linkPanel.add(btnIrARegistro);
        panel.add(linkPanel, gbc);

        btnLogin.addActionListener(this::performLogin);

        return panel;
    }

    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(createCardBorder());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel lblTitle = new JLabel("Crear Cuenta", SwingConstants.CENTER);
        lblTitle.setFont(TITLE_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        // Mensaje de bienvenida
        JLabel lblWelcome = new JLabel(
            "<html><div style='text-align:center; font-size:14px; color:#6c757d; line-height:1.5;'>" +
            "Crea tu cuenta para acceder al sistema de eventos universitarios<br>" +
            "</div></html>",
            SwingConstants.CENTER
        );
        lblWelcome.setFont(SUBTITLE_FONT);

        JLabel lblUsername = new JLabel("Usuario:");
        lblUsername.setFont(LABEL_FONT);
        lblUsername.setForeground(TEXT_PRIMARY);
        tfUsernameRegister = new JTextField(FIELD_COLUMNS);
        tfUsernameRegister.setFont(INPUT_FONT);
        tfUsernameRegister.setBorder(createInputBorder());
        tfUsernameRegister.setToolTipText("Elige un nombre de usuario (mín. 3 caracteres)");

        JLabel lblEmail = new JLabel("Correo electrónico:");
        lblEmail.setFont(LABEL_FONT);
        lblEmail.setForeground(TEXT_PRIMARY);
        tfEmailRegister = new JTextField(FIELD_COLUMNS);
        tfEmailRegister.setFont(INPUT_FONT);
        tfEmailRegister.setBorder(createInputBorder());
        tfEmailRegister.setToolTipText("Ingresa tu correo electrónico válido");

        JLabel lblPassword = new JLabel("Contraseña:");
        lblPassword.setFont(LABEL_FONT);
        lblPassword.setForeground(TEXT_PRIMARY);
        pfPasswordRegister = new JPasswordField(FIELD_COLUMNS);
        pfPasswordRegister.setFont(INPUT_FONT);
        pfPasswordRegister.setBorder(createInputBorder());
        pfPasswordRegister.setToolTipText("Crea una contraseña segura (mín. 6 caracteres)");
        
        // Indicador de fortaleza de contraseña
        lblStrengthIndicator = new JLabel(" ");
        lblStrengthIndicator.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        // Mostrar contraseñas
        cbShowPasswordRegister = new JCheckBox("Mostrar contraseñas");
        cbShowPasswordRegister.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbShowPasswordRegister.setBackground(CARD_COLOR);
        cbShowPasswordRegister.addActionListener(e -> {
            boolean mostrar = cbShowPasswordRegister.isSelected();
            pfPasswordRegister.setEchoChar(mostrar ? (char) 0 : '•');
            pfConfirmPassword.setEchoChar(mostrar ? (char) 0 : '•');
        });

        JLabel lblConfirm = new JLabel("Confirmar contraseña:");
        lblConfirm.setFont(LABEL_FONT);
        lblConfirm.setForeground(TEXT_PRIMARY);
        pfConfirmPassword = new JPasswordField(FIELD_COLUMNS);
        pfConfirmPassword.setFont(INPUT_FONT);
        pfConfirmPassword.setBorder(createInputBorder());
        pfConfirmPassword.setToolTipText("Repite tu contraseña");

        JLabel lblRol = new JLabel("Tipo de usuario:");
        lblRol.setFont(LABEL_FONT);
        lblRol.setForeground(TEXT_PRIMARY);
        cbRol = new JComboBox<>(new String[]{"Seleccione una opcion....","Alumno", "Profesor"});
        cbRol.setFont(INPUT_FONT);
        cbRol.setBackground(CARD_COLOR);
        cbRol.setBorder(createInputBorder());
        
        // Mensaje de error
        lblRegisterError = new JLabel(" ");
        lblRegisterError.setFont(ERROR_FONT);
        lblRegisterError.setForeground(ACCENT_COLOR);

        btnRegister = createSolidButton("Crear Cuenta", SECONDARY_COLOR);

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
        panel.add(lblEmail, gbc);
        gbc.gridx = 1;
        panel.add(tfEmailRegister, gbc);

        gbc.gridy = 4; gbc.gridx = 0;
        panel.add(lblPassword, gbc);
        gbc.gridx = 1;
        panel.add(pfPasswordRegister, gbc);
        
        gbc.gridy = 5; gbc.gridx = 1;
        panel.add(lblStrengthIndicator, gbc);
        
        gbc.gridy = 6; gbc.gridx = 1;
        panel.add(cbShowPasswordRegister, gbc);

        gbc.gridy = 7; gbc.gridx = 0;
        panel.add(lblConfirm, gbc);
        gbc.gridx = 1;
        panel.add(pfConfirmPassword, gbc);
        
        gbc.gridy = 8; gbc.gridx = 0;
        panel.add(lblRol, gbc);
        gbc.gridx = 1;
        panel.add(cbRol, gbc);
        
        gbc.gridy = 9; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(lblRegisterError, gbc);

        gbc.gridy = 10; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(btnRegister, gbc);
        
        // Enlace para ir a Login
        gbc.gridy = 11; gbc.gridx = 0; gbc.gridwidth = 2;
        JButton btnIrALogin = createLinkButton("¿Ya tienes cuenta? Inicia sesión");
        btnIrALogin.addActionListener(ev -> showLoginPanel());
        JPanel linkPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        linkPanel.setBackground(CARD_COLOR);
        linkPanel.add(btnIrALogin);
        panel.add(linkPanel, gbc);

        // Acción
        btnRegister.addActionListener(this::performRegister);
        
        // Listener para fortaleza de contraseña
        pfPasswordRegister.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updatePasswordStrength(); }
            public void removeUpdate(DocumentEvent e) { updatePasswordStrength(); }
            public void changedUpdate(DocumentEvent e) { updatePasswordStrength(); }
        });

        return panel;
    }

    private Border createCardBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(222, 226, 230), 1),
                BorderFactory.createEmptyBorder(30, 35, 30, 35)
            ),
            BorderFactory.createLineBorder(new Color(240, 240, 240), 8)
        );
    }

    private Border createInputBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        );
    }

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

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
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
        button.setBorder(BorderFactory.createEmptyBorder(14, 30, 14, 30));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
    
    private JButton createLinkButton(String text) {
        JButton link = new JButton(text);
        link.setFont(LINK_FONT);
        link.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
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
    
    private void showLoginPanel() {
        cardLayout.show(cardPanel, "login");
        getRootPane().setDefaultButton(btnLogin);
    }
    
    private void showRegisterPanel() {
        cardLayout.show(cardPanel, "register");
        getRootPane().setDefaultButton(btnRegister);
    }
    
    private void updatePasswordStrength() {
        char[] password = pfPasswordRegister.getPassword();
        if (password.length == 0) {
            lblStrengthIndicator.setText(" ");
            lblStrengthIndicator.setForeground(TEXT_SECONDARY);
            return;
        }
        
        int strength = calculatePasswordStrength(password);
        String text;
        Color color;
        
        if (strength < 2) {
            text = "Débil";
            color = ACCENT_COLOR;
        } else if (strength < 4) {
            text = "Media";
            color = Color.ORANGE;
        } else {
            text = "Fuerte";
            color = SUCCESS_COLOR;
        }
        
        lblStrengthIndicator.setText("Fortaleza: " + text);
        lblStrengthIndicator.setForeground(color);
    }
    
    private int calculatePasswordStrength(char[] password) {
        int strength = 0;
        if (password.length >= 6) strength++;
        if (password.length >= 8) strength++;
        
        boolean hasUpper = false, hasLower = false, hasDigit = false, hasSpecial = false;
        for (char c : password) {
            if (Character.isUpperCase(c)) hasUpper = true;
            if (Character.isLowerCase(c)) hasLower = true;
            if (Character.isDigit(c)) hasDigit = true;
            if (!Character.isLetterOrDigit(c)) hasSpecial = true;
        }
        
        if (hasUpper && hasLower) strength++;
        if (hasDigit) strength++;
        if (hasSpecial) strength++;
        
        return strength;
    }
    
    // Resto de métodos (setupFieldValidation, restoreLastUser, performLogin, performRegister, etc.)
    // Se mantienen similares pero adaptados al nuevo diseño
    
    private void setupFieldValidation() {
        DocumentListener loginListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { validateLoginFields(); }
            public void removeUpdate(DocumentEvent e) { validateLoginFields(); }
            public void changedUpdate(DocumentEvent e) { validateLoginFields(); }
        };

        tfUsernameLogin.getDocument().addDocumentListener(loginListener);
        pfPasswordLogin.getDocument().addDocumentListener(loginListener);

        DocumentListener registerListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { validateRegisterFields(); }
            public void removeUpdate(DocumentEvent e) { validateRegisterFields(); }
            public void changedUpdate(DocumentEvent e) { validateRegisterFields(); }
        };

        tfUsernameRegister.getDocument().addDocumentListener(registerListener);
        tfEmailRegister.getDocument().addDocumentListener(registerListener);
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
        String email = tfEmailRegister.getText().trim();
        char[] password = pfPasswordRegister.getPassword();
        char[] confirmPassword = pfConfirmPassword.getPassword();

        boolean valid = !username.isEmpty() && 
                       !email.isEmpty() && email.contains("@") &&
                       password.length >= 6 && 
                       Arrays.equals(password, confirmPassword);
        btnRegister.setEnabled(valid);

        if (password.length > 0 && confirmPassword.length > 0 && !Arrays.equals(password, confirmPassword)) {
            lblRegisterError.setText("Las contraseñas no coinciden");
        } else {
            lblRegisterError.setText(" ");
        }
    }

    private void restoreLastUser() {
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
        tfEmailRegister.addKeyListener(capsLockDetector);
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

                // Guardar usuario si está marcado "Recordar"
                if (cbRememberMe.isSelected()) {
                    // Implementar guardado de preferencias si es necesario
                }

                // Cerrar login y abrir ventana principal
                this.dispose();
                MainFrame mainFrame = new MainFrame(sistema, authService);
                mainFrame.setVisible(true);
            } else {
                failedAttempts++;
                if (failedAttempts >= MAX_ATTEMPTS) {
                    JOptionPane.showMessageDialog(this, 
                        "Demasiados intentos fallidos. Espere 30 segundos.", 
                        "Cuenta Bloqueada", 
                        JOptionPane.ERROR_MESSAGE);
                    btnLogin.setEnabled(false);
                    
                    lockoutTimer = new Timer();
                    lockoutTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            SwingUtilities.invokeLater(() -> {
                                failedAttempts = 0;
                                btnLogin.setEnabled(true);
                                lblLoginError.setText(" ");
                            });
                        }
                    }, 30000);
                } else {
                    lblLoginError.setText("Usuario o contraseña incorrectos. Intentos: " + failedAttempts + "/" + MAX_ATTEMPTS);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error durante el login: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            Arrays.fill(password, ' ');
        }
    }

    private void performRegister(ActionEvent e) {
        String username = tfUsernameRegister.getText().trim();
        String email = tfEmailRegister.getText().trim();
        char[] password = pfPasswordRegister.getPassword();
        char[] confirmPassword = pfConfirmPassword.getPassword();
        String rol = (String) cbRol.getSelectedItem();

        // Validaciones
        if (username.length() < 3) {
            JOptionPane.showMessageDialog(this, "El nombre de usuario debe tener al menos 3 caracteres", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (email.isEmpty() || !email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(this, "Por favor ingrese un correo electrónico válido", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (password.length < 6) {
            JOptionPane.showMessageDialog(this, "La contraseña debe tener al menos 6 caracteres", "Error", JOptionPane.ERROR_MESSAGE);
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
            Usuario nuevoUsuario = authService.register(username, password, rol, email);
            JOptionPane.showMessageDialog(this, 
                "¡Cuenta creada exitosamente!\nYa puedes iniciar sesión.", 
                "Registro Exitoso", 
                JOptionPane.INFORMATION_MESSAGE);

            // Limpiar campos y volver a login
            tfUsernameRegister.setText("");
            tfEmailRegister.setText("");
            pfPasswordRegister.setText("");
            pfConfirmPassword.setText("");
            showLoginPanel();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error durante el registro: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            Arrays.fill(password, ' ');
            Arrays.fill(confirmPassword, ' ');
        }
    }
    
    // Clase BannerLabel se mantiene igual
    private static class BannerLabel extends JLabel {
        private BufferedImage img;
        public BannerLabel(String resource) {
            setOpaque(true);
            setBackground(new Color(66, 133, 244));
            try { 
                img = ImageIO.read(getClass().getResource(resource)); 
            } catch (Exception e) { 
                img = null; 
                setBackground(new Color(66, 133, 244));
            }
        }

        @Override public void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img == null) {
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
            double r = Math.max(rw, rh);
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
}
