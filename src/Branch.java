import java.util.ArrayList;

public class Branch {
    private ArrayList<ArrayList<Define.Block>> map;
    private Define.Block location;
    private Define.Block goal;

    public Branch(Define.Block location, ArrayList<ArrayList<Define.Block>> map){
        this.map = map;
        this.location = location;
        this.goal = new Define.Block();
    }

    public Branch(Define.Block location, ArrayList<ArrayList<Define.Block>> map, Define.Block goal){
        this.map = map;
        this.location = location;
        this.goal = goal;
    }
    
    public Define.Block HighestPriorityBranch(ArrayList<ArrayList<Define.Block>> map) {
        Define.Block selectBranch = null;

        for(ArrayList<Define.Block> row : map) {
            for(Define.Block block : row) {
                if(block.type == Define.BRANCH) {
                    //Branch 우선순위 갱신
                    
                    //현재 분기점일 경우, 상하좌우 'Air(?)' 중 높은 우선순위로 갱신
                        //Block의 위치가 index 로 되어 있음 => (row, column) 일 경우 용이
                    if(block == location){

                    }
                    
                    //이전 분기점일 경우
                    else{
                        block.priority = calculate_priority(block, goal);
                    }




                    //높은 우선순위 Branch 선택
                    if(selectBranch == null || block.priority > selectBranch.priority) {
                        selectBranch = block;
                    }
                }
            }
        }
        
        // 현재 브랜치로 계속 이동하는 경우
        if(selectBranch == location){
            //상하좌우에 block 중, 높은 우선순위 return
        }

        // 이전 브랜치로 이동하는 경우
        return selectBranch;
    }

    private int calculate_priority(Define.Block block, Define.Block goal) {
        //도착지를 못 찾았을 때
        if(goal.type == '2'){

        }

        //도착지를 찾았을 때
        else{

        }

        return 0;
    }
}
