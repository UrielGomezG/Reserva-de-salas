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
        super.initialize();
        System.out.println("Controlador de Reservas (VistaInicio) inicializado.");
    }

    @FXML
    private void handleFilterUpcoming(MouseEvent event) {
        System.out.println("Filtro: Mostrando reservas futuras (Upcoming).");
        if (tabUpcomingLabel != null && tabUpcomingSeparator != null) {
            activateTab(tabUpcomingLabel, tabUpcomingSeparator);
        }
        loadBookings("upcoming");
    }

    @FXML
    private void handleFilterPast(MouseEvent event) {
        System.out.println("Filtro: Mostrando reservas pasadas (Past).");
        if (tabPastLabel != null && tabPastSeparator != null) {
            activateTab(tabPastLabel, tabPastSeparator);
        }
        loadBookings("past");
    }

    @FXML
    private void handleFilterAll(MouseEvent event) {
        System.out.println("Filtro: Mostrando todas las reservas (All).");
        if (tabAllLabel != null && tabAllSeparator != null) {
            activateTab(tabAllLabel, tabAllSeparator);
        }
        loadBookings("all");
    }

    @FXML
    protected void handleEditAction(ActionEvent event) {
        Button sourceButton = (Button) event.getSource();
        System.out.println("Acción: Editar reserva. Botón presionado: " + sourceButton.getText());
        super.handleEditAction(event);
    }

    @FXML
    protected void handleCancelAction(ActionEvent event) {
        Button sourceButton = (Button) event.getSource();
        System.out.println("Acción: Cancelar reserva. Botón presionado: " + sourceButton.getText());
        super.handleCancelAction(event);
    }
}