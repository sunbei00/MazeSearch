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
    static final int CLOSE = '7';       // (GroundTruth에서) BreakItem으로 부신 위치


    enum ImgOutput{
        GroundTruth,
        Our
    }

    final static Pos[] boundary = new Pos[]{
            new Pos(0,-1), // 상
            new Pos(0,1),  // 하
            new Pos(-1,0), // 좌
            new Pos(1,0),  // 우

            new Pos(-1,-1), // 좌상
            new Pos(-1,1),  // 좌하
            new Pos(1,-1), // 우상
            new Pos(1,1),  // 우하
    };
    final static Pos[] moveBoundary = new Pos[]{
            new Pos(0,-1), // 상
            new Pos(0,1),  // 하
            new Pos(-1,0), // 좌
            new Pos(1,0),  // 우
    };
    final static Pos[] sacnBoundary = new Pos[]{
            new Pos(-2,-2),
            new Pos(-2,-1),
            new Pos(-2,0),
            new Pos(-2,1),
            new Pos(-2,2),
            new Pos(-1,-1),
            new Pos(-1,0),
            new Pos(-1,-2),
            new Pos(-1,1),
            new Pos(-1,2),
            new Pos(0,-1),
            new Pos(0,0),
            new Pos(0,-2),
            new Pos(0,1),
            new Pos(0,2),
            new Pos(1,-2),
            new Pos(1,-1),
            new Pos(1,0),
            new Pos(1,1),
            new Pos(1,2),
            new Pos(2,-2),
            new Pos(2,-1),
            new Pos(2,0),
            new Pos(2,1),
            new Pos(2,2),
    };

    static enum Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT,
        UNKNOWN
    }


}
