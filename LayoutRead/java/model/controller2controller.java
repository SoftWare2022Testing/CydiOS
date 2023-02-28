package model;

public class controller2controller extends Edge {//source相关的widget加进来不行吗？，不找source了
    public String type="",UIActionName="";//type需要区分是嵌入UIStoryboardEmbedSegueTemplate还是展示UIStoryboardShowSegueTemplate
    public Widget tabBarSourceTriWidget=null;
    public Widget tabBarDestTriWidget=null;
    public int index=-1;
    public Window source=null;
    public Window dst=null;

    public controller2controller(String source, String dest) {
        super(source, dest);
    }

}
