public class Block {
    public int type;
    public boolean finish = false;

    public Block(){
        this.type = Define.UNKNOWN;
    }
    public Block(int type){
        this.type = type;
    }
    public void setValue(int type, boolean finish){
        this.type = type;
        this.finish = finish;
    }
}
