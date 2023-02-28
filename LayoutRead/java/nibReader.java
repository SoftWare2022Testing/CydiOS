import com.github.sd4324530.jtuple.Tuple;
import com.github.sd4324530.jtuple.Tuple3;
import com.github.sd4324530.jtuple.Tuple2;
import static com.github.sd4324530.jtuple.Tuples.tuple;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class nibReader {
    static String filepath;
    public static void main(String[] args) throws IOException {
        //filepath = "/Users/fangyongsheng/PycharmProjects/pythonProject/tools/nibRalated/nibFile/22_SBDemo/Base.lproj/Main.storyboardc/cxC-E3-X6v-view-pNB-QP-sT5.nib";
        filepath = "/Users/fangyongsheng/PycharmProjects/pythonProject/tools/nibRalated/nibFile/22_SBDemo/Base.lproj/Main.storyboardc/UITabBarController-Xdx-pk-EEV.nib";
        File file = new File(filepath);
        DataInputStream reader = new DataInputStream(new FileInputStream(file));
        //reader = new InputStreamReader(new FileInputStream(file));
        contentRead nib=new contentRead(reader);
        nib.read();
        nib.print("");

    }
}
//对于class：返回objcount个str；	循环objcount次，获取length+0x80+长度为length的str+字符串结束符号
//对于objects：返回objcount个(class_idx, start_idx, size)；	循环objcount次，地址后跟的是3*objcount个数字，获取objcount次，方法和class获取length一样
//对于keys：返回objcount个字符串；	循环objcount次，地址后跟的是数字+数字个数B的字符，将字符读取为字符串
//对于values：返回objcount个(key_idx, value, encoding)，循环objcount次，地址后跟的是数值+数据类型（encoding）+数据值
class contentRead{
    DataInputStream reader=null;
    int[][] sections;

    int[][] objects;
    String[] keys;
    List<Tuple3> values ;
    String[] _class ;

    Tuple2 nibContent ;


    public contentRead(DataInputStream reader) throws IOException {
        StringBuffer pfx=new StringBuffer();
        for(int i=0;i<10;i++){
            // System.out.println(reader.read());
            pfx.append((char)reader.readByte());
        }
        if(pfx.toString().contains("Prefix")) {//如果开头是{
            // }
            for(int i=0;i<8;i++)
            pfx.append((char) reader.readByte());
        }
        System.out.println("Prefix:"+pfx.toString());
        Integer headers= (int)reader.read();
        for(int i=0;i<3;i++)
            reader.read();
        System.out.println("headers:"+headers.toString());
        if(!(pfx.toString().equals("NIBArchive") ||  pfx.toString().equals("Prefix: NIBArchive"))) {
            throw new IllegalStateException("this is not a NIBArchive file.");
        }
        this.reader=reader;
    }

    public void read() throws IOException {
        sections=readheader();
        objects=readobject();
        keys=readKeys();
        values=readValue();
        _class=readclass();
        return ;
    }

    public Tuple2 print(String prefix) throws IOException {
        int len=objects.length;
        String[] classNames=new String[len];
        List<List<Tuple2>> classes_key2value=new ArrayList<>(len);
        ///
        for(int i=0;i<len;i++) {
            String className = _class[objects[i][0]];
            List<Tuple3> obj_values=new ArrayList<>(objects[i][2]);
            for(int j=0;j<objects[i][2];j++)
                obj_values.add(values.get(objects[i][1]+j));
            System.out.printf(prefix+"%3d:%s",i,className);
            classNames[i]=className;
            System.out.println();
            List<Tuple2> class_key2value=new ArrayList<>(objects[i][2]);
            for(Tuple3 v : obj_values){
                String k_str=keys[(int)v.first];
                String v_str=v.second.toString();
                boolean printSubNib = (k_str.equals( "NS.bytes"))&&(  v_str.length() > 40) &&( v_str.startsWith("NIBArchive"));
                if(printSubNib){
                    System.out.println(prefix+'\t' + k_str + " = Encoded NIB Archive");
                    DataInputStream reader2;
                    reader2 = new DataInputStream(new StringBufferInputStream(v_str));
                    contentRead nib=new contentRead(reader2);
//                    for(int k=0;k<10;k++)
//                        nib.reader.readByte();
//                    for(int k=0;k<10;k++){
//                        int t=nib.toInt();
//                        continue;
//                    }
                    nib.read();
                    class_key2value.add(tuple(k_str,nib.print(prefix+'\t')));

                }else{
                    byte[] tmp=new byte[v_str.length()];
                    for(int k=0;k<v_str.length();k++){
                        tmp[k]=(byte)v_str.charAt(k);
                    }
                    String res=new String(tmp);
                    System.out.println(prefix+'\t'+k_str+" = ("+v.third.toString()+")"+res);
                    class_key2value.add(tuple(k_str,res));
                }

            }
            classes_key2value.add(class_key2value);
        }
        nibContent=tuple(classNames,classes_key2value);
        return nibContent;

    }

    public int[][] readheader() throws IOException {
        int[][] tmp_sections=new int[4][2];
        Integer hsize=toInt();
        System.out.println("Header size (words):"+hsize.toString());
        for(int i=0;i<4;i++)
            for(int j=0;j<2;j++)
                tmp_sections[i][j]=toInt();
        return tmp_sections;
    }

    public int[][] readobject() throws IOException {
        int count=sections[0][0];
        int[][] tmp_object=new int[count][3];
        for(int i=0;i<count;i++){
            tmp_object[i][0]=readFlexNumber();//class_idx
            tmp_object[i][1]=readFlexNumber();//start_idx
            tmp_object[i][2]=readFlexNumber();//size
        }
        return tmp_object;
    }

