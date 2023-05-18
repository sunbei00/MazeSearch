import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class Define {
    static final int AIR = '0';         // (둘다에서) 이동할 수 있는 공간. 이미 이동해 본 공간 
    static final int WALL = '1';        // (둘다에서) 벽
    static final int UNKNOWN = '2';     // (our Map에서) 모르는 공간
    static final int BRANCH_BLOCK = '3';// (our Map에서) 분기점 위치
    static final int PLAYER = '4';      // (our Map에서) 플레이어 위치
    static final int GOAL = '5';        // (our Map에서) Move 또는 Scan 시 발견하는 목적지
    static final int BREAK = '6';       // (GroundTruth에서) BreakItem으로 부신 위치


    enum ImgOutput{
        GroundTruth,
        Our
    }

    static class Block{

        public int type;

        public Block(){
            this.type = UNKNOWN;
        }
        public Block(int type){
            this.type = type;
        }
    }

    static class Pos{
        public int x;
        public int y;

        public Pos(int x, int y){
            this.x = x;
            this.y = y;
        }
        public Pos(){
            this.x = 1;
            this.y = 0;
        }
    }

    final static Define.Pos[] boundary = new Define.Pos[]{
            new Define.Pos(0,-1), // 상
            new Define.Pos(0,1),  // 하
            new Define.Pos(-1,0), // 좌
            new Define.Pos(1,0),  // 우

            new Define.Pos(-1,-1), // 좌상
            new Define.Pos(-1,1),  // 좌하
            new Define.Pos(1,-1), // 우상
            new Define.Pos(1,1),  // 우하
    };
    final static Define.Pos[] sacnBoundary = new Define.Pos[]{
            new Define.Pos(-2,-2),
            new Define.Pos(-2,-1),
            new Define.Pos(-2,0),
            new Define.Pos(-2,1),
            new Define.Pos(-2,2),
            new Define.Pos(-1,-2),
            new Define.Pos(-1,-1),
            new Define.Pos(-1,0),
            new Define.Pos(-1,1),
            new Define.Pos(-1,2),
            new Define.Pos(0,-1),
            new Define.Pos(0,0),
            new Define.Pos(0,-2),
            new Define.Pos(0,1),
            new Define.Pos(0,2),
            new Define.Pos(1,-2),
            new Define.Pos(1,-1),
            new Define.Pos(1,0),
            new Define.Pos(1,1),
            new Define.Pos(1,2),
            new Define.Pos(2,-2),
            new Define.Pos(2,-1),
            new Define.Pos(2,0),
            new Define.Pos(2,1),
            new Define.Pos(2,2),
    };

    public static class orientation{
        boolean exist;
        int distance;
        BranchBlock linkedBranch;
        public orientation(boolean exist, int distance, BranchBlock linkedBranch){
            this.exist = exist;
            this.distance = distance;
            this.linkedBranch = linkedBranch;
        }
    }

    public static int hashCode(int x,int y) {
        return x*10000 + y;
    }

    public static class BranchBlock{

        public orientation up = new orientation(false,-1, null);
        public orientation down = new orientation(false,-1, null);
        public orientation left = new orientation(false,-1, null);
        public orientation right = new orientation(false,-1, null);
        public int x;
        public int y;
        public BranchBlock(int x,int y){
            this.x = x;
            this.y = y;
        }
        @Override
        public int hashCode() {
            return x*10000 + y;
        }
    }

    public static HashMap<Integer, BranchBlock> branchBlockHashMap = new HashMap<Integer, BranchBlock>();
}
