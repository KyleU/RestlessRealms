package restless.realms.server.mail;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.Range;

import restless.realms.server.database.FieldLengths;
import restless.realms.server.util.HasIntegerId;

@Entity
@NamedQueries({
    @NamedQuery(name="mailMessage.getUnreadCount", query="select count(m) from MailMessage m where m.toName = ? and m.deleted = false and m.readTimestamp is null", cacheable=true),
    @NamedQuery(name="mailMessage.getByFrom", query="select m from MailMessage m where m.fromName = ? order by sentTimestamp desc", cacheable=true),
    @NamedQuery(name="mailMessage.getByTo", query="select m from MailMessage m where m.toName = ? and m.deleted = false order by sentTimestamp desc", cacheable=true),
})
public class MailMessage implements HasIntegerId {
    private Integer id;

    private String fromName;
    private String toName;
    
    private Date sentTimestamp;
    private Date readTimestamp;
    private boolean deleted;
    
    private String content;
    
    private boolean attachmentsRetrieved;

    private Integer attachment1;
    private Integer attachment2;
    private Integer attachment3;
    private Integer attachment4;
    private Integer attachment5;
    
    private int currency;
    private int tokens;
    private int xp;

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

    @Length(max=FieldLengths.PLAYER_NAME)
    @Index(name="MailMessage_fromName")
    public String getFromName() {
        return fromName;
    }
    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    @Length(max=FieldLengths.PLAYER_NAME)
    @Index(name="MailMessage_toName")
    public String getToName() {
        return toName;
    }
    public void setToName(String toName) {
        this.toName = toName;
    }
    
    @Temporal(TemporalType.TIMESTAMP)
    public Date getSentTimestamp() {
        return sentTimestamp;
    }
    public void setSentTimestamp(Date sentTimestamp) {
        this.sentTimestamp = sentTimestamp;
    }
    
    @Temporal(TemporalType.TIMESTAMP)
    public Date getReadTimestamp() {
        return readTimestamp;
    }
    public void setReadTimestamp(Date readTimestamp) {
        this.readTimestamp = readTimestamp;
    }

    @Basic
    public boolean isDeleted() {
        return deleted;
    }
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
    
    @Length(max=FieldLengths.MAIL_CONTENT)
    @NotEmpty
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    
    @Basic
    public boolean isAttachmentsRetrieved() {
        return attachmentsRetrieved;
    }
    public void setAttachmentsRetrieved(boolean attachmentsRetrieved) {
        this.attachmentsRetrieved = attachmentsRetrieved;
    }

    @Basic
    public Integer getAttachment1() {
        return attachment1;
    }
    public void setAttachment1(Integer attachment1) {
        this.attachment1 = attachment1;
    }

    @Basic
    public Integer getAttachment2() {
        return attachment2;
    }
    public void setAttachment2(Integer attachment2) {
        this.attachment2 = attachment2;
    }
    
    @Basic
    public Integer getAttachment3() {
        return attachment3;
    }
    public void setAttachment3(Integer attachment3) {
        this.attachment3 = attachment3;
    }
    
    @Basic
    public Integer getAttachment4() {
        return attachment4;
    }
    public void setAttachment4(Integer attachment4) {
        this.attachment4 = attachment4;
    }
    
    @Basic
    public Integer getAttachment5() {
        return attachment5;
    }
    public void setAttachment5(Integer attachment5) {
        this.attachment5 = attachment5;
    }
    
    @Range(min=0)
    public int getCurrency() {
        return currency;
    }
    public void setCurrency(int currency) {
        this.currency = currency;
    }

    @Range(min=0)
    public int getTokens() {
        return tokens;
    }
    public void setTokens(int tokens) {
        this.tokens = tokens;
    }
    
    @Range(min=0)
    public int getXp() {
        return xp;
    }
    public void setXp(int xp) {
        this.xp = xp;
    }
}