import Graphviz.GraphViz;
import model.*;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import util.deleteFile;

public class appsDirDealer {//找到每一个带有nib文件/storyboard文件夹的目录，保存到Apps
                            // 随后遍历每一个文件夹， 处理其中的nib文件或者进入文件夹

    //python有把nib复制过来的部分，不用重写了
    //需要的是把文件夹下的每一个文件夹里的nib文件和stroyboard都进行分析，生成dot文件,后续再说，先按单个app进行处理

    //需要分析的是python导出的文件夹
    //目的是分析所有的.nib文件，和正向compile一样，提取内容时也是把这个当作storyboard和单独nib分别进行分析
    //不需要写入nib文件的内容，这个python3已经做过了，需要的是构建出整体的WTG，也就是说没必要去针对一个文件夹去生成一个东西
    //最终
    //步骤如下：
    //1.对于文件夹来说，依次遍历其中所有文件和文件夹，如果是.storyboardc结尾的文件夹或者是.nib结尾的文件就进行处理，
    //  如果二者都不满足就进入该文件夹，继续寻找。
    //  特殊情况是.nib文件夹，这个需要在找到.nib结尾的文件之后进行判断，如果是文件夹的话拿出其中一个.nib文件即可
    //2.分析结果存下来，.storyboardc的文件夹处理完成之后就进行边的合并;所有文件都处理完成之后进行结果的合并
    //3.写入WTG
    //具体组织结构的思路参考python ibtool相关内容即可

    public static void main(String[] args) throws IOException {


        //String filepath = "/Users/fangyongsheng/PycharmProjects/pythonProject/tools/nibRalated/nibFile/22_SBDemo/Base.lproj/Main.storyboardc";
        String filepath =
                "/Users/fangyongsheng/IdeaProjects/20220312FirstMaven/BinaryRead/java/Apps/nibFile/32_Panda";
        String appName = "DeleteAccount";
        AppDealer result=new AppDealer();
        result.dirDeal(filepath);
        result.genDot(appName);//传入这个参数是为了输出有应用名称的文件夹

    }
}
class AppDealer{
    public List<Window> windowList=new ArrayList<>();
    public List<Edge> edgeList=new ArrayList<>();

    AppDealer(){
    }
    //递归对目录进行处理，前序遍历
    public void dirDeal(String rootPath) throws IOException {
        //判断后缀，如果是storyboardc、nib就分别处理，否则递归

        String[] nibAndList = rootPath.split("\\.");
        String suffix;
        if(nibAndList.length!=0)
            suffix=nibAndList[nibAndList.length-1];
        else
            suffix="";
        //后缀如果是.storyboardc
        if(suffix.equals("storyboardc")){//名字取的有问题，后续再改吧
            storyBoardDealer t=new storyBoardDealer();
            t.parseStoryBoard(rootPath);
            //这里返回的是合并window之后的内容
            windowList.addAll(t.windowList);
            edgeList.addAll(t.edgeList);

            return ;
        }
        //后缀如果是.nib
        if(suffix.equals("nib")){
            File file = new File(rootPath);
            //.nib文件夹
            if(file.isDirectory()){
                //拿到前缀名，根据前缀名进行判断
                File nibsDir = new File(rootPath);
                File[] nibfiles = nibsDir.listFiles();
                for(int i=0;i<nibfiles.length;i++){

                    String nibName=nibfiles[i].toString();//这里拿到的应该是绝对路径
                    String[] strList = nibName.split("/");
                    String prefix = strList[strList.length - 1].split("\\.")[0];

                    if(prefix.contains("objects")){//objects-12.3+nib和runtime.nib分析一个即可
                        storyBoardDealer t=new storyBoardDealer();
                        t.parseNib(nibName);
                        windowList.addAll(t.windowList);
                        edgeList.addAll(t.edgeList);
                    }
                }
            }
            //.nib文件
            else{
                storyBoardDealer t=new storyBoardDealer();
                t.parseNib(rootPath);
                windowList.addAll(t.windowList);
                edgeList.addAll(t.edgeList);
            }

            return ;
        }
        //后缀不是storyboardc/nib的话就是舍弃文件，遍历文件夹。虽然不可能有单独非nib文件的情况，和分析nib无关，略过即可
        File file=new File(rootPath);
        if(file.isDirectory()){
            File[] subFiles = file.listFiles();
            for(int i=0;i<subFiles.length;i++){
                String nextStr=subFiles[i].toString();
                dirDeal(nextStr);
            }
        }

    }

    void genDot(String appName){//这不是针对单个stroyboard，是针对于整个应用分析完成之后的window和edge，当然，也可以针对SB，但是没必要
        //拿到文件名，也就是最后一个//的内容,用于拼接路径
        String outPathStr="/Users/fangyongsheng/IdeaProjects/20220312FirstMaven/BinaryRead/java/resultDir/"+appName;
        File outPath=new File(outPathStr);
        if(outPath.exists()){//有了就删除
            deleteFile t=new deleteFile();
            t.deleteFileOrDirectory(outPathStr);
        }
        outPath.mkdirs();

        GraphViz gv;
        gv = new GraphViz();
        gv.setdir(outPathStr);

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
                if(t_view.ViewType!="")
                    strContent.append(t_view.ViewType+"\\n");
                else
                    strContent.append("View\\n");
                strContent.append("Bound:["+bound+"]"+"\\nCenter:["+center+"]\\n");
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

                if(t_widget.type!="")
                    strContent.append(t_widget.type+"\\n");
                else
                    strContent.append("Widget\\n");//添加widget node
                NumberFormat formatter = new DecimalFormat("0.00");
                String bound=formatter.format(t_widget.boundX)+","+formatter.format(t_widget.boundY)+","+formatter.format(t_widget.boundWidth)+","+formatter.format(t_widget.boundHigh);
                String center=formatter.format(t_widget.centerX)+","+formatter.format(t_widget.centerY);
//                if(t_widget.type!="")
//                    strContent.append(t_widget.type+"\\n");
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
        File out = new File(gv.TEMP_DIR+"//"+fileName+"."+ type);

        String res=gv.getDotSource();
        byte[] img= gv.getGraph( res, type );
        gv.writeGraphToFile(img, out );


    }




}
