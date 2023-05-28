import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;



public class Model {
    private String readPath = null;
    private String writePath = null;
    private int row = -1;
    private int col = -1;
    public ArrayList<ArrayList<Block>> groundTruth = null;
    public ArrayList<ArrayList<Block>> our = null;

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
            this.groundTruth = new ArrayList<ArrayList<Block>>();
            this.groundTruth.add(count,new ArrayList<Block>()); // 최소 1개의 row가 있다고 가정.
            while((ch = fileReader.read()) != -1){
                if(ch == '\n'){
                    count++;
                    this.groundTruth.add(count,new ArrayList<Block>());
                }
                else if(ch == Define.AIR || ch == Define.WALL)
                    this.groundTruth.get(count).add(new Block(ch));
                else if( ch > 32  && ch != 127) // 제어 문자, 빈 문자, 0,1 이외의 값이 들어오면 오류 처리
                    throw new IOException("File is Error(Error Character) : " +  (char)ch);
            }
            this.row = this.groundTruth.size();
            this.col = this.groundTruth.get(0).size();

            for(int i=0;i<this.groundTruth.size();i++)
                if(this.groundTruth.get(i).size() != col)
                    throw new IOException("File is Error(Column Error)");

            fileReader.close();
        }catch (IOException e){
            e.printStackTrace();
            System.exit(0);
        }
    }


    public int test = 0; // tmp
    public void buildOur(){
        try{
            if(readPath == null)
                throw new IOException("Need to run 'fileRead'");
            this.our = new ArrayList<ArrayList<Block>>();

            for(int i=0;i<row;i++){
                this.our.add(i,new ArrayList<Block>());
                for(int j=0;j<col;j++)
                    this.our.get(i).add(new Block(Define.UNKNOWN));
            }

            Game game = new Game(this);

            //game.useScan(new Define.Pos(10,10));
            for(test=0;test < 64;test++){ // getRow()*getCol()*2
                game.Move();
                //game.useBreak(new Define.Pos(3,2));

                 // path 사진으로 출력
                File Folder = new File("path");
                if (!Folder.exists()) {
                    try{
                        Folder.mkdir(); //폴더 생성합니다.
                        System.out.println("폴더가 생성되었습니다.");
                    }
                    catch(Exception e){
                        e.getStackTrace();
                    }
                }
                setWritePath("./path/Our" + test + ".bmp");
                ImgWrite(Define.ImgOutput.Our);
            }


            // our = groundTruth;
            /*
            BranchBlockGraph bbg = new BranchBlockGraph(this);
            bbg.clear();
            our.get(game.playerPos.y).get(game.playerPos.x).type = Define.AIR; // for build graph
            bbg.checkBranchBlock();
            bbg.buildGraph();
            our.get(game.playerPos.y).get(game.playerPos.x).type = Define.PLAYER; // for build graph

            for(BranchBlock b : bbg.branchBlockHashMap.values()){
                our.get(b.y).get(b.x).type = Define.BRANCH_BLOCK;
            }

             */


            setWritePath("Result.txt");
            resultWrite(game.getEnergy());

        }catch (IOException e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void ImgWrite(Define.ImgOutput imgOutput){
        try{
            if(this.writePath == null)
                throw new IOException("Need to set variable 'writePath'");
            if(this.groundTruth == null && imgOutput == Define.ImgOutput.GroundTruth)
                throw new IOException("Need to run 'fileRead'");
            if(this.our == null && imgOutput == Define.ImgOutput.Our)
                throw new IOException("Need to run 'buildOur'");

            BufferedImage img = new BufferedImage(col, row, BufferedImage.TYPE_3BYTE_BGR);
            File file = new File(writePath);

            int count=0;
            Block element;
            Iterator<ArrayList<Block>> rowIT = null;
            Iterator<Block> colIT = null;

            switch (imgOutput){
                case GroundTruth:
                    rowIT = this.groundTruth.iterator();
                    break;
                case Our:
                    rowIT = our.iterator();
                    break;
            }

            while(rowIT.hasNext()){
                colIT = rowIT.next().iterator();
                while(colIT.hasNext()){
                    element = colIT.next();
                    switch (element.type){ // xx RR GG BB
                        case Define.AIR:
                            img.setRGB(count%col,count/col,0x00ffffff); // white
                            break;
                        case Define.WALL:
                            img.setRGB(count%col,count/col,0x00000000); // black
                            break;
                        case Define.BRANCH_BLOCK:
                            img.setRGB(count%col,count/col,0x0066ff66); // Green
                            break;
                        case Define.UNKNOWN:
                            img.setRGB(count%col,count/col,0x00444444); // Gray
                            break;
                        case Define.PLAYER:
                            img.setRGB(count%col,count/col,0x00ff0000); // RED
                            break;
                        case Define.GOAL:
                            img.setRGB(count%col,count/col,0x00660066); // purple
                            break;
                        case Define.BREAK:
                            img.setRGB(count%col,count/col,0x00ff00cc); // pink
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

    public int getRow(){
        return this.row;
    }
    public int getCol(){
        return this.col;
    }

    public void resultWrite(int remainEnergy){
        try{
            if(this.writePath == null)
                throw new IOException("Need to set variable 'wrtiePath'");
            FileWriter fileWriter = new FileWriter(this.writePath);

            int initialEnergy = getCol()*getRow()*2;

            fileWriter.write("Initial Energy : " + initialEnergy + "\n");
            fileWriter.write("Wasted Energy : " + (initialEnergy - remainEnergy) + "\n");
            fileWriter.write("Remain Energy : " + remainEnergy + "\n");
            fileWriter.write("Insert File Name : " + readPath + "\n");

            fileWriter.flush();
            fileWriter.close();
        }catch (IOException e){
            e.printStackTrace();
            System.exit(0);
        }
    }
}
