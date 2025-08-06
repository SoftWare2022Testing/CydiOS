package Graphviz;

import java.io.File;

public class test {
    public static void createDotGraph(String dotFormat,String fileName)
    {
        GraphViz gv=new GraphViz();
        gv.setdir("/Users/fangyongsheng/IdeaProjects/20220312FirstMaven/BinaryRead/java/resultDir/");
        gv.addln(gv.start_graph());
        gv.add(dotFormat);
        gv.addln(gv.end_graph());
        System.out.println(gv.getDotSource());
        // png为输出格式，还可改为pdf，gif，jpg等
        String type = "gif";
        // gv.increaseDpi();
        gv.decreaseDpi();
        gv.decreaseDpi();
        File out = new File(gv.TEMP_DIR+fileName+"."+ type);

        String res=gv.getDotSource();
        byte[] img= gv.getGraph( res, type );
        gv.writeGraphToFile(img, out );
    }

    public static void main(String[] args) throws Exception {
        String dotFormat="1->2;1->3;1->4;4->5;4->6;6->7;5->7;3->8;3->6;8->7;2->8;2->5;";
       // createDotGraph(dotFormat, "/Users/fangyongsheng/IdeaProjects/20220312FirstMaven/BinaryRead/java/resultDir/DotGraph");
        createDotGraph(dotFormat, "DotGraph");


    }
}

