package com.blogpessoal.Blog.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blogpessoal.Blog.model.Tema;

public interface TemaRepository extends JpaRepository <Tema, Long>{
	public List <Tema> findAllByDescricaoContainingIgnoreCase(String descricao);	

}


