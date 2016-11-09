package restless.realms.server.playercharacter.statistics;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.hibernate.validator.Range;

@Entity
@org.hibernate.annotations.Entity(dynamicUpdate=true)
@NamedQueries({
    @NamedQuery(name="statistic.getByPlayer", query="select s from Statistic s where s.key.playerName = ?"),
    @NamedQuery(name="statistic.getByKey", query="select s from Statistic s where s.key.statistic = ?"),
    @NamedQuery(name="statistic.getTotals", query="select s.key.statistic, sum(s.amount) from Statistic s group by s.key.statistic"),
    @NamedQuery(name="leaderboard.getByKills", query="select s.key.playerName as name, sum(s.amount) as value from Statistic s where s.key.statistic = 'deal-DEATH' group by s.key.playerName order by sum(s.amount) desc"),
    @NamedQuery(name="leaderboard.getByAdventuresCompleted", query="select s.key.playerName as name, sum(s.amount) as value from Statistic s where s.key.statistic = 'adventure-complete' group by s.key.playerName order by sum(s.amount) desc"),
    @NamedQuery(name="leaderboard.getByQuestsCompleted", query="select s.key.playerName as name, sum(s.amount) as value from Statistic s where s.key.statistic = 'quest-complete' group by s.key.playerName order by sum(s.amount) desc"),
    @NamedQuery(name="leaderboard.getByItemsSold", query="select s.key.playerName as name, sum(s.amount) as value from Statistic s where s.key.statistic = 'item-sell' group by s.key.playerName order by sum(s.amount) desc"),
    @NamedQuery(name="leaderboard.getByItemsBought", query="select s.key.playerName as name, sum(s.amount) as value from Statistic s where s.key.statistic = 'item-buy' group by s.key.playerName order by sum(s.amount) desc"),
    @NamedQuery(name="leaderboard.getByJuraks", query="select s.key.playerName as name, sum(s.amount) as value from Statistic s where s.key.statistic = 'kill-jurak' or s.key.statistic = 'kill-jurak2' group by s.key.playerName order by sum(s.amount) desc"),
    @NamedQuery(name="leaderboard.getByBattlemasters", query="select s.key.playerName as name, sum(s.amount) as value from Statistic s where s.key.statistic = 'kill-battlemaster' group by s.key.playerName order by sum(s.amount) desc"),
    @NamedQuery(name="leaderboard.getByPhysicalDamage", query="select s.key.playerName as name, sum(s.amount) as value from Statistic s where s.key.statistic = 'deal-PHYSICAL' group by s.key.playerName order by sum(s.amount) desc"),
    @NamedQuery(name="leaderboard.getByFireDamage", query="select s.key.playerName as name, sum(s.amount) as value from Statistic s where s.key.statistic = 'deal-FIRE' group by s.key.playerName order by sum(s.amount) desc"),
    @NamedQuery(name="leaderboard.getByElectricDamage", query="select s.key.playerName as name, sum(s.amount) as value from Statistic s where s.key.statistic = 'deal-ELECTRIC' group by s.key.playerName order by sum(s.amount) desc"),
    @NamedQuery(name="leaderboard.getByIceDamage", query="select s.key.playerName as name, sum(s.amount) as value from Statistic s where s.key.statistic = 'deal-ICE' group by s.key.playerName order by sum(s.amount) desc"),
    @NamedQuery(name="leaderboard.getByHealingDamage", query="select s.key.playerName as name, sum(s.amount) as value from Statistic s where s.key.statistic = 'deal-HEALING' group by s.key.playerName order by sum(s.amount) desc"),
    @NamedQuery(name="leaderboard.getByGauntlet", query="select s.key.playerName as name, sum(s.amount) as value from Statistic s where s.key.statistic = 'adventure-gauntlet' group by s.key.playerName order by sum(s.amount) desc"),
    @NamedQuery(name="leaderboard.getByAdventuresAbandoned", query="select s.key.playerName as name, sum(s.amount) as value from Statistic s where s.key.statistic = 'adventure-abandon' group by s.key.playerName order by sum(s.amount) desc"),
    @NamedQuery(name="leaderboard.getByDeaths", query="select s.key.playerName as name, sum(s.amount) as value from Statistic s where s.key.statistic = 'receive-DEATH' group by s.key.playerName order by sum(s.amount) desc"),
})
public class Statistic {
    private StatisticKey key;
    private int amount;
    
    public Statistic() {
    }

    @Id
    public StatisticKey getKey() {
        return key;
    }
    public void setKey(StatisticKey key) {
        this.key = key;
    }

    @Range(min=0)
    public int getAmount() {
        return amount;
    }
    public void setAmount(int amount) {
        this.amount = amount;
    }
}