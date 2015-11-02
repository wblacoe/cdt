package corpus.dep.converter;

/**
 *
 * @author wblacoe
 */
public class WordCountPair {

    private String word;
    private Long count;
    
    public WordCountPair(String word){
        this.word = word;
        count = -1L;
    }
    public WordCountPair(String word, Long count){
        this.word = word;
        this.count = count;
    }

    public String getWord() {
        return word;
    }

    public Long getCount() {
        return count;
    }
    
    @Override
    public int hashCode(){
        return word.hashCode();
    }
    
    @Override
    public boolean equals(Object o){
        return ((WordCountPair) o).equals(word);
    }
    
}
