package com.linker.processor.messageprocessors;

import com.linker.common.*;
import com.linker.processor.ProcessorUtils;
import com.linker.processor.express.PostOffice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class InternalMessageProcessor extends MessageProcessor<Message> {

    final MessageProcessorService messageProcessorService;
    final ProcessorUtils processorUtils;
    final PostOffice postOffice;


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
    public void doProcess(Message message, Message data, MessageContext context) {
        if (processorUtils.isCurrentDomainMessage(message)) {
            processMessage(message);
        } else {
            routeMessage(message);
        }
    }

    void processMessage(Message message) {
        Message innerMessage = Utils.convert(message.getContent().getData(), Message.class);
        messageProcessorService.process(innerMessage);
    }

    void routeMessage(Message message) {
        postOffice.deliverMessage(message);
    }
}
