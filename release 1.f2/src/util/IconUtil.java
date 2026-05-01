package util;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * This class is to be used as a repeatable template for adding Icon images
 * wherever needed to avoid repeating long blocks of code for each Icon.
 * @author Justin Hovious
 */
public class IconUtil {
    
    public static ImageView loadIcon(String path, double size) {
        Image img = new Image(IconUtil.class.getResourceAsStream(path));
        ImageView view = new ImageView(img);
        view.setFitWidth(size);
        view.setFitHeight(size);
        view.setPreserveRatio(true);
        return view;
    }
    
}
