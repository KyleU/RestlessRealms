package restless.realms.server.item;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Embeddable;

@Embeddable
@SuppressWarnings("serial")
public class IconInfo implements Serializable {
    private int x;
    private int y;
    
    public IconInfo() {
	}
    
    public IconInfo(int x, int y) {
		super();
		this.x = x;
		this.y = y;
	}

    @Basic
    public int getX() {
        return x;
    }
    public void setX(int x) {
        this.x = x;
    }

    @Basic
    public int getY() {
        return y;
    }
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + x;
        result = prime * result + y;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        IconInfo other = (IconInfo)obj;
        if(x != other.x)
            return false;
        if(y != other.y)
            return false;
        return true;
    }
}
