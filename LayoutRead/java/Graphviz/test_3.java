package Graphviz;

import java.io.File;

public class test_3 {
    public static void createDotGraph(String dotFormat,String fileName)
    {
        File t=new File("");
        GraphViz gv = new GraphViz();
        gv.setdir("/Users/fangyongsheng/IdeaProjects/20220312FirstMaven/BinaryRead/java/resultDir/");
        gv.addln(gv.start_graph());
        gv.addln("A -> B;");
        gv.addln("A -> C;");
        gv.addln(gv.end_graph());
        System.out.println(gv.getDotSource());

        String res=gv.getDotSource(),type="gif";
        byte[] img= gv.getGraph( res, type );
        File out = new File("/Users/fangyongsheng/IdeaProjects/20220312FirstMaven/BinaryRead/java/resultDir/out." + type);
        gv.writeGraphToFile(img, out );



    }

    public static void main(String[] args) throws Exception {
        String dotFormat="1->2;1->3;1->4;4->5;4->6;6->7;5->7;3->8;3->6;8->7;2->8;2->5;";
        // createDotGraph(dotFormat, "/Users/fangyongsheng/IdeaProjects/20220312FirstMaven/BinaryRead/java/resultDir/DotGraph");
        createDotGraph(dotFormat, "DotGraph");


    }
}
