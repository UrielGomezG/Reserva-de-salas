package interfaz.reservadesalas.Controladores;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ControladorPrincipal

{

    @FXML private Label tabUpcoming;
    @FXML private Label tabPast;
    @FXML private Label tabAll;

    @FXML private VBox tabUpcomingContainer;
    @FXML private Label tabUpcomingLabel;
    @FXML private Separator tabUpcomingSeparator;

    @FXML private VBox tabPastContainer;
    @FXML private Label tabPastLabel;
    @FXML private Separator tabPastSeparator;

    @FXML private VBox tabAllContainer;
    @FXML private Label tabAllLabel;
    @FXML private Separator tabAllSeparator;
    
    @FXML private VBox bookingsListContainer; 
    
    @FXML private GridPane gridCalendario;
    @FXML private Label labelMesActual;
    @FXML private Node navPerfil;
    
    private YearMonth mesActual = YearMonth.now();

    @FXML
    public void initialize() {
        System.out.println("Controlador Principal inicializado.");
        
        if (gridCalendario != null) {
            generarCalendario();
        }
        
        if (bookingsListContainer != null) {
            loadBookings("upcoming");
            activateTab(tabUpcomingLabel, tabUpcomingSeparator);
        }
        
        if (navPerfil != null) {
            System.out.println("DEBUG: 'navPerfil' inyectado en ControladorPrincipal.");
            navPerfil.setStyle(navPerfil.getStyle() + "-fx-cursor: hand;");
        } else {
            System.out.println("DEBUG: 'navPerfil' NO inyectado en ControladorPrincipal.");
        }
    }

    private void resetTabs() {
        tabUpcomingLabel.getStyleClass().setAll("tab-inactive");
        tabPastLabel.getStyleClass().setAll("tab-inactive");
        tabAllLabel.getStyleClass().setAll("tab-inactive");
        
        tabUpcomingSeparator.setVisible(false);
        tabPastSeparator.setVisible(false);
        tabAllSeparator.setVisible(false);
    }

    private void activateTab(Label label, Separator separator) {
        resetTabs();
        label.getStyleClass().setAll("tab-active");
        separator.setVisible(true);
    }

    private void loadBookings(String filter) {
        if (bookingsListContainer == null) {
            return;
        }

        bookingsListContainer.getChildren().clear();
        
        try {
            String fxmlFile = "";
            switch (filter) {
                case "past":
                    fxmlFile = "/interfaz/reservadesalas/Vista/BookingsPast.fxml";
                    break;
                case "all":
                    fxmlFile = "/interfaz/reservadesalas/Vista/BookingsAll.fxml"; 
                    break;
                case "upcoming":
                default:
                    fxmlFile = "/interfaz/reservadesalas/Vista/BookingsUpcoming.fxml"; 
                    break;
            }

            System.out.println("DEBUG: cargar bookings partial -> " + fxmlFile);
            java.net.URL resource = getClass().getResource(fxmlFile);
            if (resource == null) {
                String msg = "Recurso parcial no encontrado: " + fxmlFile;
                System.err.println("ERROR: " + msg);
                Label errorLabel = new Label(msg);
                errorLabel.setStyle("-fx-text-fill: red;");
                bookingsListContainer.getChildren().add(errorLabel);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Node newContent = loader.load();
            attachHandlersToPartial(newContent);
            
            if (newContent instanceof VBox) {
                bookingsListContainer.getChildren().addAll(((VBox) newContent).getChildren());
            } else {
                 bookingsListContainer.getChildren().add(newContent);
            }
            

        } catch (IOException e) {
            System.err.println("Error al cargar la vista de reservas (" + filter + "): " + e.getMessage());
            e.printStackTrace();
            Label errorLabel = new Label("Error al cargar las reservas. VERIFICA LA RUTA DEL FXML: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red;");
            bookingsListContainer.getChildren().add(errorLabel);
        }
    }
    
    @FXML
    private void handleFilterUpcoming(MouseEvent event) {
        System.out.println("Acción: Filtrando Próximas Reservas");
        activateTab(tabUpcomingLabel, tabUpcomingSeparator);
        loadBookings("upcoming");
    }

    @FXML
    private void handleFilterPast(MouseEvent event) {
        System.out.println("Acción: Filtrando Reservas Pasadas");
        activateTab(tabPastLabel, tabPastSeparator);
        loadBookings("past");
    }

    @FXML
    private void handleFilterAll(MouseEvent event) {
        System.out.println("Acción: Filtrando Todas las Reservas");
        activateTab(tabAllLabel, tabAllSeparator);
        loadBookings("all");
    }

    private void generarCalendario() {
        gridCalendario.getChildren().clear();
        
        LocalDate primerDia = mesActual.atDay(1);
        int diasDelMes = mesActual.lengthOfMonth();
        int diaDelaSemanaInicio = primerDia.getDayOfWeek().getValue() % 7; 
        
        if (labelMesActual != null) {
            labelMesActual.setText(mesActual.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy", new java.util.Locale("es", "ES"))));
        }
        
        int fila = 0;
        int columna = diaDelaSemanaInicio;
        
        for (int dia = 1; dia <= diasDelMes; dia++) {
            Label labelDia = new Label(String.valueOf(dia));
            labelDia.getStyleClass().addAll("day-number");
            labelDia.setStyle("-fx-alignment: center;");
            
            final int diaFinal = dia;
            labelDia.setOnMouseClicked(e -> {
                System.out.println("Día " + diaFinal + " clickeado");
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
    private void handlePreviousMonth(MouseEvent event) {
        System.out.println("Calendario: Mes Anterior");
        mesActual = mesActual.minusMonths(1);
        generarCalendario();
    }

    @FXML
    private void handleNextMonth(MouseEvent event) {
        System.out.println("Calendario: Mes Siguiente");
        mesActual = mesActual.plusMonths(1);
        generarCalendario();
    }

    @FXML
    private void handleDayClick(MouseEvent event) {
        System.out.println("Calendario: Día clickeado");
    }
 
    @FXML
    private void cambiarAVistaCalendario(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaz/reservadesalas/Vista/VistaCalendario.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();
            scene.setRoot(root);
            String css = getClass().getResource("/interfaz/reservadesalas/CSS/EstilosCalendario.css").toExternalForm();
            scene.getStylesheets().clear();
            scene.getStylesheets().add(css);
            root.applyCss();
            stage.setTitle("Starsoft - Calendario de Reservas");
            stage.show();

            System.out.println("Navegación exitosa: Vista Calendario cargada.");

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cargar la vista del calendario. VERIFICA LA RUTA: " + e.getMessage());
        }
    }

    @FXML
    private void cambiarAVistaPerfil(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaz/reservadesalas/Vista/VistaPerfil.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();
            scene.setRoot(root);
            String css = getClass().getResource("/interfaz/reservadesalas/CSS/EstilosInicio.css").toExternalForm();
            scene.getStylesheets().clear();
            scene.getStylesheets().add(css);
            root.applyCss();
            stage.setTitle("Starsoft - Perfil");
            stage.show();

            System.out.println("Navegación exitosa: Vista Perfil cargada.");

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cargar la vista del perfil. VERIFICA LA RUTA: " + e.getMessage());
        }
    }

    @FXML
    private void cambiarAVistaReporte(MouseEvent event) {
        try {
            String path = "/interfaz/reservadesalas/Vista/VistaReporte.fxml";
            System.out.println("DEBUG: cargar VistaReporte FXML -> " + path);
            java.net.URL res = getClass().getResource(path);
            System.out.println("DEBUG: resourceUrl = " + res);
            if (res == null) throw new IOException("Recurso no encontrado: " + path);
            FXMLLoader loader = new FXMLLoader(res);
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();
            scene.setRoot(root);
            String css = getClass().getResource("/interfaz/reservadesalas/CSS/EstilosInicio.css").toExternalForm();
            scene.getStylesheets().clear();
            scene.getStylesheets().add(css);
            root.applyCss();
            stage.setTitle("Starsoft - Reporte");
            stage.show();

            System.out.println("Navegación exitosa: Vista Reporte cargada.");

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cargar la vista de reporte. VERIFICA LA RUTA: " + e.getMessage());
        }
    }

    @FXML
    private void cambiarAVistaNotificaciones(MouseEvent event) {
        try {
            String path = "/interfaz/reservadesalas/Vista/VistaNotificaciones.fxml";
            System.out.println("DEBUG: cargar VistaNotificaciones FXML -> " + path);
            java.net.URL res = getClass().getResource(path);
            System.out.println("DEBUG: resourceUrl = " + res);
            if (res == null) throw new IOException("Recurso no encontrado: " + path);
            FXMLLoader loader = new FXMLLoader(res);
            Parent root = loader.load();

            Stage owner = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Stage dialog = new Stage();
            dialog.initOwner(owner);
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.initStyle(StageStyle.UTILITY);

            Scene scene = new Scene(root);
            try {
                String css = getClass().getResource("/interfaz/reservadesalas/CSS/EstilosInicio.css").toExternalForm();
                scene.getStylesheets().add(css);
            } catch (Exception ex) {
                System.err.println("Advertencia: no se pudo cargar CSS para el diálogo de notificaciones: " + ex.getMessage());
            }

            dialog.setScene(scene);
            dialog.setTitle("Notificaciones");
            dialog.setResizable(false);
            dialog.sizeToScene();
            dialog.showAndWait();

            System.out.println("Navegación exitosa: Diálogo de Notificaciones mostrado.");

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cargar la vista de notificaciones. VERIFICA LA RUTA: " + e.getMessage());
        }
    }

    @FXML
    private void handleNotificacion(MouseEvent event) {
        cambiarAVistaNotificaciones(event);
    }

    private void attachHandlersToPartial(javafx.scene.Node node) {
        if (node instanceof Parent) {
            Parent parent = (Parent) node;
            for (javafx.scene.Node child : parent.getChildrenUnmodifiable()) {
                if (child instanceof Button) {
                    Button b = (Button) child;
                    if (b.getStyleClass().contains("btn-edit")) {
                        b.setOnAction(e -> handleEditAction(new ActionEvent(b, e.getTarget())));
                    } else if (b.getStyleClass().contains("btn-cancel")) {
                        b.setOnAction(e -> handleCancelAction(new ActionEvent(b, e.getTarget())));
                    }
                }
                attachHandlersToPartial(child);
            }
        }
    }

    @FXML
    private void cambiarAVistaInicio(MouseEvent event) {
        try {
            String path = "/interfaz/reservadesalas/Vista/VistaInicio.fxml";
            System.out.println("DEBUG: cargar VistaInicio FXML -> " + path);
            java.net.URL res = getClass().getResource(path);
            System.out.println("DEBUG: resourceUrl = " + res);
            FXMLLoader loader = new FXMLLoader(res);
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();
            scene.setRoot(root);
            String css = getClass().getResource("/interfaz/reservadesalas/CSS/EstilosInicio.css").toExternalForm();
            scene.getStylesheets().clear();
            scene.getStylesheets().add(css);
            root.applyCss();
            stage.setTitle("Starsoft - Mis Reservas");
            stage.show();

            System.out.println("Navegación exitosa: Vista Inicio cargada.");

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cargar la vista de inicio. VERIFICA LA RUTA: " + e.getMessage());
        }
    }

    @FXML
    private void handleNuevaReserva(ActionEvent event) {
        System.out.println("Calendario: Abrir formulario Nueva Reserva");
        
        final String path = "/interfaz/reservadesalas/Vista/VistaNuevaReserva.fxml";

        try {
            System.out.println("DEBUG: Intentando cargar FXML de Nueva Reserva -> " + path);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();
            scene.setRoot(root);
            String css = getClass().getResource("/interfaz/reservadesalas/CSS/EstilosInicio.css").toExternalForm();
            scene.getStylesheets().clear();
            scene.getStylesheets().add(css);
            root.applyCss();
            stage.setTitle("Starsoft - Nueva Reserva");
            stage.show();

            System.out.println("Navegación exitosa: Vista Nueva Reserva cargada.");

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("¡ERROR AL CARGAR VISTA NUEVA RESERVA! VERIFICA RUTA: " + e.getMessage());
        }
    }

    @FXML
    private void handleConfirmarReserva(ActionEvent event) {
        System.out.println("Nueva Reserva: Confirmar Reserva (botón clicado)");

        try {
            String path = "/interfaz/reservadesalas/Vista/VistaInicio.fxml";
            java.net.URL res = getClass().getResource(path);
            if (res == null) throw new IOException("Recurso no encontrado: " + path);
            FXMLLoader loader = new FXMLLoader(res);
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();
            scene.setRoot(root);
            String css = getClass().getResource("/interfaz/reservadesalas/CSS/EstilosInicio.css").toExternalForm();
            scene.getStylesheets().clear();
            scene.getStylesheets().add(css);
            root.applyCss();
            stage.setTitle("Starsoft - Mis Reservas");
            stage.show();

            System.out.println("Navegación: retorno a Vista Inicio tras confirmar reserva.");

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al confirmar reserva / volver a Inicio: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditAction(ActionEvent event) {
        System.out.println("Acción: Editar Reserva (Botón Clickeado). Navegando a Modificar Reserva.");
        
        final String path = "/interfaz/reservadesalas/Vista/VistaModificarReserva.fxml";

        try {
            System.out.println("DEBUG: Intentando cargar FXML de Modificar Reserva -> " + path);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();
            String css = getClass().getResource("/interfaz/reservadesalas/CSS/EstilosInicio.css").toExternalForm();
            scene.getStylesheets().setAll(css);
            scene.setRoot(root);
            stage.setTitle("Starsoft - Modificar Reserva");
            stage.show();

            System.out.println("Navegación exitosa: Vista Modificar Reserva cargada.");

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("¡ERROR AL CARGAR VISTA MODIFICAR RESERVA! VERIFICA RUTA: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleGuardarCambios(ActionEvent event) {
        System.out.println("Acción: Guardar Cambios en Reserva");
        handleConfirmarReserva(event);
    }

    @FXML
    private void handleCancelAction(ActionEvent event) {
        System.out.println("Acción: Cancelar Reserva (Botón Clickeado)");
        try {
            handleConfirmarReserva(event);
        } catch (Exception ex) {
            System.err.println("Error al procesar cancelar: " + ex.getMessage());
        }
    }
    
}