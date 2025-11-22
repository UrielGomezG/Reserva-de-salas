package interfaz.reservadesalas.Controladores;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class ControladorReporte extends ControladorPrincipal {
    
    @FXML private TextArea descriptionTextArea;
    
    private String tipoIncidenteSeleccionado = "";

    @FXML
    public void initialize() {
        System.out.println("Controlador Reporte inicializado. Listo para recibir incidentes.");
    }

    @FXML
    private void handleSelectSala(MouseEvent event) {
        System.out.println("ACCIÓN: Abrir modal/desplegable para seleccionar sala.");
    }
    
    @FXML
    private void handleSelectFecha(MouseEvent event) {
        System.out.println("ACCIÓN: Abrir DatePicker para seleccionar fecha.");
    }

    @FXML
    private void handleSelectIncidente(MouseEvent event) {
        VBox card = (VBox) event.getSource();
        Label incidentLabel = (Label) card.getChildren().get(1);
        String nuevoTipo = incidentLabel.getText();
        VBox parentVBox = (VBox) card.getParent().getParent(); 
        GridPane incidentGrid = (GridPane) parentVBox.getChildren().get(1);
        
        for (javafx.scene.Node node : incidentGrid.getChildren()) {
            if (node instanceof VBox) {
                node.getStyleClass().remove("incident-card-selected");
            }
        }
        
        card.getStyleClass().add("incident-card-selected");
        
        tipoIncidenteSeleccionado = nuevoTipo;
        System.out.println("Incidente seleccionado: " + tipoIncidenteSeleccionado);
    }

    @FXML
    private void handleEntregarReporte(ActionEvent event) {
        String descripcion = descriptionTextArea.getText();
        
        if (tipoIncidenteSeleccionado.isEmpty() || descripcion.trim().isEmpty()) {
            System.err.println("ERROR: Debe seleccionar un tipo de incidente y proporcionar una descripción.");
            return;
        }

        System.out.println("--- REPORTE FINAL ---");
        System.out.println("Tipo de Incidente: " + tipoIncidenteSeleccionado);
        System.out.println("Descripción: " + descripcion.trim());
        System.out.println("ACCIÓN: Datos listos para ser enviados a la base de datos o sistema de gestión.");
                
        descriptionTextArea.clear();
        tipoIncidenteSeleccionado = "";
        
        VBox parentVBox = (VBox) descriptionTextArea.getParent().getParent(); 
        GridPane incidentGrid = (GridPane) parentVBox.getChildren().get(1);
        for (javafx.scene.Node node : incidentGrid.getChildren()) {
            if (node instanceof VBox) {
                node.getStyleClass().remove("incident-card-selected");
            }
        }
    }
    

}