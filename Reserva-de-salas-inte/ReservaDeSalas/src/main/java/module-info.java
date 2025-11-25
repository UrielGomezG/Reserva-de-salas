module interfaz.reservadesalas {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;

    opens interfaz.reservadesalas.Lanzador to javafx.graphics;
    opens interfaz.reservadesalas.Controladores to javafx.fxml;
    opens interfaz.reservadesalas.Modelo to javafx.base;
    opens interfaz.reservadesalas.Servicio to javafx.base;
    opens interfaz.reservadesalas.util to javafx.base;
    opens interfaz.reservadesalas.database to javafx.base;
}