module com.atps.automatedtextprocessingsystem {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.logging;

    opens com.atps.automatedtextprocessingsystem to javafx.fxml;
    exports com.atps.automatedtextprocessingsystem;
}