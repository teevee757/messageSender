package com.flyingj.jmssqs.createQueue.service;

import com.flyingj.jmssqs.createQueue.config.SqsProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jms.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class MessageSender {
    private static final int listenerDuration = 1;

    @Autowired
    SqsProperties sqsProperties;

    @Autowired
    Session session;

    @Autowired
    MessageProducer messageProducer;

    public void process() {
        try (Stream<Path> walk = Files.walk(Paths.get(sqsProperties.getMessagePath()))) {
            List<String> result = walk.map(x -> x.toString())
                    .filter(f -> f.endsWith(sqsProperties.getMessageFileType())).collect(Collectors.toList());
            result.forEach(file -> sendMessages(session, messageProducer, file));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendMessages( Session session, MessageProducer messageProducer, String file ) {
        try {
            TextMessage message = session.createTextMessage(new String(Files.readAllBytes(Paths.get(file))));
            messageProducer.send(message);
            System.out.println("Send message " + message.getJMSMessageID());
        } catch (JMSException | IOException e1) {
            e1.printStackTrace();
        }
    }
}
