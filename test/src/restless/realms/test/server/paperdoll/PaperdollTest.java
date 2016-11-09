package restless.realms.test.server.paperdoll;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;

import restless.realms.server.item.Item;
import restless.realms.test.server.IntegrationTestCase;


public class PaperdollTest extends IntegrationTestCase {
    @Autowired
    private HibernateTemplate template;
    
    @Test
    @SuppressWarnings("unchecked")
    public void validatePaperdollImages() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style=\"background-color:#222;\">");
        sb.append("<table>\n");
        sb.append("  <thead>\n");
        sb.append("    <tr>\n");
        sb.append("      <th>Icon</th>\n");
        sb.append("      <th>Name</th>\n");
        sb.append("      <th>Male Paperdoll</th>\n");
        sb.append("      <th>Female Paperdoll</th>\n");
        sb.append("    </tr>\n");
        sb.append("  </thead>\n");
        sb.append("  <tbody>\n");
        for(Iterator<Item> items = template.iterate("from Item order by type, name"); items.hasNext();) {
            Item item = items.next();
            sb.append("    <tr>\n");
            int xOffset = item.getIcon().getX() * 45;
            int yOffset = item.getIcon().getY() * 45;
            String backgroundPosition = "-" + xOffset + " -" + yOffset; 
            sb.append("      <td valign=\"top\"><img src=\"../war/restlessrealms/clear.cache.gif\" height=\"45\" width=\"45\" style=\"background-image:url(../war/img/icon/items.png);background-position:" + backgroundPosition + "\" /></td>\n");
            sb.append("      <td valign=\"top\">" + item.getName() + "<br/>X:" + item.getIcon().getX() + " Y:" + item.getIcon().getY() + "</td>\n");
            String paperdoll = item.getType().toString().toLowerCase() + "/" + item.getIcon().getX() + "-" + item.getIcon().getY() + ".png";
            sb.append("      <td><img style=\"background-image:url(../war/img/paperdoll/male/model/default.png);\" src=\"../war/img/paperdoll/male/" + paperdoll + "\" /></td>\n");
            sb.append("      <td><img style=\"background-image:url(../war/img/paperdoll/female/model/default.png);\" src=\"../war/img/paperdoll/female/" + paperdoll + "\" /></td>\n");
            sb.append("    </tr>\n");
        }
        sb.append("  </tbody>\n");
        sb.append("</table>");
        sb.append("</body></html>");
        System.out.println(sb.toString());
        try {
            FileWriter fileWriter = new FileWriter("util/paperdollaudit.html");
            fileWriter.write(sb.toString());
            fileWriter.flush();
        } catch(IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
