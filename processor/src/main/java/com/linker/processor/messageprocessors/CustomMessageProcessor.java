package com.linker.processor.messageprocessors;

import com.linker.common.Message;
import com.linker.common.MessageContent;
import com.linker.common.MessageContext;
import com.linker.common.MessageProcessor;
import com.linker.common.MessageType;
import com.linker.common.MessageUtils;
import com.linker.common.models.MessageForwardMessage;
import com.linker.common.models.MessageRequestMessage;
import com.linker.processor.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class CustomMessageProcessor extends MessageProcessor<MessageRequestMessage> {

    @Autowired
    MessageService messageService;

    @Override
    public MessageType getMessageType() {
        return MessageType.MESSAGE;
    }

    @Override
    public void doProcess(Message message, MessageRequestMessage data, MessageContext context) throws IOException {
        String from = message.getFrom();
        String to = data.getTo();
        MessageForwardMessage messageData = new MessageForwardMessage(from, data.getContent());
        MessageContent content = MessageUtils.createMessageContent(MessageType.MESSAGE, messageData);

        messageService.sendMessage(
                Message.builder()
                        .content(content)
                        .from(from)
                        .to(to)
                        .meta(message.getMeta())
                        .build()
        );
    }
}
