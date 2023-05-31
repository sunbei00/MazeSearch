import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class Define {
    static final int AIR = '0';         // 이동할 수 있는 공간 혹은 이미 이동해 본 공간 
    static final int WALL = '1';        // 이동할 수 없는 공간 (벽)
    static final int UNKNOWN = '2';     // (our Map에서) 모르는 공간
    static final int BRANCH_BLOCK = '3';// (our Map에서) 분기점 위치
    static final int PLAYER = '4';      // (our Map에서) 플레이어 위치
    static final int GOAL = '5';        // (our Map에서) Move 또는 Scan 시 발견하는 목적지
    static final int BREAK = '6';       // BreakItem으로 벽을 부신 위치
    static final int CLOSE = '7';       // (our Map에서) 이동할 필요 없다고 판단한 위치


    // 맵 출력 시 어떤 것을 출력할지 사용하는 Enum
    enum ImgOutput{
        GroundTruth,
        Our
    }

    // 이동하면서 볼 수 있는 상대적 위치
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

    // 이동할 수 있는 상대적 위치
    final static Pos[] moveBoundary = new Pos[]{
            new Pos(0,-1), // 상
            new Pos(0,1),  // 하
            new Pos(-1,0), // 좌
            new Pos(1,0),  // 우
    };

    // scan을 통해 볼 수 있는 상대적 위치
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

    // 방향을 넘겨주기 위한 enum
    enum Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT,
        UNKNOWN
    }


}
