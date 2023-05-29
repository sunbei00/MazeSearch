import java.util.ArrayList;
import java.util.Stack;

public class BreakItemUtil {
    private static Pool.PosPool posPool = new Pool.PosPool();

    public static boolean isGoodBreak(Pos goal , Game game, Model model){
        long beforeTime = System.currentTimeMillis();
        // goal 찾았다는 가정이 들어감.

        Stack<Pos> wallList = new Stack<>();
        int row = model.getRow();
        int col = model.getCol();
        for(int i = 1; i < row-1; i++)
            for(int j=1; j < col-1; j++)
                if(model.our.get(i).get(j).type == Define.WALL){
                    Pos wallPos = posPool.get();
                    wallPos.setValue(j, i);
                    wallList.push(wallPos);
                }
        DestInfo dest = null;
        boolean isFind = false;
        while(wallList.size() != 0){
            Pos wallPos = wallList.pop();
            model.our.get(wallPos.y).get(wallPos.x).type = Define.AIR;
            model.our.get(game.playerPos.y).get(game.playerPos.x).type = Define.AIR; // for build graph

            game.branchBlockGraph.clear();
            game.branchBlockGraph.checkBranchBlock();
            for(Pos BranchBlockPos : game.addBranchBlockPos)
                game.branchBlockGraph.addHashMap(BranchBlockPos);
            BranchBlock head = game.branchBlockGraph.buildGraph();
            Route route = new Route(head, game.playerPos, game.branchBlockGraph.branchBlockHashMap);
            route.SetList();
            ArrayList<DestInfo> destInfos = route.Dijkstra(game.branchBlockGraph.branchBlockHashMap.get(game.playerPos.hashCode()));

            for(DestInfo destInfo : destInfos)
                if(destInfo.branchBlock.x == goal.x && destInfo.branchBlock.y == goal.y)
                    if(destInfo.distance != Integer.MAX_VALUE){
                        dest = destInfo;
                        isFind = true;
                        break;
                    }

            model.our.get(game.playerPos.y).get(game.playerPos.x).type = Define.PLAYER;
            model.our.get(wallPos.y).get(wallPos.x).type = Define.WALL;
            posPool.push(wallPos);
            if(isFind)
                break;
        }
        long afterTime = System.currentTimeMillis(); // 코드 실행 후에 시간 받아오기
        long secDiffTime = (afterTime - beforeTime); //두 시간에 차 계산
        System.out.println("시간차이(m) : "+secDiffTime);
        if(dest != null){

            for (Define.Direction direction : dest.directions) {
                int distance = 0;
                if(direction == Define.Direction.UP)
                    distance = game.branchBlockGraph.branchBlockHashMap.get(Util.HashCode(game.playerPos.x,game.playerPos.y)).up.distance - 1;
                if(direction == Define.Direction.DOWN)
                    distance = game.branchBlockGraph.branchBlockHashMap.get(Util.HashCode(game.playerPos.x,game.playerPos.y)).down.distance - 1;
                if(direction == Define.Direction.LEFT)
                    distance = game.branchBlockGraph.branchBlockHashMap.get(Util.HashCode(game.playerPos.x,game.playerPos.y)).left.distance - 1;
                if(direction == Define.Direction.RIGHT)
                    distance = game.branchBlockGraph.branchBlockHashMap.get(Util.HashCode(game.playerPos.x,game.playerPos.y)).right.distance - 1;

                game.checkIsEndEnergy();
                game.decreaseEnergy();
                game.increaseMana();
                Pos tmp = MapUtil.DirectionPosition(game.playerPos, direction, model);

                if(model.our.get(tmp.y).get(tmp.x).type == Define.WALL){
                    if(!game.useBreak(new Pos(tmp.x,tmp.y))){
                        System.out.println("Break Item Error");
                        System.exit(0);
                    }
                }
                MapUtil.moveDirection(game.playerPos, direction, model);
                MapUtil.applyMove(game.playerPos, game.prevPos, model);
                MapUtil.checkFinish(game.playerPos,game.getEnergy() ,model);
                MapUtil.lookAround(game.playerPos, model);
                for(int i=0; i < distance; i++){
                    game.useScanWithScanPriority();
                    game.checkIsEndEnergy();
                    game.decreaseEnergy();
                    game.increaseMana();
                    boolean isBreak = false;
                    tmp = MapUtil.moveAround(game.playerPos, game.prevPos, model);

                    if(model.our.get(tmp.y).get(tmp.x).type == Define.WALL){
                        if(!game.useBreak(new Pos(tmp.x,tmp.y))){
                            System.out.println("Break Item Error");
                            System.exit(0);
                        }
                        isBreak = true;
                    }
                    if(!isBreak)
                        MapUtil.moveAround(game.playerPos, game.prevPos, model);
                    else{
                        Define.Direction direct = MapUtil.getDirection(game.playerPos, tmp);
                        MapUtil.moveDirection(game.playerPos,direct,model);
                    }
                    MapUtil.applyMove(game.playerPos, game.prevPos, model);
                    MapUtil.lookAround(game.playerPos, model);
                    MapUtil.checkFinish(game.playerPos, game.getEnergy() ,model);
                }
            }
            return true;
        }else{
            return false;
        }
    }
}
