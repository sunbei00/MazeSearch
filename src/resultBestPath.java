import java.util.ArrayList;

public class resultBestPath {
    // 현재까지 밝혀낸 맵에서 최적의 경로를 알기 위한 클래스.
    
    private static Pos playerPos;
    private static Pos prevPos;


    // 현재까지 밝혀낸 맵에서 최적의 경로를 알기 위한 함수.
    public static ArrayList<ArrayList<Boolean>> BestWay(Pos goal,Model model){

        ArrayList<ArrayList<Boolean>> result = new ArrayList<>();

        int row = model.getRow();
        int col = model.getCol();

        for(int i = 0; i < row; i++)
            result.add(new ArrayList<>());
        for(int i = 0; i < row; i++)
            for(int j = 0; j < col; j++)
                result.get(i).add(false);

        for(int i = 0; i < row; i++)
            for(int j = 0; j < col; j++)
                if(model.our.get(i).get(j).type == Define.PLAYER)
                    model.our.get(i).get(j).type = Define.AIR;

        Pos movePos;
        playerPos = new Pos(1,0);
        prevPos = new Pos(1,0);
        result.get(0).set(1,true);
        BranchBlockGraph branchBlockGraph = new BranchBlockGraph(model);
        branchBlockGraph.clear();
        branchBlockGraph.checkBranchBlock();
        branchBlockGraph.addHashMap(goal);
        BranchBlock head = branchBlockGraph.buildGraph();

        // Branch 우선 순위 계산 및 경로로 이동
        Route route = new Route(head, playerPos, branchBlockGraph.branchBlockHashMap);
        route.SetList();
        ArrayList<DestInfo> destInfos = route.Dijkstra(branchBlockGraph.branchBlockHashMap.get(playerPos.hashCode()));

        ArrayList<Define.Direction> Directions = null;
            for(DestInfo destInfo : destInfos)
                if(goal.x == destInfo.branchBlock.x && goal.y == destInfo.branchBlock.y)
                    Directions = destInfo.directions;


        for (Define.Direction direction : Directions) {
            int distance = 0;
            if(direction == Define.Direction.UP)
                distance = branchBlockGraph.branchBlockHashMap.get(Util.HashCode(playerPos.x,playerPos.y)).up.distance - 1;
            if(direction == Define.Direction.DOWN)
                distance = branchBlockGraph.branchBlockHashMap.get(Util.HashCode(playerPos.x,playerPos.y)).down.distance - 1;
            if(direction == Define.Direction.LEFT)
                distance = branchBlockGraph.branchBlockHashMap.get(Util.HashCode(playerPos.x,playerPos.y)).left.distance - 1;
            if(direction == Define.Direction.RIGHT)
                distance = branchBlockGraph.branchBlockHashMap.get(Util.HashCode(playerPos.x,playerPos.y)).right.distance - 1;

            movePos = MapUtil.moveDirection(playerPos, direction, model);
            prevPos.setValue(playerPos.x,playerPos.y);
            playerPos.setValue(movePos.x, movePos.y);
            result.get(playerPos.y).set(playerPos.x, true);

            for(int i=0; i < distance; i++){
                movePos = MapUtil.moveAround(playerPos, prevPos, model);
                prevPos.setValue(playerPos.x,playerPos.y);
                playerPos.setValue(movePos.x, movePos.y);
                result.get(playerPos.y).set(playerPos.x, true);
            }
        }
        return result;
    }
}
