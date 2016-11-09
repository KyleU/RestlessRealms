package restless.realms.server.chat;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;

import restless.realms.server.database.FieldLengths;
import restless.realms.server.util.HasIntegerId;

@Entity
@NamedQueries({
    @NamedQuery(name="chatMessage.get", query="select c from ChatMessage c where c.id > ? and occurred > ? order by c.id desc", cacheable=true)
})
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@SuppressWarnings("serial")
public class ChatMessage implements Serializable, HasIntegerId {
	private Integer id;
	
	private String channel;
	private String playerName;
	private String content;
	private Date occurred;

	public ChatMessage() {
    }
	
	public ChatMessage(Integer id, String channel, String playerName, String content, Date occurred) {
        this.id = id;
        this.channel = channel;
        this.playerName = playerName;
        this.content = content;
        this.occurred = occurred;
    }
	
    @Id 
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Override
	public Integer getId() {
		return id;	}
	@Override
    public void setId(Integer id) {
		this.id = id;
	}
	
    @NotEmpty
    @Length(max=FieldLengths.STRING_ID)
	public String getChannel() {
		return channel;
	}
	public void setChannel(String channel) {
		this.channel = channel;
	}
	
    @NotEmpty
    @Length(max=FieldLengths.PLAYER_NAME)
	public String getPlayerName() {
		return playerName;
	}
	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}
	
    @NotEmpty
    @Length(max=FieldLengths.CHAT_CONTENT)
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
    @Temporal(TemporalType.TIMESTAMP) 
	public Date getOccurred() {
		return occurred;
	}
	public void setOccurred(Date occurred) {
		this.occurred = occurred;
	}
}