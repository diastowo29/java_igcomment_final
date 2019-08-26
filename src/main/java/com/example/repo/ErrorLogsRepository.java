package com.example.repo;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.example.model.ErrorLogs;

public interface ErrorLogsRepository extends CrudRepository<ErrorLogs, Long> {
	List<ErrorLogs> findById(long id);
}
