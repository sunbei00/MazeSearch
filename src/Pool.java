import java.util.AbstractMap;
import java.util.Stack;

public class Pool {

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
