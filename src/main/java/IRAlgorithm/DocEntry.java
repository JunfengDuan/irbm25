package IRAlgorithm;

import java.util.Map;

public class DocEntry implements Comparable<DocEntry>{

    public String id;

    public double score;

    DocEntry(String id, double score){
        this.id = id;
        this.score = score;
    }

    @Override
    public int compareTo(DocEntry doc) {
        double diff = this.score - doc.score;
        if(diff > 0){
            return 1;
        }else if(diff < 0){
            return -1;
        }else{
            return 0;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

}
