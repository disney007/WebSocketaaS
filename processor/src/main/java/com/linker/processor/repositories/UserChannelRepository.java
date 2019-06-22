package com.linker.processor.repositories;

import com.linker.processor.models.UserChannel;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserChannelRepository extends CrudRepository<UserChannel, String> {
}
