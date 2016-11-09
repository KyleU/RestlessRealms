package restless.realms.server.quest;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.Range;

import restless.realms.server.database.FieldLengths;
import restless.realms.server.util.HasStringId;
import restless.realms.server.util.ScaleOptions;

@Entity
@NamedQueries({
    @NamedQuery(name="quest.getAll", query="select q from Quest q order by suggestedLevel, name")
})
public class Quest implements HasStringId {
    private String id;
    private String name;
    
    private int suggestedLevel;
    private int qualificationLevel;
    private Integer qualificationItem;
    private String qualificationProfession;
    private boolean repeatable;
    
    private String completionAdventure;
    private Integer completionItem;
    private String completionMobArchetype;
    private int completionQuantity;
    
    private int rewardXp;
    private Integer rewardSkill;
    
    private String progressText;
    private String introText;
    private String completionText;
    
    public Quest() {
    }
    
    public Quest(
            String id, String name, int suggestedLevel, 
            int qualificationLevel, Integer qualificationItem, String qualificationProfession, boolean repeatable,
            String completionAdventure, Integer completionItem, String completionMobArchetype, int completionQuantity, 
            int rewardXp, Integer rewardSkill,
            String progressText, String introText, String completionText) {
        super();
        this.id = id;
        this.name = name;
        this.suggestedLevel = suggestedLevel;
        this.qualificationLevel = qualificationLevel;
        this.qualificationItem = qualificationItem;
        this.qualificationProfession = qualificationProfession;
        this.repeatable = repeatable;
        this.completionAdventure = completionAdventure;
        this.completionItem = completionItem;
        this.completionMobArchetype = completionMobArchetype;
        this.completionQuantity = completionQuantity;
        this.rewardXp = rewardXp;
        this.progressText = progressText;
        this.introText = introText;
        this.completionText = completionText;
    }

    @Id
    @Length(max=FieldLengths.STRING_ID)
    @Override
    public String getId() {
        return id;
    }
    @Override
    public void setId(String id) {
        this.id = id;
    }

    @NotEmpty
    @Length(max=FieldLengths.QUEST_NAME)
    @Column(unique=true)
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @Range(min=1, max=ScaleOptions.MAX_LEVEL)
    public int getSuggestedLevel() {
        return suggestedLevel;
    }
    public void setSuggestedLevel(int suggestedLevel) {
        this.suggestedLevel = suggestedLevel;
    }

    @Range(min=0, max=ScaleOptions.MAX_LEVEL)
    public int getQualificationLevel() {
        return qualificationLevel;
    }
    public void setQualificationLevel(int qualificationLevel) {
        this.qualificationLevel = qualificationLevel;
    }

    @Basic
    public Integer getQualificationItem() {
        return qualificationItem;
    }
    public void setQualificationItem(Integer qualificationItem) {
        this.qualificationItem = qualificationItem;
    }

    @Length(max=FieldLengths.STRING_ID)
    public String getQualificationProfession() {
        return qualificationProfession;
    }
    public void setQualificationProfession(String qualificationProfession) {
        this.qualificationProfession = qualificationProfession;
    }

    @Basic
    public boolean isRepeatable() {
        return repeatable;
    }
    public void setRepeatable(boolean repeatable) {
        this.repeatable = repeatable;
    }

    @Basic
    public String getCompletionAdventure() {
        return completionAdventure;
    }
    public void setCompletionAdventure(String completionAdventure) {
        this.completionAdventure = completionAdventure;
    }
    
    @Basic
    public Integer getCompletionItem() {
        return completionItem;
    }
    public void setCompletionItem(Integer completionItem) {
        this.completionItem = completionItem;
    }

    @Length(max=FieldLengths.STRING_ID)
    public String getCompletionMobArchetype() {
        return completionMobArchetype;
    }
    public void setCompletionMobArchetype(String completionMobArchetype) {
        this.completionMobArchetype = completionMobArchetype;
    }

    @Range(min=1, max=100)
    public int getCompletionQuantity() {
        return completionQuantity;
    }
    public void setCompletionQuantity(int completionQuantity) {
        this.completionQuantity = completionQuantity;
    }

    @Range(min=0, max=1000000)
    public int getRewardXp() {
        return rewardXp;
    }
    public void setRewardXp(int rewardXp) {
        this.rewardXp = rewardXp;
    }

    @Basic
    public Integer getRewardSkill() {
        return rewardSkill;
    }
    public void setRewardSkill(Integer rewardSkill) {
        this.rewardSkill = rewardSkill;
    }

    @NotEmpty
    @Length(max=FieldLengths.QUEST_PROGRESS)
    public String getProgressText() {
        return progressText;
    }
    public void setProgressText(String progressText) {
        this.progressText = progressText;
    }
    
    @NotEmpty
    @Length(max=FieldLengths.QUEST_TEXT)
    public String getIntroText() {
        return introText;
    }
    public void setIntroText(String introText) {
        this.introText = introText;
    }

    @NotEmpty
    @Length(max=FieldLengths.QUEST_TEXT)
    public String getCompletionText() {
        return completionText;
    }
    public void setCompletionText(String completionText) {
        this.completionText = completionText;
    }
}
