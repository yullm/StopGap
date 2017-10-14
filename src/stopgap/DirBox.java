package stopgap;

/*
    Simple Class for holding informatin about about child directories visually.
*/

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class DirBox extends HBox{
    
    public CheckBox asDir;
    public TextField folderDir;
    public Button search;
    public Button delete;
    
    public DirBox(VBox root){
        super();
        
        this.setPadding(new Insets(10));
        this.setSpacing(8);
        
        asDir = new CheckBox(": Copy as Folder");
        asDir.setSelected(true);
        
        folderDir = new TextField();
        folderDir.prefWidthProperty().bind(root.widthProperty().subtract(260));
        
        search = new Button("Select Folder");
        delete = new Button("X");
        
        this.getChildren().addAll(asDir,folderDir,search,delete);
    }
}
