package model;

//edge处理controller到controller的连线：
//controller到controller的连线才会有这个，这个是处理controller连线的
//如果type是embed就把后面的controller界面加到前面的里，否则就是一个新的window


//edge处理controller到nib的连线，把controller中与该nib相关的的固有属性加到nib中后形成一个新的window
//一定是把nib和controller的widget、view加载到一起，形成一个新的window，这时候nib的全部拿来，controller拿相关的
//不是所有controller相关的都是

//edge处理nib到nib的连线（即nib文件中出现了子nib这种：cxC-E3-X6v-view-pNB-QP-sT5.txt），父子nib直接合并即可

//到时候先处理和nib的连线，然后处理和controller的连线
    //对于nib2nib的连线，合并两个view和widget，删除edge
    //对于controller2nib的连线，把controller中与该nib相关的与nib合并，成为一个新的window，删除这条边
    //对于controller2controller的连线
        //保留show类型的边保留，不处理，即保留该边
        //合并embed类型的，合并两个controller对应的window，删除该边

//nib2nib

public class Edge{
    public String sourceName="",destName="";//nib文件名称?应该是window吗，不对，因为如果指向window的话那些没处理的就没法办了
    public Window source=null,dest=null;
    public int index=-1;

    Edge(String source,String dest){
        this.sourceName=source;
        this.destName=dest;
    }

}
