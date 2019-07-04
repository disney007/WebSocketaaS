package com.linker.processor.services;

import com.linker.common.Address;
import com.linker.processor.models.UserChannel;
import com.linker.processor.repositories.UserChannelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.HashSet;

@Service
public class UserChannelService {
    @Autowired
    UserChannelRepository userChannelRepository;

    public UserChannel getById(String userId) {
        return userChannelRepository.findById(userId).orElse(null);
    }

    public void addAddress(String userId, Address address) {
        UserChannel userChannel = getById(userId);
        if (userChannel == null) {
            userChannel = new UserChannel(userId, new HashSet<>(), ZonedDateTime.now().toInstant().toEpochMilli());
        }
        userChannel.getAddresses().add(address);
        userChannelRepository.save(userChannel);
    }

    public void removeAddress(String userId, Address address) {
        userChannelRepository.findById(userId)
                .ifPresent(userChannel -> {
                    userChannel.getAddresses().remove(address);
                    if (userChannel.getAddresses().size() == 0) {
                        userChannelRepository.delete(userChannel);
                    } else {
                        userChannelRepository.save(userChannel);
                    }
                });
    }
}
