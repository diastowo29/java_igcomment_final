package com.example.repo;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.example.model.DataEntry;

public interface DataEntryRepository extends CrudRepository<DataEntry, Long> {
	List<DataEntry> findById(long id);

	List<DataEntry> findByCifAccountId(String accountId);
	
	DataEntry findByCifPostId(String postId);
}
