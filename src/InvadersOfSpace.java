package invadersofspace;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 *
 * @author D-Pow
 * 1-2-16
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

class GamePane extends Pane{
    private int Width;
    private int Height;
    Thread gameThread;
    int lives;
    int lifeSymbolSize = 30;
    public ImageView lifeSymbol;
    private Group lifeSymbols = new Group();
    public ImageView spaceShip;
    public int spaceShipSize = 50;
    private boolean justShot;
    private int shotCounter = 0;
    private int bulletSpeed = 10; //In milliseconds
    private Group bullets = new Group();
    private Group aliens = new Group();
    double alienSize;
    int alienMoveCounter = 0;
    int alienMoveDirection = 0; //0 = left, 1 = right
    private Group alienSeekers = new Group();
    private List alienStartPoints = new ArrayList<>();
    private Group alienReturners = new Group();
    private boolean winner = false;
    
    public GamePane(int w, int h){
        this.Width = w;
        this.Height = h;
        lives = 3;
        alienSize = Width/20;
        initKeyListener();
        initObjects();
        
        gameThread = new Thread(()->{
            while (true){
                try {
                    Platform.runLater(()->runGame());
                    TimeUnit.MILLISECONDS.sleep(10);
                }
                catch (Exception ex) {}
            }
        });
        gameThread.start();
    }//End GamePane Constructor
    
