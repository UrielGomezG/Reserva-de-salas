package interfaz.reservadesalas.Controladores;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import interfaz.reservadesalas.Servicio.ReservaService;
import interfaz.reservadesalas.Servicio.UsuarioService;
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
        super.initialize();
        System.out.println("Controlador de Calendario de Reservas inicializado.");
        if (gridCalendario != null && labelMesActual != null) {
            generarCalendario();
        }
    }

    @FXML
    private void handleNextMonth(MouseEvent event) {
        System.out.println("Navegando al mes siguiente...");
        mesActual = mesActual.plusMonths(1);
        generarCalendario();
    }

    @FXML
    private void handlePreviousMonth(MouseEvent event) {
        System.out.println("Navegando al mes anterior...");
        mesActual = mesActual.minusMonths(1);
        generarCalendario();
    }
    
    @FXML
    private void handleDayClick(MouseEvent event) {
        Label dia = (Label) event.getSource();
        String textoDia = dia.getText();
        if (!textoDia.isEmpty() && !textoDia.trim().equals("")) {
            try {
                int diaNum = Integer.parseInt(textoDia);
                LocalDate fechaSeleccionada = mesActual.atDay(diaNum);
                System.out.println("Día seleccionado: " + fechaSeleccionada);
                // Aquí se podría abrir un diálogo para ver las reservas del día
            } catch (NumberFormatException e) {
                System.out.println("Día seleccionado: " + textoDia);
            }
        }
    }

    protected void generarCalendario() {
        if (gridCalendario == null || labelMesActual == null) {
            return;
        }

        gridCalendario.getChildren().clear();
        
        // Actualizar label del mes
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("es", "ES"));
        labelMesActual.setText(mesActual.format(formatter));
        
        // Obtener el primer día del mes y calcular el día de la semana
        LocalDate primerDia = mesActual.atDay(1);
        int diasDelMes = mesActual.lengthOfMonth();
        int diaDelaSemanaInicio = primerDia.getDayOfWeek().getValue() % 7; // 0 = Domingo, 1 = Lunes, etc.
        
        int fila = 0;
        int columna = diaDelaSemanaInicio;
        
        // Agregar días del mes
        for (int dia = 1; dia <= diasDelMes; dia++) {
            Label labelDia = new Label(String.valueOf(dia));
            labelDia.getStyleClass().addAll("day-number");
            labelDia.setStyle("-fx-alignment: center; -fx-cursor: hand;");
            
            final int diaFinal = dia;
            labelDia.setOnMouseClicked(e -> {
                LocalDate fecha = mesActual.atDay(diaFinal);
                System.out.println("Día " + diaFinal + " clickeado - Fecha: " + fecha);
                handleDayClick(e);
            });
            
            gridCalendario.add(labelDia, columna, fila);
            
            columna++;
            if (columna == 7) {
                columna = 0;
                fila++;
            }
        }
    }

    @FXML
    protected void handleNuevaReserva(ActionEvent event) {
        System.out.println("Abriendo formulario para crear una nueva reserva.");
        super.handleNuevaReserva(event);
    }
}