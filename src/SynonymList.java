import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jmuia on 2016-01-21.
 */
public class SynonymList {
    private static SynonymList instance = new SynonymList();
    private ConcurrentHashMap<String, HashSet<String>> synonyms;

    private SynonymList() {
        this.synonyms = new ConcurrentHashMap<>();
    }

    public static synchronized void addSynonym(String a, String b) {
        HashSet<String> aSynonyms = instance.synonyms.getOrDefault(a, new HashSet<>());
        aSynonyms.add(b);

        HashSet<String> bSynonyms = instance.synonyms.getOrDefault(b, new HashSet<>());
        bSynonyms.add(a);

        instance.synonyms.put(a, aSynonyms);
        instance.synonyms.put(b, bSynonyms);
    }

    public static synchronized boolean exists(String word) {
        return instance.synonyms.containsKey(word);
    }

    public static synchronized void removeSynonym(String word) {
        HashSet<String> wordSynonyms = instance.synonyms.getOrDefault(word, new HashSet<>());
        instance.synonyms.remove(word);

        for (String s: wordSynonyms) {
            HashSet<String> sSynonyms = instance.synonyms.getOrDefault(s, new HashSet<>());
            sSynonyms.remove(word);
            instance.synonyms.put(s, sSynonyms);
        }
    }

    public static synchronized HashSet<String> getSynonyms(String word) { return _getSynonyms(word, new HashSet<>()); }

    private static synchronized HashSet<String> _getSynonyms(String word, HashSet<String> synonyms) {
        synonyms.add(word);

        HashSet<String> wordSynonyms = instance.synonyms.getOrDefault(word, new HashSet<>());
        HashSet<String> remaining = new HashSet<>(wordSynonyms);
        remaining.removeAll(synonyms);

        for (String s: remaining) {
            HashSet<String> tmp = _getSynonyms(s, new HashSet<>(synonyms));
            synonyms.addAll(tmp);
        }
        return synonyms;
    }
}