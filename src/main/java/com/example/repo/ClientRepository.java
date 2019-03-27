package com.example.repo;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.example.model.Client;

public interface ClientRepository extends CrudRepository<Client, Long> {
	List<Client> findByCifClientId(String clientId);
}
