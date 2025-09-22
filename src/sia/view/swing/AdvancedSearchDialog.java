package sia.view.swing;

import sia.Evento;
import sia.SistemaEventos;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdvancedSearchDialog extends JDialog {
    private final SistemaEventos sistema;
    private final EventTableModel tableModel;

    private JTextField tfId, tfNombre, tfTipo, tfFecha, tfSala;
    private JSpinner spCapacidadMin, spCapacidadMax;
    private JCheckBox cbConCupo, cbSinCupo;

    public AdvancedSearchDialog(Frame owner, SistemaEventos sistema, EventTableModel tableModel) {
        super(owner, "Búsqueda Avanzada", true);
        this.sistema = sistema;
        this.tableModel = tableModel;
        initUI();
        pack();
        setLocationRelativeTo(owner);
        setSize(500, 350); // Tamaño aumentado para mejor visualización
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel de formulario con scroll por si hay muchos campos
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        
        // Campos de búsqueda
        formPanel.add(new JLabel("ID:"));
        tfId = new JTextField();
        formPanel.add(tfId);

        formPanel.add(new JLabel("Nombre:"));
        tfNombre = new JTextField();
        formPanel.add(tfNombre);

        formPanel.add(new JLabel("Tipo:"));
        tfTipo = new JTextField();
        formPanel.add(tfTipo);

        formPanel.add(new JLabel("Fecha (YYYY-MM-DD):"));
        tfFecha = new JTextField();
        formPanel.add(tfFecha);

        formPanel.add(new JLabel("Sala:"));
        tfSala = new JTextField();
        formPanel.add(tfSala);

        formPanel.add(new JLabel("Capacidad Mín:"));
        spCapacidadMin = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
        formPanel.add(spCapacidadMin);

        formPanel.add(new JLabel("Capacidad Máx:"));
        spCapacidadMax = new JSpinner(new SpinnerNumberModel(10000, 0, 10000, 1));
        formPanel.add(spCapacidadMax);

        formPanel.add(new JLabel("Solo con Cupo:"));
        cbConCupo = new JCheckBox();
        formPanel.add(cbConCupo);

        formPanel.add(new JLabel("Solo sin Cupo:"));
        cbSinCupo = new JCheckBox();
        formPanel.add(cbSinCupo);

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Criterios de Búsqueda"));

        // Botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnBuscar = new JButton("Buscar");
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnLimpiar = new JButton("Limpiar");

        btnBuscar.addActionListener(e -> buscar());
        btnCancelar.addActionListener(e -> dispose());
        btnLimpiar.addActionListener(e -> limpiarCampos());

        buttonPanel.add(btnLimpiar);
        buttonPanel.add(btnCancelar);
        buttonPanel.add(btnBuscar);

        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void limpiarCampos() {
        tfId.setText("");
        tfNombre.setText("");
        tfTipo.setText("");
        tfFecha.setText("");
        tfSala.setText("");
        spCapacidadMin.setValue(0);
        spCapacidadMax.setValue(10000);
        cbConCupo.setSelected(false);
        cbSinCupo.setSelected(false);
    }

    private void buscar() {
        try {
            // Obtener criterios de búsqueda
            String id = tfId.getText().trim();
            String nombre = tfNombre.getText().trim();
            String tipo = tfTipo.getText().trim();
            String fecha = tfFecha.getText().trim();
            String sala = tfSala.getText().trim();
            int capMin = (Integer) spCapacidadMin.getValue();
            int capMax = (Integer) spCapacidadMax.getValue();
            boolean conCupo = cbConCupo.isSelected();
            boolean sinCupo = cbSinCupo.isSelected();

            // Validar criterios de cupo
            if (conCupo && sinCupo) {
                JOptionPane.showMessageDialog(this, 
                    "No puede seleccionar 'Con Cupo' y 'Sin Cupo' simultáneamente", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Filtrar eventos
            List<Evento> eventos = sistema.getEventos();
            java.util.List<Evento> resultados = new java.util.ArrayList<>();

            for (Evento e : eventos) {
                // Aplicar filtros
                if (!id.isEmpty() && !e.getIdEvento().toLowerCase().contains(id.toLowerCase())) 
                    continue;
                if (!nombre.isEmpty() && !e.getNombre().toLowerCase().contains(nombre.toLowerCase())) 
                    continue;
                if (!tipo.isEmpty() && !e.getTipo().toLowerCase().contains(tipo.toLowerCase())) 
                    continue;
                if (!fecha.isEmpty() && !e.getFecha().toLowerCase().contains(fecha.toLowerCase())) 
                    continue;
                if (!sala.isEmpty() && !e.getSala().toLowerCase().contains(sala.toLowerCase())) 
                    continue;
                if (e.getCapacidad() < capMin || e.getCapacidad() > capMax) 
                    continue;

                // Filtro de cupo
                int inscritos = e.getAsistentes().size();
                if (conCupo && inscritos >= e.getCapacidad()) 
                    continue;
                if (sinCupo && inscritos < e.getCapacidad()) 
                    continue;

                // Si pasa todos los filtros, agregar a resultados
                resultados.add(e);
            }

            // Actualizar tabla con resultados
            tableModel.setEventos(resultados);
            
            JOptionPane.showMessageDialog(this, 
                "Se encontraron " + resultados.size() + " eventos", 
                "Resultados", JOptionPane.INFORMATION_MESSAGE);
            
            dispose();
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error en la búsqueda: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}