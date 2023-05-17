package com.jwt.authentication.springboot.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.io.IOException;
import java.util.ArrayList;

public class JWTValidarFilter extends BasicAuthenticationFilter {
    public static final String HEADER_ATRIBUTO = "Authorization";//atributo do cabeçalho
    public static final String ATRIBUTO_PREFIXO = "Bearer ";//atributo do prefixo

    public JWTValidarFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override //método para interceptar o cabeçalho da requisição
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {
        String atributo = request.getHeader(HEADER_ATRIBUTO); //para procurar no cabeçalho o atributo autorization
        if (atributo == null){//verificando se o atributo é nulo
            chain.doFilter(request,response);
            return;
        }
        if (!atributo.startsWith(ATRIBUTO_PREFIXO)) {//verificando se possui o prefixo informando o tipo do token
            chain.doFilter(request,response);
            return;
        }
        String token = atributo.replace(ATRIBUTO_PREFIXO,"");
        UsernamePasswordAuthenticationToken authenticationToken = getAuthenticationToken(token);

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        chain.doFilter(request,response);
    }

    //interpretação do token: faz a leitura do token e retorna os dados do usuário para garantir que é um usuario válido
    private UsernamePasswordAuthenticationToken getAuthenticationToken(String token){
        String usuario = JWT.require(Algorithm.HMAC512(JWTAutenticarFilter.SPRING_SECURITY_FORM_PASSWORD_KEY))
                .build()
                .verify(token)
                .getSubject();
        if (usuario == null){
            return null;
        }
        return new UsernamePasswordAuthenticationToken(usuario,null,new ArrayList<>());
    }
}
