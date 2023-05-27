public class Orientation {
    boolean exist;
    int distance;
    int priority;
    BranchBlock linkedBranch;
    public Orientation(boolean exist, int distance, BranchBlock linkedBranch){
        this.exist = exist;
        this.distance = distance;
        this.linkedBranch = linkedBranch;
        if(exist) {
            this.priority = 0;
        }
    }
}
