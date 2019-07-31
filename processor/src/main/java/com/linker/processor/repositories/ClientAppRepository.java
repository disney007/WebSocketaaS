package com.linker.processor.repositories;

import com.linker.common.client.ClientApp;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientAppRepository extends CrudRepository<ClientApp, String> {
}
