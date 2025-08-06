import java.io.*;
import java.util.*;
import com.github.sd4324530.jtuple.Tuple2;
import model.*;
import analyz_nib.*;
import util.find;
import Graphviz.*;
import java.text.*;


public class storyBoardDealer {

    public static void main(String[] args) throws IOException {

        //String filepath = "/Users/fangyongsheng/PycharmProjects/pythonProject/tools/nibRalated/nibFile/22_SBDemo/Base.lproj/Main.storyboardc";
        String filepath = "/Users/fangyongsheng/Desktop/temp/";
        storyBoardDealer result=new storyBoardDealer();
        result.parseStoryBoard(filepath);
        result.genDot();
//        File file = new File(filepath);
//        DataInputStream reader = new DataInputStream(new FileInputStream(file));
//        //reader = new InputStreamReader(new FileInputStream(file));
//        contentRead nib=new contentRead(reader);
//        nib.read();
//        nib.print("");
    }

    List<String> nibNames = new ArrayList<>();
    List<Tuple2> nibContents = new ArrayList<>();

    public List<Window> windowList=new ArrayList<>();
    public List<Edge> edgeList=new ArrayList<>();
    find findUtil=new find();
    int top_level;


    storyBoardDealer() throws IOException {
//        loadNib(dir);
//        List<nibFileIndex> connet=ConnectNib();//只是展示一下storyboard内部controller、nib之间的关系
//        for(int i=0;i<nibNames.size();i++)
//            extractFeature(i);//拿到classSwapper，然后针对每个nib去提取window和edge
//        mergeWindow();//依次合并n2n,c2n,c2c的边，形成新的window，更新windowList和edgeList
//        genDot();//windowList,edgeList为输入
//        //形成.dot文件用于展示
//        return ;

    }
    public void parseStoryBoard(String dir) throws IOException {
        //重置全局变量
        resetInitVar();
        //输入为SB文件夹
        loadNib(dir);
        List<nibFileIndex> connet=ConnectNib();//只是展示一下storyboard内部controller、nib之间的关系
        for(int i=0;i<nibNames.size();i++)
            extractFeature(i);//拿到classSwapper，然后针对每个nib去提取window和edge
        mergeWindow();
    }

    public void parseNib(String dir) throws IOException {
        resetInitVar();
        //输入默认为一个nib文件，不能是文件夹
        //读取nib内容
        File filePath = new File(dir);
        String fullName=filePath.toString();
        String[] nibAndList = fullName.split("/");
        String nibName = nibAndList[nibAndList.length - 1].split("\\.")[0];
        if(nibName.contains("object")){
            nibName = nibAndList[nibAndList.length - 2].split("\\.")[0];
        }
        nibNames.add(nibName);

        DataInputStream reader = new DataInputStream(new FileInputStream(filePath));
        System.out.println(fullName);
        contentRead nib = new contentRead(reader);
        nib.read();
        Tuple2 nibContent = nib.print("");
        nibContents.add(nibContent);
        //进行处理
        extractFeature(0);

    }

    private void resetInitVar(){
        nibNames = new ArrayList<>();
        nibContents = new ArrayList<>();

        windowList=new ArrayList<>();
        edgeList=new ArrayList<>();
    }


