package restless.realms.server.tip;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.NamedQueries;
import org.hibernate.validator.Length;
import org.hibernate.validator.Range;

import restless.realms.server.util.HasIntegerId;
import restless.realms.server.util.ScaleOptions;

@Entity
@NamedQueries({
})
public class Tip implements HasIntegerId {
    private Integer id;
    
    private String content;
    private int minLevel;
    private int maxLevel;
    
	public Tip() {
    }
	
    public Tip(Integer id, String content, int minLevel, int maxLevel) {
        super();
        this.id = id;
        this.content = content;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
    }

    @Id 
    @Override
    public Integer getId() {
        return id;
    }
    @Override
    public void setId(Integer id) {
        this.id = id;
    }
    
    @Length(max=1000) 
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    
    @Range(min=0, max=ScaleOptions.MAX_LEVEL)
    public int getMinLevel() {
        return minLevel;
    }
    public void setMinLevel(int minLevel) {
        this.minLevel = minLevel;
    }

    @Range(min=0, max=ScaleOptions.MAX_LEVEL)
    public int getMaxLevel() {
        return maxLevel;
    }
    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }
}
