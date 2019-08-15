package atlow.chemi.mymada;

public class DictionaryWord {
    public String name;
    public String definition;
    public String key;
    public boolean approved;

    public DictionaryWord(String name, String definition, String key, boolean approved)  {
        this.name = name;
        this.definition = definition;
        this.key = key;
        this.approved = approved;
    }
    public DictionaryWord(String name, String definition, boolean approved)  {
        this.name = name;
        this.definition = definition;
        this.approved = approved;
    }
    public String getName() {
        return name;
    }
    void setName(String name) {
        this.name = name;
    }
    public String getDefinition() {
        return definition;
    }
    void setDefinition(String definition) {
        this.definition = definition;
    }
    public String getKey() {
        return key;
    }
    void setKey(String key) {
        this.key = key;
    }
    public boolean isApproved() {
        return approved;
    }
    void setApproved(boolean approved) {
        this.approved = approved;
    }
}