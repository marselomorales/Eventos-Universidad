package sia.view.swing;

import sia.*;
import sia.exceptions.CapacidadLlenaException;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class ManageAsistentesDialog extends JDialog {

    private final Evento evento;

    private DefaultListModel<Persona> modelo;
    private JList<Persona> lst;

    public ManageAsistentesDialog(Frame owner, Evento evento) {
        super(owner, "Asistentes de: " + (evento != null ? evento.getNombre() : ""), true);
        this.evento = evento;
        initUI();
    }

    private void initUI() {
        setMinimumSize(new Dimension(520, 420));
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout(10,10));

        modelo = new DefaultListModel<>();
        for (Persona p : evento.getAsistentes()) modelo.addElement(p);

        lst = new JList<>(modelo);
        lst.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane sp = new JScrollPane(lst);
        sp.setBorder(BorderFactory.createTitledBorder("Asistentes"));

        JButton btnAgregar = new JButton("Agregar persona");
        JButton btnEliminar = new JButton("Eliminar seleccionada");
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(btnEliminar);
        south.add(btnAgregar);

        btnAgregar.addActionListener(e -> onAdd());
        btnEliminar.addActionListener(e -> onDelete());

        add(sp, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
        pack();
    }

    // Abre el diálogo para crear Estudiante/Profesor y agrega al evento
    private void onAdd() {
        AddPersonaDialog dlg = new AddPersonaDialog(
                SwingUtilities.getWindowAncestor(this) instanceof Frame ? (Frame) SwingUtilities.getWindowAncestor(this) : null
        );
        dlg.setVisible(true);
        Persona p = dlg.getPersonaCreada();
        if (p == null) return;

        // Evita duplicados por ID dentro del evento
        for (Persona ex : evento.getAsistentes()) {
            if (Objects.equals(ex.getId(), p.getId())) {
                JOptionPane.showMessageDialog(this, "Persona ya inscrita en el evento.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }
        
        // Validar capacidad
        int cap = evento.getCapacidad();
        if (cap > 0 && evento.getAsistentes().size() >= cap) {
            JOptionPane.showMessageDialog(this,
                    "El evento está completo (" + cap + ").",
                    "Capacidad llena", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Agregar (evita duplicado ya validado arriba)
        boolean ok = evento.agregarAsistente(p);
        if (!ok) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo agregar (duplicado).",
                    "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        modelo.addElement(p);
    }

    private void onDelete() {
        Persona sel = lst.getSelectedValue();
        if (sel == null) return;
        evento.eliminarAsistente(sel.getId());
        modelo.removeElement(sel);
    }
}