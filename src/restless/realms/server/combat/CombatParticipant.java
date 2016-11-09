package restless.realms.server.combat;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.hibernate.validator.Length;

import restless.realms.server.database.FieldLengths;

import com.sun.istack.internal.NotNull;

@Embeddable
public class CombatParticipant {
    public static enum Role {ALLY, ENEMY};
    public static enum Type {PLAYER, MOB};

    private Role role;
    private String name;
    private Type type;
    
    public CombatParticipant() {
    }

    public CombatParticipant(Role role, String name, Type type) {
        super();
        this.role = role;
        this.name = name;
        this.type = type;
    }
    
    @Enumerated(EnumType.ORDINAL)
    public Role getRole() {
        return role;
    }
    public void setRole(Role role) {
        this.role = role;
    }

    @NotNull
    @Length(max=FieldLengths.PLAYER_NAME)
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @Enumerated(EnumType.ORDINAL)
    public Type getType() {
        return type;
    }
    public void setType(Type type) {
        this.type = type;
    }
}
