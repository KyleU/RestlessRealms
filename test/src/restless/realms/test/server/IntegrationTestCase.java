package restless.realms.test.server;

import javax.servlet.http.HttpServletRequest;

import junit.framework.Assert;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import restless.realms.server.database.seed.StaticSeedDataImporter;
import restless.realms.server.session.Session;
import restless.realms.server.session.SessionDao;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/restless/realms/server/configuration/spring.xml"})
@Transactional
public abstract class IntegrationTestCase {
    @Autowired
	protected HibernateTemplate hibernateTemplate;
	
    @Autowired
    private SessionDao sessionDao;
    
    protected HttpServletRequest getMockRequest(Session session) {
        if(session == null) {
            session = getTestSession();
        }
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("rrsession", session);
        return request;
    }

    protected Session getTestSession() {
        Session ret = sessionDao.get(StaticSeedDataImporter.TEST_SESSION_ID);
        Assert.assertNotNull(ret);
        return ret;
    }
}
