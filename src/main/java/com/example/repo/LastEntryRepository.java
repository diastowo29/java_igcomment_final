package com.example.repo;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.example.model.LastEntry;

public interface LastEntryRepository extends CrudRepository<LastEntry, Long> {
	List<LastEntry> findById(long id);

	LastEntry findByCifAccountId(String accountId);
}
