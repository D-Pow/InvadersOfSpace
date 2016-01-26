package invadersofspace;

import javafx.scene.image.ImageView;

public class Alien{
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
