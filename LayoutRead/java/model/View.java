package model;

import java.util.*;


public class View {
    public float boundX=-1,boundY=-1,boundWidth=-1,boundHigh=-1;
    public float centerX=-1,centerY=-1;
    public Window sourceWindow=null;
    public String sourceNibName="",ViewType="",ViewInfo="";
    public View fatherView=null;
    //生成dot图使用
    public String dotIndex="-1";


    public List<View> subViews=new LinkedList<>();
    public List<Widget> subWidget=new LinkedList<>();


    public View(String sourceNibName, View fatherView){
        this.sourceNibName=sourceNibName;
        this.fatherView=fatherView;
    }

    public void setBounds(String UIBounds){
        String[] tmp=UIBounds.substring(1,UIBounds.length()-2).split(", ");

        if(tmp.length!=4)
            throw new IllegalArgumentException("UIBound should be 4 items:"+UIBounds);

        boundX=Float.valueOf(tmp[0]);
        boundY=Float.valueOf(tmp[1]);
        boundWidth=Float.valueOf(tmp[2]);
        boundHigh=Float.valueOf(tmp[3]);
    }

    public void setCenter(String UICenter){
        String[] tmp=UICenter.substring(1,UICenter.length()-2).split(", ");

        if(tmp.length!=2)
            throw new IllegalArgumentException("UICenter should be 4 items:"+UICenter);

        centerX=Float.valueOf(tmp[0]);
        centerY=Float.valueOf(tmp[1]);

    }

    public void setViewType(String ViewType){
        this.ViewType= ViewType;
    }
    public void setViewInfo(String ViewInfo){
        this.ViewInfo= ViewInfo;
    }
    public View getFatherView(){
        return this.fatherView;
    }


}
