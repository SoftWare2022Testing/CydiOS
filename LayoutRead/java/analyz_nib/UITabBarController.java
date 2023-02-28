package analyz_nib;

import com.github.sd4324530.jtuple.Tuple2;
import model.*;
import util.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


public class UITabBarController {
    public List<Window> windowList=new ArrayList<>();
    public List<Edge> edgeList=new ArrayList<>();

    find findUtil =new find();
    List<String> t=new LinkedList<>();
    Window t_window=null;

    public UITabBarController(int nibNameIndex, int x, List<List<Tuple2>> classes_key2value, String[] itemNames, List<String> nibNames) {
        List<Widget> widgetList_1=new LinkedList<>();
        List<View> viewList_1=new LinkedList<>();
        int tarbar= Integer.parseInt(findUtil.findElement(classes_key2value.get(x),"UITabBar").get(0));
        int UItems= Integer.parseInt(findUtil.findElement(classes_key2value.get(tarbar),"UIItems").get(0));

        List<String> tarbarItems=findUtil.findElement(classes_key2value.get(UItems),"UINibEncoderEmptyKey");

        //对于每一个TabBarItem
        List<String> tmp_listStr=null;
        Widget tmp_widget=null;

        for(int i=0;i<tarbarItems.size();i++){
            String textName="",imageName="";

            t.clear();t.add("UITitle");t.add("NS.bytes");
            tmp_listStr=findUtil.findElement(Integer.valueOf(tarbarItems.get(i)),classes_key2value,t);
            if(tmp_listStr.size()==1)
                textName=tmp_listStr.get(0);

            t.clear();t.add("UIImage");t.add("UISystemSymbolResourceName");t.add("NS.bytes");
            tmp_listStr=findUtil.findElement(Integer.valueOf(tarbarItems.get(i)),classes_key2value,t);
            imageName=tmp_listStr.get(0);

            Widget t1=new Widget(nibNames.get(nibNameIndex), Integer.valueOf(tarbarItems.get(i)),textName,imageName);
            t1.type="UITabBarItem";
            widgetList_1.add(t1);
        }

        //对于其中的subNibs和subControllers，建立对应的边
        t.clear();t.add("UIChildViewControllers");t.add("UINibEncoderEmptyKey");
        List<String> subControllers=findUtil.findElement(x,classes_key2value,t);

        //controller2nib的处理
        for(int i=0;i<subControllers.size();i++){
            //nibName subNib的处理
            int subControllerIndex=Integer.valueOf(subControllers.get(i));

            tmp_listStr=findUtil.findElement(classes_key2value.get(subControllerIndex),"UITabBarItem");
            if(tmp_listStr.size()==0)//就是没有UITabBarItem
                throw new IllegalArgumentException("no UITabBarItem");
            int widgetIndex=Integer.valueOf(tmp_listStr.get(0));


            for(int j=0;j<widgetList_1.size();j++){
                if(widgetList_1.get(j).index==widgetIndex) {
                    tmp_widget = widgetList_1.get(j);
                    break;
                }
            }
            if(tmp_widget==null)//假设一个controller起码有一个nib
                throw new IllegalArgumentException("tmp_Widget cannot be null && no subNib belong to found");

            t.clear();t.add("UIChildViewControllers");
            t.add("UINibEncoderEmptyKey");t.add("UINibName");
            t.add("NS.bytes");
            List<String> nibName=findUtil.findElement(subControllerIndex,classes_key2value,t);

            if(nibName.size()!=1)
                throw new IllegalArgumentException("subNib cannot be 2:nibName.size():"+String.valueOf(nibName.size()));

            controller2nib tmp_c2n=new controller2nib(nibNames.get(nibNameIndex),nibName.get(0));
            tmp_c2n.triWidget=tmp_widget;
            tmp_c2n.relatedWidget.add(tmp_widget);

            //这里有可能需要传一个View，碰到再说
            Widget navifationLable=findUtil.findNavagationBar(nibNameIndex,subControllerIndex,classes_key2value,nibNames);
            if(navifationLable!=null) {
                widgetList_1.add(navifationLable);
                tmp_c2n.relatedWidget.add(navifationLable);
            }
            edgeList.add(tmp_c2n);
        }

        t_window=new Window(viewList_1,widgetList_1,nibNames.get(nibNameIndex));
        t_window.type="UITabBarController";
        windowList.add(t_window);

        //controller2controller的处理,

        for(int i=0;i<subControllers.size();i++){

            int subControllersIndex=Integer.valueOf(subControllers.get(i));

            tmp_listStr=findUtil.findElement(classes_key2value.get(subControllersIndex),"UITabBarItem");
            if(tmp_listStr.size()==0)
                continue;
            int widgetIndex=Integer.valueOf(tmp_listStr.get(0));

            Widget tmp_Widget=null;
            for(int j=0;j<widgetList_1.size();j++){
                if(widgetList_1.get(j).index==widgetIndex)
                    tmp_Widget=widgetList_1.get(j);
            }
            if(tmp_Widget==null)
                throw new IllegalArgumentException("tmp_Controller cannot be null && no subNib belong to found");

            t.clear();t.add("UIChildViewControllers");
            t.add("UINibEncoderEmptyKey");t.add("UIStoryboardSegueTemplates");
            t.add("UINibEncoderEmptyKey");

            List<String> SegueTemplatesIndex=findUtil.findElement(subControllersIndex,classes_key2value,t);



            for(int j=0;j<SegueTemplatesIndex.size();j++){
                int num=Integer.valueOf(SegueTemplatesIndex.get(j));

                //拿到subController Name
                t.clear();t.add("UIDestinationViewControllerIdentifier");t.add("NS.bytes");
                List<String> ConTrollerNames=findUtil.findElement(num,classes_key2value,t);
                //拿到actionName
                t.clear();t.add("UIActionName");t.add("NS.bytes");
                List<String> UIActionName=findUtil.findElement(num,classes_key2value,t);

                //new c2c
                if(ConTrollerNames.size()!=1)
                    throw new IllegalArgumentException("Controller2Controller should only 1 in each item");
                controller2controller tmp_c2c=
                        new controller2controller(nibNames.get(nibNameIndex),ConTrollerNames.get(0));
                //UIActionName
                if(UIActionName.size()==1)
                    tmp_c2c.UIActionName=UIActionName.get(0);
                //Type
                if(itemNames[num].equals("UIStoryboardShowSegueTemplate"))
                    tmp_c2c.type="show";
                else if(itemNames[num].equals("UIStoryboardEmbedSegueTemplate")){
                    tmp_c2c.type="embed";
                }else
                    throw new IllegalArgumentException("Controller2Controller UIStoryboardSegueTemplate not show or embed:"+itemNames[num]);
                //triWidget
                List<String> tmpWid_1=findUtil.findElement(classes_key2value.get(subControllersIndex),"UITabBarItem");
                if(tmpWid_1.size()==0)//就是没有UITabBarItem
                    throw new IllegalArgumentException("no UITabBarItem");
                widgetIndex=Integer.valueOf(tmpWid_1.get(0));

                Widget tmp_Widget_c=null;
                for(int k=0;k<widgetList_1.size();k++){
                    if(widgetList_1.get(k).index==widgetIndex) {
                        tmp_Widget_c = widgetList_1.get(k);
                        break;
                    }
                }
                if(tmp_Widget_c==null)//主TabBar中肯定要有这个子TabBar
                    throw new IllegalArgumentException("tmp_Widget cannot be null && no subNib belong to found");

                tmp_c2c.tabBarSourceTriWidget=tmp_Widget_c;
                edgeList.add(tmp_c2c);
            }
        }


    }

}
