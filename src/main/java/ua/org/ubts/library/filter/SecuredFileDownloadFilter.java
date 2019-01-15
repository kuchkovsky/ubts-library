package ua.org.ubts.library.filter;

import org.springframework.web.filter.OncePerRequestFilter;
import ua.org.ubts.library.service.SecuredFileTokenService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SecuredFileDownloadFilter extends OncePerRequestFilter {

    private SecuredFileTokenService securedFileTokenService;

    public SecuredFileDownloadFilter(SecuredFileTokenService securedFileTokenService) {
        this.securedFileTokenService = securedFileTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String requestUri = request.getRequestURI();
        if (requestUri.contains("/covers/") || requestUri.contains("/books/documents")) {
            chain.doFilter(request, response);
        } else {
            String token = request.getParameter("token");
            if (token == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            } else if (!securedFileTokenService.verifyToken(token)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            } else {
                chain.doFilter(request, response);
            }
        }
    }

}
