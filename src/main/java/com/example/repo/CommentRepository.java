package com.example.repo;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.example.model.Comments;

public interface CommentRepository extends CrudRepository<Comments, Long> {
	List<Comments> findByIgid(String ig_id);

}
