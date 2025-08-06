package analyz_nib;

import com.github.sd4324530.jtuple.Tuple2;
import model.Edge;
import model.View;
import model.Widget;
import model.Window;
import util.find;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class nib_analyz {
    public List<Window> windowList=new ArrayList<>();
    public List<Edge> edgeList=new ArrayList<>();

    find findUtil = new find();
    List<String> t=new LinkedList<>();
    String nib_name="";
    List<String> temp_search=new LinkedList<>();

    public nib_analyz(int nibNameIndex, int x, List<List<Tuple2>> classes_key2value, String[] itemNames,List<String> nibNames,int top_level){
        List<Widget> widget_List=new LinkedList<>();
        List<View> view_List=new LinkedList<>();
        this.nib_name=nibNames.get(nibNameIndex);

        if(itemNames[x].equals("UIClassSwapper")){//传进来的就一定是不是controller，所以不用再进行判断了
            itemNames[x]=classSwapperViewDeal(x,classes_key2value);
        }
        //应该在这里+parse_xxxxxxxx,反射加上 整体结构更加清晰


//        Method get = instance.getClass().getMethod(methodName);
//        Object result = get.invoke(instance);


        //tableview
        if(itemNames[x].equals("UITableView")){

            View root =new View(nib_name,null);
            //加入root view
            root.setBounds(classes_key2value.get(x).get(0).second.toString());root.setCenter(classes_key2value.get(x).get(1).second.toString());
            root.setViewType(itemNames[x]);
            view_List.add(root);

            //先考虑不是原型的情况，继续找UITableViewDataSource
            int source=findUtil.findTableDataSourceIndex(top_level,classes_key2value,itemNames);
            //一般情况
            if(source!=-1&&itemNames[source].equals("UITableViewDataSource")){
                temp_search.clear();
                temp_search.add("UITableSections");temp_search.add("UINibEncoderEmptyKey");
                //Sections，即table的块
                List<String> tStr=findUtil.findElement(source,classes_key2value,temp_search);
                //View有时也会有一些文本信息，如可能会有块名的信息，存放在ViewInfo中
                for (int i=0;i<tStr.size();i++){
                    View UITableSection=new View(nib_name,root);
                    UITableSection.setViewType("UITableSection");//nib文件中并无Section这种View，没有center与bound的信息
                    for(int nums=0;nums<classes_key2value.get(Integer.parseInt(tStr.get(i))).size();nums++){
                        List<Tuple2> key2value=classes_key2value.get(Integer.parseInt(tStr.get(i)));
                        String number=(String)key2value.get(nums).second;
                        if(key2value.get(nums).first.equals("UITableSectionHeaderTitle")){
                            temp_search.clear();
                            temp_search.add("NS.bytes");
                            UITableSection.setViewInfo(findUtil.findElement(Integer.parseInt(number.substring(1)),classes_key2value,temp_search).get(0));
                        }
                        else if(key2value.get(nums).first.equals("UITableSectionRows")){
                            temp_search.clear();
                            temp_search.add("UINibEncoderEmptyKey");
                            temp_search.add("UITableRowCell");
                            List<String> RowCells=findUtil.findElement(Integer.parseInt(number.substring(1)),classes_key2value,temp_search);
                            for(String s : RowCells){
                                View UITableViewCell=new View(nib_name,UITableSection);
                                UITableViewCell.setViewType(itemNames[Integer.parseInt(s)]);
                                UITableViewCell.setBounds(classes_key2value.get(Integer.parseInt(s)).get(0).second.toString());UITableViewCell.setCenter(classes_key2value.get(Integer.parseInt(s)).get(1).second.toString());
                                view_List.add(UITableViewCell);
                                DFS_view(Integer.parseInt(s),UITableViewCell,classes_key2value,itemNames,view_List,widget_List);
                            }
                        }
                    }
                    view_List.add(UITableSection);
                }
            }
            //自定义原型cell，嵌套nib情况,没有DataSource
            else {
                temp_search.clear();
                temp_search.add("UITableViewCellPrototypeNibs");temp_search.add("UINibEncoderEmptyKey");temp_search.add("NS.bytes");

                //        archiveData
            }
        }
        //非tableview情况
        else {
            View root =new View(nib_name,null);
            //加入root view
            parseBounds(root,classes_key2value.get(x));
            parseCenter(root,classes_key2value.get(x));

            root.setViewType(itemNames[x]);//这里是上面处理过的item【x】
            view_List.add(root);
            DFS_view(x,root,classes_key2value,itemNames,view_List,widget_List);
        }
        this.windowList.add(new Window(view_List,widget_List,nib_name));
    }
    private void DFS_view(int x,View fatherview,List<List<Tuple2>> classes_key2value,String[] itemNames,List<View> view_List,List<Widget> widget_List){

        System.out.println(x);
        List<String> t=new LinkedList<>();//利用t进行层序遍历
        t.add("UISubviews");//加进来的是每一层的目标节点名称
        t.add("UINibEncoderEmptyKey");
        List<String> tStr=findUtil.findElement(x,classes_key2value,t);
        //widget
        if(tStr.size()==0){
            List<String> temp_search=new LinkedList<>();
            List<String> temp_search_image=new LinkedList<>();
            String textName="",imageName="";

            //判断是哪个小部件 写入text与image
            if(itemNames[x].equals("UITextField")){
                temp_search.add("UIPlaceholder");temp_search.add("NS.bytes");
            }
            else if(itemNames[x].equals("UIButton")){
                temp_search.add("UIButtonStatefulContent");temp_search.add("UINibEncoderEmptyKey");temp_search.add("UITitle");temp_search.add("NS.bytes");
                temp_search_image.add("UIButtonStatefulContent");temp_search_image.add("UINibEncoderEmptyKey");temp_search_image.add("UIImage");temp_search_image.add("UIResourceName");temp_search_image.add("NS.bytes");
            }
            else if(itemNames[x].equals("UILabel")||itemNames[x].equals("UITableViewLabel")){
                temp_search.add("UIText");temp_search.add("NS.bytes");
            }else
            if(findUtil.findElement(x,classes_key2value,temp_search).size()!=0)
                textName = findUtil.findElement(x,classes_key2value,temp_search).get(0);
            if(findUtil.findElement(x,classes_key2value,temp_search_image).size()!=0)
                imageName=findUtil.findElement(x,classes_key2value,temp_search_image).get(0);
            //如果这是一个View不是widget咋办？，不能默认就是Widget
            Widget widget_save=new Widget(nib_name,x,textName,imageName);
            parseBounds(widget_save,classes_key2value.get(x));
            parseCenter(widget_save,classes_key2value.get(x));

            //widget_save.setBounds(classes_key2value.get(x).get(0).second.toString());
            //widget_save.setCenter(classes_key2value.get(x).get(1).second.toString());
            widget_save.setType(itemNames[x]);
            //注意这里传进来的fatherview是本身
            widget_save.setFatherView(fatherview.getFatherView());
            widget_List.add(widget_save);
            //从view中把widget自己删除
            view_List.remove(fatherview);
            return;
        }
        else{
            for (int i=0;i<tStr.size();i++){
                int tarIndex=Integer.parseInt(tStr.get(i));
                //如果对应的子节点是UIClassSwapper，就把名称换成UIOriginalClassName
                if(itemNames[tarIndex].equals("UIClassSwapper")){
                    itemNames[tarIndex]=classSwapperViewDeal(tarIndex,classes_key2value);
                }

                //给子类建好view作为根再往下搜
                View sub_root =new View(nib_name,fatherview);
                parseBounds(sub_root,classes_key2value.get(tarIndex));
                parseCenter(sub_root,classes_key2value.get(tarIndex));///

                //sub_root.setBounds(classes_key2value.get(Integer.parseInt(tStr.get(i))).get(0).second.toString());
                //sub_root.setCenter(classes_key2value.get(Integer.parseInt(tStr.get(i))).get(1).second.toString());
                sub_root.setViewType(itemNames[tarIndex]);
                view_List.add(sub_root);
                DFS_view(Integer.parseInt(tStr.get(i)),sub_root,classes_key2value,itemNames,view_List,widget_List);
            }
        }
    }
    //作用是改该item的值为UIOriginalClassName对应的str
    private String classSwapperViewDeal(int x, List<List<Tuple2>> classes_key2value){
        temp_search.clear();
        temp_search.add("UIOriginalClassName");
        temp_search.add("NS.bytes");
        List<String> tStr=findUtil.findElement(x,classes_key2value,temp_search);
        //这里默认有且只有一个，出现问题再说
        return tStr.get(0);

    }

    private void parseBounds(Object viewOrWidget,List<Tuple2> key2value){
        //解viewOrWidget的包，判断是哪个
        View inView=null;
        Widget inWidget=null;
        String objName=viewOrWidget.getClass().toString();
        if(objName.equals("class model.View") )
            inView= (View) viewOrWidget;
        if(objName.equals("class model.Widget"))
            inWidget= (Widget) viewOrWidget;
        //没有这个属性的话就不做处理
        List<String> tStrs=findUtil.findElement(key2value,"UIBounds");
        if(tStrs.size()==0)
            return ;
        //赋值
        String boundsStr=tStrs.get(0);
        if(inView!=null)
            inView.setBounds(boundsStr);
        if(inWidget!=null)
            inWidget.setBounds(boundsStr);
    }

    private void parseCenter(Object viewOrWidget,List<Tuple2> key2value){
        //解viewOrWidget的包，判断是哪个
        View inView=null;
        Widget inWidget=null;
        String objName=viewOrWidget.getClass().toString();
        if(objName.equals("class model.View") )
            inView= (View) viewOrWidget;
        if(objName.equals("class model.Widget"))
            inWidget= (Widget) viewOrWidget;

        List<String> tStrs=findUtil.findElement(key2value,"UICenter");
        if(tStrs.size()==0)//没有这个属性的话就不做处理
            return ;
        String centerStr=tStrs.get(0);
        if(inView!=null)
            inView.setCenter(centerStr);
        if(inWidget!=null)
            inWidget.setCenter(centerStr);
    }
}
