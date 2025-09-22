package sia.view.swing;

import sia.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class AddPersonaDialog extends JDialog {

    private JTextField txtId;
    private JTextField txtNombre;
    private JTextField txtEmail;

    private JComboBox<String> cmbTipo;

    // Campos Estudiante
    private JTextField txtCarrera;
    private JSpinner spNivel;

    // Campos Profesor
    private JTextField txtDepartamento;
    private JTextField txtCategoria;

    private Persona personaCreada = null;

    public AddPersonaDialog(Frame owner) {
        super(owner, "Agregar Persona", true);
        initUI();
    }

    public Persona getPersonaCreada() {
        return personaCreada;
    }

    private void initUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 8, 4, 8);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;

        int y = 0;

        // Tipo
        gc.gridx = 0; gc.gridy = y; gc.weightx = 0; panel.add(new JLabel("Tipo:"), gc);
        cmbTipo = new JComboBox<>(new String[]{"Estudiante", "Profesor"});
        gc.gridx = 1; gc.gridy = y++; gc.weightx = 1; panel.add(cmbTipo, gc);

        // ID
        gc.gridx = 0; gc.gridy = y; gc.weightx = 0; panel.add(new JLabel("ID:"), gc);
        txtId = new JTextField();
        gc.gridx = 1; gc.gridy = y++; gc.weightx = 1; panel.add(txtId, gc);

        // Nombre
        gc.gridx = 0; gc.gridy = y; gc.weightx = 0; panel.add(new JLabel("Nombre:"), gc);
        txtNombre = new JTextField();
        gc.gridx = 1; gc.gridy = y++; gc.weightx = 1; panel.add(txtNombre, gc);

        // Email
        gc.gridx = 0; gc.gridy = y; gc.weightx = 0; panel.add(new JLabel("Email:"), gc);
        txtEmail = new JTextField();
        gc.gridx = 1; gc.gridy = y++; gc.weightx = 1; panel.add(txtEmail, gc);

        // ---- Panel Estudiante ----
        JPanel pnlEst = new JPanel(new GridBagLayout());
        GridBagConstraints ge = new GridBagConstraints();
        ge.insets = new Insets(2, 6, 2, 6);
        ge.fill = GridBagConstraints.HORIZONTAL;
        ge.weightx = 1.0;

        int ye = 0;
        ge.gridx = 0; ge.gridy = ye; ge.weightx = 0; pnlEst.add(new JLabel("Carrera:"), ge);
        txtCarrera = new JTextField();
        ge.gridx = 1; ge.gridy = ye++; ge.weightx = 1; pnlEst.add(txtCarrera, ge);

        ge.gridx = 0; ge.gridy = ye; ge.weightx = 0; pnlEst.add(new JLabel("Nivel:"), ge);
        spNivel = new JSpinner(new SpinnerNumberModel(1, 0, 20, 1));
        ge.gridx = 1; ge.gridy = ye++; ge.weightx = 1; pnlEst.add(spNivel, ge);

        // ---- Panel Profesor ----
        JPanel pnlProf = new JPanel(new GridBagLayout());
        GridBagConstraints gp = new GridBagConstraints();
        gp.insets = new Insets(2, 6, 2, 6);
        gp.fill = GridBagConstraints.HORIZONTAL;
        gp.weightx = 1.0;

        int yp = 0;
        gp.gridx = 0; gp.gridy = yp; gp.weightx = 0; pnlProf.add(new JLabel("Departamento:"), gp);
        txtDepartamento = new JTextField();
        gp.gridx = 1; gp.gridy = yp++; gp.weightx = 1; pnlProf.add(txtDepartamento, gp);

        gp.gridx = 0; gp.gridy = yp; gp.weightx = 0; pnlProf.add(new JLabel("Categoría:"), gp);
        txtCategoria = new JTextField();
        gp.gridx = 1; gp.gridy = yp++; gp.weightx = 1; pnlProf.add(txtCategoria, gp);

        // Contenedor dinámico
        JPanel pnlTipo = new JPanel(new CardLayout());
        pnlTipo.add(pnlEst, "Estudiante");
        pnlTipo.add(pnlProf, "Profesor");

        // Al cambiar tipo, swap de panel
        cmbTipo.addActionListener(e -> {
            CardLayout cl = (CardLayout) pnlTipo.getLayout();
            cl.show(pnlTipo, (String) cmbTipo.getSelectedItem());
        });
        // Mostrar Estudiante por defecto
        ((CardLayout) pnlTipo.getLayout()).show(pnlTipo, "Estudiante");

        // Añadir contenedor dinámico
        gc.gridx = 0; gc.gridy = y; gc.gridwidth = 2; panel.add(pnlTipo, gc);
        y++;

        // Botones
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnOk = new JButton("Guardar");
        JButton btnCancel = new JButton("Cancelar");
        buttons.add(btnCancel);
        buttons.add(btnOk);

        btnCancel.addActionListener(e -> {
            personaCreada = null;
            setVisible(false);
        });

        btnOk.addActionListener(e -> onGuardar());

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(getOwner());
        setMinimumSize(new Dimension(420, getHeight()));
    }

    private void onGuardar() {
        String tipo = (String) cmbTipo.getSelectedItem();
        String id = safe(txtId.getText());
        String nombre = safe(txtNombre.getText());
        String email = safe(txtEmail.getText());

        if (id.isEmpty() || nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "ID y Nombre son obligatorios.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if ("Estudiante".equalsIgnoreCase(tipo)) {
            String carrera = safe(txtCarrera.getText());
            int nivel = (Integer) spNivel.getValue();
            personaCreada = new Estudiante(id, nombre, email, carrera, nivel);
        } else {
            String depto = safe(txtDepartamento.getText());
            String categoria = safe(txtCategoria.getText());
            personaCreada = new Profesor(id, nombre, email, depto, categoria);
        }

        setVisible(false);
    }

    private static String safe(String s) { return s == null ? "" : s.trim(); }
}
