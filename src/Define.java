public class Define {
    static final int AIR = '0';         // (둘다에서) 이동할 수 있는 공간. 이미 이동해 본 공간 
    static final int WALL = '1';        // (둘다에서) 벽
    static final int UNKNOWN = '2';     // (our Map에서) 모르는 공간
    static final int BRANCH = '3';      // (our Map에서) 분기점 위치
    static final int PLAYER = '4';      // (our Map에서) 플레이어 위치
    static final int GOING = '5';       // (our Map에서) 플레이어의 상하좌우에 위치하면 반드시 플레이어가 이동
    static final int GOAL = '6';        // (our Map에서) Move 또는 Scan 시 발견하는 목적지
    static final int BREAK = '7';       // (GroundTruth에서) BreakItem으로 부신 위치

    enum ImgOutput{
        GroundTruth,
        Our
    }

    static class Block{

        public int type;
        public int priority; // using in branch

        public Block(){
            this.type = UNKNOWN;
            this.priority = -1;
        }
        public Block(int type){
            this.type = type;
            this.priority = -1;
        }
        public Block(int type, int priority){
            this.type = type;
            this.priority = priority;
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
}