    private void initKeyListener(){
        this.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>(){
            @Override
            public void handle(KeyEvent ke) {
                if (ke.getCode() == KeyCode.LEFT){
                    if (spaceShip.getX() > 0){
                        spaceShip.setX(spaceShip.getX() - 5);
                    }
                }
                else if (ke.getCode() == KeyCode.RIGHT){
                    if (spaceShip.getX() < Width - spaceShipSize){
                        spaceShip.setX(spaceShip.getX() + 5);
                    }
                }
                
                if (ke.getCode() == KeyCode.UP || ke.getCode() == KeyCode.SPACE){
                    shoot();
                }
                else if (ke.getCode() == KeyCode.DOWN){
                    endRun();
                }
            }
        });
    }
    
    private void initObjects(){
        spaceShip = new ImageView("SpaceShip.png");
        spaceShip.setFitWidth(spaceShipSize);
        spaceShip.setFitHeight(spaceShipSize);
        spaceShip.setX(Width/2 - spaceShipSize/2);
        spaceShip.setY(Height - spaceShipSize - 10);
        
        for (int i = 0; i < 10; i++){
            for (int j = 0; j < 3; j++){
                Alien alien = new Alien(alienSize);
                //The first term is used to center the group of aliens
                //The second term is to position each individual alien within the group
                //The third term is to add spacing between the aliens
                alien.alienImage.setX(alienSize*5 + i*alienSize + i*5);
                alien.alienImage.setY(alienSize + j*alienSize + j*5);
                aliens.getChildren().add(alien.alienImage);
                alienStartPoints.add(new Tuple(alien.alienImage.getX(), alien.alienImage.getY()));
            }
        }
        
        for (int i = 0; i < lives; i++){
            lifeSymbol = new ImageView("SpaceShip.png");
            lifeSymbol.setFitWidth(lifeSymbolSize);
            lifeSymbol.setFitHeight(lifeSymbolSize);
            lifeSymbol.setX(Width - lifeSymbolSize);
            lifeSymbol.setY(lifeSymbolSize*i);
            lifeSymbols.getChildren().add(lifeSymbol);
        }
        
        this.getChildren().addAll(spaceShip, lifeSymbols, aliens, alienSeekers, bullets);
    }
    
    public void shoot(){
        if (!justShot){
            //Rectangle(x, y, width, height);
            Rectangle bullet = new Rectangle(
                    spaceShip.getX() + spaceShipSize/2 - 1, //-2 for bullet's size
                    spaceShip.getY(),
                    2, 10);
            bullet.setStroke(Color.WHITE);
            bullet.setFill(Color.WHITE);
            bullets.getChildren().add(bullet);
            justShot = true;
        }
    }
    
    public void runGame(){
        shotCounter++;
        if (shotCounter == 100){
            justShot = false;
            shotCounter = 0;
        }
        moveAliens();
        alienSeek();
        for (Node shot : bullets.getChildren()){
            Rectangle bullet = (Rectangle) shot;
            bullet.setY(bullet.getY()-1);
            if (bullet.getY() <= 0 ||
                    checkCollision(bullet.getX(), bullet.getY(), bullet.getWidth(), bullet.getHeight())){
                bullets.getChildren().remove(shot);
            }
        }//End for-loop dealing with bullets
        if (checkCollision(spaceShip.getX(), spaceShip.getY(), spaceShip.getFitWidth(), spaceShip.getFitHeight())){
            endRun();
        }
    }
    
    public boolean checkCollision(double x, double y, double width, double height){
        //This checks the collision of the bullet or spaceShip with all aliens
        for (Node a : aliens.getChildren()){
            ImageView alien = (ImageView) a;
            //If the bullet/spaceShip is within bounds of the alien, mark it as collide
            if (x < alien.getX() + alien.getFitWidth() && x+width > alien.getX()
                    && y < alien.getY() + alien.getFitHeight() && y+height > alien.getY()){
                int index = aliens.getChildren().indexOf(alien);
                alienStartPoints.remove(index);
                aliens.getChildren().remove(a);
                checkIfWinner();
                return true;
            }
        }
        for (Node s : alienSeekers.getChildren()){
            ImageView seeker = (ImageView) s;
            //Since seekers are taken from the end of the aliens group,
            //the start points that correspond with the killed seeker
            //can be taken from the end of the start points list.
            if (x < seeker.getX() + seeker.getFitWidth() && x+width > seeker.getX()
                    && y < seeker.getY() + seeker.getFitHeight() && y+height > seeker.getY()){
                int index = alienSeekers.getChildren().indexOf(seeker);
                alienStartPoints.remove(aliens.getChildren().size() - 1 - index);
                alienSeekers.getChildren().remove(s);
                checkIfWinner();
                return true;
            }
        }
        return false;
    }
    
    public void moveAliens(){
        alienMoveCounter++;
        if (alienMoveCounter%50 == 0 && alienMoveDirection == 0){
            for (Node n : aliens.getChildren()){
                ImageView alien = (ImageView) n;
                alien.setX(alien.getX() - 10);
            }
        }
        if (alienMoveCounter%50 == 0 && alienMoveDirection == 1){
            for (Node n : aliens.getChildren()){
                ImageView alien = (ImageView) n;
                alien.setX(alien.getX() + 10);
            }
        }
        if (alienMoveCounter == 299){
            //Move aliens down a row
            for (Node n : aliens.getChildren()){
                ImageView alien = (ImageView) n;
                alien.setY(alien.getY() + 20);
                if (alien.getY() > Height){
                    alien.setY(-10);
                }
            }
            
            //Change if the aliens move left or right
            alienMoveCounter = 0;
            if (alienMoveDirection == 0){
                alienMoveDirection = 1;
            }
            else if (alienMoveDirection == 1){
                alienMoveDirection = 0;
            }
            if (aliens.getChildren().size() != 1){
                Node last = aliens.getChildren().get(aliens.getChildren().size() - 1);
                aliens.getChildren().remove(aliens.getChildren().size() - 1);
                ImageView a = (ImageView) last;
                alienSeekers.getChildren().add(a);
            }
        }
    }
    
    public void alienSeek(){
        for (Node n : alienSeekers.getChildren()){
            ImageView alien = (ImageView) n;
            //If the alien is close to the ship, go towards the ship for kamikaze
            if (alien.getY() > spaceShip.getY() - 3*spaceShipSize){
                if (alien.getX() < spaceShip.getX()){
                    alien.setX(alien.getX() + 2);
                }
                else if (alien.getX() > spaceShip.getX()){
                    alien.setX(alien.getX() - 2);
                }
                alien.setY(alien.getY() + 1);
            
                if (alien.getY() == Height){
                    alien.setY(0);
                }
            }
            //else: avoid the spaceship so the alien doesn't get shot
            else{
                if (alien.getX() < spaceShip.getX() && alien.getX() > 0 + alien.getFitWidth()){
                    alien.setX(alien.getX() - 1);
                }
                else if (alien.getX() > spaceShip.getX() && alien.getX() < Width - alien.getFitWidth()){
                    alien.setX(alien.getX() + 1);
                }
                alien.setY(alien.getY() + 1);
            }
        }
    }
    
    public void checkIfWinner(){
        if (winner == false && aliens.getChildren().size() == 0 && alienSeekers.getChildren().size() == 0){
            winner = true;
            ImageView Win = new ImageView("Win.png");
            Win.setFitWidth(alienSize*10);
            Win.setFitHeight(alienSize*10);
            Win.setX((Width - Win.getFitWidth())/2);
            Win.setY(10);
            aliens.getChildren().clear();
            //alienSeekers.getChildren().clear();
            this.getChildren().add(Win);
            
            Text message = new Text("You won\nE.T. Thanks you\nfor saving the\ngalaxy" + 
                    "\nPlay again?(y,n)");
            message.setFont(Font.font("Vernanda", 40));
            message.setFill(Color.RED);
            message.setTextAlignment(TextAlignment.CENTER);
            message.setX(Width/4);
            message.setY(Height/2);
            this.getChildren().add(message);
        }
    }
    
    public void destroyShip(){
        if (lives == 0){
            endGame();
            return;
        }
        else{
            lifeSymbols.getChildren().remove(lives-1);
            lives--;
        }
        
        ImageView Exp1 = new ImageView("Explosion_1.png");
        Exp1.setFitWidth(spaceShipSize);
        Exp1.setFitHeight(spaceShipSize);
        Exp1.setX(spaceShip.getX());
        Exp1.setY(spaceShip.getY());
        
        ImageView Exp2 = new ImageView("Explosion_2.png");
        Exp2.setFitWidth(spaceShipSize);
        Exp2.setFitHeight(spaceShipSize);
        Exp2.setX(spaceShip.getX());
        Exp2.setY(spaceShip.getY());
        
        ImageView Exp3 = new ImageView("Explosion_3.png");
        Exp3.setFitWidth(spaceShipSize);
        Exp3.setFitHeight(spaceShipSize);
        Exp3.setX(spaceShip.getX());
        Exp3.setY(spaceShip.getY());
        
        KeyFrame e1 = new KeyFrame(Duration.millis(0), new EventHandler(){
            @Override
            public void handle(Event event) {
                getChildren().remove(spaceShip);
                getChildren().add(Exp1);
            }
        });
        
        KeyFrame e2 = new KeyFrame(Duration.millis(500), new EventHandler(){
            @Override
            public void handle(Event event) {
                getChildren().remove(Exp1);
                getChildren().add(Exp2);
            }
        });
        
        KeyFrame e3 = new KeyFrame(Duration.millis(1000), new EventHandler(){
            @Override
            public void handle(Event event) {
                getChildren().remove(Exp2);
                getChildren().add(Exp3);
            }
        });
        
        KeyFrame rebirth = new KeyFrame(Duration.millis(1500), new EventHandler(){
            @Override
            public void handle(Event event) {
                getChildren().remove(Exp3);
                getChildren().add(spaceShip);
            }
        });
        
        Timeline destruction = new Timeline();
        destruction.setCycleCount(1);
        destruction.getKeyFrames().addAll(e1,e2,e3,rebirth);
        
        destruction.play();
    }
    
    public void endRun(){
        gameThread.stop();
        destroyShip();
        resetPositions();
        
        //Starting a new thread is inefficient. This should be improved at a later time
        gameThread = new Thread(()->{
            while (true){
                try {
                    Platform.runLater(()->runGame());
                    TimeUnit.MILLISECONDS.sleep(10);
                }
                catch (Exception ex) {}
            }
        });
        gameThread.start();
    }
    
    public void resetPositions(){
        try{this.getChildren().remove(spaceShip);}
        catch (Exception e){}
        Node node = aliens.getChildren().get(0);
        ImageView first = (ImageView) node;
        
        //Using the lowest Y-value for an alien, given by alienSize (see initObjects()),
        //move all aliens up until the top row fits the first row where aliens originated
        double distanceToMoveAliens = first.getY() - alienSize;
        for (Node a : aliens.getChildren()){
            ImageView alien = (ImageView) a;
            alien.setY(alien.getY() - distanceToMoveAliens);
        }
        
        for (Node s : alienSeekers.getChildren()){
            ImageView seeker = (ImageView) s;
            int index = alienSeekers.getChildren().indexOf(seeker);
            Tuple resetPoint = (Tuple) alienStartPoints.get(aliens.getChildren().size() - 1 - index);
            //alienSeekers.getChildren().remove(s);
            seeker.setX(resetPoint.getX());
            seeker.setY(resetPoint.getY());
            //aliens.getChildren().add(seeker);
        }
    }
    
    public void endGame(){
        this.getChildren().clear();
        
        Text message = new Text("You lose!\nBut the galaxay\nis still in peril.\n"
                + "Try again?(y,n)");
        message.setFont(Font.font("Vernanda", 40));
        message.setFill(Color.RED);
        message.setTextAlignment(TextAlignment.CENTER);
        message.setX(Width/4);
        message.setY(Height/2);
        this.getChildren().add(message);
    }
}//End class GamePane

class Alien{
    /**
     * This class is only to set the alienImage properties.
     * In reality, only the alienImage is needed after the properties are set.
     */
    public ImageView alienImage;
    public int startX;
    public int startY;
    
    public Alien(double size){
        alienImage = new ImageView("Alien.png");
        alienImage.setFitWidth(size);
        alienImage.setFitHeight(size);
    }
}

class Tuple{
    private final double x;
    private final double y;
    
    public Tuple(double x, double y){
        this.x = x;
        this.y = y;
    }
    
    public double getX(){
        return this.x;
    }
    
    public double getY(){
        return this.y;
    }
}
