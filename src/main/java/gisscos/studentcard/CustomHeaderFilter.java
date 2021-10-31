package gisscos.studentcard;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Компонент - фильтр для добавления заголовка ко всем запросам для UI
 */
//@Component
public class CustomHeaderFilter extends OncePerRequestFilter {

    @Override
    public void doFilterInternal(@NonNull final HttpServletRequest request, final HttpServletResponse response,
                                 final FilterChain chain) throws IOException, ServletException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        chain.doFilter(request, response);
    }
}
