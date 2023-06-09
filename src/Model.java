import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.io.*;
import java.util.Iterator;



public class Model {
    // 데이터 관련 입출력 함수

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
    public void setWritePath(String writePath){
        this.writePath = writePath;
    }

    // 맵 파일을 읽는 함수
    public void fileRead() {
        try{
            if(this.readPath == null)
                throw new IOException("Need to set variable 'readPath'");
            FileReader fileReader = new FileReader(this.readPath);

            int ch;
            this.groundTruth = new ArrayList<ArrayList<Block>>();
            this.groundTruth.add(new ArrayList<Block>()); // 최소 1개의 row가 있다고 가정.
            while((ch = fileReader.read()) != -1){
                if(ch == '\n')
                    this.groundTruth.add(new ArrayList<Block>());
                else if(ch == Define.AIR || ch == Define.WALL)
                    this.groundTruth.get(groundTruth.size()-1).add(new Block(ch));
                else if( ch > 32  && ch != 127) // 제어 문자, 빈 문자, 0,1 이외의 값이 들어오면 오류 처리
                    throw new IOException("File is Error(Error Character) : " +  (char)ch);
            }
            while(groundTruth.get(groundTruth.size()-1).size() == 0)
                groundTruth.remove(groundTruth.size()-1);

            this.row = this.groundTruth.size();
            this.col = this.groundTruth.get(0).size();

            for(int i=0;i<this.groundTruth.size();i++)
                if(this.groundTruth.get(i).size() != col)
                    throw new IOException("File is Error(Row Column Error)");

            fileReader.close();
        }catch (IOException e){
            e.printStackTrace();
            System.exit(0);
        }
    }


    // OurMap을 만드는 함수
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

            while(true){
                game.Move();
                System.out.println(game.getEnergy());

            }

        }catch (IOException e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    // 이미지 파일로 맵을 시각화해주는 함수
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
                            img.setRGB(count%col,count/col,0x0033ffff); // sky
                            break;
                        case Define.CLOSE:
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

    // 결과 출력해주는 함수 
    public void resultWrite(int remainEnergy, ArrayList<ArrayList<Boolean>> bestWay){

        int initialEnergy = getCol()*getRow()*2;
        int bestWayUsingEnergy = 0;
        if(bestWay != null){
            int row = getRow();
            int col = getCol();
            for(int i=0; i<row; i++)
                for(int j=0; j<col; j++)
                    if(bestWay.get(i).get(j))
                        bestWayUsingEnergy++;
        }


        System.out.println();
        System.out.println("Initial Energy : " + initialEnergy);
        System.out.println("Wasted Energy : " + (initialEnergy - remainEnergy));
        System.out.println("Remain Energy : " + remainEnergy);
        System.out.println("using Energy in best way : " + bestWayUsingEnergy);
        System.out.println("ratio with wasted energy and best way energy : " + ((double)(initialEnergy - remainEnergy) / bestWayUsingEnergy));
        System.out.println("Insert File Name : " + readPath + "\n\n");
        System.out.println();


        if(bestWay != null){
            int row = getRow();
            int col = getCol();
            for(int i=0; i<row; i++){
                for(int j=0; j<col; j++)
                    if(bestWay.get(i).get(j))
                        System.out.print("1");
                    else
                        System.out.print(" ");
                System.out.println();
            }
        }

        try{
            if(this.writePath == null)
                throw new IOException("Need to set variable 'wrtiePath'");
            FileWriter fileWriter = new FileWriter(this.writePath);


            fileWriter.write("Initial Energy : " + initialEnergy + "\n");
            fileWriter.write("Wasted Energy : " + (initialEnergy - remainEnergy) + "\n");
            fileWriter.write("Remain Energy : " + remainEnergy + "\n");
            fileWriter.write("using Energy in best way : " + bestWayUsingEnergy + "\n");
            fileWriter.write("ratio with wasted energy and best way energy : " + ((double)(initialEnergy - remainEnergy) / bestWayUsingEnergy) + "\n");
            fileWriter.write("Insert File Name : " + readPath + "\n\n");

            if(bestWay != null){
                int row = getRow();
                int col = getCol();
                for(int i=0; i<row; i++){
                    for(int j=0; j<col; j++)
                        if(bestWay.get(i).get(j))
                            fileWriter.write("1");
                        else
                            fileWriter.write(" ");
                    fileWriter.write("\n");
                }
            }

            fileWriter.flush();
            fileWriter.close();



        }catch (IOException e){
            e.printStackTrace();
            System.exit(0);
        }
    }

    // 결과와 이미지를 출력해주는 함수
    public void printImageSet(int remainEnergy, Pos playerPos, Pos breakPos, Pos goal, boolean isGoal){
        if(playerPos != null)
            our.get(playerPos.y).get(playerPos.x).type = Define.PLAYER;
        if(breakPos != null)
            our.get(breakPos.y).get(breakPos.x).type = Define.BREAK;

        setWritePath("./GroundTruth.bmp");
        ImgWrite(Define.ImgOutput.GroundTruth);
        setWritePath("./Our.bmp");
        ImgWrite(Define.ImgOutput.Our);
        setWritePath("result.txt");
        if(isGoal && goal != null)
            resultWrite(remainEnergy,resultBestPath.BestWay(goal,this));
        else
            resultWrite(remainEnergy,null);

    }
}
