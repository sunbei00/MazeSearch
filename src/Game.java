import java.lang.reflect.Array;
import java.util.ArrayList;

public class Game {

    private int energy;
    private float mana;
    private boolean breakItem;
    private Define.Pos breakPos;
    private Model model;
    private Define.Pos playerPos = new Define.Pos();
    private static Define.Pos look = new Define.Pos(); // optimize for memory (Temp)
    private static Define.Pos prevPos = new Define.Pos(); // Temp for move
    private static Define.Pos movePos = new Define.Pos(); // Temp for moveAround
    private static Define.BranchBlock prevBranchBlock = null; // Temp for move
    private static int accumulateDistance = 0;

    private Route.Direction prevBranchDirection;

    public int getEnergy() {
        return energy;
    }

    public boolean isEnergy(){
        if(this.energy > 0)
            return true;
        return false;
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
        if(this.mana == 3.0f)
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
        lookAround();
        Define.branchBlockHashMap.clear();
        Define.branchBlockHashMap.put(Define.HashCode(1,0), new Define.BranchBlock(1,0));
        prevPos.setValue(1,0);
        Game.prevBranchBlock = Define.branchBlockHashMap.get(Define.HashCode(1,0));
        if(model.our.get(1).get(1).type == Define.AIR){
            prevBranchBlock.down.exist = true;
            //  playerPos.x-prevPos.x    playerPos.y-prevPos.y
            prevBranchDirection = Route.Direction.DOWN;

        }else{
            // Game Over
        }
        accumulateDistance = 0;
    }

    private void calcIndex(Define.Pos pos){ // index error 방지
        if(pos.x < 0)
            pos.x = 0;
        if(pos.x >= this.model.getCol())
            pos.x = this.model.getCol() - 1;
        if(pos.y < 0)
            pos.y = 0;
        if(pos.y >= this.model.getRow())
            pos.y = this.model.getRow() - 1;
    }

    private void lookAround(){
        for(Define.Pos p : Define.boundary){
            look.setValue(this.playerPos.x, this.playerPos.y);
            look.x += p.x;
            look.y += p.y;
            calcIndex(look);
            if(this.model.our.get(look.y).get(look.x).type != Define.UNKNOWN) // 이미 알고있으면 계산x
                continue;
            if(this.model.groundTruth.get(look.y).get(look.x).type == Define.WALL) // 벽 표시
                this.model.our.get(look.y).get(look.x).type = Define.WALL;
            if(this.model.groundTruth.get(look.y).get(look.x).type == Define.AIR || this.model.groundTruth.get(look.y).get(look.x).type == Define.BREAK){
                this.model.our.get(look.y).get(look.x).type = Define.AIR;
            }
        }
    }

    private boolean isFinish(){
        if(playerPos.x == 1 && playerPos.y == 0)
            return false;
        if(playerPos.x == 0 || playerPos.x == this.model.getCol()-1)
            if(playerPos.y == 0 || playerPos.y == this.model.getRow()-1)
                return true;
        return false;
    }

    private boolean isBranchBlock(){
         /*
            경우의 수 ( n := 길의 수 )
            i)   n > 3   : Branch Block으로 만들어줘야 함. -> true 반환
            ii)  n == 2  : 이동할 수 있음                 -> false 반환
            iii) n = 1   : 막 다른 골목                   -> true 반환
        */
        int i = 0;
        for (Define.Pos p : Define.moveBoundary) {
            look.setValue(this.playerPos.x, this.playerPos.y);
            look.x += p.x;
            look.y += p.y;

            calcIndex(look);
            if (this.model.our.get(look.y).get(look.x).type == Define.AIR)
                i++;
        }

        switch (i){
            case 2:
                return false;
            default:
                return true;
        }
    }

    private void moveAround() {
        // Branch Block이 아니라는 가정이 필수!! 잊지말기!!
        movePos.setValue(playerPos.x, playerPos.y);
        for (Define.Pos p : Define.moveBoundary) {
            look.setValue(this.playerPos.x, this.playerPos.y);
            look.x += p.x;
            look.y += p.y;
            calcIndex(look);
            if (this.model.our.get(look.y).get(look.x).type == Define.AIR) {
                if (look.isEquals(prevPos)) // 이전에 이동한 위치일 시
                    continue;
                movePos.setValue(look.x, look.y);
                return;
            }
        }
    }

    private void setNewBranchBlock(Define.BranchBlock branchBlock){
        int x_sub = playerPos.x-prevPos.x;
        int y_sub = playerPos.y-prevPos.y;

        if(x_sub == 1){
            // left
            branchBlock.left.distance = accumulateDistance;
            branchBlock.left.exist = true;
            branchBlock.left.linkedBranch = prevBranchBlock;
        }
        if(x_sub == -1){
            // right
            branchBlock.right.distance = accumulateDistance;
            branchBlock.right.exist = true;
            branchBlock.right.linkedBranch = prevBranchBlock;
        }
        if(y_sub == -1){
            // down
            branchBlock.down.distance = accumulateDistance;
            branchBlock.down.exist = true;
            branchBlock.down.linkedBranch = prevBranchBlock;
        }
        if(y_sub == 1){
            // up
            branchBlock.up.distance = accumulateDistance;
            branchBlock.up.exist = true;
            branchBlock.up.linkedBranch = prevBranchBlock;
        }
        if(prevBranchDirection == Route.Direction.UP){
            prevBranchBlock.up.distance = accumulateDistance;
            prevBranchBlock.up.exist = true;
            prevBranchBlock.up.linkedBranch = branchBlock;
        }
        if(prevBranchDirection == Route.Direction.DOWN){
            prevBranchBlock.down.distance = accumulateDistance;
            prevBranchBlock.down.exist = true;
            prevBranchBlock.down.linkedBranch = branchBlock;
        }
        if(prevBranchDirection == Route.Direction.RIGHT){
            prevBranchBlock.right.distance = accumulateDistance;
            prevBranchBlock.right.exist = true;
            prevBranchBlock.right.linkedBranch = branchBlock;
        }
        if(prevBranchDirection == Route.Direction.LEFT){
            prevBranchBlock.left.distance = accumulateDistance;
            prevBranchBlock.left.exist = true;
            prevBranchBlock.left.linkedBranch = branchBlock;
        }
        for (Define.Pos p : Define.moveBoundary) {
            look.setValue(this.playerPos.x, this.playerPos.y);
            look.x += p.x;
            look.y += p.y;
            calcIndex(look);
            if (this.model.our.get(look.y).get(look.x).type == Define.AIR) {
                if(p.x == 1) // right
                    branchBlock.right.exist = true;
                if(p.x == -1) // left
                    branchBlock.left.exist = true;
                if(p.y == 1) // down
                    branchBlock.down.exist = true;
                if(p.y == -1) // up
                    branchBlock.up.exist = true;
            }
        }
    }

