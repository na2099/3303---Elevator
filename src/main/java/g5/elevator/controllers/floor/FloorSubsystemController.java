package g5.elevator.controllers.floor;

import g5.elevator.SchedulerLauncher;
import g5.elevator.controllers.Updatable;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class FloorSubsystemController implements Initializable, Updatable {
    @FXML
    GridPane startPane;
    @FXML
    Slider startSlider;
    @FXML
    Label startCountLabel;
    @FXML
    FlowPane floors;
    @FXML
    ScrollPane root;
    private ArrayList<FXMLLoader> floorPages;
    private boolean started = false;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        floorPages = new ArrayList<>();
        startCountLabel.textProperty().bind(Bindings.format("%.0f", startSlider.valueProperty()));
        update();
    }

    @Override
    public void update() {
        startPane.setVisible(!started);
        startPane.setMaxHeight(started ? 0 : startPane.getMaxHeight());
    }



    @FXML
    public void startHandler() {
        if(startSlider.getValue() < 1) return;
        for(int i = 0; i < startSlider.getValue(); i++) {

            FXMLLoader fxmlLoader = new FXMLLoader(SchedulerLauncher.class.getResource("floor-view.fxml"));
            try {
                fxmlLoader.load();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            floors.getChildren().add(fxmlLoader.getRoot());
            floorPages.add(fxmlLoader);
            FloorNodeController controller = fxmlLoader.getController();
            controller.init(i);
        }
        VBox pane = floorPages.get(0).getRoot();
        root.setPrefWidth(pane.getWidth() + 10);
        root.setPrefHeight(pane.getHeight() + 50);
        started = true;
        update();
    }


    public void close() {
        for(FXMLLoader fxml : floorPages) {
            FloorNodeController controller = fxml.getController();
            controller.close();
        };
    }
}