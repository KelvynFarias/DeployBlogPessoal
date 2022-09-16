package com.blogpessoal.Blog.service;

import java.nio.charset.Charset;
import java.util.Optional;

import com.blogpessoal.Blog.model.Usuario;
import com.blogpessoal.Blog.model.UsuarioLogin;
import com.blogpessoal.Blog.repository.UsuarioRepository;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // função que verifica se o usuario já está cadastrado, criptografa a senha, e manda o obj de usuario para o banco de dados
    public Optional<Usuario> cadastrarUsuario(Usuario usuario) {
    	   
    	// verifica se o usuario já está cadastrado no banco de dados
        if (usuarioRepository.findByUsuario(usuario.getUsuario()).isPresent())
            return Optional.empty();
        
        // criptografa a senha digitada pelo usuario antes de mandar o objeto para o banco
        usuario.setSenha(criptografarSenha(usuario.getSenha()));
        
        // manda o objeto de usuario para o banco de dados com a senha criptografada
        return Optional.of(usuarioRepository.save(usuario));
    
    }

    public Optional<Usuario> atualizarUsuario(Usuario usuario) {
        
        if(usuarioRepository.findById(usuario.getId()).isPresent()) {

            Optional<Usuario> buscaUsuario = usuarioRepository.findByUsuario(usuario.getUsuario());

            if ( (buscaUsuario.isPresent()) && ( buscaUsuario.get().getId() != usuario.getId()))
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Usuário já existe!", null);

            usuario.setSenha(criptografarSenha(usuario.getSenha()));

            return Optional.ofNullable(usuarioRepository.save(usuario));
            
        }

        return Optional.empty();
    
    }   

    public Optional<UsuarioLogin> autenticarUsuario(Optional<UsuarioLogin> usuarioLogin) {

        Optional<Usuario> usuario = usuarioRepository.findByUsuario(usuarioLogin.get().getUsuario());

        if (usuario.isPresent()) {

            if (compararSenhas(usuarioLogin.get().getSenha(), usuario.get().getSenha())) {

                usuarioLogin.get().setId(usuario.get().getId());
                usuarioLogin.get().setNome(usuario.get().getNome());
                usuarioLogin.get().setFoto(usuario.get().getFoto());
                usuarioLogin.get().setToken(gerarBasicToken(usuarioLogin.get().getUsuario(),        usuarioLogin.get().getSenha()));
                usuarioLogin.get().setSenha(usuario.get().getSenha());

                return usuarioLogin;

            }
        }   

        return Optional.empty();
        
    }
    // função que criptografa a senha do usuario
    private String criptografarSenha(String senha) {
        
    	// semelhante ao Scanner(); do java
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // usando o metodo do bcrypt que criptografa a senha digitada e retorna a senha já criptografada
        return encoder.encode(senha);

    }
    
    private boolean compararSenhas(String senhaDigitada, String senhaBanco) {
        
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        return encoder.matches(senhaDigitada, senhaBanco);

    }

    private String gerarBasicToken(String usuario, String senha) {

        String token = usuario + ":" + senha;
        byte[] tokenBase64 = Base64.encodeBase64(token.getBytes(Charset.forName("US-ASCII")));
        return "Basic " + new String(tokenBase64);

    }

}