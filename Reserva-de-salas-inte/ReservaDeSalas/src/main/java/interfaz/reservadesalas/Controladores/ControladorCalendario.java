package interfaz.reservadesalas.Controladores;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

public class ControladorCalendario extends ControladorPrincipal{

    @FXML private Label labelMesActual;
    @FXML private GridPane gridCalendario;
    @FXML
    public void initialize() {
        System.out.println("Controlador de Calendario de Reservas inicializado.");
        actualizarVistaCalendario(2024, 7); 
    }

    @FXML
    private void handleNextMonth(MouseEvent event) {
        System.out.println("Navegando al mes siguiente...");
    }

    @FXML
    private void handlePreviousMonth(MouseEvent event) {
        System.out.println("Navegando al mes anterior...");
    }
    
    @FXML
    private void handleDayClick(MouseEvent event) {
        Label dia = (Label) event.getSource();
        System.out.println("Día seleccionado: " + dia.getText());
    }

    private void actualizarVistaCalendario(int anio, int mes) {
        labelMesActual.setText("Julio " + anio);
    }

    @FXML
    private void handleNuevaReserva(ActionEvent event) {
        System.out.println("Abriendo formulario para crear una nueva reserva.");
    }
}