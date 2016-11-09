package restless.realms.server.playercharacter.party;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;

import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;

import restless.realms.server.database.FieldLengths;
import restless.realms.server.util.HasIntegerId;

@Entity
public class Party implements HasIntegerId {
    private Integer id;
    private String leader;
    
    private List<String> members;

    @Id 
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Override
    public Integer getId() {
        return id;
    }
    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    @NotEmpty
    @Length(max=FieldLengths.PLAYER_NAME)
    public String getLeader() {
        return leader;
    }
    public void setLeader(String leader) {
        this.leader = leader;
    }

    @CollectionOfElements(fetch=FetchType.EAGER)
    @JoinTable(
        joinColumns = @JoinColumn(name="partyId")
    )
    @IndexColumn(name="orderIndex")
    public List<String> getMembers() {
        return members;
    }
    public void setMembers(List<String> members) {
        this.members = members;
    }
}
