package restless.realms.server.achievement;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.Range;

import restless.realms.server.database.FieldLengths;
import restless.realms.server.item.IconInfo;
import restless.realms.server.util.HasStringId;

@Entity
@NamedQueries({
    @NamedQuery(name="achievement.getAll", query="select a from Achievement a order by orderIndex")
})
public class Achievement implements HasStringId {
    private String id;
    private String name;
    private String description;
    private int pointValue;
    
    private String completionKey;
    private int completionQuantity;
    
    private IconInfo icon;
    private int orderIndex;
    
    public Achievement() {
    }
    
    public Achievement(String id, String name, String description, int pointValue, String completionKey, int completionQuantity, IconInfo icon, int orderIndex) {
        super();
        this.id = id;
        this.name = name;
        this.description = description;
        this.pointValue = pointValue;
        this.completionKey = completionKey;
        this.completionQuantity = completionQuantity;
        this.icon = icon;
        this.orderIndex = orderIndex;
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
    @Length(max=FieldLengths.ACHIEVEMENT_NAME)
    @Column(unique=true)
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @NotEmpty
    @Length(max=FieldLengths.ACHIEVEMENT_DESCRIPTION)
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    @Range(min=0)
    public int getPointValue() {
        return pointValue;
    }
    public void setPointValue(int pointValue) {
        this.pointValue = pointValue;
    }

    @NotEmpty
    @Length(max=FieldLengths.STATISTIC_KEY)
    public String getCompletionKey() {
        return completionKey;
    }
    public void setCompletionKey(String completionKey) {
        this.completionKey = completionKey;
    }

    @Range(min=0)
    public int getCompletionQuantity() {
        return completionQuantity;
    }
    public void setCompletionQuantity(int completionQuantity) {
        this.completionQuantity = completionQuantity;
    }

    @Embedded
    public IconInfo getIcon() {
        return icon;
    }
    public void setIcon(IconInfo icon) {
        this.icon = icon;
    }
    
    @Range(min=0)
    public int getOrderIndex() {
        return orderIndex;
    }
    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }
}