    public String[] readKeys() throws IOException {
        int count=sections[1][0];
        String[] res=new String[count];
        for(int i=0;i<count;i++){
            StringBuffer tmp=new StringBuffer();
            int len=readFlexNumber();
            for(int j=0;j<len;j++)
                tmp.append((char)reader.read());
            res[i]=tmp.toString();
        }
        return res;
    }

    public List<Tuple3> readValue() throws IOException {
        int count=sections[2][0];
        List<Tuple3> res=new ArrayList<>(count);
        for(int i=0;i<count;i++){
           // System.out.println("i="+String.valueOf(i));
            int key_idx=readFlexNumber();
            Integer encoding= reader.read();
            switch (encoding){
                case 0:
                    Integer t0=reader.read();
                    res.add(tuple(key_idx,t0,encoding));
                    break;
                case 1:

                    int c1=reader.read();
                    int c2=reader.read();
                    int t1=c1+c2*256;
                    res.add(tuple(key_idx,t1,encoding));
                    break;

                   // throw new IllegalStateException("Unexpected value: encoding=" + encoding.toString()+" i="+(char)i);
                case 2:
                    //python里面 ptr在这里没有+，所以这个数不知道是什么，但是根据item猜测，是一个数，因此是一个4byte的数
                    StringBuffer t2=new StringBuffer();
                    for(int j=0;j<4;j++){
                        t2.append(reader.read());
                        t2.append(",");
                    }
                    t2.append("4ByteNumber not handler");
                    res.add(tuple(key_idx,t2.toString(),encoding));
                    break;
                case 3:
                    Long t3=reader.readLong();
                    res.add(tuple(key_idx,t3,encoding));
                    break;
                case 4:
                    Boolean t4=false;
                    res.add(tuple(key_idx,t4,encoding));
                    break;
                case 5:
                    Boolean t5=true;
                    res.add(tuple(key_idx,t5,encoding));
                    break;
                case 6:
                    Byte[] b=new Byte[4];
                    for(int j=0;j<4;j++)
                        b[j]=reader.readByte();
                    int l;
                    l = b[0];
                    l &= 0xff;
                    l |= ((long) b[1] << 8);
                    l &= 0xffff;
                    l |= ((long) b[2] << 16);
                    l &= 0xffffff;
                    l |= ((long) b[3] << 24);
                    float t6=Float.intBitsToFloat(l);
                    res.add(tuple(key_idx,t6,encoding));
                    break;
                case 7:
                    Double t7= toDouble();
                    res.add(tuple(key_idx,t7,encoding));
                    break;
                case 8:
                    int len=readFlexNumber();
                    if(len==0){
                        res.add(tuple(key_idx,"None",encoding));
                        break;
                    }
                    StringBuffer t8_2=new StringBuffer();
                    char first=(char)reader.read();
                    if((int)first==7){
                        ArrayList<Double> t8_1=new ArrayList<>(4);
                        if(len==17){
                            for(int j=0;j<2;j++)
                                t8_1.add(toDouble());
                            res.add(tuple(key_idx,t8_1,encoding));
                        }else if(len==33){
                            for(int j=0;j<4;j++)
                                t8_1.add(toDouble());
                            res.add(tuple(key_idx,t8_1,encoding));
                        }else
                            throw new IllegalStateException("Unexpected value length");
                    }else {
                        t8_2.append(first);
                        for (int j = 1; j < len; j++)
                            t8_2.append((char) reader.read());
                        res.add(tuple(key_idx, t8_2.toString(), encoding));
                    }
                    break;
                case 9:
                    res.add(tuple(key_idx,"None",encoding));
                    break;
                case 10:
                    String t10='@'+toInt().toString();
                    res.add(tuple(key_idx,t10,encoding));
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + encoding.toString());
            }
        }
        return res;
    }

    public String[] readclass() throws IOException {
        int count=sections[3][0];
        String[] res=new String[count];
        for(int i=0;i<count;i++){
            StringBuffer tmp=new StringBuffer();
            int len=readFlexNumber();
            Byte t=reader.readByte();
            if(t==-127){
                int tt=toInt();
                System.out.print("readClasses: Mystery value:"+tt +'(');
            }
            for(int j=0;j<len-1;j++)
                tmp.append((char)reader.read());
            reader.read();
            if(t==-127) {
                System.out.print(tmp.toString() + ')');
                System.out.println();
            }
          //  System.out.println();
            res[i]=tmp.toString();
        }

        return res;
    }


    public Integer toInt() throws IOException {
        int mul = 1;
        int res=0;
        for(int i=0;i<4;i++){
            res+=mul* (int)reader.read();
            mul=mul*256;
        }

        return res;
    }

    public Double toDouble(){
        Byte[] b=new Byte[8];
        for(int i=0;i<8;i++) {
            try {
                b[i]=reader.readByte();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        long value = 0;
        for (int i = 0; i < 8; i++) {
            value |= ((long) (b[i] & 0xff)) << (8 * i);
        }
        return Double.longBitsToDouble(value);

    }


    public int readFlexNumber() throws IOException {
        int number = 0;
        int shift = 0;
        while(true){
            int num = (int)reader.read();
            number |= (num & 0x7F) << shift ;//相加，但是后面的要算上位数，也就是七位（0-7，七位）
            shift += 7;
            if ((num & 0x80)!=0)
                break;
            if (shift > 30){
                System.out.println("int too long");
                return 0;
            }
        }
        return (number);
    }


}




