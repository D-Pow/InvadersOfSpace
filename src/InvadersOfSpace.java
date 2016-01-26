package invadersofspace;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

/**
 *
 * @author DP, 1-25-16
 */
public class InvadersOfSpace extends Application{
    public int width = 500;
    public int height = 650;
    
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage){
        Text startMessage = new Text("Invaders of Space\nPress \"Y\" to play");
        startMessage.setFont(Font.font("Vernanda", 40));
        startMessage.setFill(Color.RED);
        startMessage.setTextAlignment(TextAlignment.CENTER);
        startMessage.setX(width/4);
        startMessage.setY(height/2);
        
        StackPane startPane = new StackPane();
        startPane.getChildren().addAll(new ImageView("/SpaceBackground.png"), startMessage);
        Scene startScene = new Scene(startPane, width, height);
        
        primaryStage.setScene(startScene);
        primaryStage.setTitle("Invaders of Space!");
        primaryStage.setResizable(false);
        primaryStage.show();
        
        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>(){

            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.Y){
                    GamePane pane = new GamePane(width, height); //holds game functionality
                    pane.setFocusTraversable(true);//Very important! Allows the game pane to be focused on
                    pane.setMinSize(width, height);
                    pane.setPrefSize(width, height);
        
                    StackPane root = new StackPane();
                    root.getChildren().addAll(new ImageView("/SpaceBackground.png"),pane);
        
                    Scene gameScene = new Scene(root, width, height);
                    primaryStage.setScene(gameScene);
                }
                else if (event.getCode() == KeyCode.N){
                    stop();
                }
            }
        });//End KeyEventHandler
    }//End start() method
    
    @Override
    public void stop(){
        System.exit(0);
    }
}//End class InvadersOfSpace
