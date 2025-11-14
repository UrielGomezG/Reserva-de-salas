module interfaz.reservadesalas {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;

    opens interfaz.reservadesalas.Lanzador to javafx.graphics;

    opens interfaz.reservadesalas.Controladores to javafx.fxml;

}