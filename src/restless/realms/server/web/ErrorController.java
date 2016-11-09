package restless.realms.server.web;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.support.HandlerMethodInvocationException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.NestedServletException;

import restless.realms.server.util.JsonUtils;
import restless.realms.server.util.JsonUtils.ResponseStatus;

@Controller
@RequestMapping("/error/**")
public class ErrorController {
	private static final Log log = LogFactory.getLog(ErrorController.class);
	
	@RequestMapping("badrequest.html")
	public ModelAndView badRequest(HttpServletRequest request) throws ServletException, IOException {
		String path = (String)request.getAttribute("javax.servlet.error.request_uri");
		log.debug("Bad Request: " + path);
		return getErrorModelAndView("badrequest", path, "Bad request");
	}

	@RequestMapping("pagenotfound")
	public ModelAndView pageNotFound(HttpServletRequest request) throws ServletException, IOException {
		String path = (String)request.getAttribute("javax.servlet.forward.servlet_path");
		if(!"/router_test_int_5.txt".equals(path)) {
			log.debug("Page not found: " + path);
		}
		return getErrorModelAndView("pagenotfound", path, "Page not found");
	}
	
	@RequestMapping("exception")
	public ModelAndView exception(HttpServletRequest request) throws ServletException, IOException {
		String path = (String)request.getAttribute("javax.servlet.error.request_uri");
    	Throwable t = (Throwable)request.getAttribute("javax.servlet.error.exception");
    	if(t != null) {
            while((t instanceof NestedServletException || t instanceof HandlerMethodInvocationException) && t.getCause() != null) {
                t = t.getCause();
            }
            if(log.isWarnEnabled()) {
                log.warn("Error encountered in " + path, t);
            }
    	}
		String message = t.getMessage();
        return getErrorModelAndView(t.getClass().getSimpleName(), path, message);
	}
	
	@RequestMapping("notauthorized")
	public ModelAndView notAuthorized(HttpServletRequest request) throws ServletException, IOException {
		String path = (String)request.getAttribute("javax.servlet.error.request_uri");
		return getErrorModelAndView("notauthorized", path, "Not authorized");
	}
	
	private ModelAndView getErrorModelAndView(String type, String sourcePath, String errorMessage) {
	    Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("path", sourcePath);
        params.put("message", errorMessage);
        params.put("code", type);

	    ModelAndView ret;
		if(sourcePath.endsWith(".json")) {
            ret = JsonUtils.getModelAndView(ResponseStatus.ERROR, params);
		} else {
		    ret = new ModelAndView("exception");
		    ret.addAllObjects(params);
		}

		return ret;
	}
}
