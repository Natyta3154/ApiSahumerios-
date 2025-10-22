package com.example.AppSaumerios.repository;



import com.example.AppSaumerios.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}

