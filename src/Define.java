public class Define {
    static final int AIR = '0';
    static final int WALL = '1';
    static final int UNKNOWN = '2';
    static final int BRANCH = '3';
    static final int PLAYER = '4';
    static final int GOING = '5';

    enum ImgOutput{
        GroundTruth,
        Our
    }

    static class Block{

        public int type;
        public int priority; // using in branch

        public Block(){
            this.type = UNKNOWN;
            this.priority = -1;
        }
        public Block(int type){
            this.type = type;
            this.priority = -1;
        }
        public Block(int type, int priority){
            this.type = type;
            this.priority = priority;
        }
    }
}
