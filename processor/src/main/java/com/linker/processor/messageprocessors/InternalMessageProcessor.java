package com.linker.processor.messageprocessors;

import com.linker.common.*;
import com.linker.common.exceptions.AddressNotFoundException;
import com.linker.processor.ProcessorUtils;
import com.linker.processor.express.PostOffice;
import com.linker.processor.repositories.MessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class InternalMessageProcessor extends PersistableMessageProcessor<Message> {

    final MessageProcessorService messageProcessorService;
    final ProcessorUtils processorUtils;
    final PostOffice postOffice;

    public InternalMessageProcessor(MessageRepository messageRepository, MessageProcessorService messageProcessorService, ProcessorUtils processorUtils, PostOffice postOffice) {
        super(messageRepository);
        this.messageProcessorService = messageProcessorService;
        this.processorUtils = processorUtils;
        this.postOffice = postOffice;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.INTERNAL_MESSAGE;
    }

    @Override
    public void doPreprocess(Message message, Message data, MessageContext context) {
        if (StringUtils.isBlank(message.getTo())) {
            Message innerMessage = Utils.convert(message.getContent().getData(), Message.class);
            message.setTo(innerMessage.getTo());
        }
        super.doPreprocess(message, data, context);
    }

    @Override
    public void doProcess(Message message, Message data, MessageContext context) throws IOException {
        if (processorUtils.isDomainProcessor()) {
            processDomainMessage(message);
        } else {
            processRouterMessage(message);
        }
    }

    void processDomainMessage(Message message) {
        Message innerMessage = Utils.convert(message.getContent().getData(), Message.class);
        messageProcessorService.process(innerMessage);
    }

    void processRouterMessage(Message message) throws IOException {
        try {
            postOffice.deliveryMessage(message);
        } catch (AddressNotFoundException e) {
            log.info("address not found for user [{}]", message.getTo(), e);
            updateMessageState(message, MessageState.TARGET_NOT_FOUND);
        }
    }
}
