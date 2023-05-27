import java.lang.reflect.Array;
import java.util.ArrayList;

public class Game {

    private int energy;
    private float mana;
    private boolean breakItem;
    private Pos breakPos;
    private Model model;
    private Pos playerPos = new Pos();
    private Pos look = new Pos(); // optimize memory (Temp)
    private Pos prevPos = new Pos(); // Temp for move
    private Pos movePos = new Pos(); // Temp for moveAround
    private BranchBlock prevBranchBlock = null;
    private int accumulateDistance = 0;
    private Define.Direction prevBranchDirection;

    public int getEnergy() {
        return energy;
    }
    public boolean isEnergy(){
        if(this.energy > 0)
            return true;
        return false;
    }
    private void checkIsEndEnergy(){
        if(!isEnergy()){
            // GAME OVER
            // FILE WRITE
            // EXIT
            System.out.println("!isEnergy");
            System.exit(0);
        }
    }
    private void decreaseEnergy(){
        if(this.energy <= 0)
            return;
        this.energy--;
    }
    private void increaseMana(){
        this.mana += 0.1f;
        if(this.mana >= 3.0f)
            this.mana = 3.0f;
    }
    public boolean isMana(){
        if(Math.abs(this.mana - 3.0f) <= 10e-4) // float error
            return true;
        return false;
    }
    public boolean isBreakItem(){
        return this.breakItem;
    }
    public Game(Model model){
        this.model = model;
        this.energy = model.getCol() * model.getRow() * 2;
        this.mana = 3.0f;
        this.breakItem = true;


        this.playerPos.setValue(1,0);
        this.model.our.get(this.playerPos.y).get(this.playerPos.x).type = Define.PLAYER;
        MapUtil.lookAround(playerPos, model);
        model.branchBlockHashMap.clear();
        model.branchBlockHashMap.put(Util.HashCode(1,0), new BranchBlock(1,0));
        prevPos.setValue(1,0);
        prevBranchBlock = model.branchBlockHashMap.get(Util.HashCode(1,0));
        if(model.our.get(1).get(1).type == Define.AIR){
            prevBranchBlock.down.exist = true;
            //  playerPos.x-prevPos.x    playerPos.y-prevPos.y
            prevBranchDirection = Define.Direction.DOWN;

        }else{
            // Game Over
        }
        accumulateDistance = 0;
    }

    BranchBlock makeBranchBlock(int x,int y){
        System.out.println("Make Branch Block");
        BranchBlock newBranchBlock = new BranchBlock(playerPos.x, playerPos.y);
        BranchBlockUtil.linkBranchBlock(newBranchBlock,prevBranchBlock,accumulateDistance,playerPos,prevPos,prevBranchDirection,model);
        model.branchBlockHashMap.put(newBranchBlock.hashCode(),newBranchBlock);
        prevBranchBlock = newBranchBlock;
        accumulateDistance = 0;
        return newBranchBlock;
    }

