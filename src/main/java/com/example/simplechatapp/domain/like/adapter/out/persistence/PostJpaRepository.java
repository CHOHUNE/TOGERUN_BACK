package com.example.simplechatapp.domain.like.adapter.out.persistence;

import com.example.simplechatapp.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostJpaRepository extends JpaRepository<Post,Long> {


}