    Define.BranchBlock makeBranchBlock(int x,int y){
        System.out.println("Make Branch Block");
        Define.BranchBlock newBranchBlock = new Define.BranchBlock(playerPos.x, playerPos.y);
        setNewBranchBlock(newBranchBlock); // make Graph
        Define.branchBlockHashMap.put(newBranchBlock.hashCode(),newBranchBlock);
        prevBranchBlock = newBranchBlock;
        accumulateDistance = 0;
        return newBranchBlock;
    }

    public void Move(){
        if(!isEnergy()){
            // GAME OVER
            // FILE WRITE
            // EXIT
            System.out.println("!isEnergy");
            System.exit(0);
        }
        // GAME OVER : 우선순위 계산 할 Branch가 미존재 할 시 (우선순위에서 계산해야 할 듯)
        if(isFinish()){
            // Game Clear
            // FILE WRITE
            // Exit
            System.out.println("isFinish");
            System.exit(0);
        }
        decreaseEnergy();
        increaseMana();
        accumulateDistance++;

        moveAround(); // Branch Block이 아니라는 가정 필수
        this.prevPos.setValue(this.playerPos.x,this.playerPos.y);
        this.model.our.get(this.prevPos.y).get(this.prevPos.x).type = Define.AIR;
        this.playerPos.setValue(movePos.x, movePos.y);
        this.model.our.get(this.playerPos.y).get(this.playerPos.x).type = Define.PLAYER;
        lookAround();

        if(isBranchBlock()){
            Define.BranchBlock branchBlock = null;
            if(!Define.branchBlockHashMap.containsKey(Define.HashCode(this.playerPos.x,this.playerPos.y)))
                branchBlock = makeBranchBlock(this.playerPos.x,this.playerPos.y);
            // Branch 우선 순위 계산 및 경로로 이동
            if(branchBlock != null){
                Route route = new Route(Define.branchBlockHashMap.get(Define.HashCode(1,0)), playerPos);
                route.SetList();
                ArrayList<Define.DestInfo> destInfos = route.Dijkstra(branchBlock);
                // 어느 방향으로 이동했는지에 대해서도 저장을 해야한다,

                Priority.BranchPriority priority = new Priority.BranchPriority(model, destInfos);

                Define.Pos dest = priority.HighestPriorityBranch();

                System.out.print("test");
            }
        }
        if(isMana()){
            // 스캔 우선 순위 계산
            mana = 0.f;
            //if(Define.branchBlockHashMap.get(Define.HashCode(playerPos.x,playerPos.y)) == null)
                //makeBranchBlock(this.playerPos.x,this.playerPos.y); // 스캔 써도 BranchBlock으로 만들기.
            // useScan()
            // 우선순위 탐색 및 경로 설정.
        }
    }

    public boolean useScan(Define.Pos pos){
        for(Define.Pos p : Define.sacnBoundary){
            Define.Pos look = new Define.Pos(pos.x, pos.y);
            look.x += p.x;
            look.y += p.y;
            calcIndex(look);
            if(this.model.our.get(look.y).get(look.x).type != Define.UNKNOWN) // 이미 알고있으면 계산x
                continue;
            if(this.model.groundTruth.get(look.y).get(look.x).type == Define.WALL) // 벽 표시
                this.model.scan.add(new Define.ScanBlcok(Define.WALL, look));
            if(this.model.groundTruth.get(look.y).get(look.x).type == Define.AIR) // 길 표시
                this.model.scan.add(new Define.ScanBlcok(Define.AIR, look));

            if(look.x == 0 || look.x == this.model.getCol() - 1 || look.y == 0 || look.y == this.model.getRow() -1)
                if(!(look.x == 1 && look.y == 0))  // 출발점을 제외한 벽의 양 끝에 길이 있으면 목적지
                    if(this.model.groundTruth.get(look.y).get(look.x).type == Define.AIR)
                        this.model.scan.add(new Define.ScanBlcok(Define.GOAL, look));
        }
        return true;
    }
    public boolean useBreak(Define.Pos pos){
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
        for(Define.Pos p : Define.boundary) { // lookAround 재계산을 위해서 주변 AIR를 제외한 것들을 UNKNOWN으로 변경
            Define.Pos look = new Define.Pos(this.playerPos.x, this.playerPos.y);
            look.x += p.x;
            look.y += p.y;
            calcIndex(look);
            if(this.model.our.get(look.y).get(look.x).type != Define.AIR)
                this.model.our.get(look.y).get(look.x).type = Define.UNKNOWN;
        }
        lookAround();
        this.breakItem = false;
        breakPos = pos;
        return true;
    }

}
