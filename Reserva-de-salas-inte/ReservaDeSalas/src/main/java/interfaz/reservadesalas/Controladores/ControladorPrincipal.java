package interfaz.reservadesalas.Controladores;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;

import interfaz.reservadesalas.Servicio.ReservaService;
import interfaz.reservadesalas.Servicio.UsuarioService;
import interfaz.reservadesalas.util.ResourceManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
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
    @FXML protected Label tabUpcomingLabel;
    @FXML protected Separator tabUpcomingSeparator;

    @FXML private VBox tabPastContainer;
    @FXML protected Label tabPastLabel;
    @FXML protected Separator tabPastSeparator;

    @FXML private VBox tabAllContainer;
    @FXML protected Label tabAllLabel;
    @FXML protected Separator tabAllSeparator;
    
    @FXML protected VBox bookingsListContainer; 
    
    @FXML protected GridPane gridCalendario;
    @FXML protected Label labelMesActual;
    @FXML private Node navPerfil;
    
    protected YearMonth mesActual = YearMonth.now();
    protected ReservaService reservaService;
    protected UsuarioService usuarioService;

    @FXML
    public void initialize() {
        reservaService = ReservaService.getInstancia();
        usuarioService = UsuarioService.getInstancia();
        System.out.println("Controlador Principal inicializado.");
        
        if (gridCalendario != null) {
            generarCalendario();
        }
        
        if (bookingsListContainer != null) {
            loadBookings("upcoming");
            if (tabUpcomingLabel != null && tabUpcomingSeparator != null) {
                activateTab(tabUpcomingLabel, tabUpcomingSeparator);
            }
        }
        
        if (navPerfil != null) {
            System.out.println("DEBUG: 'navPerfil' inyectado en ControladorPrincipal.");
            navPerfil.setStyle(navPerfil.getStyle() + "-fx-cursor: hand;");
        } else {
            System.out.println("DEBUG: 'navPerfil' NO inyectado en ControladorPrincipal.");
        }
    }

    protected void resetTabs() {
        if (tabUpcomingLabel != null) tabUpcomingLabel.getStyleClass().setAll("tab-inactive");
        if (tabPastLabel != null) tabPastLabel.getStyleClass().setAll("tab-inactive");
        if (tabAllLabel != null) tabAllLabel.getStyleClass().setAll("tab-inactive");
        
        if (tabUpcomingSeparator != null) tabUpcomingSeparator.setVisible(false);
        if (tabPastSeparator != null) tabPastSeparator.setVisible(false);
        if (tabAllSeparator != null) tabAllSeparator.setVisible(false);
    }

    protected void activateTab(Label label, Separator separator) {
        if (label == null || separator == null) return;
        resetTabs();
        label.getStyleClass().setAll("tab-active");
        separator.setVisible(true);
    }

    protected void loadBookings(String filter) {
        if (bookingsListContainer == null) {
            return;
        }

        bookingsListContainer.getChildren().clear();
        
        try {
            String fxmlFile = "";
            switch (filter) {
                case "past":
                    fxmlFile = "BookingsPast.fxml";
                    break;
                case "all":
                    fxmlFile = "BookingsAll.fxml"; 
                    break;
                case "upcoming":
                default:
                    fxmlFile = "BookingsUpcoming.fxml"; 
                    break;
            }

            java.net.URL resource = ResourceManager.getViewResource(fxmlFile);
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

    protected void generarCalendario() {
        if (gridCalendario == null) return;
        
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
            labelDia.setStyle("-fx-alignment: center; -fx-cursor: hand;");
            
            final int diaFinal = dia;
            labelDia.setOnMouseClicked(e -> {
                System.out.println("Día " + diaFinal + " clickeado");
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
            FXMLLoader loader = new FXMLLoader(ResourceManager.getViewResource("VistaCalendario.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();
            scene.setRoot(root);
            String css = ResourceManager.getStyleExternalForm("EstilosCalendario.css");
            if (css != null) {
                scene.getStylesheets().clear();
                scene.getStylesheets().add(css);
            }
            root.applyCss();
            stage.setTitle("Starsoft - Calendario de Reservas");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cargar la vista del calendario: " + e.getMessage());
        }
    }

    @FXML
    private void cambiarAVistaPerfil(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(ResourceManager.getViewResource("VistaPerfil.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();
            scene.setRoot(root);
            String css = ResourceManager.getStyleExternalForm("EstilosInicio.css");
            if (css != null) {
                scene.getStylesheets().clear();
                scene.getStylesheets().add(css);
            }
            root.applyCss();
            stage.setTitle("Starsoft - Perfil");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cargar la vista del perfil: " + e.getMessage());
        }
    }

    @FXML
    private void cambiarAVistaReporte(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(ResourceManager.getViewResource("VistaReporte.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();
            scene.setRoot(root);
            String css = ResourceManager.getStyleExternalForm("EstilosInicio.css");
            if (css != null) {
                scene.getStylesheets().clear();
                scene.getStylesheets().add(css);
            }
            root.applyCss();
            stage.setTitle("Starsoft - Reporte");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cargar la vista de reporte: " + e.getMessage());
        }
    }

    @FXML
    private void cambiarAVistaNotificaciones(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(ResourceManager.getViewResource("VistaNotificaciones.fxml"));
            Parent root = loader.load();

            Stage owner = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Stage dialog = new Stage();
            dialog.initOwner(owner);
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.initStyle(StageStyle.UTILITY);

            Scene scene = new Scene(root);
            String css = ResourceManager.getStyleExternalForm("EstilosInicio.css");
            if (css != null) {
                scene.getStylesheets().add(css);
            }

            dialog.setScene(scene);
            dialog.setTitle("Notificaciones");
            dialog.setResizable(false);
            dialog.sizeToScene();
            dialog.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cargar la vista de notificaciones: " + e.getMessage());
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
                        b.setOnAction(e -> handleEditAction(e));
                    } else if (b.getStyleClass().contains("btn-cancel")) {
                        b.setOnAction(e -> handleCancelAction(e));
                    }
                }
                attachHandlersToPartial(child);
            }
        }
    }

    @FXML
    private void cambiarAVistaInicio(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(ResourceManager.getViewResource("VistaInicio.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();
            scene.setRoot(root);
            String css = ResourceManager.getStyleExternalForm("EstilosInicio.css");
            if (css != null) {
                scene.getStylesheets().clear();
                scene.getStylesheets().add(css);
            }
            root.applyCss();
            stage.setTitle("Starsoft - Mis Reservas");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cargar la vista de inicio: " + e.getMessage());
        }
    }

    @FXML
    protected void handleNuevaReserva(ActionEvent event) {
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
    private TextField campoSala;
    @FXML
    private TextField campoFecha;
    @FXML
    private TextField campoHoraInicio;
    @FXML
    private TextField campoHoraFin;
    @FXML
    private TextField campoMotivo;
    @FXML
    private javafx.scene.layout.HBox alertaConflicto;
    @FXML
    private Label labelAlerta;

    @FXML
    private void handleConfirmarReserva(ActionEvent event) {
        if (usuarioService.getUsuarioActual() == null) {
            mostrarAlertaError("Debe iniciar sesión para crear una reserva.");
            return;
        }

        if (campoSala == null || campoFecha == null || campoHoraInicio == null || 
            campoHoraFin == null || campoMotivo == null) {
            System.err.println("Error: Campos del formulario no encontrados.");
            return;
        }

        String salaNombre = campoSala.getText().trim();
        String fechaStr = campoFecha.getText().trim();
        String horaInicioStr = campoHoraInicio.getText().trim();
        String horaFinStr = campoHoraFin.getText().trim();
        String motivo = campoMotivo.getText().trim();

        if (salaNombre.isEmpty() || fechaStr.isEmpty() || horaInicioStr.isEmpty() || 
            horaFinStr.isEmpty() || motivo.isEmpty()) {
            mostrarAlertaError("Por favor, complete todos los campos.");
            return;
        }

        try {
            interfaz.reservadesalas.Servicio.SalaService salaService = interfaz.reservadesalas.Servicio.SalaService.getInstancia();
            java.util.Optional<interfaz.reservadesalas.Modelo.Sala> salaOpt = salaService.getTodasLasSalas().stream()
                    .filter(s -> s.getNombre().equalsIgnoreCase(salaNombre) || s.getId().equalsIgnoreCase(salaNombre))
                    .findFirst();

            if (!salaOpt.isPresent()) {
                mostrarAlertaError("Sala no encontrada. Por favor, verifique el nombre.");
                return;
            }

            interfaz.reservadesalas.Modelo.Sala sala = salaOpt.get();
            java.time.LocalDate fecha = java.time.LocalDate.parse(fechaStr);
            java.time.LocalTime horaInicio = java.time.LocalTime.parse(horaInicioStr);
            java.time.LocalTime horaFin = java.time.LocalTime.parse(horaFinStr);

            if (horaFin.isBefore(horaInicio) || horaFin.equals(horaInicio)) {
                mostrarAlertaError("La hora de fin debe ser posterior a la hora de inicio.");
                return;
            }

            boolean exito = reservaService.crearReserva(
                usuarioService.getUsuarioActual(),
                sala,
                fecha,
                horaInicio,
                horaFin,
                motivo
            );

            if (exito) {
                mostrarAlertaInfo("Reserva creada exitosamente.");
                cambiarAVistaInicioDesdeEvento(event);
            } else {
                mostrarAlertaError("No se pudo crear la reserva. Puede haber un conflicto de horario.");
            }

        } catch (java.time.format.DateTimeParseException e) {
            mostrarAlertaError("Formato de fecha u hora inválido. Use YYYY-MM-DD para fecha y HH:MM para hora.");
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlertaError("Error al crear la reserva: " + e.getMessage());
        }
    }

    private void cambiarAVistaInicioDesdeEvento(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(ResourceManager.getViewResource("VistaInicio.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();
            scene.setRoot(root);
            String css = ResourceManager.getStyleExternalForm("EstilosInicio.css");
            if (css != null) {
                scene.getStylesheets().clear();
                scene.getStylesheets().add(css);
            }
            root.applyCss();
            stage.setTitle("Starsoft - Mis Reservas");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al volver a Inicio: " + e.getMessage());
        }
    }

    protected void mostrarAlertaError(String mensaje) {
        javafx.scene.control.Alert alerta = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alerta.setTitle("Error");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    protected void mostrarAlertaInfo(String mensaje) {
        javafx.scene.control.Alert alerta = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alerta.setTitle("Éxito");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    @FXML
    protected void handleEditAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(ResourceManager.getViewResource("VistaModificarReserva.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();
            String css = ResourceManager.getStyleExternalForm("EstilosInicio.css");
            if (css != null) {
                scene.getStylesheets().setAll(css);
            }
            scene.setRoot(root);
            stage.setTitle("Starsoft - Modificar Reserva");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cargar la vista de modificar reserva: " + e.getMessage());
        }
    }
    
    @FXML
    protected void handleGuardarCambios(ActionEvent event) {
        handleConfirmarReserva(event);
    }

    @FXML
    protected void handleCancelAction(ActionEvent event) {
        Button sourceButton = (Button) event.getSource();
        String reservaId = (String) sourceButton.getUserData();
        
        if (reservaId != null && usuarioService.getUsuarioActual() != null) {
            boolean cancelado = reservaService.cancelarReserva(reservaId);
            if (cancelado) {
                mostrarAlertaInfo("Reserva cancelada exitosamente.");
                if (bookingsListContainer != null) {
                    String filtroActual = tabUpcomingLabel != null && tabUpcomingLabel.getStyleClass().contains("tab-active") ? "upcoming" :
                                         tabPastLabel != null && tabPastLabel.getStyleClass().contains("tab-active") ? "past" : "all";
                    loadBookings(filtroActual);
                }
            } else {
                mostrarAlertaError("No se pudo cancelar la reserva.");
            }
        }
    }
    
}