# irbm25
## 使用BM25算法计算文档相似性

bm25 是一种用来评价搜索词和文档之间相关性的算法，是一种基于概率模型提出的算法，

简单描述：我们有一个query和一批文档Docs，现在要计算query和每篇文档D之间的相关性分数,

公式为：

![Image text](https://github.com/JunfengDuan/irbm25/blob/master/image/bm25formula.jpg)

## 针对中文改进

基于字符的分词效果更好

调节词频的k1设置的小一点效果好

公式微小调整


