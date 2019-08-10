package atlow.chemi.mymada;

class DictionaryWord {
    public String name;
    public String definition;
    public String key;
    public boolean approved;

    DictionaryWord(String name, String definition, String key, boolean approved)  {
        this.name = name;
        this.definition = definition;
        this.key = key;
        this.approved = approved;
    }
    DictionaryWord(String name, String definition, boolean approved)  {
        this.name = name;
        this.definition = definition;
        this.approved = approved;
    }
    String getName() {
        return name;
    }
    void setName(String name) {
        this.name = name;
    }
    String getDefinition() {
        return definition;
    }
    void setDefinition(String definition) {
        this.definition = definition;
    }
    String getKey() {
        return key;
    }
    void setKey(String key) {
        this.key = key;
    }
    boolean isApproved() {
        return approved;
    }
    void setApproved(boolean approved) {
        this.approved = approved;
    }
}