package com.project.socialnetwork.repository;

import com.project.socialnetwork.model.AppUser;
import com.project.socialnetwork.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findAllByAppUser(AppUser user);
}