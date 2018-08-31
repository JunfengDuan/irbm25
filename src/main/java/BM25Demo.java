import IRAlgorithm.BM25;

import java.util.HashMap;
import java.util.Map;

public class BM25Demo {

    public static void main(String[] args){
        BM25 bm25 = new BM25(1.2, 0.95);
        String query = "";// 问句
        Map<String, String> corpora = new HashMap<>();//文档语料(id:doc)
        Map<String, Double> rankBM25 = bm25.rankBM25(query, corpora, 10);
    }
}
