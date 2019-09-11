import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.UnknownHostException;

public class ClientGUI extends Application {

    Client client;
    StringBuilder log;

    public ClientGUI(Client c){
        this.client = c;
        this.log = c.log;

    }

    public static void main(String[] args) throws UnknownHostException, IOException {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("Chat Server");

        VBox root = new VBox(5);
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: lightgray");

        Pane glass = new Pane();
        glass.setStyle("-fx-background-color: transparent");
        root.getChildren().add(glass);

        Text logDisplay = new Text(log.toString());
        logDisplay.setWrappingWidth(670);
        logDisplay.setX(15);
        logDisplay.setStroke(Color.WHITE);

        Pane glass2 = new Pane();
        glass2.setStyle("-fx-background-color: transparent");
        glass2.getChildren().add(logDisplay);

        ScrollPane chatBox = new ScrollPane();
        chatBox.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        chatBox.setContent(glass2);
        chatBox.setMaxWidth(700);
        chatBox.setMaxHeight(450);
        chatBox.setMinHeight(450);
        chatBox.setTranslateY(50);
        chatBox.setTranslateX(50);
        chatBox.setStyle("-fx-background-color: black; -fx-control-inner-background: black;" +
                " -fx-background-insets:0; fx-border-color:blue; -fx-border-width:1; " +
                "-fx-background: black; -fx-border-insets:0;");

        root.getChildren().add(chatBox);

        Text header = new Text("On This Server:");
        header.setStroke(Color.NAVY);
        header.setTranslateX(-370);
        header.setTranslateY(-411);
        header.setFont(Font.font ("Verdana", 16));

        StringBuilder listMaker = new StringBuilder();
        listMaker.append("\n" + "ambajamba");
        listMaker.append("\n" + "Arl.in");
        listMaker.append("\n" + "Candid");
        listMaker.append("\n" + "helium");

        Text names = new Text(listMaker.toString());
        names.setStroke(Color.DARKGOLDENROD);
        names.setX(7);
        names.setY(72);
        names.setFont(Font.font ("Verdana", 13));

        root.getChildren().addAll(header);
        glass.getChildren().add(names);

        TextArea textField = new TextArea("Enter text here...");
        textField.setMaxWidth(700);
        textField.setMaxHeight(70);
        textField.setMinHeight(70);
        textField.setWrapText(true);
        textField.setTranslateY(30);
        textField.setTranslateX(50);
        textField.setStyle("-fx-control-inner-background: grey;" +
                "-fx-background: grey; -fx-border-insets:0;");

        TextField recipientField = new TextField("Recipient:");
        recipientField.setMaxWidth(95);
        recipientField.setMinWidth(95);
        recipientField.setMaxHeight(15);
        recipientField.setTranslateY(-44);
        recipientField.setTranslateX(-370);
        recipientField.setStyle("-fx-control-inner-background: grey;" +
                "-fx-background: grey; -fx-border-insets:0;");


        Button buttonClear = new Button("Clear");
        buttonClear.setTranslateX(-395);
        buttonClear.setTranslateY(-35);
        buttonClear.setStyle("-fx-control-inner-background: grey; -fx-background-color: grey;"
                + "-fx-text-fill: black;");
        buttonClear.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                textField.clear();
                recipientField.clear();
            }
        });

        Button buttonSend = new Button("Send");
        buttonSend.setTranslateX(-345);
        buttonSend.setTranslateY(-65);
        buttonSend.setStyle("-fx-control-inner-background: grey; -fx-background-color: grey;"
                + "-fx-text-fill: black;");
        buttonSend.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                System.out.println(textField.getText());
                textField.clear();
                recipientField.clear();
            }
        });

        root.setPadding(new Insets(10));

        root.getChildren().addAll(textField, recipientField, buttonClear, buttonSend);

        primaryStage.setWidth(900);
        primaryStage.setHeight(600);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
}
