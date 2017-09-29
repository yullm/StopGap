package stopgap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class Stopgap extends Application {
    
    private static BorderPane root;
    private static DirectoryChooser chooser;
    private static TextField hostDir;
    private static VBox directoryPane;
    private static ArrayList<DirBox> directories;
    
    @Override
    public void start(Stage primaryStage) {
        directories = new ArrayList();
        chooser = new DirectoryChooser();
        chooser.setTitle("File Chooser");
        File defaultDir = new File("D:/Applications");
        chooser.setInitialDirectory(defaultDir);
        
        root = new BorderPane();
        SetupTop(primaryStage);
        SetupCenter();
        SetupBottom(primaryStage);
        
        Scene scene = new Scene(root, 500, 300);
        
        primaryStage.setTitle("Meglofriend's Stopgap");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    private static void SetupTop(Stage primaryStage){
        HBox hostBox = new HBox();
        hostBox.setPadding(new Insets(10));
        hostBox.setSpacing(8);
        
        Label hostTitle = new Label("Host Directory:");
        hostDir = new TextField();
        Button hostSearch = new Button("Select Host");
        
        hostTitle.setPrefWidth(80);
        hostSearch.setPrefWidth(80);
        
        hostDir.prefWidthProperty().bind(root.widthProperty()
                .subtract(200));
        
        hostSearch.setOnAction((ActionEvent) -> {
            chooser.setTitle("Select Host Directory");
            try{
                hostDir.setText(chooser.showDialog(primaryStage).getPath());
            }catch(NullPointerException e){}//Cancelled search
        });
        
        hostBox.getChildren().addAll(hostTitle,hostDir,hostSearch);
        root.setTop(hostBox);
    }
    
    private static void SetupCenter(){
        ScrollPane sp = new ScrollPane();
        sp.isFitToWidth();
        directoryPane = new VBox();
        directoryPane.prefWidthProperty().bind(sp.widthProperty().subtract(15));
        sp.setContent(directoryPane);
        root.setCenter(sp);
    }
    
    private static void SetupBottom(Stage primaryStage){
        //Divs setup
        HBox wrapper = new HBox();
        HBox left = new HBox();
        HBox right = new HBox();
        left.prefWidthProperty().bind(root.widthProperty().divide(4).multiply(3));
        right.prefWidthProperty().bind(root.widthProperty().divide(4));
        left.setAlignment(Pos.CENTER_LEFT);
        right.setAlignment(Pos.CENTER_RIGHT);
        left.setPadding(new Insets(10));
        left.setSpacing(8);
        right.setPadding(new Insets(10));
        right.setSpacing(8);
        wrapper.getChildren().addAll(left,right);
        root.setBottom(wrapper);
        //Left Setup
        Button add = new Button("+ Add Directory");
        Button save = new Button("Save");
        save.setOnAction((event)->{SaveConfiguration(primaryStage);});
        Button load = new Button("Load");
        load.setOnAction((event)->{LoadConfiguration(primaryStage);});
        add.setOnAction((ActionEvent)->{
            DirBox db = new DirBox(directoryPane);
            directories.add(db);
            db.search.setOnAction((event)->{
                chooser.setTitle("Select Child Directory");
                try{
                    db.folderDir.setText(chooser.showDialog(primaryStage).getPath());
                }catch(NullPointerException e){}//Cancelled search
            });
            db.delete.setOnAction((event)->{
                directoryPane.getChildren().remove(db);
                directories.remove(db);
            });
            directoryPane.getChildren().add(db);
        });
        add.fire();
        
        left.getChildren().addAll(add,save,load);
        //Right Setup
        Button start = new Button("ACTIVATE");
        right.getChildren().addAll(start);
    }
    
    private static void SaveConfiguration(Stage primaryStage){
        BufferedWriter bw = null;
        FileWriter fw = null;
        try{
            FileChooser fc = new FileChooser();
            fc.setInitialDirectory(new File("."));
            fc.setInitialFileName("StopGapConfig");
            fc.getExtensionFilters().add(new ExtensionFilter("Meglobot Configuration Files (*.mb)","*.mb"));  
            File file = fc.showSaveDialog(primaryStage);
            if(!file.exists()){
                file.createNewFile();
            }
            fw = new FileWriter(file);
            
            bw = new BufferedWriter(fw);
            bw.write(hostDir.getText() + "\n");
            for(DirBox d : directories){
                bw.write(d.folderDir.getText() + "|" + d.asDir.isSelected() + "\n");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        finally{
            try{
                if(bw != null) bw.close();
                if(fw != null) fw.close(); 
            }catch(IOException e){
                System.out.println("Error closing buffer :" + e);
            }
        }
    }
    
    private static void LoadConfiguration(Stage primaryStage){
        BufferedReader br = null;
        FileReader fr = null;
        try{
            FileChooser fc = new FileChooser();
            fc.setInitialDirectory(new File("."));
            fc.setInitialFileName("StopGapConfig");
            fc.getExtensionFilters().add(new ExtensionFilter("Meglobot Configuration Files (*.mb)","*.mb"));
            File file = fc.showOpenDialog(primaryStage);
            fr = new FileReader(file);
            br = new BufferedReader(fr);
            
            directories.clear();
            directoryPane.getChildren().clear();
            
            hostDir.setText(br.readLine());
            String line;
            while((line = br.readLine()) != null){
                String[] parts = line.split("\\|");
                DirBox db = new DirBox(directoryPane);
                db.asDir.setSelected(Boolean.parseBoolean(parts[1]));
                db.folderDir.setText(parts[0]);
                directories.add(db);
                db.search.setOnAction((event)->{
                    chooser.setTitle("Select Child Directory");
                    try{
                        db.folderDir.setText(chooser.showDialog(primaryStage).getPath());
                    }catch(NullPointerException e){}//Cancelled search
                });
                db.delete.setOnAction((event)->{
                    directoryPane.getChildren().remove(db);
                    directories.remove(db);
                });
                directoryPane.getChildren().add(db);
            }
        }catch(Exception e){
        }
        finally{
            try{
                if(br != null) br.close();
                if(fr != null) fr.close(); 
            }catch(IOException e){
                System.out.println("Error closing buffer :" + e);
            }
        }
    }
    
}
