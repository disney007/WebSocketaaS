package com.linker.processor.repositories;

import com.linker.processor.models.ClientApp;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientAppRepository extends CrudRepository<ClientApp, String> {
}
