package model;

import java.util.LinkedList;
import java.util.List;

public class Window {
    public String sourceName=null;
    public String type=null;
    //List<Edge> edgeList=null;
    public List<View> viewList=new LinkedList<>();
    public List<Widget> widgetList=new LinkedList<>();
    public boolean inEdge =false;
    //生成dot图使用
    public String subGraphIndex="-1";
    public Widget TabBarTriWidget=null;

    public Window(List<View> viewList,List<Widget> widgetList,String sourceName){
        this.sourceName=sourceName;
        this.viewList=viewList;
        this.widgetList=widgetList;
    }
}
