package util;

import com.github.sd4324530.jtuple.Tuple2;
import model.Widget;
import model.Window;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class find {

    //按照List向下进行搜索，但是要求每次只有一个符合要求的才行，即层序遍历
    public List<String> findElement(int x, List<List<Tuple2>> classes_key2value, List<String> targetStr) {
        List<String> res=new LinkedList<>();
        Queue<Integer> lq=new LinkedList<>();
        lq.offer(x);
        for(int i=0;i<targetStr.size();i++){
            int size=lq.size();
            while(size>0){
                int index=lq.poll();
                size--;
                List<Tuple2> key2value=classes_key2value.get(index);
                List<String> nextStrs= findElement(key2value,targetStr.get(i));
                if(i== targetStr.size()-1){
                    res.addAll(nextStrs);
                }else{
                    for(String v:nextStrs) {
                        if(v.equals("None"))
                            continue;
                        lq.offer(Integer.valueOf(v));
                    }
                }
            }
        }
        return res;
    }

    //查找List<Tuple2>的某个属性
    public List<String> findElement(List<Tuple2> key2value,String targetStr){
        List<String> res=new LinkedList<>();
        for(int i=0;i<key2value.size();i++){
            String key=(String) key2value.get(i).first;
            if(key.equals(targetStr)) {
                String t=(String) key2value.get(i).second;
                if(t.contains("@"))
                    res.add(t.substring(1));
                else
                    res.add(t);
            }
        }
        return res;
    }

    public Widget findNavagationBar(int nibindex, int x, List<List<Tuple2>> classes_key2value,List<String> nibNames){
        List<String> t=new LinkedList<>();
        t.add("UINavigationBar");t.add("UIItems");t.add("UINibEncoderEmptyKey");
        List<String> tarList=findElement(x,classes_key2value,t);
        if(tarList.size()==0)//没有这一项
            return null;
        if(tarList.size()>1)//抛出异常，该重写这部分了
            throw new IllegalArgumentException("Navagation bar has more than 1 widget");

        int index=Integer.valueOf(tarList.get(0));//换算到SBDemo里，是TabBar的 49里的23
        t.clear();t.add("UITitle");t.add("NS.bytes");
        String textName=findElement(index,classes_key2value,t).get(0);
        //这里中文无法读取，要看看为什么，和python里的有什么不一样
        return new Widget(nibNames.get(nibindex),index,textName,"");

    }

    public Window findWindow(String sourceName, List<Window> windowList){
        for(int i=0;i<windowList.size();i++)
            if(windowList.get(i).sourceName.equals(sourceName))
                return windowList.get(i);
        return null;
    }

    public Window findWindow(String sourceName, Widget triWidget, List<Window> windowList){
        for(int i=0;i<windowList.size();i++){
            Window t=windowList.get(i);
            if(!t.sourceName.equals(sourceName))
                continue;
            for(int j=0;j<t.widgetList.size();j++){
                Widget tar=t.widgetList.get(j);
                if(tar==triWidget)
                    return t;
            }
        }
        return null;
    }



    public int findClassSwapperIndex(int x, List<List<Tuple2>> classes_key2value, String[] itemNames){
        List<Tuple2> TopLevelObjects=classes_key2value.get(x);
        Queue<Integer> lq=new LinkedList<>();
        int res=-1;
        for(int i=0;i<TopLevelObjects.size();i++){
            String key=(String) TopLevelObjects.get(i).first;
            String value=(String) TopLevelObjects.get(i).second;
            if(!key.equals("UINibEncoderEmptyKey"))
                continue;
            lq.add(Integer.valueOf(value.substring(1)));
        }
        lq.add(x);
        while(!lq.isEmpty()){
            int temp=lq.poll();
            if(itemNames[temp].equals("UIProxyObject"))
                continue;
            res=temp;
            break;

        }
        return res;
    }

    //有改动，新增函数
    //寻找UITableViewDataSource，冗余函数，后面可以优化删除
    public int findTableDataSourceIndex(int x, List<List<Tuple2>> classes_key2value, String[] itemNames){
        List<Tuple2> TopLevelObjects=classes_key2value.get(x);
        Queue<Integer> lq=new LinkedList<>();
        int res=-1;
        for(int i=0;i<TopLevelObjects.size();i++){
            String key=(String) TopLevelObjects.get(i).first;
            String value=(String) TopLevelObjects.get(i).second;
            if(!key.equals("UINibEncoderEmptyKey"))
                continue;
            lq.add(Integer.valueOf(value.substring(1)));
        }
        lq.add(x);//再遍历找不为proxy的
        while(!lq.isEmpty()){
            int temp=lq.poll();
            if(itemNames[temp].equals("UIProxyObject")||itemNames[temp].equals("UITableView"))
                continue;
            res=temp;
            break;//往往是最后一个
        }
        return res;
    }

}
