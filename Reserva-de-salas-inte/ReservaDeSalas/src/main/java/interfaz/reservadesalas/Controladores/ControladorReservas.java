package interfaz.reservadesalas.Controladores;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

public class ControladorReservas extends ControladorPrincipal{

    @FXML private Label tabUpcoming;
    @FXML private Label tabPast;
    @FXML private Label tabAll;

    @FXML
    public void initialize() {
        System.out.println("Controlador de Reservas (VistaInicio) inicializado.");
    }

    @FXML
    private void handleFilterUpcoming(MouseEvent event) {
        System.out.println("Filtro: Mostrando reservas futuras (Upcoming).");
        updateTabStyle((Label) event.getSource());
    }

    @FXML
    private void handleFilterPast(MouseEvent event) {
        System.out.println("Filtro: Mostrando reservas pasadas (Past).");
        updateTabStyle((Label) event.getSource());
    }

    @FXML
    private void handleFilterAll(MouseEvent event) {
        System.out.println("Filtro: Mostrando todas las reservas (All).");
        updateTabStyle((Label) event.getSource());
    }

    private void updateTabStyle(Label activeTab) {
        tabUpcoming.getStyleClass().setAll("tab-inactive");
        tabPast.getStyleClass().setAll("tab-inactive");
        tabAll.getStyleClass().setAll("tab-inactive");
        activeTab.getStyleClass().setAll("tab-active");
    }

    @FXML
    private void handleEditAction(ActionEvent event) {
        Button sourceButton = (Button) event.getSource();
        System.out.println("Acción: Editar reserva. Botón presionado: " + sourceButton.getText());
    }

    @FXML
    private void handleCancelAction(ActionEvent event) {
        Button sourceButton = (Button) event.getSource();
        System.out.println("Acción: Cancelar reserva. Botón presionado: " + sourceButton.getText());
    }
}