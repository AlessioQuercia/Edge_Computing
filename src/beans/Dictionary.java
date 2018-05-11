package beans;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Path;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType (XmlAccessType.FIELD)
public class Dictionary
{
    @XmlElement(name="dictionary")
    private List<Word> wordslist;
    private static Dictionary instance;

    public Dictionary()
    {
        wordslist = new ArrayList<Word>();
    }

    //singleton
    public synchronized static Dictionary getInstance()
    {
        if(instance==null)
            instance = new Dictionary();
        return instance;
    }

    public synchronized List<Word> getWordslist() {
        return new ArrayList<Word>(wordslist);
    }

    public void setWordslist(List<Word> wordslist) {
        this.wordslist = wordslist;
    }

    public synchronized void add(Word w){
        wordslist.add(w);
    }

    public synchronized void remove(Word w){
        wordslist.remove(w);
    }

    public Word getByWord(String word){

        List<Word> wordsCopy = getWordslist();

        for(Word w: wordsCopy)
            if(w.getWord().toLowerCase().equals(word.toLowerCase()))
                return w;
        return null;
    }
}