    private void loadNib(String dir) throws IOException {
        //分析当前SB 目录下的文件时使用，不分析文件夹内的内容
        File filePath = new File(dir);
        File[] nibfiles = filePath.listFiles();
        for (int i = 0; i < nibfiles.length; i++) {
            String fullName = nibfiles[i].toString();

            String[] nibAndList = fullName.split("/");
            String nibName = nibAndList[nibAndList.length - 1].split("\\.")[0];
            if (nibName.equals("Info") || nibName.equals("")|| nibName.equals("Info-8"))
                continue;

            File file = new File(fullName);
            //SB内部出现了文件夹，这个和nib文件夹一样的
            if(file.isDirectory()){
                File[] subNibs = file.listFiles();
                //依次遍历，拿到后缀，如果不是.nib报错
                for(int j=0;j<subNibs.length;j++){
                    String tstr=subNibs[j].toString();
                    String[] splitStrs = tstr.split("\\.");
                    String suffix="";

                    if(nibAndList.length!=0)
                        suffix=splitStrs[splitStrs.length-1];
                    if(!suffix.equals("nib"))
                        throw new IllegalArgumentException("dir in SB should be nib dir,dir="+fullName);
                }
                //拿到前缀中带object的文件，文件名赋给file
                for(int j=0;j<subNibs.length;j++){
                    String tstr=subNibs[j].toString();
                    if(tstr.contains("object"))
                        file=new File(tstr);
                }

            }

            nibNames.add(nibName);
            DataInputStream reader = new DataInputStream(new FileInputStream(file));
            System.out.println(fullName);
            contentRead nib = new contentRead(reader);
            nib.read();
//          String[] classNames=new String[len];
//          List<List<Tuple2>> classes_key2value=new ArrayList<>(len);
//          nibContent=tuple(classNames,classes_key2value);
//          return nibContent;
            Tuple2 nibContent = nib.print("");
            nibContents.add(nibContent);
        }
    }

    public List<nibFileIndex> ConnectNib() {
        Boolean[] isUIController = new Boolean[nibNames.size()];
        int count = 0;
        for (int i = 0; i < nibNames.size(); i++) {
            if (nibNames.get(i).contains("Controller")) {
                count++;
                isUIController[i] = true;
            }else
                isUIController[i]=false;
        }
        List<nibFileIndex> res=new ArrayList<>(count);

        for (int i = 0; i < nibNames.size(); i++) {
            List<Integer> subNib=new LinkedList<>();
            List<Integer> subController=new LinkedList<>();
            Tuple2 nibContent = nibContents.get(i);
            List<List<Tuple2>> classes_key2value = (List<List<Tuple2>>) nibContent.second;
            String text=classes_key2value.toString();
            for(int j=0;j<nibNames.size();j++){
                if(text.contains(nibNames.get(j))){
                    if(i==j)
                        continue;
                    if(isUIController[j])
                        subController.add(j);
                    else
                        subNib.add(j);
                }
            }
            Integer[] C=subController.toArray(new Integer[subController.size()]);
            Integer[] N=subNib.toArray(new Integer[subNib.size()]);
            res.add(new nibFileIndex(i,N,C));
        }
        for(int i=0;i<res.size();i++)
            res.get(i).printSelf(nibNames);
        return res;
    }

    public void extractFeature(int x){
        Tuple2 nibContent = nibContents.get(x);
        //
        String[] itemNames=(String[]) nibContent.first;
        List<List<Tuple2>> classes_key2value = (List<List<Tuple2>>) nibContent.second;

        //find TopLevelObjectsKey
        List<Tuple2> NSObject=classes_key2value.get(0);
        int TopLevelObjectsIndex=-1;
        for(int i=0;i<NSObject.size();i++){
            Tuple2 temp=NSObject.get(i);
            String key= (String) temp.first;
            String value=(String) temp.second;
            if(key.equals("UINibTopLevelObjectsKey")){
                TopLevelObjectsIndex=Integer.valueOf(value.substring(1));
                break;
            }
        }
        if (TopLevelObjectsIndex == -1) {
            throw new IllegalArgumentException("TopLevelObjectsKey  cannot be -1");
        }
        this.top_level=TopLevelObjectsIndex;

        //find classSwapper和各种第一层的View
        //页面的结构和特征要怎么表示与存储

        int classSwapperIndex=findUtil.findClassSwapperIndex(TopLevelObjectsIndex,classes_key2value,itemNames);
        extractWidgetFeature(x,classSwapperIndex,classes_key2value,itemNames,true);


        return ;

    }

