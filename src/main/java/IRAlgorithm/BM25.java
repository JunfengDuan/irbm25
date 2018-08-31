package IRAlgorithm;

import com.huaban.analysis.jieba.JiebaSegmenter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * tf-idf中，这个信息直接就用“词频”，如果出现的次数比较多，一般就认为更相关。
 * 但是BM25洞察到：词频和相关性之间的关系是非线性的，具体来说，每一个词对于文档相关性的分数不会超过一个特定的阈值，
 * 当词出现的次数达到一个阈值后，其影响不再线性增长，而这个阈值会跟文档本身有关。
 */
public class BM25 {

    private final JiebaSegmenter segmenter = new JiebaSegmenter();

    /**
     * All terms In Corpus.
     */
    private static List<String> corpusTerms = new ArrayList<>();

    /**
     * Corpus consist of documents.
     */
    private static List<List<String>> documentList = new ArrayList<>();

    /**
     * Storage question.
     */
    private static Map<List<String>, String> corpusHashMap = new HashMap<>();


    /**
     * Free parameter, usually chosen as k1 = 1.2. range: 1.2 ~ 2.0
     * 用于对文档中的词项频率进行缩放控制：如果 k1 取 0，则相当于不考虑词频，如果 k1 取较大的值，那么对应于使用原始词项频率
     */
    private double k1;

    /**
     * Free parameter, usually chosen as b = 0.75. range: 0 ~ 1.0
     * 决定文档长度的缩放程度：b = 1 表示基于文档长度对词项权重进行完全的缩放，b = 0 表示归一化时不考虑文档长度因素
     */
    private double b;

    /**
     * Default constructor with k1 = 1.2, b = 0.75.
     */
    public BM25(){
        this(1.2, 0.75);
    }

    /**
     * Constructor 主要負責將 Data 載入以方便後續計算.
     *
     * @param k1 is a positive tuning parameter that calibrates
     * the document term frequency scaling. A k1 value of 0 corresponds to a
     * binary model (no term frequency), and a large value corresponds to using
     * raw term frequency.
     * @param b b is another tuning parameter which determines
     * the scaling by document length: b = 1 corresponds to fully scaling the
     * term weight by the document length, while b = 0 corresponds to no length
     * normalization.
     */
    public BM25(double k1, double b){
        if (k1 < 0) {
            throw new IllegalArgumentException("Negative k1 = " + k1);
        }
        if (b < 0 || b > 1) {
            throw new IllegalArgumentException("Invalid b = " + b);
        }
        this.k1 = k1;
        this.b = b;
    }

    /**
     * 使用 BM25 的方法计算文档词频
     * @param tfDocument list of strings
     * @param term String represents a term
     * @return term frequency of term in document
     */
    private double tf(List<String> tfDocument, String term) {
        // 句子中一个 term 的出現頻率
        double count = 0;
        //平均每个文档的 term 數量
        double avgDocSize = (corpusTerms.size() / documentList.size());
        for (String word : tfDocument) {
            if (term.equalsIgnoreCase(word)) {
                count++;
            }
        }
        double freq = count / tfDocument.size();
        return (freq * (k1 + 1)) / (freq + k1 * (1 - b + b * (tfDocument.size()) / avgDocSize));
    }

    /**
     * @param term a term in a question
     * @return the inverse term frequency of term in documents
     */
    private double idf(String term) {
        // 不包含term的文档数与包含term的文档数之比，0.5为平滑因子
        // count 为包含term的文档数
        double count = 0;
        for (List<String> idfDoc : documentList) {
            for (String word : idfDoc) {
                if (term.equalsIgnoreCase(word)) {
                    count++;
                    break;
                }
            }
        }
        return Math.log((documentList.size() + 0.5) / (count + 0.5));
    }

    /**
     * Rank the score
     * @param queryTermList terms of question
     * @return the TF-IDF of term
     */
    private Map<String, Double> score (List<String> queryTermList) {
        // 计算问题与每篇文档的相似值
        // docTerms 每个文档(句子)的 term list
        // term 句子中的一个词
        Map<String, Double> scoredDocument = new HashMap<>();
        for (List<String> docTerms : documentList) {
            double sumScore = 0.0;
            for (String queryTerm : queryTermList) {
                sumScore += tf(docTerms, queryTerm) * idf(queryTerm);
            }
            String docId = corpusHashMap.get(docTerms);
            scoredDocument.put(docId, sumScore);
        }
        return scoredDocument;
    }

    /** Documents similarity rank by bm25
     * @param query question
     * @param documents doc corpora
     * @param topNum return numbers
     */
    public Map<String, Double> rankBM25(String query, Map<String, String> documents, int topNum) {
        clear();
        List<String> segmentList = seg(query);
        for (Map.Entry docEntry : documents.entrySet()) {
            String id = (String) docEntry.getKey();
            String doc = (String) docEntry.getValue();
            List<String> segs = seg(doc);
            documentList.add(segs);
            corpusTerms.addAll(segs);
            corpusHashMap.put(segs, id);
        }
        // 存储文档id和对应的分数
        Map<String, Double> scoredDoc = score(segmentList);

        return getTopN(scoredDoc, topNum);
    }

    /**
     * 获取 n 个分值最高的doc
     * @param scoredDoc
     * @param topNum
     * @return
     */
    private Map<String,Double> getTopN(Map<String, Double> scoredDoc, int topNum) {
        PriorityQueue<DocEntry> maxHeap = new PriorityQueue<>(Collections.reverseOrder());
        for(Map.Entry doc: scoredDoc.entrySet()){
            String id = (String) doc.getKey();
            double score = Double.parseDouble(doc.getValue().toString());
            maxHeap.add(new DocEntry(id, score));
        }

        Map<String,Double> topNDoc = new LinkedHashMap<>();
        for(int i=0; i< Math.min(topNum, maxHeap.size()); i++){
            DocEntry entry = maxHeap.poll();
            topNDoc.put(entry.id, entry.score);
        }
        return topNDoc;
    }

    /**
     * 句子分词
     * @param sentence
     * @return
     */
    private List<String> seg(String sentence){
        List<String> segs = segmenter.sentenceProcess(sentence);
        List<String> words = segs.stream().filter(t -> t.trim().length()>1).map(w -> w.toLowerCase()).collect(Collectors.toList());
        return words;
    }

    /**
     * 清除缓存
     */
    private void clear(){
        documentList.clear();
        corpusTerms.clear();
        corpusHashMap.clear();
    }

}
