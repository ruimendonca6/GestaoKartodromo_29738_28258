package pt.kartodromo.web.config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import pt.kartodromo.web.auth.WebAuthUser;

import java.io.IOException;
import java.util.Set;

@Component
public class AuthFilter implements Filter {

    private static final Set<String> PUBLIC_PATHS = Set.of("/login", "/login/submit", "/css", "/js");

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  request  = (HttpServletRequest)  req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = request.getServletPath();

        boolean isPublic = PUBLIC_PATHS.stream().anyMatch(path::startsWith)
                || path.startsWith("/static");

        if (isPublic) {
            chain.doFilter(req, res);
            return;
        }

        HttpSession sess = request.getSession(false);
        Object user = sess != null ? sess.getAttribute("authUser") : null;

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        chain.doFilter(req, res);
    }
}
