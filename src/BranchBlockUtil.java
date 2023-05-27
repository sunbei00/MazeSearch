public class BranchBlockUtil {

    private static Pos look = new Pos();

    public static void linkBranchBlock(BranchBlock branchBlock, BranchBlock prevBranchBlock, int distance, Pos playerPos, Pos prevPos, Define.Direction prevBranchDirection, Model model){
        Define.Direction direction = MapUtil.getDirection(playerPos,prevPos);
        if(direction == Define.Direction.LEFT){
            branchBlock.left.distance = distance;
            branchBlock.left.exist = true;
            branchBlock.left.linkedBranch = prevBranchBlock;
        }
        if(direction == Define.Direction.RIGHT){
            branchBlock.right.distance = distance;
            branchBlock.right.exist = true;
            branchBlock.right.linkedBranch = prevBranchBlock;
        }
        if(direction == Define.Direction.DOWN){
            branchBlock.down.distance = distance;
            branchBlock.down.exist = true;
            branchBlock.down.linkedBranch = prevBranchBlock;
        }
        if(direction == Define.Direction.UP){
            branchBlock.up.distance = distance;
            branchBlock.up.exist = true;
            branchBlock.up.linkedBranch = prevBranchBlock;
        }
        if(prevBranchDirection == Define.Direction.UP){
            prevBranchBlock.up.distance = distance;
            prevBranchBlock.up.exist = true;
            prevBranchBlock.up.linkedBranch = branchBlock;
        }
        if(prevBranchDirection == Define.Direction.DOWN){
            prevBranchBlock.down.distance = distance;
            prevBranchBlock.down.exist = true;
            prevBranchBlock.down.linkedBranch = branchBlock;
        }
        if(prevBranchDirection == Define.Direction.RIGHT){
            prevBranchBlock.right.distance = distance;
            prevBranchBlock.right.exist = true;
            prevBranchBlock.right.linkedBranch = branchBlock;
        }
        if(prevBranchDirection == Define.Direction.LEFT){
            prevBranchBlock.left.distance = distance;
            prevBranchBlock.left.exist = true;
            prevBranchBlock.left.linkedBranch = branchBlock;
        }
        for (Pos p : Define.moveBoundary) {
            look.setValue(playerPos.x, playerPos.y);
            look.x += p.x;
            look.y += p.y;
            Util.calcIndex(look,model);
            if (model.our.get(look.y).get(look.x).type == Define.AIR) {
                if(playerPos.x == look.x && playerPos.y == look.y) // 시작점 위 방향 처리.
                    continue;
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
}