    private void calculatePriorityAndMove(){
        BranchBlock branchBlock = null;
        if(!model.branchBlockHashMap.containsKey(Util.HashCode(this.playerPos.x,this.playerPos.y)))
            branchBlock = makeBranchBlock(this.playerPos.x,this.playerPos.y);
        else
            branchBlock = model.branchBlockHashMap.get(Util.HashCode(playerPos.x,playerPos.y));

        // Branch 우선 순위 계산 및 경로로 이동
        Route route = new Route(model.branchBlockHashMap.get(Util.HashCode(1,0)), playerPos, model);
        route.SetList();
        ArrayList<DestInfo> destInfos = route.Dijkstra(branchBlock);
        // 어느 방향으로 이동했는지에 대해서도 저장을 해야한다,

        Priority.BranchPriority priority = new Priority.BranchPriority(model, destInfos);
        Pos dest = priority.HighestPriorityBranch();

        if(priority.maxPriority == Integer.MIN_VALUE){
            // Game Over
            System.out.println("이동할 수 있는 맵이 미존재");
            System.exit(0);
        }

        for (Define.Direction direction : priority.destResult.directions) {
            int distance = 0;
            if(direction == Define.Direction.UP)
                distance = model.branchBlockHashMap.get(Util.HashCode(playerPos.x,playerPos.y)).up.distance - 1;
            if(direction == Define.Direction.DOWN)
                distance = model.branchBlockHashMap.get(Util.HashCode(playerPos.x,playerPos.y)).down.distance - 1;
            if(direction == Define.Direction.LEFT)
                distance = model.branchBlockHashMap.get(Util.HashCode(playerPos.x,playerPos.y)).left.distance - 1;
            if(direction == Define.Direction.RIGHT)
                distance = model.branchBlockHashMap.get(Util.HashCode(playerPos.x,playerPos.y)).right.distance - 1;

            checkIsEndEnergy();
            decreaseEnergy();
            increaseMana();
            MapUtil.moveDirection(playerPos, direction, model);
            MapUtil.applyMove(playerPos, prevPos, model);
            for(int i=0; i < distance; i++){
                checkIsEndEnergy();
                decreaseEnergy();
                increaseMana();
                MapUtil.moveAround(playerPos, prevPos, model);
                MapUtil.applyMove(playerPos, prevPos, model);
            }
        }

        checkIsEndEnergy();
        int x_sub = playerPos.x-dest.x;
        int y_sub = playerPos.y-dest.y;
        decreaseEnergy();
        increaseMana();
        // left
        prevBranchBlock = model.branchBlockHashMap.get(Util.HashCode(playerPos.x,playerPos.y));
        if(x_sub == 1){
            MapUtil.moveDirection(playerPos,Define.Direction.LEFT, model);
            MapUtil.applyMove(playerPos, prevPos, model);
            prevBranchDirection = Define.Direction.LEFT;
        }
        // right
        if(x_sub == -1){
            MapUtil.moveDirection(playerPos,Define.Direction.RIGHT, model);
            MapUtil.applyMove(playerPos, prevPos, model);
            prevBranchDirection = Define.Direction.RIGHT;
        }
        // down
        if(y_sub == -1){
            MapUtil.moveDirection(playerPos,Define.Direction.DOWN, model);
            MapUtil.applyMove(playerPos, prevPos, model);
            prevBranchDirection = Define.Direction.DOWN;
        }
        // up
        if(y_sub == 1){
            MapUtil.moveDirection(playerPos,Define.Direction.UP, model);
            MapUtil.applyMove(playerPos, prevPos, model);
            prevBranchDirection = Define.Direction.UP;
        }
    }
    public void Move(){
        // GAME OVER : 우선순위 계산 할 Branch가 미존재 할 시 (우선순위에서 계산해야 할 듯)
        if(MapUtil.isFinish(playerPos,model)){
            // Game Clear
            // FILE WRITE
            // Exit
            System.out.println("isFinish");
            System.exit(0);
        }
        decreaseEnergy();
        increaseMana();
        accumulateDistance++;

        MapUtil.moveAround(playerPos, prevPos, model);
        MapUtil.applyMove(playerPos, prevPos, model);

        if(MapUtil.isBranchBlock(playerPos, model))
            calculatePriorityAndMove();

        /*
        if(isMana()){
            // 스캔 우선 순위 계산
            mana = 0.f;
            Priority.ScanPriority scanPriority = new Priority.ScanPriority(model,playerPos,model.our);
            Define.ScanPoint scanPoint = scanPriority.HighestPriorityScan();
            look.setValue(scanPoint.x,scanPoint.y);
            useScan(look);
            calculatePriorityAndMove();
        }
        */
        MapUtil.lookAround(playerPos, model);
    }

    public boolean useScan(Pos pos){
        for(Pos p : Define.sacnBoundary){
            Pos look = new Pos(pos.x, pos.y);
            look.x += p.x;
            look.y += p.y;
            Util.calcIndex(look,model);
            if(this.model.our.get(look.y).get(look.x).type != Define.UNKNOWN) // 이미 알고있으면 계산x
                continue;
            if(this.model.groundTruth.get(look.y).get(look.x).type == Define.WALL) // 벽 표시
                this.model.scan.add(new ScanBlock(Define.WALL, look));
            if(this.model.groundTruth.get(look.y).get(look.x).type == Define.AIR) // 길 표시
                this.model.scan.add(new ScanBlock(Define.AIR, look));

            if(look.x == 0 || look.x == this.model.getCol() - 1 || look.y == 0 || look.y == this.model.getRow() -1)
                if(!(look.x == 1 && look.y == 0))  // 출발점을 제외한 벽의 양 끝에 길이 있으면 목적지
                    if(this.model.groundTruth.get(look.y).get(look.x).type == Define.AIR)
                        this.model.scan.add(new ScanBlock(Define.GOAL, look));
        }
        return true;
    }
    public boolean useBreak(Pos pos){
        if(!isBreakItem())
            return false;
        int magnitude = Math.abs(this.playerPos.x - pos.x) + Math.abs(this.playerPos.y - pos.y);
        if(magnitude != 1) // 주변에 있는 칸이 아닌 경우 리턴 false
            return false;
        if(pos.x <= 0 || pos.x >= this.model.getCol() - 1)  // 양끝의 벽은 부실 수 없음
            return false;
        if(pos.y <= 0 || pos.x >= this.model.getRow() - 1) // 양끝의 벽은 부실 수 없음
            return false;
        if(this.model.groundTruth.get(pos.y).get(pos.x).type != Define.WALL) // 부시려는 것이 벽이 아닐 경우
            return false;

        this.model.groundTruth.get(pos.y).get(pos.x).type = Define.BREAK; // GroundTruth에 벽을 부순 위치 표시
        for(Pos p : Define.boundary) { // lookAround 재계산을 위해서 주변 AIR를 제외한 것들을 UNKNOWN으로 변경
            Pos look = new Pos(this.playerPos.x, this.playerPos.y);
            look.x += p.x;
            look.y += p.y;
            Util.calcIndex(look,model);
            if(this.model.our.get(look.y).get(look.x).type != Define.AIR)
                this.model.our.get(look.y).get(look.x).type = Define.UNKNOWN;
        }
        MapUtil.lookAround(playerPos, model);
        this.breakItem = false;
        breakPos = pos;
        return true;
    }

}
