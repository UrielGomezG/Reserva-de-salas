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
        super.initialize();
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
        try {
            VBox card = (VBox) event.getSource();
            if (card == null || card.getChildren().size() < 2) {
                System.err.println("Error: Estructura de tarjeta de incidente inválida.");
                return;
            }
            
            javafx.scene.Node labelNode = card.getChildren().get(1);
            if (!(labelNode instanceof Label)) {
                System.err.println("Error: No se encontró el label del incidente.");
                return;
            }
            
            Label incidentLabel = (Label) labelNode;
            String nuevoTipo = incidentLabel.getText();
            
            javafx.scene.Node parent = card.getParent();
            if (parent == null) {
                System.err.println("Error: No se encontró el padre de la tarjeta.");
                return;
            }
            
            javafx.scene.Node grandParent = parent.getParent();
            if (!(grandParent instanceof VBox)) {
                System.err.println("Error: Estructura de contenedor inválida.");
                return;
            }
            
            VBox parentVBox = (VBox) grandParent;
            if (parentVBox.getChildren().size() < 2) {
                System.err.println("Error: Estructura de contenedor incompleta.");
                return;
            }
            
            javafx.scene.Node gridNode = parentVBox.getChildren().get(1);
            if (!(gridNode instanceof GridPane)) {
                System.err.println("Error: No se encontró el grid de incidentes.");
                return;
            }
            
            GridPane incidentGrid = (GridPane) gridNode;
            
            for (javafx.scene.Node node : incidentGrid.getChildren()) {
                if (node instanceof VBox) {
                    node.getStyleClass().remove("incident-card-selected");
                }
            }
            
            card.getStyleClass().add("incident-card-selected");
            
            tipoIncidenteSeleccionado = nuevoTipo;
            System.out.println("Incidente seleccionado: " + tipoIncidenteSeleccionado);
        } catch (Exception e) {
            System.err.println("Error al seleccionar incidente: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEntregarReporte(ActionEvent event) {
        if (descriptionTextArea == null) {
            System.err.println("ERROR: El área de texto no está inicializada.");
            return;
        }
        
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
        
        try {
            javafx.scene.Node parent = descriptionTextArea.getParent();
            if (parent == null) {
                return;
            }
            
            javafx.scene.Node grandParent = parent.getParent();
            if (!(grandParent instanceof VBox)) {
                return;
            }
            
            VBox parentVBox = (VBox) grandParent;
            if (parentVBox.getChildren().size() < 2) {
                return;
            }
            
            javafx.scene.Node gridNode = parentVBox.getChildren().get(1);
            if (!(gridNode instanceof GridPane)) {
                return;
            }
            
            GridPane incidentGrid = (GridPane) gridNode;
            for (javafx.scene.Node node : incidentGrid.getChildren()) {
                if (node instanceof VBox) {
                    node.getStyleClass().remove("incident-card-selected");
                }
            }
        } catch (Exception e) {
            System.err.println("Error al limpiar selección de incidentes: " + e.getMessage());
        }
    }
    

}