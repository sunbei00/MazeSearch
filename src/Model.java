import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.io.*;
import java.util.Iterator;



public class Model {


    private String readPath = null;
    private String writePath = null;
    private int row = -1;
    private int col = -1;
    public ArrayList<ArrayList<Integer>> groundTruth = null;
    // public으로 할지, private으로 할지는 고민해보기
    public ArrayList<ArrayList<Integer>> our = null;

    public Model() {}
    public Model(String readPath){
        this.readPath = readPath;
    }
    public Model(String readPath, String writePath){
        this.readPath = readPath;
        this.writePath = writePath;
    }
    public void setReadPath(String readPath){
        this.readPath = readPath;
    }
    public void setWritePath(String writePath){
        this.writePath = writePath;
    }

    public void fileRead() {
        try{
            if(this.readPath == null)
                throw new IOException("Need to set variable 'readPath'");
            FileReader fileReader = new FileReader(this.readPath);

            int count=0;
            int ch;
            this.groundTruth = new ArrayList<ArrayList<Integer>>();
            this.groundTruth.add(count,new ArrayList<Integer>()); // 최소 1개의 row가 있다고 가정.
            while((ch = fileReader.read()) != -1){
                if(ch == '\n'){
                    count++;
                    this.groundTruth.add(count,new ArrayList<Integer>());
                }
                else if(ch == Define.AIR || ch == Define.WALL)
                    this.groundTruth.get(count).add(ch);
                else if( ch > 32  && ch != 127) // 제어 문자, 빈 문자, 0,1 이외의 값이 들어오면 오류 처리
                    throw new IOException("File is Error(Error Character) : " +  (char)ch);
            }
            this.row = this.groundTruth.size();
            this.col = this.groundTruth.get(0).size();

            for(int i=0;i<this.groundTruth.size();i++)
                if(this.groundTruth.get(i).size() != col)
                    throw new IOException("File is Error(Column Error)");
        }catch (IOException e){
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void buildOur(){
        try{
            if(readPath == null)
                throw new IOException("Need to run 'fileRead'");
            this.our = new ArrayList<ArrayList<Integer>>();

            for(int i=0;i<row;i++){
                our.add(i,new ArrayList<Integer>());
                for(int j=0;j<col;j++)
                    our.get(i).add(Define.UNKNOWN);
            }

            our.get(0).set(1,Define.BRANCH);

        }catch (IOException e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void ImgWrite(Define.ImgOutput imgOutput){
        try{
            if(this.writePath == null)
                throw new IOException("Need to set variable 'writePath'");
            if(groundTruth == null && imgOutput == Define.ImgOutput.GroundTruth)
                throw new IOException("Need to run 'fileRead'");
            if(our == null && imgOutput == Define.ImgOutput.Our)
                throw new IOException("Need to run 'buildOur'");

            BufferedImage img = new BufferedImage(col, row, BufferedImage.TYPE_3BYTE_BGR);
            File file = new File(writePath);

            int count=0;
            int element;
            Iterator<ArrayList<Integer>> rowIT = null;
            Iterator<Integer> colIT = null;

            switch (imgOutput){
                case GroundTruth:
                    rowIT = groundTruth.iterator();
                    break;
                case Our:
                    rowIT = our.iterator();
                    break;
            }

            while(rowIT.hasNext()){
                colIT = rowIT.next().iterator();
                while(colIT.hasNext()){
                    element = colIT.next();
                    switch (element){ // xx RR GG BB
                        case Define.AIR:
                            img.setRGB(count%col,count/col,0x00ffffff); // white
                            break;
                        case Define.WALL:
                            img.setRGB(count%col,count/col,0x00000000); // black
                            break;
                        case Define.BRANCH:
                            img.setRGB(count%col,count/col,0x0066ff66); // Green
                            break;
                        case Define.UNKNOWN:
                            img.setRGB(count%col,count/col,0x00444444); // Gray
                            break;
                        case Define.PLAYER:
                            img.setRGB(count%col,count/col,0x00ff0000); // RED
                            break;
                    }
                    count++;
                }
            }

            ImageIO.write(img, "bmp", file);

        }catch (IOException e){
            e.printStackTrace();
            System.exit(0);
        }
    }
}
