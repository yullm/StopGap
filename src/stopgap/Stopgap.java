package stopgap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import static java.nio.file.FileVisitResult.CONTINUE;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
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
import org.apache.commons.io.FileUtils;


public class Stopgap extends Application {
    
    private static BorderPane root;
    private static DirectoryChooser chooser;
    private static TextField hostDir;
    private static VBox directoryPane;
    private static ArrayList<DirBox> directories;
    private static File curPreset;
    private static ArrayList<File> copiedFiles;
    
    @Override
    public void start(Stage primaryStage) {
        copiedFiles = new ArrayList();
        directories = new ArrayList();
        chooser = new DirectoryChooser();
        chooser.setTitle("File Chooser");
        File defaultDir = new File("D:/Applications");
        chooser.setInitialDirectory(defaultDir);
        
        root = new BorderPane();
        
        SetupTop(primaryStage);
        SetupCenter();
        SetupBottom(primaryStage);
        
        LoadSession(primaryStage);
        
        Scene scene = new Scene(root, 500, 300);
        
        primaryStage.setTitle("Meglofriend's Stop Gap");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    @Override
    public void stop(){
        //save last settings
        SaveConfiguration(curPreset);
        SaveSession();
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
        Button clear = new Button("Clear");
        clear.setOnAction((event)->{ClearInterface();});
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
        
        left.getChildren().addAll(add,save,load,clear);
        //Right Setup
        Button start = new Button("ACTIVATE");
        Button stop = new Button("STOP");
        start.managedProperty().bind(start.visibleProperty());
        stop.managedProperty().bind(stop.visibleProperty());
        stop.setVisible(false);
        
        start.setOnAction((e)->{
            if(!hostDir.getText().equals("")){
                root.topProperty().get().setDisable(true);
                root.centerProperty().get().setDisable(true);
                left.setDisable(true);
                start.setVisible(false);
                stop.setVisible(true);
                StartWatching();
            }
        });
        stop.setOnAction((e)->{
            root.topProperty().get().setDisable(false);
            root.centerProperty().get().setDisable(false);
            left.setDisable(false);
            stop.setVisible(false);
            start.setVisible(true);
            if(!copiedFiles.isEmpty()){
                Collections.reverse(copiedFiles);
                try{
                    for(File f : copiedFiles){
                        if(f.exists()){
                            FileUtils.deleteQuietly(f);
                            File parent = new File(f.getParent());
                            if(parent.list().length == 0 && !parent.getPath().equals(hostDir.getText()))
                                FileUtils.deleteDirectory(parent);
                        }
                    }
                }catch(IOException ex){
                    System.out.println(ex);
                }
            }
        });  
        right.getChildren().addAll(start,stop);
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
            curPreset = file;
            fw = new FileWriter(file);
            
            bw = new BufferedWriter(fw);
            bw.write(hostDir.getText() + "\n");
            for(DirBox d : directories){
                bw.write(d.folderDir.getText() + "|" + d.asDir.isSelected() + "\n");
            }
        }catch(Exception e){
            System.out.println(e);
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
    
    private static void SaveConfiguration(File file){
        BufferedWriter bw = null;
        FileWriter fw = null;
        try{
            if(!file.exists()){
                file.createNewFile();
            }
            curPreset = file;
            fw = new FileWriter(file);
            
            bw = new BufferedWriter(fw);
            bw.write(hostDir.getText() + "\n");
            for(DirBox d : directories){
                bw.write(d.folderDir.getText() + "|" + d.asDir.isSelected() + "\n");
            }
        }catch(Exception e){
            System.out.println(e);
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
            curPreset = file;
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
            System.out.println(e);
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
    
    private static void LoadConfiguration(Stage primaryStage,String path){
        BufferedReader br = null;
        FileReader fr = null;
        try{
            FileChooser fc = new FileChooser();
            fc.setInitialDirectory(new File("."));
            fc.setInitialFileName("StopGapConfig");
            fc.getExtensionFilters().add(new ExtensionFilter("Meglobot Configuration Files (*.mb)","*.mb"));
            File file = new File(path);
            curPreset = file;
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
            System.out.println(e);
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
    
    private static void SaveSession(){
        BufferedWriter bw = null;
        FileWriter fw = null;
        try{
            if(curPreset != null){
                File config = new File("lastSession.mb");
                if(!config.exists())
                    config.createNewFile();
                fw = new FileWriter(config);
                bw = new BufferedWriter(fw);
                bw.write(curPreset.getPath());
            }
        }catch(Exception e){
            System.out.println(e);
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

    private static boolean LoadSession(Stage stage){
        BufferedReader br = null;
        FileReader fr = null;
        try{
            File file = new File("lastSession.mb");
            if(!file.exists()) return false;
            fr = new FileReader(file);
            br = new BufferedReader(fr);
            LoadConfiguration(stage,br.readLine());
            return true;
        }catch(Exception e){
            System.out.println(e);
        }
        finally{
            try{
                if(br != null) br.close();
                if(fr != null) fr.close(); 
            }catch(IOException e){
                System.out.println("Error closing buffer :" + e);
            }
        }
        return false;
    }
    
    private static void ClearInterface(){
        curPreset = null;
        hostDir.setText("");
        directories.clear();
        directoryPane.getChildren().clear();
    }
    
    private static void StartWatching(){
        //Start threads for watching each directory
        //Check all directories for validity
        //Each thread should start a collection for each file and check if the file exists already
        //Then create a collection of each file thats been copied.
        //Each iteration check if any of the files are missing or have been modified!
        try{
            File host = new File(hostDir.getText());
            if(!host.exists())
                throw new FileNotFoundException(hostDir.getText());
            for(DirBox dir : directories){
                File checkPath = new File(dir.folderDir.getText());
                if(!checkPath.exists())
                    throw new FileNotFoundException(checkPath.getPath());
                Files.walkFileTree(Paths.get(checkPath.getPath()), new SimpleFileVisitor<Path>(){
                    @Override
                    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs){
                        try{
                            File currentFile = new File(path.toString());
                            String[] pathParts = checkPath.getPath().split("\\\\");
                            String ext = "";
                            if(dir.asDir.isSelected())
                                 ext = "\\" + pathParts[pathParts.length-1];
                            ext += path.toString().replace(checkPath.getPath(), "");
                            System.out.println(ext);
                            File newFile = new File(hostDir.getText()+ext);
                            FileUtils.copyFile(currentFile, newFile);
                            copiedFiles.add(newFile);
                        }catch(IOException e){
                            System.out.println(e);
                        }
                        return CONTINUE;
                    }
                });
            }
        }catch(IOException e){
            System.out.println(e);
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Missing Directory");
            alert.setHeaderText(null);
            alert.setContentText("Folder: '" + e.getMessage() + "' not found");
            alert.showAndWait();
        }
    }
    
}
