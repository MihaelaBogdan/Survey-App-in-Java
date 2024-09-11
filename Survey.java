import java.io.Serializable;
import java.util.List;

public class Survey implements Serializable {
    private String title;
    private String question;
    private List<String> options;
    private String creatorId;

    public Survey(String title, String question, List<String> options) {
        this.title = title;
        this.question = question;
        this.options = options;
    }

    public String getTitle() {
        return title;
    }

    public String getQuestion() {
        return question;
    }

    public List<String> getOptions() {
        return options;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }
}
