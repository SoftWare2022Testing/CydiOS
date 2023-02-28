package model;

public class Widget{
    public float boundX=-1,boundY=-1,boundWidth=-1,boundHigh=-1;
    public float centerX=-1,centerY=-1;
    public String sourceNibName,textName,ImageName;
    public int index=-1;
    public View fatherView=null;
    public String type="";
    //生成dot图使用
    public String dotIndex=null;


    public Widget(String sourceNibName, int index, String textName, String ImageName){
        //sourceNibName 源nib文件的名字
        this.index=index;
        this.textName=textName;
        this.ImageName=ImageName;
        this.sourceNibName=sourceNibName;
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

    //有改动，新增几个设置函数，可以优化成一起
    public void setFatherView(View fatherView){
        this.fatherView=fatherView;
    }
    public void setType(String viewType){
        this.type=viewType;
    }

}
