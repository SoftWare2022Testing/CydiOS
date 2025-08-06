package model;

import java.util.LinkedList;
import java.util.List;

public class  controller2nib extends Edge {
    public List<Widget> relatedWidget=new LinkedList<>();
    public Widget triWidget=null;
    public int index=-1;

    public controller2nib(String source, String dest) {
        super(source, dest);
    }
}