    private void mergeWindow(){//并且要为edge中的指针进行赋值，为了后续展示的方便性
        //合并的时候view的father view也要合并一下
        List<Window> resWindowList=new LinkedList<>();
        List<controller2controller> resEdgeList=new LinkedList<>();
        //把边分为三类之后依次处理n2n，c2n,c2c
        List<Edge> n2n=new LinkedList<>();
        List<controller2nib> c2n=new LinkedList<>();
        List<controller2controller> c2c=new LinkedList<>();
        HashMap<String,List<Integer>> sourceName2indexs=new HashMap<>();
        //依靠前者判断是不是有controller，依靠后者判断对应的controller链接
        //index是指在resWindowList中的索引

        for(int i=0;i<edgeList.size();i++){
            String edgeClassName=edgeList.get(i).getClass().toString();
            if(edgeClassName.equals("class model.Edge")){
                n2n.add(edgeList.get(i));
            }
            if(edgeClassName.equals("class model.controller2nib")){
                c2n.add((controller2nib) edgeList.get(i));
            }
            if(edgeClassName.equals("class model.controller2controller")){
                c2c.add((controller2controller) edgeList.get(i));
            }
        }
        for(int i=0;i<n2n.size();i++){//nib只有一个window,所以nib名称不会重复
                                        //但是这种师弟给合并了，所以不存在n2n的边
            continue;

        }
        for(int i=0;i<c2n.size();i++){//暂时认为同一controller连出的nib建立成的window互相相连，处理完之后再处理一次
            List<View> resViewList=new LinkedList<>();
            List<Widget> resWidgetList=new LinkedList<>();
            //合并时以widget为单位，并且吧widget对应的父view加入viewList

            controller2nib t=c2n.get(i);//如果有triWidget，就和triWidget有一样的父view，否则位于顶层
            Window dest=findUtil.findWindow(t.destName,windowList);//nib不会有歧义
            Window source;

            if(t.triWidget==null)
                source=findUtil.findWindow(t.sourceName,windowList);
            else
                source=findUtil.findWindow(t.sourceName,t.triWidget,windowList);
            //如果是source中有"～"的话，需要在dest后加上iphone或者ipad再重新找
            if(source.sourceName.contains("~")){
                String[] t_strList= source.sourceName.split("~");
                String t_suffix= t_strList[1];
                dest=findUtil.findWindow(t.destName+"~"+t_suffix,windowList);
                t.destName=t.destName+"~"+t_suffix;
                //t.dest=dest;
            }

            //丢失了TabBar之间的互相转换，这个怎么保留？我选择的是在处理c2c之后，判断哪些界面是由相同的c出发得到的
            //现在先判断多少个相同的，然后从中找出发点是TabBarController的，对其互相添加边
            if(source==null || dest==null)
                throw new IllegalArgumentException("source or dest cannot be null");

            for(int j=0;j<t.relatedWidget.size();j++){
                resWidgetList.add(t.relatedWidget.get(j));
                View tmp=t.relatedWidget.get(j).fatherView;
                while(tmp!=null){
                    if(!resViewList.contains(tmp))
                        resViewList.add(tmp);
                    tmp=tmp.fatherView;
                }
            }
            resViewList.addAll(dest.viewList);
            resWidgetList.addAll(dest.widgetList);

            Window t_window=new Window(resViewList,resWidgetList,source.sourceName);
            t_window.TabBarTriWidget=t.triWidget;
            t_window.type=source.type;
            resWindowList.add(t_window);

            source.inEdge=true;
            dest.inEdge=true;

            //当前合并后的window：t_window，当前对应的resWindowList的索引：resWindowList.size()-1
            //hashmap: sourceName2indexs:HashMap<String,List<Integer>>
            String sourceName=t_window.sourceName;
            List<Integer> resWindowIndexs=sourceName2indexs.getOrDefault(sourceName,new LinkedList<>());
            //TabBarController需要添加边
            for(int j=0;j<resWindowIndexs.size();j++){
                int anotherIndex=resWindowIndexs.get(j);
                Window anotherWindow=resWindowList.get(anotherIndex);
                controller2controller t_c2c=new controller2controller(sourceName,anotherWindow.sourceName);
                t_c2c.type="show";t_c2c.tabBarSourceTriWidget=anotherWindow.TabBarTriWidget;
                t_c2c.tabBarDestTriWidget=t_window.TabBarTriWidget;
                t_c2c.source=t_window;t_c2c.dst=anotherWindow;
                resEdgeList.add(t_c2c);

                t_c2c=new controller2controller(anotherWindow.sourceName,sourceName);
                t_c2c.type="show";t_c2c.tabBarSourceTriWidget=t_window.TabBarTriWidget;
                t_c2c.tabBarDestTriWidget=anotherWindow.TabBarTriWidget;
                t_c2c.source=anotherWindow;t_c2c.dst=t_window;
                resEdgeList.add(t_c2c);

            }
            resWindowIndexs.add(resWindowList.size()-1);
            sourceName2indexs.put(sourceName,resWindowIndexs);



        }

        for(int i=0;i<c2c.size();i++){
            controller2controller t=c2c.get(i);
            if(t.type.equals("show")){
                resEdgeList.add(t);
                continue;
            }

            //否则就是 "embed"类型的 c2c,嵌入的先当父view就是null，是根view,简单粗暴的合并
            List<View> resViewList=new LinkedList<>();
            List<Widget> resWidgetList=new LinkedList<>();

            Window source=findUtil.findWindow(t.sourceName,resWindowList);
            if(source==null)
                source=findUtil.findWindow(t.sourceName,windowList);
            Window dest=findUtil.findWindow(t.destName, resWindowList);
            if(dest==null)
                dest=findUtil.findWindow(t.destName, windowList);

            //如果是source中有"～"的话，需要在dest后加上iphone或者ipad再重新找
            if(source.sourceName.contains("~")){
                String[] t_strList= source.sourceName.split("~");
                String t_suffix= t_strList[1];
                dest=findUtil.findWindow(t.destName+"~"+t_suffix,windowList);
                t.destName=t.destName+"~"+t_suffix;
                t.dest=dest;
            }

            if(source==null || dest==null)
                throw new IllegalArgumentException("c2c window find null");

            resViewList.addAll(source.viewList);
            resViewList.addAll(dest.viewList);
            resWidgetList.addAll(source.widgetList);
            resWidgetList.addAll(dest.widgetList);

            source.inEdge=true;
            dest.inEdge=true;
            Window t_window=new Window(resViewList,resWidgetList,source.sourceName);
            t_window.type=source.type;

            if(resWindowList.contains(dest))//后面的controller如果出现了就移除
                resWindowList.remove(dest);
            if(resWindowList.contains(source))
                resWindowList.remove(source);

            resWindowList.add(t_window);
        }
        //对于现在还剩下的c2c,去找它指向的源window和目的window
        for(int i=0;i<resEdgeList.size();i++){
            controller2controller c2cEdge= resEdgeList.get(i);
            Window source=null;
            if(c2cEdge.tabBarSourceTriWidget!=null) {
                source=findUtil.findWindow(c2cEdge.sourceName,c2cEdge.tabBarSourceTriWidget, resWindowList);
                if(source==null)
                    source = findUtil.findWindow(c2cEdge.sourceName, c2cEdge.tabBarSourceTriWidget, windowList);
            }
            else {
                source=findUtil.findWindow(c2cEdge.sourceName,resWindowList);
                if(source==null)
                    source = findUtil.findWindow(c2cEdge.sourceName, windowList);
            }
            Window dest=null;
            if(c2cEdge.tabBarDestTriWidget!=null) {
                dest=findUtil.findWindow(c2cEdge.destName,c2cEdge.tabBarDestTriWidget, resWindowList);
                if(dest==null)
                    dest = findUtil.findWindow(c2cEdge.destName, c2cEdge.tabBarDestTriWidget, windowList);
                if(c2cEdge.sourceName.contains("~")){
                    String[] t_strList= source.sourceName.split("~");
                    String t_suffix= t_strList[1];
                    dest=findUtil.findWindow(c2cEdge.destName+"~"+t_suffix,windowList);
                }
            }
            else {
                dest=findUtil.findWindow(c2cEdge.destName,resWindowList);
                if(dest==null)
                    dest=findUtil.findWindow(c2cEdge.destName, windowList);
                if(c2cEdge.sourceName.contains("~")) {
                    String[] t_strList = source.sourceName.split("~");
                    String t_suffix = t_strList[1];
                    dest = findUtil.findWindow(c2cEdge.destName + "~" + t_suffix, windowList);
                }
            }



            c2cEdge.source=source;
            c2cEdge.dst=dest;
        }
        // 同一TabBarController出来的window都需要建立一条边，添加到edge中即可



        //把没处理的window加到结果里
        for(int i=0;i<windowList.size();i++){
            Window t=windowList.get(i);
            if(t.inEdge==false)
                resWindowList.add(t);
        }
        windowList.clear();
        windowList.addAll(resWindowList);
        edgeList.clear();
        edgeList.addAll(resEdgeList);

    }


