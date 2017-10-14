package stopgap;

import java.io.File;

public class FilePair {
    
    public File original;
    public File copy;
    
    public FilePair(){
        this.original = null;
        this.copy = null;
    }
    
    public FilePair(File original, File copy){
        this.original = original;
        this.copy = copy;
    }
    
}
