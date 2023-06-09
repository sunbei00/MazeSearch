public class MapUtil {
    private static Pos look = new Pos(); // optimize for memory (Temp)
    private static Pos movePos = new Pos(); // optimize for memory (Temp)

    // 플레이어 위치 주변 3x3 사이즈를 보이게 만들음.
    public static void lookAround(Pos playerPos ,Model model){
        for(Pos p : Define.boundary){
            look.setValue(playerPos.x, playerPos.y);
            look.x += p.x;
            look.y += p.y;
            Util.calcIndex(look,model);
            if(model.our.get(look.y).get(look.x).type != Define.UNKNOWN) // 이미 알고있으면 계산x
                continue;
            if(model.groundTruth.get(look.y).get(look.x).type == Define.WALL) // 벽 표시
                model.our.get(look.y).get(look.x).type = Define.WALL;
            if(model.groundTruth.get(look.y).get(look.x).type == Define.AIR || model.groundTruth.get(look.y).get(look.x).type == Define.BREAK)
                model.our.get(look.y).get(look.x).type = Define.AIR;

        }
    }

    // 이전 위치와 현재 위치를 통해서 현재 플레이어 위치 기준으로 이전 위치가 어디방향인지 판단
    public static Define.Direction getDirection(Pos playerPos, Pos prevPos){
        // player pos 기준
        int x_sub = playerPos.x - prevPos.x;
        int y_sub = playerPos.y - prevPos.y;

        if(x_sub == 1)
            return Define.Direction.LEFT;
        if(x_sub == -1)
            return Define.Direction.RIGHT;
        if(y_sub == 1)
            return Define.Direction.UP;
        if(y_sub == -1)
            return Define.Direction.DOWN;

        return Define.Direction.UNKNOWN;
    }

    // 목적지에 도달했는지 판단하는 함수
    public static boolean isFinish(Pos playerPos, Model model){
        if(playerPos.x == 1 && playerPos.y == 0)
            return false;
        if(playerPos.x == 0 || playerPos.x == model.getCol()-1)
            return true;
        if(playerPos.y == 0 || playerPos.y == model.getRow()-1)
            return true;
        return false;
    }

    // 현재 위치가 목적지인지 판단하는 함수
    public static void checkFinish(Pos playerPos, Pos breakPos, Pos goal ,int remainEnergy ,Model model){
        if(isFinish(playerPos,model)){
            if(goal == null) {
                int col = model.getCol();
                int row = model.getRow();
                for(int i=0; i<row; i++){
                    if(model.our.get(i).get(0).type == Define.AIR || model.our.get(i).get(0).type == Define.PLAYER )
                        goal = new Pos(0, i);
                    if( model.our.get(i).get(model.getCol()-1).type == Define.AIR || model.our.get(i).get(model.getCol()-1).type == Define.PLAYER)
                        goal = new Pos(model.getCol()-1, i);
                }
                for(int i=0; i<col; i++){
                    if((model.our.get(0).get(i).type == Define.AIR || model.our.get(0).get(i).type == Define.PLAYER) && i != 1)
                        goal = new Pos(i, 0);
                    if( model.our.get(model.getRow()-1).get(i).type == Define.AIR ||  model.our.get(model.getRow()-1).get(i).type == Define.PLAYER)
                        goal = new Pos(i, model.getRow()-1);
                }
            }

            System.out.println("탈출!!");
            model.printImageSet(remainEnergy,playerPos,breakPos,goal,true);
            System.exit(0);
        }
    }

    // 해당 위치가 Branch Block인지 판단하는 함수
    public static boolean isBranchBlock(Pos playerPos, Model model){
         /*
            경우의 수 ( n := 길의 수 )
            i)   n > 3   : Branch Block으로 만들어줘야 함. -> true 반환
            ii)  n == 2  : 이동할 수 있음                 -> false 반환
            iii) n = 1   : 막 다른 골목                   -> true 반환
            iiii) scan에 의한 변수로인해 오류 처리 : 주변에 block이 2개이고, 길이 1개이고, unknown 1개일 때 true 반환.
        */
        int countWall = 0;
        int countUnknwon = 0;
        int countAir = 0;
        for (Pos p : Define.moveBoundary) {
            look.setValue(playerPos.x, playerPos.y);
            look.x += p.x;
            look.y += p.y;

            Util.calcIndex(look,model);
            if (playerPos.x == look.x && playerPos.y == look.y) // 시작점에서 계산을 위해.
                continue;
            if (model.our.get(look.y).get(look.x).type == Define.AIR || model.our.get(look.y).get(look.x).type == Define.BREAK )
                countAir++;
            if (model.our.get(look.y).get(look.x).type == Define.UNKNOWN){
                countUnknwon++;
            }
            if (model.our.get(look.y).get(look.x).type == Define.WALL)
                countWall++;
        }

        if(countWall == 1 && countAir == 2 && countUnknwon == 1)
            return true;
        switch (countAir){
            case 2:
                return false;
            default:
                return true;
        }
    }

    // moveAround나 moveDirection 함수로 위치를 반환하지만 our 맵으로 실제로 이동하지는 않음.
    // 위의 함수를 호출한 후에 해당 함수를 불러들여야지 적용이 된다.
    public static void applyMove(Pos playerPos, Pos prevPos, Model model){
        prevPos.setValue(playerPos.x,playerPos.y);
        model.our.get(prevPos.y).get(prevPos.x).type = Define.AIR;
        playerPos.setValue(movePos.x, movePos.y);
        model.our.get(playerPos.y).get(playerPos.x).type = Define.PLAYER;
    }

    // 현재 위치가 BranchBlock이 아니라는 가정이 필요
    // 길이 2개가 있을 때 이전 위치가 아닌 이동할 방향을 리턴
    public static Pos moveAround(Pos playerPos, Pos prevPos, Model model) {
        movePos.setValue(playerPos.x, playerPos.y);
        for (Pos p : Define.moveBoundary) {
            look.setValue(playerPos.x, playerPos.y);
            look.x += p.x;
            look.y += p.y;
            Util.calcIndex(look,model);
            if (model.our.get(look.y).get(look.x).type == Define.AIR || model.our.get(look.y).get(look.x).type == Define.BREAK) {
                if (look.isEquals(prevPos)) // 이전에 이동한 위치일 시
                    continue;
                movePos.setValue(look.x, look.y);
                return movePos;
            }
        }
        return null;
    }

    // 해당 방향이 이동할 수 있는 공간인지 판단하는데 사용하는 함수
    public static boolean isAir(Pos playerPos, Define.Direction direction, Model model){
        look.setValue(0,0);
        if(direction == Define.Direction.RIGHT)
            look.x++;
        if(direction == Define.Direction.LEFT)
            look.x--;
        if(direction == Define.Direction.UP)
            look.y--;
        if(direction == Define.Direction.DOWN)
            look.y++;
        look.x = playerPos.x + look.x;
        look.y = playerPos.y + look.y;
        Util.calcIndex(look,model);
        if(look.x == playerPos.x && look.y == playerPos.y) // 시작점을 위해서
            return false;
        if(model.our.get(look.y).get(look.x).type == Define.AIR ||  model.our.get(look.y).get(look.x).type == Define.BREAK)
            return true;
        return false;
    }

    // 현재 위치에서 해당 방향이 이동할 수 있는 블럭일 경우 위치 반환
    public static Pos moveDirection(Pos playerPos, Define.Direction direction, Model model) {
        look.setValue(playerPos.x, playerPos.y);
        if(direction == Define.Direction.UP)
            look.y += -1;
        if(direction == Define.Direction.DOWN)
            look.y += 1;
        if(direction == Define.Direction.LEFT)
            look.x += -1;
        if(direction == Define.Direction.RIGHT)
            look.x += 1;
        Util.calcIndex(look,model);
        if (model.our.get(look.y).get(look.x).type == Define.AIR || model.our.get(look.y).get(look.x).type == Define.BREAK )
            movePos.setValue(look.x, look.y);

        return movePos;
    }

    // 현재 위치에서 해당 방향의 위치 반환
    public static Pos DirectionPosition(Pos playerPos, Define.Direction direction, Model model) {
        look.setValue(playerPos.x, playerPos.y);
        if(direction == Define.Direction.UP)
            look.y += -1;
        if(direction == Define.Direction.DOWN)
            look.y += 1;
        if(direction == Define.Direction.LEFT)
            look.x += -1;
        if(direction == Define.Direction.RIGHT)
            look.x += 1;
        Util.calcIndex(look,model);
        movePos.setValue(look.x, look.y);
        return movePos;
    }

    // 목적지를 찾았는지 확인하는 함수
    public static Pos CheckFindGoal(Pos goal, Model model){
        if(goal != null)
            return null;
        int col = model.getCol();
        int row = model.getRow();
        for(int i=0; i<row; i++){
            if(model.our.get(i).get(0).type == Define.AIR )
                goal = new Pos(0, i);
            if( model.our.get(i).get(model.getCol()-1).type == Define.AIR)
                goal = new Pos(model.getCol()-1, i);
        }
        for(int i=0; i<col; i++){
            if(model.our.get(0).get(i).type == Define.AIR && i != 1)
                goal = new Pos(i, 0);
            if( model.our.get(model.getRow()-1).get(i).type == Define.AIR)
                goal = new Pos(i, model.getRow()-1);
        }
        return goal;
    }

    // 맵이 이동할 수 있는 곳이 있는지 판단하는 함수.
    public static boolean cantFindOut(Model model){
        int col = model.getCol();
        int row = model.getRow();
        for(int i = 0; i<row; i++)
            for(int j = 0; j<col;j++)
                if(model.our.get(i).get(j).type == Define.UNKNOWN)
                    return false;
        return true;
    }

}
