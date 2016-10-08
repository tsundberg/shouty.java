package shouty.web;

import shouty.DomainShouty;
import shouty.ShoutyApi;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShoutyServlet extends HttpServlet {

    private static final Pattern PERSON_PAGE_PATTERN = Pattern.compile("/people/([^/]+)");
    private static final Pattern MOVE_PATTERN = Pattern.compile("/people/([^/]+)/move");
    private static final Pattern CREATE_SHOUT_PATTERN = Pattern.compile("/people/([^/]+)/shouts");

    private final ShoutyApi shoutyApi;

    public ShoutyServlet() {
        this(new DomainShouty());
    }

    public ShoutyServlet(ShoutyApi shoutyApi) {
        this.shoutyApi = shoutyApi;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Matcher matcher = PERSON_PAGE_PATTERN.matcher(request.getPathInfo());
        if (matcher.matches()) {
            String personName = matcher.group(1);

            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().format("" +
                    "<form method=post action=/people/%s/shouts>\n" +
                    "  <input type=text name=message id=message>\n" +
                    "</form>\n", personName);
            response.getWriter().print("<ul class=messages>\n");
            for (String message : shoutyApi.getMessagesHeardBy(personName)) {
                response.getWriter().format("<li>%s</li>\n", message);

            }
            response.getWriter().print("</ul>\n");
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Matcher moveMatcher = MOVE_PATTERN.matcher(request.getPathInfo());
        Matcher createShoutMatcher = CREATE_SHOUT_PATTERN.matcher(request.getPathInfo());

        if (moveMatcher.matches()) {
            String personName = moveMatcher.group(1);
            String locationString = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8")).readLine();
            int location = Integer.parseInt(locationString);
            shoutyApi.setLocation(personName, location);
            response.setStatus(HttpServletResponse.SC_CREATED); // 201
        } else if (createShoutMatcher.matches()) {
            String personName = createShoutMatcher.group(1);
            String message = request.getParameter("message");
            shoutyApi.shout(personName, message);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND); // 404
            response.getWriter().format("Not found: %s", request.getPathInfo());
        }
    }
}