import java.io.File;
import java.util.ArrayList;
import java.util.Stack;

public class BreakItemUtil {

    // goal 주변 벽을 부수는 범위 조정 (맵 크기에 비례)
    private static double breakBlockRatio = 0.3;

    // 메모리 최적화를 위해 Pool 사용
    private static Pool.PosPool posPool = new Pool.PosPool();

    // goal을 찾았다는 가정이 존재.
    // goal 주변에 존재하는 벽을 하나씩 부수면서 다익스트라 알고리즘을 통해
    // goal로 이동할 수 있는지 판단하는 알고리즘
    // 너무 많은 시간을 소요되는 것을 방지하기 위해서, 해당 알고리즘의 수행 시간이 길수록
    // 다음에 함수 호출 시 goal 주변에 부시는 범위를 줄인다.
    public static boolean isGoodBreak(Pos goal , Game game, Model model){
        long beforeTime = System.currentTimeMillis();
        System.out.println("BreakItem 사용 여부 판단 중..");

        Stack<Pos> wallList = new Stack<>();
        int row = model.getRow();
        int col = model.getCol();
        for(int i = 1; i < row-1; i++)
            for(int j=1; j < col-1; j++)
                if(model.our.get(i).get(j).type == Define.WALL)
                    if((double)Math.abs(goal.x-j)/model.getCol() <= breakBlockRatio && (double)Math.abs(goal.y-i)/model.getRow() <= breakBlockRatio){
                        Pos wallPos = posPool.get();
                        wallPos.setValue(j, i);
                        wallList.push(wallPos);
                    }

        DestInfo dest = null;
        boolean isFind = false;
        Pos breakWall = new Pos();
        while(wallList.size() != 0){
            Pos wallPos = wallList.pop();
            model.our.get(wallPos.y).get(wallPos.x).type = Define.AIR;
            model.our.get(game.playerPos.y).get(game.playerPos.x).type = Define.AIR; // for build graph

            game.branchBlockGraph.clear();
            game.branchBlockGraph.checkBranchBlock();
            for(Pos BranchBlockPos : game.addBranchBlockPos)
                game.branchBlockGraph.addHashMap(BranchBlockPos);
            game.branchBlockGraph.addHashMap(game.playerPos);
            if(MapUtil.isAir(wallPos, Define.Direction.UP, model))
                game.branchBlockGraph.addHashMap(MapUtil.DirectionPosition(wallPos, Define.Direction.UP,model));
            if(MapUtil.isAir(wallPos, Define.Direction.LEFT, model))
                game.branchBlockGraph.addHashMap(MapUtil.DirectionPosition(wallPos, Define.Direction.LEFT,model));
            if(MapUtil.isAir(wallPos, Define.Direction.DOWN, model))
                game.branchBlockGraph.addHashMap(MapUtil.DirectionPosition(wallPos, Define.Direction.DOWN,model));
            if(MapUtil.isAir(wallPos, Define.Direction.RIGHT, model))
                game.branchBlockGraph.addHashMap(MapUtil.DirectionPosition(wallPos, Define.Direction.RIGHT,model));

            BranchBlock head = game.branchBlockGraph.buildGraph();
            Route route = new Route(head, game.playerPos, game.branchBlockGraph.branchBlockHashMap);
            route.SetList();
            ArrayList<DestInfo> destInfos = route.Dijkstra(game.branchBlockGraph.branchBlockHashMap.get(game.playerPos.hashCode()));

            for(DestInfo destInfo : destInfos)
                if(destInfo.branchBlock.x == goal.x && destInfo.branchBlock.y == goal.y)
                    if(destInfo.distance != Integer.MAX_VALUE){
                        dest = destInfo;
                        isFind = true;
                        breakWall.setValue(wallPos.x, wallPos.y);
                        break;
                    }

            model.our.get(game.playerPos.y).get(game.playerPos.x).type = Define.PLAYER;
            model.our.get(wallPos.y).get(wallPos.x).type = Define.WALL;
            posPool.push(wallPos);
            if(isFind)
                break;
        }
        long afterTime = System.currentTimeMillis(); 
        long secDiffTime = (afterTime - beforeTime); 
        if(secDiffTime > 1000) // 프로그램 속도가 너무 느려지는 것을 방지하기 위해서 block break rotaio 조정
            breakBlockRatio = Math.max(0.1, breakBlockRatio - 0.05) < breakBlockRatio ? Math.max(0.1, breakBlockRatio - 0.05) : breakBlockRatio;
        if(secDiffTime > 10000)
            breakBlockRatio = 0.05 < breakBlockRatio ? 0.05 : breakBlockRatio;
        if(secDiffTime > 20000)
            breakBlockRatio = 0.01 < breakBlockRatio ? 0.01 : breakBlockRatio;
        if(secDiffTime > 30000)
            breakBlockRatio = 0.001 < breakBlockRatio ? 0.001 : breakBlockRatio;

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

                        // Our Map에서 Branch Block의 조건에 만족하는 위치에 Branch Blcok을 만들고 Branch Block을 연결하여 graph 형식으로 만든다.
                        model.setWritePath("./Our" + ".bmp");
                        // brabch block
                        for(BranchBlock b : game.branchBlockGraph.branchBlockHashMap.values()){
                            model.our.get(b.y).get(b.x).type = Define.BRANCH_BLOCK;
                        }
                        //odel.our.get(game.playerPos.y).get(game.playerPos.x).type = Define.PLAYER;
                        model.ImgWrite(Define.ImgOutput.Our);
                        for(BranchBlock b : game.branchBlockGraph.branchBlockHashMap.values()){
                            model.our.get(b.y).get(b.x).type = Define.AIR;
                        }

                        System.out.println("Break Item Error");
                        System.exit(0);
                    }
                }
                MapUtil.moveDirection(game.playerPos, direction, model);
                MapUtil.applyMove(game.playerPos, game.prevPos, model);
                MapUtil.checkFinish(game.playerPos, game.breakPos, goal ,game.getEnergy() ,model);
                MapUtil.lookAround(game.playerPos, model);
                for(int i=0; i < distance; i++){
                    game.useScanWithScanPriority();
                    game.checkIsEndEnergy();
                    game.decreaseEnergy();
                    game.increaseMana();
                    boolean isBreak = false;


                    Pos looking = null;
                    if(game.isBreakItem()){
                        for(Pos move : Define.moveBoundary){
                            looking = posPool.get();
                            looking.setValue(game.playerPos.x + move.x , game.playerPos.y + move.y);
                            tmp = MapUtil.moveDirection(looking, MapUtil.getDirection(looking, breakWall), model);
                            if(tmp.x == breakWall.x && tmp.y == breakWall.y)
                                break;

                            posPool.push(looking);
                            looking = null;
                        }
                    }


                    if(looking != null && model.our.get(looking.y).get(looking.x).type == Define.WALL ){
                        if(!game.useBreak(new Pos(looking.x,looking.y))){
                            System.out.println("Break Item Error");
                            System.exit(0);
                        }
                        isBreak = true;
                    }

                    if(!isBreak)
                        MapUtil.moveAround(game.playerPos, game.prevPos, model);
                    else{
                        Define.Direction direct = MapUtil.getDirection(game.playerPos, looking);
                        MapUtil.moveDirection(game.playerPos,direct,model);
                    }
                    MapUtil.applyMove(game.playerPos, game.prevPos, model);
                    MapUtil.lookAround(game.playerPos, model);
                    MapUtil.checkFinish(game.playerPos, game.breakPos, goal, game.getEnergy() ,model);
                }
            }
            return true;
        } else {
            return false;
        }
    }
}
