package sia.view.swing;

import sia.Evento;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class EventTableModel extends AbstractTableModel {
    private final String[] cols = {"ID", "Nombre", "Tipo", "Fecha", "Hora", "Sala", "Capacidad", "Inscritos", "Estado"};
    private final List<Evento> data;

    public EventTableModel(List<Evento> data) {
        this.data = new ArrayList<>(data);
    }

    @Override 
    public int getRowCount() { 
        return data.size(); 
    }
    
    @Override 
    public int getColumnCount() { 
        return cols.length; 
    }
    
    @Override 
    public String getColumnName(int c) { 
        return cols[c]; 
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= data.size()) return "";
        
        Evento e = data.get(rowIndex);
        switch (columnIndex) {
            case 0: return e.getIdEvento();
            case 1: return e.getNombre();
            case 2: return e.getTipo();
            case 3: return e.getFecha();
            case 4: return e.getHora();
            case 5: return e.getSala();
            case 6: return e.getCapacidad();
            case 7: return e.getAsistentes().size();
            case 8: 
                if (e.getAsistentes().size() >= e.getCapacidad()) {
                    return "SIN CUPO";
                } else {
                    return "Disponible (" + (e.getCapacidad() - e.getAsistentes().size()) + ")";
                }
            default: return "";
        }
    }

    public Evento getEventoAt(int row) {
        if (row < 0 || row >= data.size()) return null;
        return data.get(row);
    }

    public void addEvento(Evento e) {
        data.add(e);
        fireTableRowsInserted(data.size() - 1, data.size() - 1);
    }

    public void updateEvento(int row, Evento e) {
        if (row < 0 || row >= data.size()) return;
        data.set(row, e);
        fireTableRowsUpdated(row, row);
    }

    public void removeEvento(int row) {
        if (row < 0 || row >= data.size()) return;
        data.remove(row);
        fireTableRowsDeleted(row, row);
    }
    
    public List<Evento> getEventos() {
        return new ArrayList<>(data);
    }
    
    public void setEventos(List<Evento> nuevosEventos) {
        data.clear();
        data.addAll(nuevosEventos);
        fireTableDataChanged();
    }
}