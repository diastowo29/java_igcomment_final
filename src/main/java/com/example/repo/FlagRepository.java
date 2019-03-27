package com.example.repo;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.example.model.Flag;

public interface FlagRepository extends CrudRepository<Flag, Long> {
	List<Flag> findById(long id);

	Flag findByCifAccountId(String accountId);
}
