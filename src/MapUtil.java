public class MapUtil {
    private static Pos look = new Pos(); // optimize for memory (Temp)
    private static Pos movePos = new Pos(); // optimize for memory (Temp)

    public static void lookAround(Pos playerPos, Model model){
        for(Pos p : Define.boundary){
            look.setValue(playerPos.x, playerPos.y);
            look.x += p.x;
            look.y += p.y;
            Util.calcIndex(look,model);
            if(model.our.get(look.y).get(look.x).type != Define.UNKNOWN) // 이미 알고있으면 계산x
                continue;
            if(model.groundTruth.get(look.y).get(look.x).type == Define.WALL) // 벽 표시
                model.our.get(look.y).get(look.x).type = Define.WALL;
            if(model.groundTruth.get(look.y).get(look.x).type == Define.AIR || model.groundTruth.get(look.y).get(look.x).type == Define.BREAK){
                model.our.get(look.y).get(look.x).type = Define.AIR;
            }
        }
    }

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

    public static boolean isFinish(Pos playerPos, Model model){
        if(playerPos.x == 1 && playerPos.y == 0)
            return false;
        if(playerPos.x == 0 || playerPos.x == model.getCol()-1)
            return true;
        if(playerPos.y == 0 || playerPos.y == model.getRow()-1)
            return true;
        return false;
    }

    public static void checkFinish(Pos playerPos, int remainEnergy ,Model model){
        if(isFinish(playerPos,model)){
            // FILE WRITE
            System.out.println("탈출!!");
            model.setWritePath("./GroundTruth.bmp");
            model.ImgWrite(Define.ImgOutput.GroundTruth);
            model.setWritePath("./Our.bmp");
            model.ImgWrite(Define.ImgOutput.Our);
            model.setWritePath("result.txt");
            model.resultWrite(remainEnergy);
            System.exit(0);
        }
    }

    public static boolean isBranchBlock(Pos playerPos, Model model){
         /*
            경우의 수 ( n := 길의 수 )
            i)   n > 3   : Branch Block으로 만들어줘야 함. -> true 반환
            ii)  n == 2  : 이동할 수 있음                 -> false 반환
            iii) n = 1   : 막 다른 골목                   -> true 반환
        */
        int i = 0;
        for (Pos p : Define.moveBoundary) {
            look.setValue(playerPos.x, playerPos.y);
            look.x += p.x;
            look.y += p.y;

            Util.calcIndex(look,model);
            if (playerPos.x == look.x && playerPos.y == look.y) // 시작점에서 계산을 위해.
                continue;
            if (model.our.get(look.y).get(look.x).type == Define.AIR)
                i++;
        }

        switch (i){
            case 2:
                return false;
            default:
                return true;
        }
    }

    public static void applyMove(Pos playerPos, Pos prevPos, Model model){
        prevPos.setValue(playerPos.x,playerPos.y);
        model.our.get(prevPos.y).get(prevPos.x).type = Define.AIR;
        playerPos.setValue(movePos.x, movePos.y);
        model.our.get(playerPos.y).get(playerPos.x).type = Define.PLAYER;
    }
    public static Pos moveAround(Pos playerPos, Pos prevPos, Model model) {
        // Branch Block이 아니라는 가정이 필수!! 잊지말기!!
        movePos.setValue(playerPos.x, playerPos.y);
        for (Pos p : Define.moveBoundary) {
            look.setValue(playerPos.x, playerPos.y);
            look.x += p.x;
            look.y += p.y;
            Util.calcIndex(look,model);
            if (model.our.get(look.y).get(look.x).type == Define.AIR) {
                if (look.isEquals(prevPos)) // 이전에 이동한 위치일 시
                    continue;
                movePos.setValue(look.x, look.y);
                return movePos;
            }
        }
        return null;
    }

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

    public static Pos moveDirection(Pos playerPos, Define.Direction direction, Model model) {
        // direction 방향의 Block이 아니라는 가정이 필수!! 잊지말기!!
        movePos.setValue(playerPos.x, playerPos.y);
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

    public static boolean isDeadEnd(BranchBlock branchBlock, Define.Direction direction, Model model){
        look.setValue(branchBlock.x, branchBlock.y);
        Pos tmp = moveDirection(look, direction, model);
        look.setValue(tmp.x, tmp.y);

        int count = 0;
        tmp = moveDirection(look, Define.Direction.LEFT, model);
        if(model.our.get(tmp.y).get(tmp.y).type == Define.WALL)
            count++;
        tmp = moveDirection(look, Define.Direction.RIGHT, model);
        if(model.our.get(tmp.y).get(tmp.y).type == Define.WALL)
            count++;
        tmp = moveDirection(look, Define.Direction.UP, model);
        if(model.our.get(tmp.y).get(tmp.y).type == Define.WALL)
            count++;
        tmp = moveDirection(look, Define.Direction.DOWN, model);
        if(model.our.get(tmp.y).get(tmp.y).type == Define.WALL)
            count++;

        if(count >= 3)
            return true;

        return false;
    }
}