    private void genDot(){//这不是针对单个stroyboard，是针对于整个应用分析完成之后的window和edge，当然，也可以针对SB，但是没必要
        GraphViz gv;
        gv = new GraphViz();
        gv.setdir("/Users/fangyongsheng/IdeaProjects/20220312FirstMaven/BinaryRead/java/resultDir/");
        gv.addln(gv.start_graph());
        gv.addln(" rankdir=LR;");
        gv.addln(" node[shape=box];");
        gv.addln("layout=fdp;");//为了可以有子图
        for(int i=0;i<windowList.size();i++){
            Window t=windowList.get(i);
            StringBuilder strContent=new StringBuilder();

            t.subGraphIndex="cluster"+i;
            gv.addln(" subgraph "+t.subGraphIndex+" {");

            strContent.delete(0,strContent.length());

            strContent.append("sourceNibFileName:"+t.sourceName+"\\n");
            if(t.type!="")
                strContent.append("windowType:"+t.type+"\\n");
            gv.addln(" label=\""+strContent+"\";");

            //ViewList
            for(int j=0;j<t.viewList.size();j++){
                View t_view=t.viewList.get(j);
                t_view.dotIndex="view"+i+"_"+j;

                NumberFormat formatter = new DecimalFormat("0.00");
                String bound=formatter.format(t_view.boundX)+","+formatter.format(t_view.boundY)+","+formatter.format(t_view.boundWidth)+","+formatter.format(t_view.boundHigh);
                String center=formatter.format(t_view.centerX)+","+formatter.format(t_view.centerY);
                strContent.delete(0,strContent.length());
                strContent.append("View\\nBound:["+bound+"]"+"\\nCenter:["+center+"]\\n");
                gv.addln(" "+t_view.dotIndex+" [label=\""+strContent+"\"];");

            }

            //该window所有view建立好之后可以建立view之间的边
            for(int j=0;j<t.viewList.size();j++){
                View t_view=t.viewList.get(j);
                strContent.delete(0,strContent.length());
                strContent.append("subView");
                if(t_view.fatherView!=null){
                    gv.addln(t_view.fatherView.dotIndex+" -> "+t_view.dotIndex+" " +
                            "[label=\""+strContent+"\"];");
                }
            }

            //WidgetList
            for(int j=0;j<t.widgetList.size();j++){
                Widget t_widget=t.widgetList.get(j);
                t_widget.dotIndex="widget"+i+"_"+j;
                strContent.delete(0,strContent.length());

                strContent.append("Widget\\n");//添加widget node
                NumberFormat formatter = new DecimalFormat("0.00");
                String bound=formatter.format(t_widget.boundX)+","+formatter.format(t_widget.boundY)+","+formatter.format(t_widget.boundWidth)+","+formatter.format(t_widget.boundHigh);
                String center=formatter.format(t_widget.centerX)+","+formatter.format(t_widget.centerY);
                if(t_widget.type!="")
                    strContent.append(t_widget.type+"\\n");
                if(t_widget.textName!="")
                    strContent.append("textName:"+t_widget.textName+"\\n");
                if(t_widget.ImageName!="")
                    strContent.append("imageName:"+t_widget.ImageName+"\\n");
                strContent.append("Bound:["+bound+"]"+" \\nCenter:["+center+"];");
                gv.addln(" "+t_widget.dotIndex+" [label=\""+strContent+"\"];" );


                //添加其与view相连的边
                if(t_widget.fatherView!=null){
                    View t_view=t_widget.fatherView;
                    strContent.delete(0,strContent.length());
                    strContent.append("subWidget");
                    gv.addln(t_view.dotIndex+" -> "+t_widget.dotIndex+" " +
                            "[label=\""+strContent+"\"];");
                }
            }

            gv.addln(" }");

        }
       // System.out.println(gv.getDotSource());
        for(int i=0;i<edgeList.size();i++){
            StringBuffer strContent=new StringBuffer();
            controller2controller c2c= (controller2controller) edgeList.get(i);
            String sourceDotIndex=c2c.source.subGraphIndex;
            String destDotIndex=c2c.dst.subGraphIndex;

            if(c2c.type!="")
                strContent.append("type:"+c2c.type+"\\n");
            if(c2c.UIActionName!="")
                strContent.append("UIActionName"+c2c.UIActionName+"\\n");

            gv.addln(sourceDotIndex+" -> "+destDotIndex +
                    " [label=\""+ strContent +"\"];");

        }
        gv.addln(gv.end_graph());
//        gv.increaseDpi();
//        gv.increaseDpi();
//        gv.increaseDpi();
//        gv.increaseDpi();
       // System.out.println(gv.getDotSource());
        System.out.print(gv.getDotSource());
        String type="png";
        String fileName="AppName";
        File out = new File(gv.TEMP_DIR+fileName+"."+ type);

        String res=gv.getDotSource();
        byte[] img= gv.getGraph( res, type );
        gv.writeGraphToFile(img, out );


    }
    //提取controller、nib特征
    private void extractWidgetFeature(int nibNameIndex,int x,List<List<Tuple2>> classes_key2value,String[] itemNames,boolean analy_sub) {

        String UIOriginalClassName=null;//是controller的话还要根据sourceName进行判断才行，暂定classSwapper包括controller和view
        if(itemNames[x].equals("UIClassSwapper") ){
            //是controller，传参数到controller_analyz中，返回window List和edge List
            List<String> t_strs=new LinkedList<>();
            t_strs.add("UIOriginalClassName");//加进来的是每一层的目标节点名称
            t_strs.add("NS.bytes");
            List<String> tStr=findUtil.findElement(x,classes_key2value,t_strs);
            if(tStr.get(0).contains("Controller")) {
                controller_analyz t = new controller_analyz(nibNameIndex, x, classes_key2value, itemNames, nibNames);
                windowList.addAll(t.windowList);
                edgeList.addAll(t.edgeList);
            }
            else {
                //认为是普通nib,暂时不认为有独立的非SB中的controller
                nib_analyz t=new nib_analyz(nibNameIndex,x,classes_key2value,itemNames,nibNames,this.top_level);
                windowList.addAll(t.windowList);
                edgeList.addAll(t.edgeList);
            }

        }else{
            nib_analyz t=new nib_analyz(nibNameIndex,x,classes_key2value,itemNames,nibNames,this.top_level);
            windowList.addAll(t.windowList);
            edgeList.addAll(t.edgeList);
        }

    }

}
class nibFileIndex{
    int selfIndex;
    int[] subNibIndex;
    int[] subControllerIndex;

    nibFileIndex(int selfIndex,Integer[] subNibIndex,Integer[] subControllerIndex){
        this.selfIndex=selfIndex;
        this.subNibIndex=new int[subNibIndex.length];
        for(int i=0;i<subNibIndex.length;i++)
            this.subNibIndex[i]=subNibIndex[i];
        this.subControllerIndex=new int[subControllerIndex.length];
        for(int i=0;i<subControllerIndex.length;i++)
            this.subControllerIndex[i]=subControllerIndex[i];
    }

    public void printSelf(List<String> nibNames){
        if(subControllerIndex.length==0 && subNibIndex.length==0)
            return ;

        System.out.println(nibNames.get(selfIndex));
        for(int i=0;i<subControllerIndex.length;i++){
            System.out.println("\t"+"subController:"+nibNames.get(subControllerIndex[i]));
        }
        for(int i=0;i<subNibIndex.length;i++){
            System.out.println("\t"+"subNib:"+nibNames.get(subNibIndex[i]));
        }
        return ;
    }
}