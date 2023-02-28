package analyz_nib;

import com.github.sd4324530.jtuple.Tuple2;
import com.sun.deploy.security.SelectableSecurityManager;
import model.*;
import util.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class controller_analyz {
    public List<Window> windowList=new ArrayList<>();
    public List<Edge> edgeList=new ArrayList<>();

    find findUtil = new find();
    List<String> t=new LinkedList<>();//利用t进行层序遍历
    Window t_window=null;

    public controller_analyz(int nibNameIndex, int x, List<List<Tuple2>> classes_key2value, String[] itemNames,List<String> nibNames){



        t.add("UIOriginalClassName");//加进来的是每一层的目标节点名称
        t.add("NS.bytes");
        List<String> tStr=findUtil.findElement(x,classes_key2value,t);
        if(tStr.size()==0)
            throw new IllegalArgumentException("UIClassSwapper UIOriginalClassName not found:"+nibNames.get(nibNameIndex));
        String UIOriginalClassName=tStr.get(0);
        //判断类型
        switch(UIOriginalClassName){
            //根据Controller类型的不同作不同的处理，目的是提取出该Controller的
            // ViewList：属性包括bounds，center，父view，源nib文件名称
            // WidgetList：包括bounds，center，从属的view，源nib文件名称（可能没有从属的View）
            //widget textName和imageName，widget type，widget编号（提取的内容中的编号，用于判断是不是跳转的Widget）
            // EdgeList：nib、controller两类对象的连线，解决的是最终有几个window的问题
            //处理顺序应当是nib2nib，controller2nib，controller2controller
            //因为nib2nib是在一个nib文件内实现的，在提取view、widget时就完成
            //controller2nib处理是为了后续处理controller2congtroller更加方便,
            //但是这个如果是TabBar的话就会出现controller中多个widget都需要传过去的问题，那c2n就需要一个widget列表
            //controller2controller目的是理出window2window的关系，embed的window就合并
            //Edge：nib2nib，合并两个nib的view和widget，把后面nib的特征放到前面的里面并删除该边；
            //controller2nib：nib的合并到controller中，
            //但是注意的是，与nib无关的widget和view未被合并，后续检查controller中是否有目的widget即可完成controller2controller的匹配
            //形成新的Window，但是source是从controller对应的那个，然后该没有的widget也没有
            //controller2controller：看情况是embed还是show，
            //embed就是后面controller对应的nib合并到前面的里面，并删除该边
            //show的话不做处理，保留该边

            case "UITabBarController" :
                UITabBarController t_1=new UITabBarController(nibNameIndex,x,classes_key2value,itemNames,nibNames);
                windowList.addAll(t_1.windowList);
                edgeList.addAll(t_1.edgeList);
                //处理这么几个东西：tarBarItem，navigationItem，2nib，2controller

                break;

            default://"UITableViewController"和"UIViewController"
                List<Widget> widgetList_2=new LinkedList<>();
                List<View> viewList_2=new LinkedList<>();

                //navigation bar(可能没有)  &&  nibName  &&  SegueTemple
                //navigation bar，这个可能有
                List<String> navigationStrs= findUtil.findElement(classes_key2value.get(x),"UINavigationItem");
                if(navigationStrs.size()!=0){
                    int navigationIndex=Integer.valueOf(navigationStrs.get(0));
                    t.clear();t.add("UITitle");t.add("NS.bytes");
                    String textName="inCode";
                    List<String> tStrs=findUtil.findElement(navigationIndex,classes_key2value,t);
                    if(tStrs.size()!=0){//可能没有这个属性
                        textName=findUtil.findElement(navigationIndex,classes_key2value,t).get(0);
                    }
                    Widget t2=new Widget(nibNames.get(nibNameIndex),navigationIndex,textName,"" );
                    t2.type="UINavigationItem";
                    widgetList_2.add(t2);
                }

                t_window=new Window(viewList_2,widgetList_2,nibNames.get(nibNameIndex));
                t_window.type=UIOriginalClassName;
                windowList.add(t_window);

                //subNib,这个一定会有
                t.clear();t.add("UINibName");t.add("NS.bytes");
                List<String> tStrs=findUtil.findElement(x,classes_key2value,t);
                if(tStrs.size()!=0){
                    String subNibName=findUtil.findElement(x,classes_key2value,t).get(0);
                    controller2nib c2n_2=new controller2nib(nibNames.get(nibNameIndex),subNibName);
                    c2n_2.relatedWidget=widgetList_2;
                    edgeList.add(c2n_2);
                }

                //subController,这个可能有,如果有的话就根据列表下的数字依次进行分析，没有就算了
                t.clear();t.add("UIStoryboardSegueTemplates");t.add("UINibEncoderEmptyKey");
                List<String> SegueTemplatesIndexs=findUtil.findElement(x,classes_key2value,t);
                for(int i=0;i<SegueTemplatesIndexs.size();i++){
                    int num=Integer.valueOf(SegueTemplatesIndexs.get(i));//对于c2c的边，处理方法可以单独抽象出去，回头再说
                    //拿到subController Name
                    t.clear();t.add("UIDestinationViewControllerIdentifier");t.add("NS.bytes");
                    List<String> ConTrollerNames=findUtil.findElement(num,classes_key2value,t);
                    //拿到actionName
                    t.clear();t.add("UIActionName");t.add("NS.bytes");
                    List<String> UIActionName=findUtil.findElement(num,classes_key2value,t);

                    //new c2c
                    if(ConTrollerNames.size()!=0){
                        if(ConTrollerNames.size()!=1)
                            throw new IllegalArgumentException("Controller2Controller should only 1 in each item");

                        controller2controller tmp_c2c=
                                new controller2controller(nibNames.get(nibNameIndex),ConTrollerNames.get(0));
                        //UIActionName
                        if(UIActionName.size()==1)
                            tmp_c2c.UIActionName=UIActionName.get(0);
                        //Type,push也当作一个新界面看
//                        if(itemNames[num].equals("UIStoryboardShowSegueTemplate") || itemNames[num].equals("UIStoryboardPushSegueTemplate"))
//                            tmp_c2c.type="show";
//                        else if(itemNames[num].equals("UIStoryboardEmbedSegueTemplate")){
//                            tmp_c2c.type="embed";
                        if(itemNames[num].equals("UIStoryboardEmbedSegueTemplate"))
                            tmp_c2c.type="embed";
                        else
                                tmp_c2c.type="show";
//                        }else
//                            throw new IllegalArgumentException("Controller2Controller UIStoryboardSegueTemplate not show or embed:"+itemNames[num]);
                        edgeList.add(tmp_c2c);
                    }
                }
                break;
        }
        return ;

    }



}
