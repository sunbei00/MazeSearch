import java.util.ArrayList;
import java.util.HashMap;

public class BranchBlockGraph {

    Model model = null;
    private ArrayList<ArrayList<Block>> graphMap = new ArrayList<ArrayList<Block>>();
    private HashMap<Integer, BranchBlock> branchBlockHashMap = new HashMap<Integer, BranchBlock>();
    private static Pos look = new Pos(); // tmp for optimizing Memory

    private void calcIndex(Pos pos){ // index error 방지
        if(pos.x < 0)
            pos.x = 0;
        if(pos.x >= this.model.getCol())
            pos.x = this.model.getCol() - 1;
        if(pos.y < 0)
            pos.y = 0;
        if(pos.y >= this.model.getRow())
            pos.y = this.model.getRow() - 1;
    }

    public BranchBlockGraph(Model model){
        this.model = model;
        int row = model.getRow();
        int col = model.getCol();
        for(int i=0; i<row; i++)
            graphMap.add(new ArrayList<>());

        for(int y=0; y<row; y++)
            for(int x=0; x<col; x++)
                graphMap.get(y).add(new Block(graphMap.get(y).get(x).type));
    }

    private void ClearGraphMap(){
        int row = model.getRow();
        int col = model.getCol();
        for(int y=0; y<row; y++)
            for(int x=0; x<col; x++)
                graphMap.get(y).get(x).setValue(Define.WALL, false);
    }

    public void clear(){
        ClearGraphMap();
        branchBlockHashMap.clear();
    }
    public void checkBranchBlcok(){

    }

    private void moveAround() {
        Pos current = new Pos(1,0);

        for (Pos p : Define.moveBoundary) {
            look.setValue(current.x, current.y);
            look.x += p.x;
            look.y += p.y;
            calcIndex(look);
            if (this.model.our.get(look.y).get(look.x).type == Define.AIR) {
                if (look.isEquals(current)) // 이전에 이동한 위치일 시
                    continue;
                break;
            }
        }
    }

}
