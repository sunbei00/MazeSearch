import java.util.AbstractMap;
import java.util.Stack;

public class Pool {
    // 메모리를 효율적으로 사용하기 위한 Pool 클래스


    // BranchBlockGraph 클래스에서 이전 위치와 현재 위치를 저장하기 위해 Pair Pool 사용
    public static class PosPairPool {
        private Stack<AbstractMap.SimpleEntry<Pos, Pos>> stack = new Stack<>();

        public AbstractMap.SimpleEntry<Pos, Pos> get(){
            if(stack.size() != 0)
                return stack.pop();
            return new AbstractMap.SimpleEntry<>(new Pos(0,0), new Pos(0,0));
        }
        public void push(AbstractMap.SimpleEntry<Pos, Pos> push){
            stack.push(push);
            push = null;
        }
    }

    // Pos 객체가 자주 사용되기 때문에 메모리 최적화를 위한 클래스
    public static class PosPool {

        private Stack<Pos> stack = new Stack<>();

        public Pos get(){
            if(stack.size() != 0)
                return stack.pop();
            return new Pos(0,0);
        }
        public void push(Pos push){
            stack.push(push);
            push = null;
        }
    }

    // BranchBlockGraph 클래스에서 BranchBlock 간의 연결을 위해 사용되는 메모리 최적화를 위한 클래스
    public static class LinkPool {
        static class Link{
            public BranchBlock prevBranchBlock;
            public Define.Direction prevBranchDirection;
            public Link(BranchBlock prevBranchBlock, Define.Direction prevBranchDirection){
                this.prevBranchBlock = prevBranchBlock;
                this.prevBranchDirection = prevBranchDirection;
            }
            public void setValue(BranchBlock prevBranchBlock, Define.Direction prevBranchDirection){
                this.prevBranchBlock = prevBranchBlock;
                this.prevBranchDirection = prevBranchDirection;
            }
        }
        private Stack<Link> stack = new Stack<>();

        public Link get(){
            if(stack.size() != 0)
                return stack.pop();
            return new Link(null, Define.Direction.UNKNOWN);
        }
        public void push(Link push){
            stack.push(push);
            push = null;
        }
    }

}
