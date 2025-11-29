module interfaz.sara {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    
    requires java.sql;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;

    opens interfaz.sara to javafx.fxml;
    opens interfaz.sara.Controladores to javafx.fxml;
    opens interfaz.sara.Controladores.Usuario to javafx.fxml;
    opens interfaz.sara.Controladores.Admin to javafx.fxml;
    exports interfaz.sara;
    exports interfaz.sara.ConexionBD;
}