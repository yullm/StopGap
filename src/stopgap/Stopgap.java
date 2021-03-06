package stopgap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import static java.nio.file.FileVisitResult.CONTINUE;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
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
    private static ArrayList<FilePair> copiedFiles;
    private static WatchService watcher;

    
    @Override
    public void start(Stage primaryStage) {
        //Initialise members
        copiedFiles = new ArrayList();
        directories = new ArrayList();
        chooser = new DirectoryChooser();
        chooser.setTitle("File Chooser");
        File defaultDir = new File("D:/Applications");
        chooser.setInitialDirectory(defaultDir);
        
        root = new BorderPane();
        //Call Pane Setup Functions
        SetupTop(primaryStage);
        SetupCenter();
        SetupBottom(primaryStage);
        // Load last session
        LoadSession(primaryStage);
        Scene scene = new Scene(root, 500, 300);
        
        // Load application icon.
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("ico.png")));
        
        primaryStage.setTitle("Meglofriend's Stop Gap V.1.1");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    @Override
    public void stop(){
        //save last settings and clear copies
        clearWatcher();
        SaveConfiguration(curPreset);
        SaveSession();
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    private static void SetupTop(Stage primaryStage){
        // create the host selection bar
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
        // Center holds the child directory selectors in a scrollable pane
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
            clearWatcher();
        });  
        right.getChildren().addAll(start,stop);
    }
    
    private static void clearWatcher(){
        //Removes all copied files and clears the list.
        if(!copiedFiles.isEmpty()){
                Collections.reverse(copiedFiles);
                try{
                    if(watcher != null) watcher.close();
                    for(FilePair pair : copiedFiles){
                        if(pair.copy.exists()){
                            File parent = new File(pair.copy.getParent());
                            FileUtils.deleteQuietly(pair.copy);
                            if(parent.list().length == 0 && !parent.getPath().equals(hostDir.getText()))
                                FileUtils.deleteDirectory(parent);
                        }
                    }
                }catch(IOException ex){
                    System.out.println(ex);
                }
                copiedFiles.clear();
            }
        
    }
    
    private static void SaveConfiguration(Stage primaryStage){
        // method for saving the current configuration
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
            //write host string
            bw.write(hostDir.getText() + "\n");
            //write a line for each child directory and include the asDirectory varaible.
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
        //overload method for file argument
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
        //method for load a configuration from a file.
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
        //overload method to load file from a path instead of choosing one.
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
        // Saves the name of the file of the current configuration for the next session.
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
        // checks for last configuration and loads it.
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
        //clears all settings for a fresh start.
        curPreset = null;
        hostDir.setText("");
        directories.clear();
        directoryPane.getChildren().clear();
    }
    
    private static void StartWatching(){
        //Checks all directories for validity
        //Then create sa collection of each file for copying.
        //Starts a thread to maintaining file consistency.
        try{
            watcher = FileSystems.getDefault().newWatchService();
            File host = new File(hostDir.getText());
            
            if(!host.exists())
                throw new FileNotFoundException(hostDir.getText());
            
            for(DirBox dir : directories){
                //get directory information.
                File dirFile = new File(dir.folderDir.getText());
                Path dirPath = Paths.get(dirFile.getPath());
                if(!dirFile.exists())
                    throw new FileNotFoundException(dirFile.getPath());
                String[] dirParts = dirFile.getPath().split("\\\\");
                //if adding directory as folder and not its contents to the root create a entry to list for the folder.
                if(dir.asDir.isSelected()){
                    copiedFiles.add(new FilePair(dirFile,new File(hostDir.getText() + "\\" + dirParts[dirParts.length-1])));
                }
                
                // walk the folder hierarchy for watcher registry and file copying                
                Files.walkFileTree(dirPath, new SimpleFileVisitor<Path>(){
                    //Start by registering each directory to the watcher.
                    @Override
                    public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs) throws IOException{
                        path.register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
                                StandardWatchEventKinds.ENTRY_MODIFY,
                                StandardWatchEventKinds.ENTRY_DELETE);
                        return CONTINUE;
                    }
                    
                    // When a file is visited the copy it over to the desired location.
                    @Override
                    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs)throws IOException{
                            File currentFile = new File(path.toString());
                            String[] pathParts = dirFile.getPath().split("\\\\");
                            String ext = "";
                            //get folder name if base directory is folder and not root.
                            if(dir.asDir.isSelected())
                                 ext = "\\" + pathParts[pathParts.length-1];
                            ext += path.toString().replace(dirFile.getPath(), "");
                            File newFile = new File(hostDir.getText()+ext);
                            //copy files and add entry to list
                            FileUtils.copyFile(currentFile, newFile);
                            copiedFiles.add(new FilePair(currentFile,newFile));
                        return CONTINUE;
                    }
                });
                
            }
            
            //start watch task for dir
            Task task = new Task<Void>(){
                @Override
                public Void call(){
                    try{
                        WatchKey watchKey;
                        while((watchKey = watcher.take()) != null){
                            for(WatchEvent e : watchKey.pollEvents()){
                                // gets directory of the event
                                Path eventDir = (Path)watchKey.watchable();
                                // add to the context and you have the file that has changed.
                                File editFile = new File(eventDir + "\\" + e.context());                               
                                if(editFile.exists()){
                                    if(e.kind() == StandardWatchEventKinds.ENTRY_CREATE){
                                        // This section checks the base directories to know where to copy the file
                                        String base = "";
                                        String ext = "";
                                        String[] dirParts = eventDir.toString().split("\\\\");
                                        String checkString = dirParts[0];
                                        outerloop:
                                        for(int i = 1; i <= dirParts.length; i++){
                                            System.out.println("Check String: " + checkString);
                                            for(DirBox d : directories){
                                                if(d.folderDir.getText().equals(checkString)){ 
                                                    base = d.folderDir.getText();
                                                    if(d.asDir.isSelected()){
                                                        String[] baseParts = base.split("\\\\");
                                                        ext += "\\" +  baseParts[baseParts.length-1];
                                                    }
                                                    break outerloop;
                                                }
                                            }
                                            checkString += "\\" + dirParts[i];
                                        }
                                        ext += editFile.getPath().replace(base, "");
                                        System.out.println("new Path: " + hostDir.getText()+ext);
                                        
                                        File newFile = new File(hostDir.getText()+ext);
                                        if(!editFile.isDirectory()){
                                                FileUtils.copyFile(editFile, newFile);
                                        }else{
                                            // if the new file is a directory copy it and register it for watching.
                                            FileUtils.copyDirectory(editFile, newFile);
                                            Files.walkFileTree(Paths.get(editFile.getPath()), new SimpleFileVisitor<Path>(){
                                                @Override
                                                public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs) throws IOException{
                                                    path.register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
                                                            StandardWatchEventKinds.ENTRY_MODIFY,
                                                            StandardWatchEventKinds.ENTRY_DELETE);
                                                    return CONTINUE;
                                                }
                                            });
                                        }
                                        copiedFiles.add(new FilePair(editFile,newFile));
                                        System.out.println();
                                    }
                                    if(e.kind() == StandardWatchEventKinds.ENTRY_MODIFY){
                                           // if a file has changed reflect changes in the copy
                                        for(FilePair p : copiedFiles){
                                            if(p.original.equals(editFile)){
                                                if(!editFile.isDirectory()){
                                                    System.out.println("Modified: " + editFile.getPath());
                                                    FileUtils.copyFile(p.original, p.copy);
                                                }
                                            }
                                        }
                                    }
                                }else{
                                    if(e.kind() == StandardWatchEventKinds.ENTRY_DELETE){
                                        //if a file has been removed, remove the copy.
                                        System.out.println("Deleted: " + editFile.getPath());    
                                        for(FilePair p : copiedFiles){
                                            if(p.original.equals(editFile)){
                                                if(!editFile.isDirectory()) FileUtils.deleteQuietly(p.copy);
                                                else FileUtils.deleteDirectory(p.copy);
                                            }
                                        }
                                    }
                                }
                            }
                            watchKey.reset();
                        }
                    }catch(InterruptedException | IOException e){
                        e.printStackTrace();
                    }
                    catch(ClosedWatchServiceException e){
                        System.out.println("Watcher Closed");
                    }
                    return null;
                }
            };
            Thread watchThread = new Thread(task);
            //Ensure thread stops when the application stops.
            watchThread.setDaemon(true);
            watchThread.start();

        }catch(IOException e){
            System.out.println(e);
            // Alert if any folders are missing.
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Missing Directory");
            alert.setHeaderText(null);
            alert.setContentText("Folder: '" + e.getMessage() + "' not found");
            alert.showAndWait();
        }
    }
    
}
